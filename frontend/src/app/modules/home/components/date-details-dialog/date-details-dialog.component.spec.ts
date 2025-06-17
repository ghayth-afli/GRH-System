import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DateDetailsDialogComponent } from './date-details-dialog.component';

describe('DateDetailsDialogComponent', () => {
  let component: DateDetailsDialogComponent;
  let fixture: ComponentFixture<DateDetailsDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DateDetailsDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DateDetailsDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
