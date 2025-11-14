<script setup lang="ts">
import { computed } from 'vue';

import { CountTo } from '@vben/common-ui';
import { createIconifyIcon } from '@vben/icons';

import { Card } from 'ant-design-vue';

defineOptions({ name: 'ComparisonCard' });

const props = defineProps<{
  icon: string;
  iconColor?: string;
  loading?: boolean;
  title: string;
  todayCount: number;
  value: number;
}>();

const iconMap: Record<string, any> = {
  menu: createIconifyIcon('ant-design:appstore-outlined'),
  box: createIconifyIcon('ant-design:box-plot-outlined'),
  cpu: createIconifyIcon('ant-design:cluster-outlined'),
  message: createIconifyIcon('ant-design:message-outlined'),
};

const IconComponent = computed(() => iconMap[props.icon] || iconMap.menu);
</script>

<template>
  <Card class="stat-card" :loading="loading">
    <div class="flex h-full flex-col">
      <div class="mb-4 flex items-start justify-between">
        <div class="flex flex-1 flex-col">
          <span class="mb-2 text-sm font-medium text-gray-500">
            {{ title }}
          </span>
          <span class="text-3xl font-bold text-gray-800">
            <span v-if="value === -1">--</span>
            <CountTo v-else :end-val="value" :duration="1000" />
          </span>
        </div>
        <div :class="`text-4xl ${iconColor}`">
          <IconComponent />
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
    </div>
  </Card>
</template>

<style scoped>
.stat-card {
  height: 160px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.stat-card:hover {
  box-shadow: 0 6px 20px rgb(0 0 0 / 8%);
  transform: translateY(-4px);
}

.stat-card :deep(.ant-card-body) {
  display: flex;
  flex-direction: column;
  height: 100%;
}
</style>
