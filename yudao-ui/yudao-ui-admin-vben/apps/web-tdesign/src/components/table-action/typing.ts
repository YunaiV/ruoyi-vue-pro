import type { TdButtonProps, TooltipProps } from 'tdesign-vue-next';

export interface PopConfirm {
  title: string;
  okText?: string;
  cancelText?: string;
  confirm: () => void;
  cancel?: () => void;
  icon?: string;
  disabled?: boolean;
}

export interface ActionItem {
  onClick?: () => void;
  type?: TdButtonProps['theme'];
  label?: string;
  icon?: string;
  color?: 'error' | 'success' | 'warning';
  popConfirm?: PopConfirm;
  disabled?: boolean;
  divider?: boolean;
  // 权限编码控制是否显示
  auth?: string[];
  // 业务控制是否显示
  ifShow?: ((action: ActionItem) => boolean) | boolean;
  tooltip?: string | TooltipProps;
  loading?: boolean;
  size?: TdButtonProps['size'];
  shape?: TdButtonProps['shape'];
  variant?: TdButtonProps['variant'];
  danger?: boolean;
}
