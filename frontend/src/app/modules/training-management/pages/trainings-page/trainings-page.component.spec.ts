import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TrainingsPageComponent } from './trainings-page.component';

describe('TrainingsPageComponent', () => {
  let component: TrainingsPageComponent;
  let fixture: ComponentFixture<TrainingsPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [TrainingsPageComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TrainingsPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
