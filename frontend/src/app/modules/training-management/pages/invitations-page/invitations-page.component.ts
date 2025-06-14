import { Component, inject } from '@angular/core';
import { Invitation } from '../../models/invitation';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';
import { InvitationService } from '../../services/invitation.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-invitations-page',
  standalone: false,
  templateUrl: './invitations-page.component.html',
  styleUrl: './invitations-page.component.css',
})
export class InvitationsPageComponent {
  invitations: Invitation[] = [];
  // Pagination settings
  pageSize = 10;
  pageIndex = 0;
  pageSizeOptions = [5, 10, 25, 100];
  totalItems = 0;
  totalPages = 0;
  pages: number[] = [];
  visiblePages: (number | string)[] = [];

  // Filter settings
  searchTerm: string = '';
  selectedStatus: string = 'all';
  displayedInvitations: Invitation[] = [];

  // Inject services
  authService = inject(AuthService);
  private invitationService = inject(InvitationService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  // To hold subscriptions
  private queryParamsSubscription: Subscription | undefined;
  private invitationsSubscription: Subscription | undefined;

  ngOnInit() {
    this.queryParamsSubscription = this.route.queryParamMap.subscribe(
      (params) => {
        const pageSize = params.get('pageSize');

        if (pageSize) {
          this.pageSize = parseInt(pageSize, 10);
        }

        this.loadInvitations();
      }
    );
  }

  ngOnDestroy() {
    if (this.queryParamsSubscription) {
      this.queryParamsSubscription.unsubscribe();
    }
    if (this.invitationsSubscription) {
      this.invitationsSubscription.unsubscribe();
    }
  }

  // Filter handlers
  onPageSizeChange() {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {
        pageSize: this.pageSize,
      },
      queryParamsHandling: 'merge',
    });
  }

  onSearchTermChange() {
    this.pageIndex = 0;
    this.applyFilters();
  }

  onStatusChange() {
    this.pageIndex = 0;
    this.applyFilters();
  }

  onTypeChange() {
    this.pageIndex = 0;
    this.applyFilters();
  }

  // Pagination methods
  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages && page !== this.pageIndex) {
      this.pageIndex = page;
      this.updateVisiblePages();
      this.updateDisplayedInvitations(this.invitations);
    }
  }

  previousPage(): void {
    if (this.pageIndex > 0) {
      this.pageIndex--;
      this.updateVisiblePages();
      this.updateDisplayedInvitations(this.invitations);
    }
  }

  nextPage(): void {
    if (this.pageIndex < this.totalPages - 1) {
      this.pageIndex++;
      this.updateVisiblePages();
      this.updateDisplayedInvitations(this.invitations);
    }
  }

  changePageSize(size: number): void {
    this.pageSize = size;
    this.pageIndex = 0;
    this.calculatePagination();
    this.updateDisplayedInvitations(this.invitations);
    this.onPageSizeChange();
  }

  isPrevDisabled(): boolean {
    return this.pageIndex === 0;
  }

  isNextDisabled(): boolean {
    return this.pageIndex === this.totalPages - 1 || this.totalPages === 0;
  }

  private loadInvitations() {
    const id = this.route.snapshot.params['id'];
    if (id) {
      if (this.invitationsSubscription) {
        this.invitationsSubscription.unsubscribe();
      }
      this.invitationsSubscription = this.invitationService
        .getAllInvitationsByTrainingId(+id)
        .subscribe({
          next: (invitations) => {
            this.invitations = invitations;
            this.totalItems = invitations.length;
            this.applyFilters();
          },
          error: (error) => {
            console.error('Failed to load invitations:', error);
          },
        });
    } else {
      console.error('Training ID is missing');
    }
  }

  private applyFilters() {
    let filteredInvitations = [...this.invitations];

    // Apply search filter
    if (this.searchTerm) {
      filteredInvitations = filteredInvitations.filter((invitation) =>
        invitation.userName
          .toLowerCase()
          .includes(this.searchTerm.toLowerCase())
      );
    }

    // Apply status filter
    if (this.selectedStatus && this.selectedStatus !== 'all') {
      filteredInvitations = filteredInvitations.filter(
        (invitation) => invitation.status === this.selectedStatus
      );
    }

    this.totalItems = filteredInvitations.length;

    this.calculatePagination();
    if (this.pageIndex >= this.totalPages && this.totalPages > 0) {
      this.pageIndex = this.totalPages - 1;
    } else if (this.totalPages === 0) {
      this.pageIndex = 0;
    }

    this.updateDisplayedInvitations(filteredInvitations);
  }

  private updateDisplayedInvitations(sourceList: Invitation[]): void {
    // Renamed from updateDisplayedTrainings for clarity
    const startIndex = this.pageIndex * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    this.displayedInvitations = sourceList.slice(startIndex, endIndex);
  }

  private calculatePagination(): void {
    this.totalPages = Math.ceil(this.totalItems / this.pageSize);
    if (this.totalPages === 0) {
      // Handle case with no items
      this.pages = [];
    } else {
      this.pages = Array.from({ length: this.totalPages }, (_, i) => i);
    }
    this.updateVisiblePages();
  }

  private updateVisiblePages(): void {
    this.visiblePages = [];

    if (this.totalPages <= 0) {
      return;
    }

    if (this.totalPages <= 7) {
      this.visiblePages = this.pages;
    } else {
      this.visiblePages.push(0);
      const currentPageIndex = this.pageIndex;

      let startPage = Math.max(1, currentPageIndex - 1);
      let endPage = Math.min(this.totalPages - 2, currentPageIndex + 1);

      // Adjust window if near the beginning
      if (currentPageIndex < 3) {
        endPage = Math.min(this.totalPages - 2, 3);
      }

      // Adjust window if near the end
      if (currentPageIndex > this.totalPages - 4) {
        startPage = Math.max(1, this.totalPages - 4);
      }

      if (startPage > 1) {
        this.visiblePages.push('...');
      }

      for (let i = startPage; i <= endPage; i++) {
        this.visiblePages.push(i);
      }

      if (endPage < this.totalPages - 2) {
        this.visiblePages.push('...');
      }

      this.visiblePages.push(this.totalPages - 1);
    }
  }
}
