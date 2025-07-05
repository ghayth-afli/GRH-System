import {
  Component,
  OnInit,
  AfterViewInit,
  ChangeDetectorRef,
} from '@angular/core';
import { DashboardService } from '../../services/dashboard.service';
import { Chart, registerables } from 'chart.js';

@Component({
  selector: 'app-dashboard-page',
  standalone: false,
  templateUrl: './dashboard-page.component.html',
  styleUrls: ['./dashboard-page.component.css'],
})
export class DashboardPageComponent implements OnInit, AfterViewInit {
  dashboardData: any = {};
  isLoading = true;
  errorMessage: string | null = null;

  constructor(
    private dashboardService: DashboardService,
    private cdr: ChangeDetectorRef
  ) {
    Chart.register(...registerables);
  }

  ngOnInit() {
    this.dashboardService.getDashboardData().subscribe({
      next: (data) => {
        console.log('Dashboard data received:', data);
        this.dashboardData = data.dashboardData || {};
        this.isLoading = false;
        if (!this.dashboardData.kpiMetrics) {
          this.errorMessage = 'No dashboard data available.';
          console.error(
            'Dashboard data is empty or malformed:',
            this.dashboardData
          );
        } else {
          console.log('Data loaded, triggering change detection');
          this.cdr.detectChanges(); // Ensure template updates
          setTimeout(() => {
            console.log('Rendering charts after data fetch');
            this.renderCharts();
          }, 0); // Defer to ensure DOM is ready
        }
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = 'Failed to load dashboard data.';
        console.error('Error fetching dashboard data:', err);
        this.cdr.detectChanges();
      },
    });
  }

  ngAfterViewInit() {
    console.log(
      'ngAfterViewInit called, isLoading=',
      this.isLoading,
      'data=',
      this.dashboardData
    );
    // No chart rendering here; handled in ngOnInit subscription
  }

  renderCharts() {
    console.log('Attempting to render charts with data:', this.dashboardData);
    if (!this.dashboardData.kpiMetrics) {
      console.error('Cannot render charts: dashboardData is empty');
      this.errorMessage = 'Cannot render charts: No data available.';
      return;
    }
    this.renderGenderChart();
    this.renderAgeChart();
    this.renderDepartmentChart();
    this.renderLeaveTypeChart();
    this.renderFunnelChart();
    this.renderLeaveTrendChart();
  }

  renderGenderChart() {
    const ctx = document.getElementById('genderChart') as HTMLCanvasElement;
    if (!ctx) {
      console.error('Gender chart canvas not found');
      return;
    }
    try {
      new Chart(ctx, {
        type: 'doughnut',
        data: {
          labels:
            this.dashboardData.genderDistribution?.map((g: any) => g.name) ||
            [],
          datasets: [
            {
              data:
                this.dashboardData.genderDistribution?.map(
                  (g: any) => g.value
                ) || [],
              backgroundColor: ['#28a745', '#dc3545'],
              borderColor: ['#fff'],
              borderWidth: 2,
            },
          ],
        },
        options: {
          plugins: {
            legend: { position: 'bottom' },
            tooltip: { enabled: true },
          },
        },
      });
      console.log('Gender chart rendered');
    } catch (err) {
      console.error('Failed to render gender chart:', err);
    }
  }

  renderAgeChart() {
    const ctx = document.getElementById('ageChart') as HTMLCanvasElement;
    if (!ctx) {
      console.error('Age chart canvas not found');
      return;
    }
    try {
      new Chart(ctx, {
        type: 'bar',
        data: {
          labels:
            this.dashboardData.ageDistribution?.map((a: any) => a.ageRange) ||
            [],
          datasets: [
            {
              label: 'Employees',
              data:
                this.dashboardData.ageDistribution?.map((a: any) => a.count) ||
                [],
              backgroundColor: '#fd5b00',
              borderColor: '#fd5b00',
              borderWidth: 1,
            },
          ],
        },
        options: {
          scales: { y: { beginAtZero: true } },
          plugins: { tooltip: { enabled: true } },
        },
      });
      console.log('Age chart rendered');
    } catch (err) {
      console.error('Failed to render age chart:', err);
    }
  }

