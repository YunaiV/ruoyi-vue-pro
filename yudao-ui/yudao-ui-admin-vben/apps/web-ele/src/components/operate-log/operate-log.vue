<script setup lang="ts">
import type { OperateLogProps } from './typing';

import { DICT_TYPE } from '@vben/constants';
import { getDictLabel, getDictObj } from '@vben/hooks';
import { formatDateTime } from '@vben/utils';

import { ElTag, ElTimeline, ElTimelineItem } from 'element-plus';

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
    <ElTimeline>
      <ElTimelineItem
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
          <ElTag :color="getUserTypeColor(log.userType)">
            {{ log.userName }}
          </ElTag>
          {{ log.action }}
        </p>
      </ElTimelineItem>
    </ElTimeline>
  </div>
</template>
