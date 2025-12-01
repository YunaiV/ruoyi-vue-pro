import type { DescriptionsProps } from 'ant-design-vue/es/descriptions';
import type { JSX } from 'vue/jsx-runtime';

import type { CSSProperties, VNode } from 'vue';

import type { Recordable } from '@vben/types';

export interface DescriptionItemSchema {
  labelMinWidth?: number;
  contentMinWidth?: number;
  labelStyle?: CSSProperties; // 自定义标签样式
  field: string; // 对应 data 中的字段名
  label: JSX.Element | string | VNode; // 内容的描述
  span?: number; // 包含列的数量
  show?: (...arg: any) => boolean; // 是否显示
  slot?: string; // 插槽名称
  render?: (
    val: any,
    data?: Recordable<any>,
  ) => Element | JSX.Element | number | string | undefined | VNode; // 自定义需要展示的内容
}

export interface DescriptionProps extends DescriptionsProps {
  useCard?: boolean; // 是否包含卡片组件
  schema: DescriptionItemSchema[]; // 描述项配置
  data: Recordable<any>; // 数据
  title?: string; // 标题
  bordered?: boolean; // 是否包含边框
  column?:
    | number
    | {
        lg: number;
        md: number;
        sm: number;
        xl: number;
        xs: number;
        xxl: number;
      }; // 列数
}

export interface DescInstance {
  setDescProps(descProps: Partial<DescriptionProps>): void;
}
