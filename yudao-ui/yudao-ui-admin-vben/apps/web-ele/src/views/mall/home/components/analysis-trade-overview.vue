<script setup lang="ts">
import type { AnalysisOverviewTradeItem } from './data';

import { computed } from 'vue';

import { CountTo } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';

interface Props {
  items?: AnalysisOverviewTradeItem[];
  modelValue?: AnalysisOverviewTradeItem[];
  columnsNumber?: number;
}

defineOptions({
  name: 'AnalysisOverview',
});

const props = withDefaults(defineProps<Props>(), {
  items: () => [],
  modelValue: () => [],
  columnsNumber: 4,
});

const emit = defineEmits(['update:modelValue']);

const itemsData = computed({
  get: () => (props.modelValue?.length ? props.modelValue : props.items),
  set: (value) => emit('update:modelValue', value),
});

// 计算动态的grid列数类名
const gridColumnsClass = computed(() => {
  const colNum = props.columnsNumber;
  return {
    'lg:grid-cols-1': colNum === 1,
    'lg:grid-cols-2': colNum === 2,
    'lg:grid-cols-3': colNum === 3,
    'lg:grid-cols-4': colNum === 4,
    'lg:grid-cols-5': colNum === 5,
    'lg:grid-cols-6': colNum === 6,
  };
});
</script>

<template>
  <div class="grid grid-cols-1 gap-4 md:grid-cols-2" :class="gridColumnsClass">
    <template v-for="item in itemsData" :key="item.title">
      <div class="flex flex-col gap-2 bg-[var(--el-bg-color-overlay)] p-6">
        <div class="flex items-center justify-between text-gray-500">
          <span>{{ item.title }}</span>
          <el-tooltip
            :content="item.tooltip"
            placement="top-start"
            v-if="item.tooltip"
          >
            <IconifyIcon icon="ep:warning" />
          </el-tooltip>
        </div>
        <div class="mb-4 text-3xl">
          <CountTo
            :prefix="item.prefix"
            :end-val="item.value"
            :decimals="item.decimals"
          />
        </div>
        <div class="flex flex-row gap-1 text-sm">
          <span class="text-gray-500">环比</span>
          <span
            class="flex items-center gap-0.5 whitespace-nowrap"
            :class="
              Number(item.percent) > 0 ? 'text-red-500' : 'text-green-500'
            "
          >
            <span>{{ Math.abs(Number(item.percent)) }}%</span>
            <IconifyIcon
              :icon="
                Number(item.percent) > 0 ? 'ep:caret-top' : 'ep:caret-bottom'
              "
              class="flex-shrink-0 !text-sm"
            />
          </span>
        </div>
      </div>
    </template>
  </div>
</template>
