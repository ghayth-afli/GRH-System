import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PublishDocumentDialogComponent } from './publish-document-dialog.component';

describe('PublishDocumentDialogComponent', () => {
  let component: PublishDocumentDialogComponent;
  let fixture: ComponentFixture<PublishDocumentDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [PublishDocumentDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PublishDocumentDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
