import { Component } from '@angular/core';

@Component({
  selector: 'app-application-page',
  standalone: false,
  templateUrl: './application-page.component.html',
  styleUrl: './application-page.component.css',
})
export class ApplicationPageComponent {
  applications = [
    {
      id: 1,
      name: 'MOHAMED GHAYTH AFLI',
      email: 'mohamed.ghayth12@gmail.com',
      phone: '+216 95 357 697',
      status: 'PENDING',
      score: '72%',
      scoreClass: 'score-medium',
      tags: ['SpringBoot', 'React', 'MySQL', 'Docker', 'AWS'],
      date: 'May 13, 2025',
    },
    {
      id: 2,
      name: 'Jane Smith',
      email: 'jane.smith@example.com',
      phone: '+1 234 567 8901',
      status: 'REVIEWED',
      score: '88%',
      scoreClass: 'score-high',
      tags: ['Java', 'Spring', 'Kubernetes', 'Microservices'],
      date: 'May 10, 2025',
    },
    {
      id: 3,
      name: 'David Johnson',
      email: 'david.johnson@example.com',
      phone: '+1 987 654 3210',
      status: 'INTERVIEWING',
      score: '76%',
      scoreClass: 'score-medium',
      tags: ['Java', 'Spring Boot', 'PostgreSQL', 'CI/CD'],
      date: 'May 8, 2025',
    },
  ];
  pageSizeOptions = [10, 25, 50];
  pageSize = this.pageSizeOptions[0]; // Default to 10

  onPageSizeChange() {
    // Placeholder for page size change logic (e.g., update service call)
    console.log(`Page size changed to ${this.pageSize}`);
  }
}
