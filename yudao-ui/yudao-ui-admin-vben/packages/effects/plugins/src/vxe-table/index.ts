import { defineAsyncComponent } from 'vue';

export { setupVbenVxeTable } from './init';
export { default as VbenVxeTableToolbar } from './table-toolbar.vue';
export type { VxeTableGridOptions } from './types';
export * from './use-vxe-grid';
export { default as VbenVxeGrid } from './use-vxe-grid.vue';
export { useTableToolbar } from './use-vxe-toolbar';
export * from './validation';

export type {
  VxeGridListeners,
  VxeGridProps,
  VxeGridPropTypes,
  VxeTableInstance,
} from 'vxe-table';

// 异步导出 vxe-table 相关组件提供给需要单独使用 vxe-table 的场景
export const AsyncVxeTable = defineAsyncComponent(() =>
  import('vxe-table').then((mod) => mod.VxeTable),
);
export const AsyncVxeColumn = defineAsyncComponent(() =>
  import('vxe-table').then((mod) => mod.VxeColumn),
);
export const AsyncVxeToolbar = defineAsyncComponent(() =>
  import('vxe-table').then((mod) => mod.VxeToolbar),
);
