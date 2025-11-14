<script setup lang="ts">
import type { AnalysisOverviewItem } from './data';

import { computed } from 'vue';

import { VbenCountToAnimator } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';

interface Props {
  items?: AnalysisOverviewItem[];
  modelValue?: AnalysisOverviewItem[];
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

// 计算环比增长率
const calculateGrowthRate = (
  currentValue: number,
  previousValue: number,
): { isPositive: boolean; rate: number } => {
  if (previousValue === 0) {
    return { rate: currentValue > 0 ? 100 : 0, isPositive: currentValue >= 0 };
  }

  const rate = ((currentValue - previousValue) / previousValue) * 100;
  return { rate: Math.abs(rate), isPositive: rate >= 0 };
};

// 格式化增长率显示
const formatGrowthRate = (rate: number): string => {
  return rate.toFixed(1);
};
</script>

<template>
  <div class="grid grid-cols-1 gap-4 md:grid-cols-2" :class="gridColumnsClass">
    <template v-for="item in itemsData" :key="item.title">
      <el-card :title="item.title" class="w-full">
        <template #header>
          <div class="text-lg font-semibold">
            <div class="flex items-center justify-between">
              <div class="flex items-center">
                <span>{{ item.title }}</span>
                <span v-if="item.tooltip" class="ml-1 inline-block">
                  <el-tooltip>
                    <template #default>
                      <div
                        class="inline-flex h-4 w-4 translate-y-[-3px] items-center justify-center rounded-full bg-gray-200 text-xs font-bold text-gray-600"
                      >
                        !
                      </div>
                    </template>
                    <template #content>
                      {{ item.tooltip }}
                    </template>
                  </el-tooltip>
                </span>
              </div>
              <el-tag>今日</el-tag>
            </div>
          </div>
        </template>
        <template #default>
          <!-- 左右布局：左边数字，右边图标 -->
          <div class="flex items-center justify-between">
            <!-- 左侧：数字显示 -->
            <div class="flex-1">
              <div class="flex items-baseline">
                <!-- prefix 前缀 -->
                <span
                  v-if="item.prefix"
                  class="mr-1 text-3xl font-medium text-gray-600"
                >
                  {{ item.prefix }}
                </span>
                <!-- 数字动画 -->
                <VbenCountToAnimator
                  :end-val="item.value"
                  :start-val="1"
                  class="text-3xl font-bold text-gray-900"
                  prefix=""
                />
              </div>
            </div>

            <!-- 右侧：环比增长率图标和数值 -->
            <div
              v-if="item.showGrowthRate && item.totalValue !== undefined"
              class="flex items-center space-x-2 rounded-lg bg-gray-50 px-3 py-2"
            >
              <IconifyIcon
                :icon="
                  calculateGrowthRate(item.value, item.totalValue).isPositive
                    ? 'lucide:trending-up'
                    : 'lucide:trending-down'
                "
                class="size-5"
                :class="[
                  calculateGrowthRate(item.value, item.totalValue).isPositive
                    ? 'text-green-500'
                    : 'text-red-500',
                ]"
              />
              <span
                class="text-sm font-semibold"
                :class="[
                  calculateGrowthRate(item.value, item.totalValue).isPositive
                    ? 'text-green-500'
                    : 'text-red-500',
                ]"
              >
                {{
                  calculateGrowthRate(item.value, item.totalValue).isPositive
                    ? '+'
                    : '-'
                }}{{
                  formatGrowthRate(
                    calculateGrowthRate(item.value, item.totalValue).rate,
                  )
                }}%
              </span>
            </div>
          </div>
        </template>
        <template #footer v-if="item.totalTitle">
          <div class="flex items-center justify-between">
            <span>{{ item.totalTitle }}</span>
            <VbenCountToAnimator
              :end-val="item.totalValue"
              :start-val="1"
              prefix=""
            />
          </div>
        </template>
      </el-card>
    </template>
  </div>
</template>
<style lang="scss" scoped>
/* 移除 el-card header 的下边框 */
:deep(.el-card__header) {
  padding-bottom: 16px;
  border-bottom: none !important;
}
</style>
