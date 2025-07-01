import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ExceptionDetailDialogComponent } from './exception-detail-dialog.component';

describe('ExceptionDetailDialogComponent', () => {
  let component: ExceptionDetailDialogComponent;
  let fixture: ComponentFixture<ExceptionDetailDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ExceptionDetailDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ExceptionDetailDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
