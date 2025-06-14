import { AppointmentStatus } from './appointment-status';

export interface Appointment {
  id: number;
  medicalVisitId: number;
  doctorName: string;
  timeSlot: Date;
  status: AppointmentStatus;
  userFullName: string;
  userEmail: string;
}
