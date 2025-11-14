import type { DescriptionsProps } from 'naive-ui';

import type { CSSProperties, VNode } from 'vue';

// TODO @xingyu：【content】这个纠结下；1）vben2.0 是 render；https://doc.vvbin.cn/components/desc.html#usage 2）
// TODO @xingyu：vben2.0 还有 sapn【done】、labelMinWidth、contentMinWidth
// TODO @xingyu：【hidden】这个纠结下；1）vben2.0 是 show；
export interface DescriptionItemSchema {
  label: string | VNode; // 内容的描述
  field?: string; // 对应 data 中的字段名
  content?: ((data: any) => string | VNode) | string | VNode; // 自定义需要展示的内容，比如说 dict-tag
  span?: number; // 包含列的数量
  labelStyle?: CSSProperties; // 自定义标签样式
  contentStyle?: CSSProperties; // 自定义内容样式
  hidden?: ((data: any) => boolean) | boolean; // 是否显示
}

// TODO @xingyu：vben2.0 还有 title【done】、bordered【done】d、useCollapse、collapseOptions
// TODO @xingyu：from 5.0：bordered 默认为 true
// TODO @xingyu：from 5.0：column 默认为 lg: 3, md: 3, sm: 2, xl: 3, xs: 1, xxl: 4
// TODO @xingyu：from 5.0：size 默认为 small；有 'default', 'middle', 'small', undefined
// TODO @xingyu：from 5.0：useCollapse 默认为 true
export interface DescriptionsOptions {
  data?: Record<string, any>; // 数据
  schema?: DescriptionItemSchema[]; // 描述项配置
  componentProps?: DescriptionsProps; // antd Descriptions 组件参数
}
