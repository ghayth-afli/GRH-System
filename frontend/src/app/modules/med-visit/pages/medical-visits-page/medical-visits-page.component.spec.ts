import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MedicalVisitsPageComponent } from './medical-visits-page.component';

describe('MedicalVisitsPageComponent', () => {
  let component: MedicalVisitsPageComponent;
  let fixture: ComponentFixture<MedicalVisitsPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [MedicalVisitsPageComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MedicalVisitsPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
