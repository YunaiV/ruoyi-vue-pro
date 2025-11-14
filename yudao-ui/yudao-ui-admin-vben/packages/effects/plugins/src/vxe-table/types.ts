import type {
  VxeGridListeners,
  VxeGridPropTypes,
  VxeGridProps as VxeTableGridProps,
  VxeUIExport,
} from 'vxe-table';

import type { Ref } from 'vue';

import type { ClassType, DeepPartial } from '@vben/types';

import type { BaseFormComponentType, VbenFormProps } from '@vben-core/form-ui';

import type { VxeGridApi } from './api';

import { useVbenForm } from '@vben-core/form-ui';

export interface VxePaginationInfo {
  currentPage: number;
  pageSize: number;
  total: number;
}

interface ToolbarConfigOptions extends VxeGridPropTypes.ToolbarConfig {
  /** 是否显示切换搜索表单的按钮 */
  search?: boolean;
}

export interface VxeTableGridOptions<T = any> extends VxeTableGridProps<T> {
  /** 工具栏配置 */
  toolbarConfig?: ToolbarConfigOptions;
}

export interface SeparatorOptions {
  show?: boolean;
  backgroundColor?: string;
}

export interface VxeGridProps<
  T extends Record<string, any> = any,
  D extends BaseFormComponentType = BaseFormComponentType,
> {
  /**
   * 标题
   */
  tableTitle?: string;
  /**
   * 标题帮助
   */
  tableTitleHelp?: string;
  /**
   * 组件class
   */
  class?: ClassType;
  /**
   * vxe-grid class
   */
  gridClass?: ClassType;
  /**
   * vxe-grid 配置
   */
  gridOptions?: DeepPartial<VxeTableGridOptions<T>>;
  /**
   * vxe-grid 事件
   */
  gridEvents?: DeepPartial<VxeGridListeners<T>>;
  /**
   * 表单配置
   */
  formOptions?: VbenFormProps<D>;
  /**
   * 显示搜索表单
   */
  showSearchForm?: boolean;
  /**
   * 搜索表单与表格主体之间的分隔条
   */
  separator?: boolean | SeparatorOptions;
}

export type ExtendedVxeGridApi<
  D extends Record<string, any> = any,
  F extends BaseFormComponentType = BaseFormComponentType,
> = VxeGridApi<D> & {
  useStore: <T = NoInfer<VxeGridProps<D, F>>>(
    selector?: (state: NoInfer<VxeGridProps<any, any>>) => T,
  ) => Readonly<Ref<T>>;
};

export interface SetupVxeTable {
  configVxeTable: (ui: VxeUIExport) => void;
  useVbenForm: typeof useVbenForm;
}
