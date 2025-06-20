export interface AttendanceRecord {
  employeeId: string;
  employeeName: string;
  department: string;
  date: string; // ISO 8601 date string (e.g., '2024-06-20')
  status: 'PRESENT' | 'LATE' | 'ABSENT' | 'HALF_DAY' | 'ON_LEAVE' | string; // Extend if needed
  firstPunch: string | null; // ISO 8601 time string (e.g., '08:30:00')
  lastPunch: string | null;
  totalHours: string; // Could also be number if formatted accordingly
  allPunches: string[]; // Array of ISO 8601 time strings
  punchCount: number;
  issues: Set<string> | string[]; // Use string[] for compatibility if Set is not required
}
