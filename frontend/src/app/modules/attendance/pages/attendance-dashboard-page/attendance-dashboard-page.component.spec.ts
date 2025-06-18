import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AttendanceDashboardPageComponent } from './attendance-dashboard-page.component';

describe('AttendanceDashboardPageComponent', () => {
  let component: AttendanceDashboardPageComponent;
  let fixture: ComponentFixture<AttendanceDashboardPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AttendanceDashboardPageComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AttendanceDashboardPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
