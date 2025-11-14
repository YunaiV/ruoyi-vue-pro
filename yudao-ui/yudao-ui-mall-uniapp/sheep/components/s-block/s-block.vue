<!-- 装修组件容器 -->
<template>
  <view :style="[elStyles, elBackground]"><slot /></view>
</template>

<script setup>
  /**
   * 容器组件 - 装修组件的样式容器
   */
  import { computed, provide, unref } from 'vue';
  import sheep from '@/sheep';

  const props = defineProps({
    styles: {
      type: Object,
      default() {},
    },
  });

  // 组件样式
  const elBackground = computed(() => {
    if (props.styles) {
      if (props.styles.bgType === 'color') {
        return { background: props.styles.bgColor };
      }
      if (props.styles.bgType === 'img') {
        return {
          background: `url(${sheep.$url.cdn(props.styles.bgImg)}) no-repeat top center / 100% auto`,
        };
      }
    }
  });

  const elStyles = computed(() => {
    if (props.styles) {
      return {
        marginTop: `${props.styles.marginTop || 0}px`,
        marginBottom: `${props.styles.marginBottom || 0}px`,
        marginLeft: `${props.styles.marginLeft || 0}px`,
        marginRight: `${props.styles.marginRight || 0}px`,
        paddingTop: `${props.styles.paddingTop || 0}px`,
        paddingRight: `${props.styles.paddingRight || 0}px`,
        paddingBottom: `${props.styles.paddingBottom || 0}px`,
        paddingLeft: `${props.styles.paddingLeft || 0}px`,
        borderTopLeftRadius: `${props.styles.borderTopLeftRadius || 0}px`,
        borderTopRightRadius: `${props.styles.borderTopRightRadius || 0}px`,
        borderBottomRightRadius: `${props.styles.borderBottomRightRadius || 0}px`,
        borderBottomLeftRadius: `${props.styles.borderBottomLeftRadius || 0}px`,
        overflow: 'hidden',
      };
    }
  });
</script>
