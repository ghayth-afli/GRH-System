import { ELeaveType } from './leave-type.enum';

export interface Leave {
  Name: string;
  Department: string;
  startDate: string;
  endDate: string;
  leaveType: ELeaveType;
  status: string;
}

export interface LeaveResponse {
  content: Leave[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}

export interface LeaveBalance {
  id: number;
  userDn: string;
  totalLeave: number;
  usedLeave: number;
  remainingLeave: number;
  lastUpdatedDate: string;
}
