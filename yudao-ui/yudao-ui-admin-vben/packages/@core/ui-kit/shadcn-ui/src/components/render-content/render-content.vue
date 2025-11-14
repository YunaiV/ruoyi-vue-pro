<script lang="ts">
import type { Component, PropType } from 'vue';

import { defineComponent, h } from 'vue';

import { isFunction, isObject, isString } from '@vben-core/shared/utils';

export default defineComponent({
  name: 'RenderContent',
  props: {
    content: {
      default: undefined as
        | PropType<(() => any) | Component | string>
        | undefined,
      type: [Object, String, Function],
    },
    renderBr: {
      default: false,
      type: Boolean,
    },
  },
  setup(props, { attrs, slots }) {
    return () => {
      if (!props.content) {
        return null;
      }
      const isComponent =
        (isObject(props.content) || isFunction(props.content)) &&
        props.content !== null;
      if (!isComponent) {
        if (props.renderBr && isString(props.content)) {
          const lines = props.content.split('\n');
          const result = [];
          for (const [i, line] of lines.entries()) {
            result.push(h('p', { key: i }, line));
            // if (i < lines.length - 1) {
            //   result.push(h('br'));
            // }
          }
          return result;
        } else {
          return props.content;
        }
      }
      return h(props.content as never, {
        ...attrs,
        props: {
          ...props,
          ...attrs,
        },
        slots,
      });
    };
  },
});
</script>
