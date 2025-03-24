import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EditPersonalInfoModalFormComponent } from './edit-personal-info-modal-form.component';

describe('EditPersonalInfoModalFormComponent', () => {
  let component: EditPersonalInfoModalFormComponent;
  let fixture: ComponentFixture<EditPersonalInfoModalFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [EditPersonalInfoModalFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EditPersonalInfoModalFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
