import { Invitation } from './invitation';

export interface Training {
  id: number;
  title: string;
  description: string;
  department: string;
  startDate: Date;
  endDate: Date;
  createdBy: string;
  invitations: Invitation[];
  createdAt: string;
}
