import {
  AfterViewInit,
  Component,
  ElementRef,
  OnInit,
  ViewChild,
  inject,
} from '@angular/core';
import { Chart } from 'chart.js/auto';
import { ActivatedRoute } from '@angular/router';
import { JobApplicationService } from '../../services/job-application.service';
import { ApplicationDetailsResponseDTO } from '../../models/application-response';

@Component({
  selector: 'app-application-details-page',
  standalone: false,
  templateUrl: './application-details-page.component.html',
  styleUrls: ['./application-details-page.component.css'],
})
export class ApplicationDetailsPageComponent implements OnInit, AfterViewInit {
  @ViewChild('scoreChart') scoreChart!: ElementRef<HTMLCanvasElement>;

  private route = inject(ActivatedRoute);
  jobApplicationService = inject(JobApplicationService);

  applicationDetails: ApplicationDetailsResponseDTO | null = null;
  scoreChartInstance: Chart | null = null;
  jobId: number | null = null;
  ngOnInit() {
    // Get the application ID from the route parameters
    this.route.paramMap.subscribe((params) => {
      const id = Number(params.get('applicationid'));
      const jobId = Number(params.get('id'));
      this.jobId = jobId;
      if (id) {
        this.loadApplicationDetails(id);
      }
    });
  }

  ngAfterViewInit() {
    // Chart will be initialized after data is loaded
  }

  loadApplicationDetails(applicationId: number) {
    this.jobApplicationService.getApplicationDetails(applicationId).subscribe({
      next: (application) => {
        this.applicationDetails = application;
        console.log('Application Details:', application);

        // Initialize chart after data is loaded
        setTimeout(() => {
          this.initializeChart();
        });
      },
      error: (error) => {
        console.error('Error loading application details:', error);
      },
    });
  }

  initializeChart() {
    // Ensure the chart element exists and we have data
    if (!this.scoreChart?.nativeElement || !this.applicationDetails) {
      return;
    }

    const ctx = this.scoreChart.nativeElement.getContext('2d');
    if (ctx) {
      // Destroy previous chart instance if it exists
      if (this.scoreChartInstance) {
        this.scoreChartInstance.destroy();
      }

      const score = this.applicationDetails.matchResult.score * 100; // Convert decimal to percentage

      this.scoreChartInstance = new Chart(ctx, {
        type: 'doughnut',
        data: {
          datasets: [
            {
              data: [score, 100 - score],
              backgroundColor: ['#fd5b00', '#f0f0f0'],
              borderWidth: 0,
            },
          ],
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          cutout: '75%',
          plugins: {
            legend: { display: false },
            tooltip: { enabled: false },
          },
        },
      });
    }
  }

  // Helper methods for template use
  getAdaptedCriteriaValue(criteriaName: string): {
    value: number;
    max: number;
    percentage: number;
  } {
    if (!this.applicationDetails) {
      return { value: 0, max: 0, percentage: 0 };
    }

    const criteria = this.applicationDetails.matchResult.adapted_criteria.find(
      (c) => c.name === criteriaName
    );

    if (!criteria) {
      return { value: 0, max: 0, percentage: 0 };
    }

    // Get the weighted score from the corresponding detail category
    let detail: any;

    switch (criteriaName) {
      case 'Technical Skills Match':
        detail = this.applicationDetails.matchResult.details.skills_match;
        break;
      case 'Relevant Experience':
        detail =
          this.applicationDetails.matchResult.details.relevant_experience;
        break;
      case 'Problem Solving Capability':
        detail = this.applicationDetails.matchResult.details.cultural_fit; // Assuming problem solving is part of cultural fit
        break;
      case 'Education':
        detail = this.applicationDetails.matchResult.details.education;
        break;
      case 'Technical Certifications':
        detail = this.applicationDetails.matchResult.details.certifications;
        break;
      case 'Development Tools/Frameworks':
        detail = this.applicationDetails.matchResult.details.skills_match; // Using skills match for tools as well
        break;
      case 'Open Source Contributions':
        detail =
          this.applicationDetails.matchResult.details.achievements_projects;
        break;
      default:
        detail = { weighted_score: 0 };
    }

    const max = criteria.weight;
    const value = detail ? detail.weighted_score : 0;
    const percentage = (value / max) * 100;

    return { value, max, percentage };
  }

  getRedFlags(): string[] {
    return this.applicationDetails?.matchResult.red_flags || [];
  }

  getRoleSpecificInsights(): string[] {
    return this.applicationDetails?.matchResult.role_specific_insights || [];
  }

  formatScorePercentage(): string {
    if (!this.applicationDetails) return '0%';
    return `${Math.round(this.applicationDetails.matchResult.score * 100)}%`;
  }

  changeApplicationStatus() {
    // Implement status change functionality
    console.log('Change status clicked');
  }

  downloadResume() {
    // Implement resume download functionality
    console.log('Download resume clicked');
  }
}
