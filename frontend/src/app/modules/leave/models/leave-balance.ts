export interface LeaveBalance {
  id: number;
  userDn: string;
  totalLeave: number;
  usedLeave: number;
  remainingLeave: number;
  lastUpdatedDate: string;
}
