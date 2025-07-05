import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProcessRequestDialogComponent } from './process-request-dialog.component';

describe('ProcessRequestDialogComponent', () => {
  let component: ProcessRequestDialogComponent;
  let fixture: ComponentFixture<ProcessRequestDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ProcessRequestDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ProcessRequestDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
