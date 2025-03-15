import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CalendarSideBarComponent } from './calendar-side-bar.component';

describe('CalendarSideBarComponent', () => {
  let component: CalendarSideBarComponent;
  let fixture: ComponentFixture<CalendarSideBarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CalendarSideBarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CalendarSideBarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
