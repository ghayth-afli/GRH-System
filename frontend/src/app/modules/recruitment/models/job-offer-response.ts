import { EjobOfferStatus } from './EjobOfferStatus';

export interface JobOfferResponse {
  id: number;
  title: string;
  description: string;
  department: string;
  responsibilities: string;
  qualifications: string;
  role: string;
  applied: boolean;
  numberOfApplications: number;
  status: string;
  isInternal: boolean;
  createdAt: string;
  updatedAt: string;
}
