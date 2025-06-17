import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateEditMedicalVisitComponent } from './create-edit-medical-visit.component';

describe('CreateEditMedicalVisitComponent', () => {
  let component: CreateEditMedicalVisitComponent;
  let fixture: ComponentFixture<CreateEditMedicalVisitComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CreateEditMedicalVisitComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CreateEditMedicalVisitComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
