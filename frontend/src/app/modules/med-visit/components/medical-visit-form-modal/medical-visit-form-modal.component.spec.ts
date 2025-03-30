import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MedicalVisitFormModalComponent } from './medical-visit-form-modal.component';

describe('MedicalVisitFormModalComponent', () => {
  let component: MedicalVisitFormModalComponent;
  let fixture: ComponentFixture<MedicalVisitFormModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [MedicalVisitFormModalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MedicalVisitFormModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
