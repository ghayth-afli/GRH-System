import { Component, inject, ViewChild } from '@angular/core';
import { TrainingService } from '../../services/training.service';
import { MatPaginator } from '@angular/material/paginator';
import { AuthService } from '../../../../core/services/auth.service';
import { MatDialog } from '@angular/material/dialog';
import { MatTableDataSource } from '@angular/material/table';
import { Training } from '../../models/training';
import { TrainingFormModalComponent } from '../../components/training-form-modal/training-form-modal.component';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import { InvitationService } from '../../services/invitation.service';
@Component({
  selector: 'app-trainings-page',
  standalone: false,

  templateUrl: './trainings-page.component.html',
  styleUrl: './trainings-page.component.css',
})
export class TrainingsPageComponent {
  private trainingService = inject(TrainingService);
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  authService = inject(AuthService);
  invitationService = inject(InvitationService);
  public dialog = inject(MatDialog);
  //date now
  currentDate = new Date();
  dataSource!: MatTableDataSource<Training>;
  pageSize = 10;
  pageSizeOptions = [10, 50, 100];
  displayedColumns: string[] = [];
  trainings: Training[] = [];
  ngOnInit(): void {
    this.setDisplayedColumns();
    this.loadTrainings();
  }

  addTraining(): void {
    const dialogRef = this.dialog.open(TrainingFormModalComponent, {
      width: '35%',
      height: 'auto',
    });
  }

  editTraining(training: Training): void {
    const dialogRef = this.dialog.open(TrainingFormModalComponent, {
      width: '35%',
      height: 'auto',
      data: training,
    });
  }
  deleteTraining(training: Training): void {
    this.trainingService.deleteTraining(training.id).subscribe({
      next: () => {
        this.loadTrainings();
      },
      error: (error) => {
        console.error('Error deleting training:', error);
      },
    });
  }

  rejectTraining(training: Training): void {
    this.invitationService.rejectInvitation(training.id).subscribe({
      next: () => {
        this.loadTrainings();
      },
      error: (error) => {
        console.error('Error rejecting training:', error);
      },
    });
  }
  confirmTraining(training: Training): void {
    this.invitationService.confirmInvitation(training.id).subscribe({
      next: () => {
        this.loadTrainings();
      },
      error: (error) => {
        console.error('Error accepting training:', error);
      },
    });
  }

  exportToPDF(): void {
    const doc = new jsPDF();
    doc.setFontSize(12);
    doc.text('Trainings List', 14, 16);
    autoTable(doc, {
      startY: 20,
      head: [this.displayedColumns],
      body: this.trainings.map((training) => [
        training.id,
        training.title,
        training.description,
        training.startDate.toString(),
        training.endDate.toString(),
        training.invitations.length,
        this.authService.hasRole('HR') ? training.department : '',
        this.authService.hasRole('HR') ? training.createdBy : '',
        this.authService.hasRole('HR') ? training.createdAt : '',
      ]),
    });
    doc.save('trainings.pdf');
  }

  exportcsv(): void {
    const csvData = this.trainings.map((training) => {
      return {
        id: training.id,
        title: training.title,
        description: training.description,
        startDate: training.startDate.toString(),
        endDate: training.endDate.toString(),
        invitations: training.invitations.length,
        department: this.authService.hasRole('HR') ? training.department : '',
        createdBy: this.authService.hasRole('HR') ? training.createdBy : '',
        createdAt: this.authService.hasRole('HR') ? training.createdAt : '',
      };
    });
    const csvContent =
      'data:text/csv;charset=utf-8,' +
      Object.keys(csvData[0]).join(',') +
      '\n' +
      csvData
        .map((row) => Object.values(row).join(','))
        .join('\n')
        .replace(/,/g, ';');
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement('a');
    link.setAttribute('href', encodedUri);
    link.setAttribute('download', 'trainings.csv');
    document.body.appendChild(link);
    link.click();
  }

  filter($event: KeyboardEvent): void {
    this.dataSource.filter = ($event.target as HTMLInputElement).value
      .trim()
      .toLowerCase();
  }

  private setDisplayedColumns(): void {
    this.displayedColumns = [
      'id',
      'title',
      'description',
      'startDate',
      'endDate',
      'invitations',
      'status',
      'actions',
    ];
    if (this.authService.hasRole('HR')) {
      this.displayedColumns.push('department');
      this.displayedColumns.push('createdAt');
      this.displayedColumns.splice(this.displayedColumns.indexOf('actions'), 1);
    }
  }
  private loadTrainings(): void {
    this.trainingService.getAllTrainings().subscribe({
      next: (trainings) => {
        this.trainings = trainings;
        this.dataSource = new MatTableDataSource<Training>(this.trainings);
        this.dataSource.paginator = this.paginator;
      },
      error: (error) => {
        console.error('Error loading trainings:', error);
      },
    });
  }
}
