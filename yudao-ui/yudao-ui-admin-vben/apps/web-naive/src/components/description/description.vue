<script lang="tsx">
import type { DescriptionsProps } from 'naive-ui';

import type { PropType } from 'vue';

import type { DescriptionItemSchema, DescriptionsOptions } from './typing';

import { defineComponent } from 'vue';

import { get } from '@vben/utils';

import { NDescriptions, NDescriptionsItem } from 'naive-ui';

/** 对 Descriptions 进行二次封装 */
const Description = defineComponent({
  name: 'Descriptions',
  props: {
    data: {
      type: Object as PropType<Record<string, any>>,
      default: () => ({}),
    },
    schema: {
      type: Array as PropType<DescriptionItemSchema[]>,
      default: () => [],
    },
    // Descriptions 原生 props
    componentProps: {
      type: Object as PropType<DescriptionsProps>,
      default: () => ({}),
    },
  },

  setup(props: DescriptionsOptions) {
    // TODO @xingyu：每个 field 的 slot 的考虑
    // TODO @xingyu：from 5.0：extra: () => getSlot(slots, 'extra')
    /** 过滤掉不需要展示的 */
    const shouldShowItem = (item: DescriptionItemSchema) => {
      if (item.hidden === undefined) return true;
      return typeof item.hidden === 'function'
        ? !item.hidden(props.data)
        : !item.hidden;
    };
    /** 渲染内容 */
    const renderContent = (item: DescriptionItemSchema) => {
      if (item.content) {
        return typeof item.content === 'function'
          ? item.content(props.data)
          : item.content;
      }
      return item.field ? get(props.data, item.field) : null;
    };

    return () => (
      <NDescriptions
        {...props}
        bordered={props.componentProps?.bordered}
        column={props.componentProps?.column}
        labelPlacement={props.componentProps?.labelPlacement || 'left'}
        size={props.componentProps?.size}
        title={props.componentProps?.title}
      >
        {props.schema?.filter(shouldShowItem).map((item) => (
          <NDescriptionsItem
            contentStyle={item.contentStyle}
            key={item.field || String(item.label)}
            label={item.label as string}
            labelStyle={item.labelStyle}
            span={item.span}
          >
            {renderContent(item)}
          </NDescriptionsItem>
        ))}
      </NDescriptions>
    );
  },
});

// TODO @xingyu：from 5.0：emits: ['register'] 事件
export default Description;
</script>
