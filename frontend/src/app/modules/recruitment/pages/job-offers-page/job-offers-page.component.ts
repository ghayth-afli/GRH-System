import { Component } from '@angular/core';

@Component({
  selector: 'app-job-offers-page',
  standalone: false,
  templateUrl: './job-offers-page.component.html',
  styleUrl: './job-offers-page.component.css',
})
export class JobOffersPageComponent {
  pageSizeOptions = [10, 25, 50];
  pageSize = this.pageSizeOptions[0]; // Default to 10

  jobOffers = [
    {
      id: 1,
      title: 'Senior Software Engineer',
      department: 'Engineering',
      isInternal: true,
      status: 'Active',
      applicants: 5,
    },
    {
      id: 2,
      title: 'Marketing Manager',
      department: 'Marketing',
      isInternal: false,
      status: 'Inactive',
      applicants: 3,
    },
    {
      id: 3,
      title: 'HR Specialist',
      department: 'Human Resources',
      isInternal: true,
      status: 'Finished',
      applicants: 8,
    },
  ];

  onPageSizeChange() {
    console.log(`Page size changed to ${this.pageSize}`);
  }

  deleteJobOffer(id: number) {
    console.log(`Delete job offer with ID: ${id}`);
  }

  toggleInternalExternal(id: number) {
    console.log(`Toggle internal/external for job offer with ID: ${id}`);
  }

  finishStopJobOffer(id: number) {
    console.log(`Finish/Stop job offer with ID: ${id}`);
  }
}
