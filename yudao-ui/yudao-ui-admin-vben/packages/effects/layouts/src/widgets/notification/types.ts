interface NotificationItem {
  id: any;
  avatar: string;
  date: string;
  isRead?: boolean;
  message: string;
  title: string;
  /**
   * 跳转链接，可以是路由路径或完整 URL
   * @example '/dashboard' 或 'https://example.com'
   */
  link?: string;
  query?: Record<string, any>;
  state?: Record<string, any>;
}

export type { NotificationItem };
