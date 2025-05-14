import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-job-offer-details',
  standalone: false,
  templateUrl: './job-offer-details.component.html',
  styleUrl: './job-offer-details.component.css',
})
export class JobOfferDetailsComponent {
  jobOffer = {
    id: 1,
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
    status: 'Active',
    applicants: 5,
  };
  isCopied = false;

  constructor(private route: ActivatedRoute) {}

  ngOnInit() {
    // Simulate fetching job offer by ID
    const id = this.route.snapshot.paramMap.get('id');
    // In a real app, fetch data from a service using the ID
    console.log(`Fetching job offer with ID: ${id}`);
  }

  copyToClipboard() {
    const text = `
Title: ${this.jobOffer.title}
Department: ${this.jobOffer.department}
Role: ${this.jobOffer.role}
Type: ${this.jobOffer.isInternal ? 'Internal' : 'External'}
Status: ${this.jobOffer.status}
Applicants: ${this.jobOffer.applicants}
Description: ${this.jobOffer.description}
Responsibilities: ${this.jobOffer.responsibilities}
Qualifications: ${this.jobOffer.qualifications}
    `.trim();

    navigator.clipboard
      .writeText(text)
      .then(() => {
        this.isCopied = true;
        setTimeout(() => {
          this.isCopied = false;
        }, 2000); // Show "Copied!" for 2 seconds
      })
      .catch((err) => {
        console.error('Failed to copy to clipboard:', err);
      });
  }
}
