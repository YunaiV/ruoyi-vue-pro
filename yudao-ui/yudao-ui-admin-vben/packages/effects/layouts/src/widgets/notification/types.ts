interface NotificationItem {
  avatar: string;
  date: string;
  isRead?: boolean;
  message: string;
  title: string;
  id?: number;
}

export type { NotificationItem };
