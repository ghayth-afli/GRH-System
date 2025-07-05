import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RequestDocumentDialogComponent } from './request-document-dialog.component';

describe('RequestDocumentDialogComponent', () => {
  let component: RequestDocumentDialogComponent;
  let fixture: ComponentFixture<RequestDocumentDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [RequestDocumentDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RequestDocumentDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
