export interface AttendanceRecord {
  employeeId: string;
  employeeName: string;
  employeeDepartment: string;
  date: string;
  status: string;
  firstPunch: string | null;
  lastPunch: string | null;
  totalHours: string;
  punchTimes: string[];
  punches: number;
}
