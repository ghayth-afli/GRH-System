import { JobOffer } from './job-offer';

export interface JobOfferResponse {
  totalItems: number;
  totalPages: number;
  currentPage: number;
  content: JobOffer[];
}
