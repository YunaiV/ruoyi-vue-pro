import { createIconifyIcon } from '@vben/icons';

// bpm 图标
// TODO @siye：可以新建出一个 bpm 目录哇？icons/bpm；
const SvgBpmRunningIcon = createIconifyIcon('svg:bpm-running');
const SvgBpmApproveIcon = createIconifyIcon('svg:bpm-approve');
const SvgBpmRejectIcon = createIconifyIcon('svg:bpm-reject');
const SvgBpmCancelIcon = createIconifyIcon('svg:bpm-cancel');

export {
  SvgBpmApproveIcon,
  SvgBpmCancelIcon,
  SvgBpmRejectIcon,
  SvgBpmRunningIcon,
};
