import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TrainingFormModalComponent } from './training-form-modal.component';

describe('TrainingFormModalComponent', () => {
  let component: TrainingFormModalComponent;
  let fixture: ComponentFixture<TrainingFormModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [TrainingFormModalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TrainingFormModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
