<script setup lang="ts">
import { computed } from 'vue';

import { getDictObj } from '@vben/hooks';

import { NTag } from 'naive-ui';

interface DictTagProps {
  /**
   * 字典类型
   */
  type: string;
  /**
   * 字典值
   */
  value: any;
  /**
   * 图标
   */
  icon?: string;
}

const props = defineProps<DictTagProps>();

/** 获取字典标签 */
const dictTag = computed(() => {
  const defaultDict = {
    label: '',
    colorType: 'primary',
  };
  // 校验参数有效性
  if (!props.type || props.value === undefined || props.value === null) {
    return defaultDict;
  }

  // 获取字典对象
  const dict = getDictObj(props.type, String(props.value));
  if (!dict) {
    return defaultDict;
  }

  // 处理颜色类型
  let colorType = dict.colorType;
  switch (colorType) {
    case 'danger': {
      colorType = 'error';
      break;
    }
    case 'info': {
      colorType = 'info';
      break;
    }
    case 'primary': {
      colorType = 'primary';
      break;
    }
    case 'success': {
      colorType = 'success';
      break;
    }
    case 'warning': {
      colorType = 'warning';
      break;
    }
    default: {
      if (!colorType) {
        colorType = '';
      }
    }
  }

  return {
    label: dict.label || '',
    colorType,
  };
});
</script>

<template>
  <NTag v-if="dictTag.label" :type="dictTag.colorType as any">
    {{ dictTag.label }}
  </NTag>
</template>
