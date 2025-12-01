<script setup lang="ts">
// TODO @xingyu：要不要改成 yudao-ui-admin-vue3/src/components/OperateLogV2/src/OperateLogV2.vue 这种；一行：时间、userType、userName、action
import type { OperateLogProps } from './typing';

import { DICT_TYPE } from '@vben/constants';
import { getDictLabel, getDictObj } from '@vben/hooks';
import { formatDateTime } from '@vben/utils';

import { Tag, Timeline } from 'ant-design-vue';

defineOptions({ name: 'OperateLogV2' });

withDefaults(defineProps<OperateLogProps>(), {
  logList: () => [],
});

function getUserTypeColor(userType: number) {
  const dict = getDictObj(DICT_TYPE.USER_TYPE, userType);
  if (dict && dict.colorType) {
    return `hsl(var(--${dict.colorType}))`;
  }
  return 'hsl(var(--primary))';
}
</script>
<template>
  <div>
    <Timeline>
      <Timeline.Item
        v-for="log in logList"
        :key="log.id"
        :color="getUserTypeColor(log.userType)"
      >
        <template #dot>
          <p
            :style="{ backgroundColor: getUserTypeColor(log.userType) }"
            class="absolute left-1 top-0 flex h-5 w-5 items-center justify-center rounded-full text-xs text-white"
          >
            {{ getDictLabel(DICT_TYPE.USER_TYPE, log.userType)[0] }}
          </p>
        </template>
        <p class="ml-2">{{ formatDateTime(log.createTime) }}</p>
        <p class="ml-2 mt-2">
          <Tag :color="getUserTypeColor(log.userType)">
            {{ log.userName }}
          </Tag>
          {{ log.action }}
        </p>
      </Timeline.Item>
    </Timeline>
  </div>
</template>
