import { Component, inject, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { Invitation } from '../../models/invitation';
import { ActivatedRoute } from '@angular/router';
import { TrainingService } from '../../services/training.service';

@Component({
  selector: 'app-invitations-page',
  standalone: false,

  templateUrl: './invitations-page.component.html',
  styleUrl: './invitations-page.component.css',
})
export class InvitationsPageComponent {
  private trainingService = inject(TrainingService);
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  route = inject(ActivatedRoute);
  trainingId: string | null = null;
  dataSource!: MatTableDataSource<Invitation>;
  pageSize = 10;
  pageSizeOptions = [10, 50, 100];
  displayedColumns = ['id', 'employeeName', 'status'];
  invitations: Invitation[] = [];

  ngOnInit(): void {
    this.fetchRouteParams();
    this.loadInvitations();
  }

  private fetchRouteParams(): void {
    this.route.paramMap.subscribe({
      next: (params) => {
        this.trainingId = params.get('id');
      },
      error: (error) => {
        console.error('Error fetching route params:', error);
      },
    });
  }
  private loadInvitations(): void {
    if (this.trainingId) {
      const trainingId = Number(this.trainingId);
      if (!isNaN(trainingId)) {
        this.trainingService.getTrainingById(trainingId).subscribe({
          next: (training) => {
            this.invitations = training.invitations;
            this.initializeDataSource();
            console.log('Invitations:', this.invitations);
          },
          error: (error) => {
            console.error('Error loading invitations:', error);
          },
        });
      } else {
        console.error('Invalid training ID:', this.trainingId);
      }
    }
  }

  private initializeDataSource(): void {
    this.dataSource = new MatTableDataSource<Invitation>(this.invitations);
    this.dataSource.paginator = this.paginator;
  }
  applyFilter(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }
}