  renderDepartmentChart() {
    const ctx = document.getElementById('departmentChart') as HTMLCanvasElement;
    if (!ctx) {
      console.error('Department chart canvas not found');
      return;
    }
    try {
      new Chart(ctx, {
        type: 'bar',
        data: {
          labels:
            this.dashboardData.employeesByDepartment?.map(
              (d: any) => d.department
            ) || [],
          datasets: [
            {
              label: 'Male',
              data:
                this.dashboardData.employeesByDepartment?.map(
                  (d: any) => d.male
                ) || [],
              backgroundColor: '#28a745',
            },
            {
              label: 'Female',
              data:
                this.dashboardData.employeesByDepartment?.map(
                  (d: any) => d.female
                ) || [],
              backgroundColor: '#dc3545',
            },
          ],
        },
        options: {
          scales: {
            y: { beginAtZero: true, stacked: false },
            x: { stacked: false },
          },
          plugins: { tooltip: { enabled: true } },
        },
      });
      console.log('Department chart rendered');
    } catch (err) {
      console.error('Failed to render department chart:', err);
    }
  }

  renderLeaveTypeChart() {
    const ctx = document.getElementById('leaveTypeChart') as HTMLCanvasElement;
    if (!ctx) {
      console.error('Leave type chart canvas not found');
      return;
    }
    try {
      new Chart(ctx, {
        type: 'bar',
        data: {
          labels:
            this.dashboardData.leaveStatistics?.byType?.map(
              (l: any) => l.leaveType
            ) || [],
          datasets: [
            {
              label: 'Leaves',
              data:
                this.dashboardData.leaveStatistics?.byType?.map(
                  (l: any) => l.count
                ) || [],
              backgroundColor: '#ffc107',
              borderColor: '#ffc107',
              borderWidth: 1,
            },
          ],
        },
        options: {
          scales: { y: { beginAtZero: true } },
          plugins: { tooltip: { enabled: true } },
        },
      });
      console.log('Leave type chart rendered');
    } catch (err) {
      console.error('Failed to render leave type chart:', err);
    }
  }

  renderFunnelChart() {
    const ctx = document.getElementById('funnelChart') as HTMLCanvasElement;
    if (!ctx) {
      console.error('Funnel chart canvas not found');
      return;
    }
    try {
      new Chart(ctx, {
        type: 'bar',
        data: {
          labels:
            this.dashboardData.recruitmentFunnel?.map((r: any) => r.stage) ||
            [],
          datasets: [
            {
              label: 'Candidates',
              data:
                this.dashboardData.recruitmentFunnel?.map(
                  (r: any) => r.count
                ) || [],
              backgroundColor: [
                '#FD5B00',
                '#28a745',
                '#dc3545',
                '#ffc107',
                '#6c757d',
              ],
              borderColor: ['#fff'],
              borderWidth: 1,
            },
          ],
        },
        options: {
          indexAxis: 'y',
          scales: { x: { beginAtZero: true } },
          plugins: { tooltip: { enabled: true } },
        },
      });
      console.log('Funnel chart rendered');
    } catch (err) {
      console.error('Failed to render funnel chart:', err);
    }
  }

  renderLeaveTrendChart() {
    const ctx = document.getElementById('leaveTrendChart') as HTMLCanvasElement;
    if (!ctx) {
      console.error('Leave trend chart canvas not found');
      return;
    }
    try {
      new Chart(ctx, {
        type: 'line',
        data: {
          labels:
            this.dashboardData.leaveStatistics?.trend?.map(
              (t: any) => t.month
            ) || [],
          datasets: [
            {
              label: 'Leaves',
              data:
                this.dashboardData.leaveStatistics?.trend?.map(
                  (t: any) => t.count
                ) || [],
              borderColor: '#ffc107',
              backgroundColor: 'rgba(255, 193, 7, 0.2)',
              fill: true,
              tension: 0.4,
            },
          ],
        },
        options: {
          scales: { y: { beginAtZero: true } },
          plugins: { tooltip: { enabled: true } },
        },
      });
      console.log('Leave trend chart rendered');
    } catch (err) {
      console.error('Failed to render leave trend chart:', err);
    }
  }
}
