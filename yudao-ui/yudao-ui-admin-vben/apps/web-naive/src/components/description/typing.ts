import type { DescriptionsProps as NDescriptionsProps } from 'naive-ui';
import type { JSX } from 'vue/jsx-runtime';

import type { CSSProperties, VNode } from 'vue';

import type { Recordable } from '@vben/types';

export interface DescriptionItemSchema {
  labelMinWidth?: number;
  contentMinWidth?: number;
  // 自定义标签样式
  labelStyle?: CSSProperties;
  // 对应 data 中的字段名
  field: string;
  // 内容的描述
  label: JSX.Element | string | VNode;
  // 包含列的数量
  span?: number;
  // 是否显示
  show?: (...arg: any) => boolean;
  // 插槽名称
  slot?: string;
  // 自定义需要展示的内容
  render?: (
    val: any,
    data?: Recordable<any>,
  ) => Element | JSX.Element | number | string | undefined | VNode;
}

export interface DescriptionProps extends NDescriptionsProps {
  // 是否包含卡片组件
  useCard?: boolean;
  // 描述项配置
  schema: DescriptionItemSchema[];
  // 数据
  data: Recordable<any>;
}

export interface DescInstance {
  setDescProps(descProps: Partial<DescriptionProps>): void;
}
