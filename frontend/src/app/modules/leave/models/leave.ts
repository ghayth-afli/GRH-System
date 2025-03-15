import {LeaveType} from './leave-type';

export interface Leave {
  id: number;
  Name: string;
  Department: string;
  startDate: string;
  endDate: string;
  leaveType: LeaveType;
  status: string;
}
