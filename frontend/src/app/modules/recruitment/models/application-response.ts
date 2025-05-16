export interface ApplicationResponseDTO {
  id: number;
  candidateId: number;
  FullName: string;
  isInternal: boolean;
  Email: string;
  Phone: string;
  status: string;
  score: number;
  submissionDate: string;
}
export interface ApplicationDetailsResponseDTO {
  id: number;
  jobOfferId: number;
  applicantName: string;
  applicationDate: string;
  status: string;
  resumeUrl: string;
  // Add any additional fields from your detailed DTO
}
