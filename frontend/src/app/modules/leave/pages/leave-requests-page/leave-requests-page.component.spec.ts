import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LeaveRequestsPageComponent } from './leave-requests-page.component';

describe('LeaveRequestsPageComponent', () => {
  let component: LeaveRequestsPageComponent;
  let fixture: ComponentFixture<LeaveRequestsPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [LeaveRequestsPageComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LeaveRequestsPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
