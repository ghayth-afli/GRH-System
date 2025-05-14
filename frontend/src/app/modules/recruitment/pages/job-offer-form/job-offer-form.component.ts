import { Component, ViewChild } from '@angular/core';
import { NgForm } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-job-offer-form',
  standalone: false,
  templateUrl: './job-offer-form.component.html',
  styleUrl: './job-offer-form.component.css',
})
export class JobOfferFormComponent {
  isEditMode = false;
  submitted = false;
  @ViewChild('jobOfferForm') jobOfferForm!: NgForm;

  jobOffer = {
    title: '',
    description: '',
    department: '',
    responsibilities: '',
    qualifications: '',
    role: '',
    isInternal: false,
  };

  constructor(private route: ActivatedRoute, private router: Router) {}

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    this.isEditMode = !!id;
    if (this.isEditMode) {
      // Simulate fetching job offer data for edit mode
      this.jobOffer = {
        title: 'Senior Java Developer',
        description:
          'We are looking for an experienced Java developer to join our backend team. You will be responsible for developing scalable and high-performance enterprise applications.',
        department: 'Software Engineering',
        responsibilities:
          'Design, implement, and maintain Java-based applications. Collaborate with cross-functional teams. Participate in code reviews and contribute to best practices.',
        qualifications:
          "Bachelor's degree in Computer Science or related field. 5+ years of experience in Java development. Strong understanding of Spring Boot and RESTful APIs.",
        role: 'Backend Developer',
        isInternal: true,
      };
    }
  }

  onSubmit() {
    this.submitted = true;
    if (this.jobOfferForm.valid) {
      const jsonRequest = JSON.stringify(this.jobOffer, null, 2);
      console.log('Submitting job offer:', jsonRequest);
      // Simulate backend request
      // In a real app, send to backend via HttpClient
      this.router.navigate(['/recruitment/job-offers']);
    }
  }
}
