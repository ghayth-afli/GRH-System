import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TrainingFormPageComponent } from './training-form-page.component';

describe('TrainingFormPageComponent', () => {
  let component: TrainingFormPageComponent;
  let fixture: ComponentFixture<TrainingFormPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [TrainingFormPageComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TrainingFormPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
