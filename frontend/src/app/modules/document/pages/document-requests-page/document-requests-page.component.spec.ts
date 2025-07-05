import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DocumentRequestsPageComponent } from './document-requests-page.component';

describe('DocumentRequestsPageComponent', () => {
  let component: DocumentRequestsPageComponent;
  let fixture: ComponentFixture<DocumentRequestsPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DocumentRequestsPageComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DocumentRequestsPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
