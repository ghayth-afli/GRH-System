import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { delay } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class DashboardService {
  getDashboardData(): Observable<any> {
    const mockData = {
      dashboardData: {
        kpiMetrics: {
          totalEmployees: 1000,
          timeToHire: 45,
          trainingParticipationRate: 0.75,
          leaveApprovalRate: 0.92,
        },
        genderDistribution: [
          { name: 'Male', value: 450 },
          { name: 'Female', value: 550 },
        ],
        ageDistribution: [
          { ageRange: '20-30', count: 150 },
          { ageRange: '31-40', count: 400 },
          { ageRange: '41-50', count: 300 },
          { ageRange: '51-60', count: 150 },
        ],
        employeesByDepartment: [
          { department: 'Engineering', male: 120, female: 80 },
          { department: 'HR', male: 10, female: 40 },
          { department: 'Sales', male: 50, female: 50 },
          { department: 'Marketing', male: 20, female: 30 },
        ],
        leaveStatistics: {
          byType: [
            { leaveType: 'Annual', count: 200 },
            { leaveType: 'Sick', count: 150 },
            { leaveType: 'Remote Work', count: 100 },
          ],
          trend: [
            { month: 'Jan', count: 30 },
            { month: 'Feb', count: 25 },
            { month: 'Mar', count: 40 },
          ],
        },
        recruitmentFunnel: [
          { stage: 'Applied', count: 500 },
          { stage: 'Screened', count: 200 },
          { stage: 'Interview', count: 100 },
          { stage: 'Offered', count: 20 },
          { stage: 'Hired', count: 15 },
        ],
        openPositions: [
          { department: 'Engineering', count: 5 },
          { department: 'Sales', count: 3 },
          { department: 'Marketing', count: 2 },
        ],
        upcomingTrainings: [
          {
            title: 'Advanced Leadership Program',
            startDate: '2023-10-15',
            endDate: '2023-10-20',
            enrolled: 15,
          },
          {
            title: 'Project Management Pro',
            startDate: '2023-11-01',
            endDate: '2023-11-05',
            enrolled: 25,
          },
        ],
      },
    };
    return of(mockData).pipe(delay(1000));
  }
}
