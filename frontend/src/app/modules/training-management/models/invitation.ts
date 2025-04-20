import { EStatus } from './invitation-status';

export interface Invitation {
  id: number;
  employeeName: string;
  status: EStatus;
  employeeId: string;
}
