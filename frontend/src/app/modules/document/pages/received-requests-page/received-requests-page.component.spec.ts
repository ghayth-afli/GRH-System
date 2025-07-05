import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReceivedRequestsPageComponent } from './received-requests-page.component';

describe('ReceivedRequestsPageComponent', () => {
  let component: ReceivedRequestsPageComponent;
  let fixture: ComponentFixture<ReceivedRequestsPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ReceivedRequestsPageComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReceivedRequestsPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
