export interface Notification {
  id: number;
  title: string;
  message: string;
  sender: string;
  type: string;
  createdAt: Date;
  read: boolean;
  sourceId: string;
  actionUrl: string;
}
