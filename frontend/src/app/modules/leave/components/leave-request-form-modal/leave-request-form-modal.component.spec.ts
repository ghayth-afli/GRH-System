import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LeaveRequestFormModalComponent } from './leave-request-form-modal.component';

describe('LeaveRequestFormModalComponent', () => {
  let component: LeaveRequestFormModalComponent;
  let fixture: ComponentFixture<LeaveRequestFormModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [LeaveRequestFormModalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LeaveRequestFormModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
