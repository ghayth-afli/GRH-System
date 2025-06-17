import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SubmitLeaveRequestDialogComponent } from './submit-leave-request-dialog.component';

describe('SubmitLeaveRequestDialogComponent', () => {
  let component: SubmitLeaveRequestDialogComponent;
  let fixture: ComponentFixture<SubmitLeaveRequestDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [SubmitLeaveRequestDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SubmitLeaveRequestDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
