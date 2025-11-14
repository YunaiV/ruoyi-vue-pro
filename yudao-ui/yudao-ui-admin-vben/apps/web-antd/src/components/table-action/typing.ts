import type {
  ButtonProps,
  ButtonType,
} from 'ant-design-vue/es/button/buttonTypes';
import type { TooltipProps } from 'ant-design-vue/es/tooltip/Tooltip';

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
  type?: ButtonType;
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
