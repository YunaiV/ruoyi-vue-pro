<script setup lang="ts">
import type { ComparisonCardProps } from './types';

import { computed } from 'vue';

import {
  Card,
  CardContent,
  VbenCountToAnimator,
  VbenIcon,
  VbenLoading,
} from '@vben-core/shadcn-ui';

/** 对比卡片 */
defineOptions({ name: 'ComparisonCard' });

const props = defineProps<ComparisonCardProps>();

// TODO @haohao：看看能不能用中立的 icon，类似 ADD、EDIT 那种。目的：方便后续迁移到 ele 版本里。
const iconMap: Record<string, string> = {
  menu: 'ant-design:appstore-outlined',
  box: 'ant-design:box-plot-outlined',
  cpu: 'ant-design:cluster-outlined',
  message: 'ant-design:message-outlined',
};

const iconName = computed(() => iconMap[props.icon] || iconMap.menu);
</script>

<template>
  <Card
    class="relative h-40 cursor-pointer transition-all duration-300 hover:-translate-y-1 hover:shadow-lg"
  >
    <VbenLoading :spinning="loading" />
    <CardContent class="flex h-full flex-col p-6">
      <div class="mb-4 flex items-start justify-between">
        <div class="flex flex-1 flex-col">
          <span class="mb-2 text-sm font-medium text-gray-500">
            {{ title }}
          </span>
          <span class="text-3xl font-bold text-gray-800">
            <span v-if="value === -1">--</span>
            <VbenCountToAnimator v-else :end-val="value" :duration="1000" />
          </span>
        </div>
        <div :class="`text-4xl ${iconColor || ''}`">
          <VbenIcon :icon="iconName" />
        </div>
      </div>

      <div class="mt-auto border-t border-gray-100 pt-3">
        <div class="flex items-center justify-between text-sm">
          <span class="text-gray-400">今日新增</span>
          <span v-if="todayCount === -1" class="text-gray-400">--</span>
          <span v-else class="font-medium text-green-500">
            +{{ todayCount }}
          </span>
        </div>
      </div>
    </CardContent>
  </Card>
</template>
