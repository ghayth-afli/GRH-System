export interface JobOffer {
  id: number;
  title: string;
  description: string;
  department: string;
  responsibilities: string;
  qualifications: string;
  role: string;
  status: 'OPEN' | 'CLOSED' | string;
  isInternal: boolean;
  createdAt: string;
  updatedAt: string;
}
