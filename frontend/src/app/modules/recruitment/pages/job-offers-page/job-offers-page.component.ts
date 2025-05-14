import { Component } from '@angular/core';
import { ConfirmationModalComponent } from '../../../../shared/components/confirmation-modal/confirmation-modal.component';
import { MatDialog } from '@angular/material/dialog';

@Component({
  selector: 'app-job-offers-page',
  standalone: false,
  templateUrl: './job-offers-page.component.html',
  styleUrl: './job-offers-page.component.css',
})
export class JobOffersPageComponent {
  pageSize = 10;
  pageSizeOptions = [5, 10, 25, 100];
  jobOffers = [
    {
      id: 1,
      title: 'Frontend Developer',
      department: 'Engineering',
      isInternal: false,
      status: 'Active',
      applicants: 10,
      createdOn: '2025-01-02',
    },
    {
      id: 2,
      title: 'Backend Developer',
      department: 'Engineering',
      isInternal: true,
      status: 'Inactive',
      applicants: 5,
      createdOn: '2024-12-15',
    },
  ];

  constructor(private dialog: MatDialog) {}

  openDeleteConfirmation(id: number, title: string, createdOn: string) {
    // const dialogData: ConfirmationModalData = {
    //   title: 'Delete Job Offer',
    //   message:
    //     'Are you sure you want to delete this job offer? This action cannot be undone.',
    //   jobTitle: title,
    //   createdOn: createdOn,
    //   jobId: id,
    // };

    const dialogRef = this.dialog.open(ConfirmationModalComponent, {
      width: '500px',
      //data: dialogData,
      panelClass: 'custom-dialog',
    });

    // dialogRef.afterClosed().subscribe((result) => {
    //   if (result?.confirmed) {
    //     console.log(
    //       'Deletion confirmed:',
    //       JSON.stringify({ action: 'delete', jobId: result.jobId }, null, 2)
    //     );
    //     // In a real app, call service to delete job offer
    //   }
    // });
  }

  deleteJobOffer(id: number) {
    // Placeholder: Now handled by openDeleteConfirmation
  }

  toggleInternalExternal(id: number) {
    console.log(`Toggling internal/external for job offer ID: ${id}`);
  }

  finishStopJobOffer(id: number) {
    console.log(`Finishing/Stopping job offer ID: ${id}`);
  }

  onPageSizeChange() {
    console.log(`Page size changed to: ${this.pageSize}`);
  }
}
