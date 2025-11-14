import type { Arrayable } from '@vueuse/core';
import type { FlattenedItem } from 'radix-vue';

import type { Recordable } from '@vben-core/typings';

export interface TreeProps {
  /** 单选时允许取消已有选项 */
  allowClear?: boolean;
  /** 非关联选择时，自动选中上级节点 */
  autoCheckParent?: boolean;
  /** 显示边框 */
  bordered?: boolean;
  /** 取消父子关联选择 */
  checkStrictly?: boolean;
  /** 子级字段名 */
  childrenField?: string;
  /** 默认展开的键 */
  defaultExpandedKeys?: Array<number | string>;
  /** 默认展开的级别（优先级高于defaultExpandedKeys） */
  defaultExpandedLevel?: number;
  /** 默认值 */
  defaultValue?: Arrayable<number | string>;
  /** 禁用 */
  disabled?: boolean;
  /** 禁用字段名 */
  disabledField?: string;
  /** 自定义节点类名 */
  getNodeClass?: (item: FlattenedItem<Recordable<any>>) => string;
  iconField?: string;
  /** label字段 */
  labelField?: string;
  /** 是否多选 */
  multiple?: boolean;
  /** 显示由iconField指定的图标 */
  showIcon?: boolean;
  /** 启用展开收缩动画 */
  transition?: boolean;
  /** 树数据 */
  treeData: Recordable<any>[];
  /** 值字段 */
  valueField?: string;
}

export function treePropsDefaults() {
  return {
    allowClear: false,
    autoCheckParent: true,
    bordered: false,
    checkStrictly: false,
    defaultExpandedKeys: () => [],
    defaultExpandedLevel: 0,
    disabled: false,
    disabledField: 'disabled',
    iconField: 'icon',
    labelField: 'label',
    multiple: false,
    showIcon: true,
    transition: true,
    valueField: 'value',
    childrenField: 'children',
  };
}
