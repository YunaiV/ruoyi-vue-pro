<script lang="tsx">
import type { DescriptionProps } from 'element-plus';

import type { PropType } from 'vue';

import type { DescriptionItemSchema, DescriptionsOptions } from './typing';

import { defineComponent } from 'vue';

import { get } from '@vben/utils';

import { ElDescriptions, ElDescriptionsItem } from 'element-plus';

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
      type: Object as PropType<DescriptionProps>,
      default: () => ({}),
    },
  },

  setup(props: DescriptionsOptions) {
    // TODO @puhui999：每个 field 的 slot 的考虑
    // TODO @puhui999：from 5.0：extra: () => getSlot(slots, 'extra')
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
      <ElDescriptions
        {...props}
        border={props.componentProps?.border}
        column={props.componentProps?.column}
        direction={props.componentProps?.direction}
        extra={props.componentProps?.extra}
        size={props.componentProps?.size}
        title={props.componentProps?.title}
      >
        {props.schema?.filter(shouldShowItem).map((item) => (
          <ElDescriptionsItem
            key={item.field || String(item.label)}
            label={item.label as string}
            span={item.span}
          >
            {renderContent(item)}
          </ElDescriptionsItem>
        ))}
      </ElDescriptions>
    );
  },
});

// TODO @puhui999：from 5.0：emits: ['register'] 事件
export default Description;
</script>
