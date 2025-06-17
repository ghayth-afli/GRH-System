import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TakeRegistrationDialogComponent } from './take-registration-dialog.component';

describe('TakeRegistrationDialogComponent', () => {
  let component: TakeRegistrationDialogComponent;
  let fixture: ComponentFixture<TakeRegistrationDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [TakeRegistrationDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TakeRegistrationDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
