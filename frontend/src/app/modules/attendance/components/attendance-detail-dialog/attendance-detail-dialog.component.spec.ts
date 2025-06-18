import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AttendanceDetailDialogComponent } from './attendance-detail-dialog.component';

describe('AttendanceDetailDialogComponent', () => {
  let component: AttendanceDetailDialogComponent;
  let fixture: ComponentFixture<AttendanceDetailDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AttendanceDetailDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AttendanceDetailDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
