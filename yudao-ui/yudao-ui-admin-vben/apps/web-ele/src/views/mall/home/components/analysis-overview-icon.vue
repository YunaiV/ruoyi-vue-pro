<script setup lang="ts">
import type { AnalysisOverviewIconItem } from './data';

import { computed } from 'vue';

import { CountTo } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';

interface Props {
  items?: AnalysisOverviewIconItem[];
  modelValue?: AnalysisOverviewIconItem[];
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
      <div
        class="flex flex-row items-center gap-3 rounded bg-[var(--el-bg-color-overlay)] p-4"
      >
        <div
          class="flex h-12 w-12 flex-shrink-0 items-center justify-center rounded"
          :class="`${item.iconColor} ${item.iconBgColor}`"
        >
          <IconifyIcon :icon="item.icon" class="text-2xl" />
        </div>
        <div class="flex flex-col gap-1">
          <div class="flex items-center gap-1 text-gray-500">
            <span class="text-sm">{{ item.title }}</span>
            <el-tooltip
              :content="item.tooltip"
              placement="top-start"
              v-if="item.tooltip"
            >
              <IconifyIcon
                icon="ep:warning"
                class="flex items-center text-sm"
              />
            </el-tooltip>
          </div>
          <div class="flex flex-row items-baseline gap-2">
            <div class="text-3xl">
              <CountTo
                :prefix="item.prefix"
                :end-val="item.value"
                :decimals="item.decimals"
              />
            </div>
            <span
              v-if="item.percent !== undefined"
              :class="
                Number(item.percent) > 0 ? 'text-red-500' : 'text-green-500'
              "
              class="flex items-center whitespace-nowrap"
            >
              <span class="text-sm">{{ Math.abs(Number(item.percent)) }}%</span>
              <IconifyIcon
                :icon="
                  Number(item.percent) > 0 ? 'ep:caret-top' : 'ep:caret-bottom'
                "
                class="ml-0.5 text-sm"
              />
            </span>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>
