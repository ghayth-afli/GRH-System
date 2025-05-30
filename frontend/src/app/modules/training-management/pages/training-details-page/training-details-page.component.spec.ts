import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TrainingDetailsPageComponent } from './training-details-page.component';

describe('TrainingDetailsPageComponent', () => {
  let component: TrainingDetailsPageComponent;
  let fixture: ComponentFixture<TrainingDetailsPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [TrainingDetailsPageComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TrainingDetailsPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
