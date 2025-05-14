import { AfterViewInit, Component, ElementRef, ViewChild } from '@angular/core';
import { Chart } from 'chart.js/auto';

@Component({
  selector: 'app-application-details-page',
  standalone: false,
  templateUrl: './application-details-page.component.html',
  styleUrls: ['./application-details-page.component.css'],
})
export class ApplicationDetailsPageComponent implements AfterViewInit {
  @ViewChild('scoreChart') scoreChart!: ElementRef<HTMLCanvasElement>;

  ngAfterViewInit() {
    const ctx = this.scoreChart.nativeElement.getContext('2d');
    if (ctx) {
      new Chart(ctx, {
        type: 'doughnut',
        data: {
          datasets: [
            {
              data: [72, 28],
              backgroundColor: ['#fd5b00', '#f0f0f0'],
              borderWidth: 0,
            },
          ],
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          cutout: '75%',
          plugins: {
            legend: { display: false },
            tooltip: { enabled: false },
          },
        },
      });
    }
  }
}
