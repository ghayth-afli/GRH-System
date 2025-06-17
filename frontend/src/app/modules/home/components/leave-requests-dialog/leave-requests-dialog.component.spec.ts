import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LeaveRequestsDialogComponent } from './leave-requests-dialog.component';

describe('LeaveRequestsDialogComponent', () => {
  let component: LeaveRequestsDialogComponent;
  let fixture: ComponentFixture<LeaveRequestsDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [LeaveRequestsDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LeaveRequestsDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
