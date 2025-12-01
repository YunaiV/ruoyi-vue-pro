<script lang="ts" setup>
import { computed } from 'vue';

import { IconifyIcon } from '@vben/icons';

import { ElCard, ElTag } from 'element-plus';

/** 交易对照卡片 */
defineOptions({ name: 'ComparisonCard' });

const props = withDefaults(defineProps<Props>(), {
  title: '',
  tag: '',
  prefix: '',
  value: 0,
  reference: 0,
  decimals: 0,
});

interface Props {
  title?: string;
  tag?: string;
  prefix?: string;
  value?: number | string;
  reference?: number | string;
  decimals?: number;
}

/** 计算环比百分比 */
const percent = computed(() => {
  const refValue = Number(props.reference);
  const curValue = Number(props.value);
  if (!refValue || refValue === 0) return 0;
  return ((curValue - refValue) / refValue) * 100;
});

/** 格式化今日数据 */
const formattedValue = computed(() => {
  const numValue = Number(props.value);
  return numValue.toFixed(props.decimals);
});

/** 格式化昨日数据 */
const formattedReference = computed(() => {
  const numValue = Number(props.reference);
  return numValue.toFixed(props.decimals);
});
</script>

<template>
  <ElCard :border="false" class="h-full">
    <div class="flex flex-col gap-2">
      <div class="flex items-center justify-between text-gray-500">
        <span>{{ title }}</span>
        <ElTag v-if="tag">{{ tag }}</ElTag>
      </div>
      <div class="flex items-baseline justify-between">
        <div class="text-3xl">{{ prefix }}{{ formattedValue }}</div>
        <span
          :class="percent > 0 ? 'text-red-500' : 'text-green-500'"
          class="flex items-center gap-0.5"
        >
          {{ Math.abs(percent).toFixed(2) }}%
          <IconifyIcon
            :icon="percent > 0 ? 'ep:caret-top' : 'ep:caret-bottom'"
            class="text-sm"
          />
        </span>
      </div>
      <div class="mt-2 border-t border-gray-200 pt-2">
        <div class="flex items-center justify-between text-sm">
          <span class="text-gray-500">昨日数据</span>
          <span>{{ prefix }}{{ formattedReference }}</span>
        </div>
      </div>
    </div>
  </ElCard>
</template>
