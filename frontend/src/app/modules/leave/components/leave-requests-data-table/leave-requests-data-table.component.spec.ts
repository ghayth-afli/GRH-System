import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LeaveRequestsDataTableComponent } from './leave-requests-data-table.component';

describe('LeaveRequestsDataTableComponent', () => {
  let component: LeaveRequestsDataTableComponent;
  let fixture: ComponentFixture<LeaveRequestsDataTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [LeaveRequestsDataTableComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LeaveRequestsDataTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
