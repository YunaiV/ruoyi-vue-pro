import type { ButtonProps } from 'naive-ui/es/button/src/Button';
import type { TooltipProps } from 'naive-ui/es/tooltip/src/Tooltip';

export interface PopConfirm {
  title: string;
  okText?: string;
  cancelText?: string;
  confirm: () => void;
  cancel?: () => void;
  icon?: string;
  disabled?: boolean;
}

export interface ActionItem extends ButtonProps {
  onClick?: () => void;
  type?: ButtonProps['type'];
  label?: string;
  color?: 'error' | 'success' | 'warning';
  icon?: string;
  popConfirm?: PopConfirm;
  disabled?: boolean;
  divider?: boolean;
  // 权限编码控制是否显示
  auth?: string[];
  // 业务控制是否显示
  ifShow?: ((action: ActionItem) => boolean) | boolean;
  tooltip?: string | TooltipProps;
}
