export interface NotificationData<T> {
  eventType: string;
  entityId: string;
  entityType: string;
  payload: Record<string, T>;
  timestamp: string | null;
}
