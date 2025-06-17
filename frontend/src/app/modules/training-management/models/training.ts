import { Invitation } from './invitation';

export interface Training {
  id: number;
  title: string;
  description: string;
  department: string;
  startDate: string;
  endDate: string;
  createdBy: string;
  createdAt: string;
  isConfirmed: boolean;
  totalInvitations: number;
}
