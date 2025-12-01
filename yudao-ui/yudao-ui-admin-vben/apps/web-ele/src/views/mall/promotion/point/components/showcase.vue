<!-- 积分商城活动橱窗组件：用于展示和选择积分商城活动 -->
<script lang="ts" setup>
import type { MallPointActivityApi } from '#/api/mall/promotion/point';

import { computed, ref, watch } from 'vue';

import { IconifyIcon } from '@vben/icons';

import { ElImage, ElTooltip } from 'element-plus';

import { getPointActivityListByIds } from '#/api/mall/promotion/point';

import PointTableSelect from './table-select.vue';

interface PointShowcaseProps {
  modelValue?: number | number[];
  limit?: number;
  disabled?: boolean;
}

const props = withDefaults(defineProps<PointShowcaseProps>(), {
  modelValue: undefined,
  limit: Number.MAX_VALUE,
  disabled: false,
});

const emit = defineEmits(['update:modelValue', 'change']);

const pointActivityList = ref<MallPointActivityApi.PointActivity[]>([]); // 已选择的活动列表
const pointTableSelectRef = ref<InstanceType<typeof PointTableSelect>>(); // 活动选择表格组件引用
const isMultiple = computed(() => props.limit !== 1); // 是否为多选模式

/** 计算是否可以添加 */
const canAdd = computed(() => {
  if (props.disabled) {
    return false;
  }
  if (!props.limit) {
    return true;
  }
  return pointActivityList.value.length < props.limit;
});

/** 监听 modelValue 变化，加载活动详情 */
watch(
  () => props.modelValue,
  async (newValue) => {
    // eslint-disable-next-line unicorn/no-nested-ternary
    const ids = Array.isArray(newValue) ? newValue : newValue ? [newValue] : [];
    if (ids.length === 0) {
      pointActivityList.value = [];
      return;
    }
    // 只有活动发生变化时才重新查询
    if (
      pointActivityList.value.length === 0 ||
      pointActivityList.value.some((activity) => !ids.includes(activity.id!))
    ) {
      pointActivityList.value = await getPointActivityListByIds(ids);
    }
  },
  { immediate: true },
);

/** 打开活动选择对话框 */
function handleOpenActivitySelect() {
  pointTableSelectRef.value?.open(pointActivityList.value);
}

/** 选择活动后触发 */
function handleActivitySelected(
  activities:
    | MallPointActivityApi.PointActivity
    | MallPointActivityApi.PointActivity[],
) {
  pointActivityList.value = Array.isArray(activities)
    ? activities
    : [activities];
  emitActivityChange();
}

/** 删除活动 */
function handleRemoveActivity(index: number) {
  pointActivityList.value.splice(index, 1);
  emitActivityChange();
}

/** 触发变更事件 */
function emitActivityChange() {
  if (props.limit === 1) {
    const activity =
      pointActivityList.value.length > 0 ? pointActivityList.value[0] : null;
    emit('update:modelValue', activity?.id || 0);
    emit('change', activity);
  } else {
    emit(
      'update:modelValue',
      pointActivityList.value.map((activity) => activity.id!),
    );
    emit('change', pointActivityList.value);
  }
}
</script>

<template>
  <div class="flex flex-wrap items-center gap-2">
    <!-- 已选活动列表 -->
    <div
      v-for="(activity, index) in pointActivityList"
      :key="activity.id"
      class="group relative h-[60px] w-[60px] overflow-hidden rounded-lg"
    >
      <ElTooltip :content="activity.spuName">
        <div class="relative h-full w-full">
          <ElImage
            :src="activity.picUrl"
            class="h-full w-full rounded-lg object-cover"
            :preview-src-list="[activity.picUrl!]"
            fit="cover"
          />
          <!-- 删除按钮 -->
          <IconifyIcon
            v-if="!disabled"
            icon="ep:circle-close-filled"
            class="absolute -right-2 -top-2 cursor-pointer text-xl text-red-500 opacity-0 transition-opacity hover:text-red-600 group-hover:opacity-100"
            @click="handleRemoveActivity(index)"
          />
        </div>
      </ElTooltip>
    </div>

    <!-- 添加活动按钮 -->
    <ElTooltip v-if="canAdd" content="选择活动">
      <div
        class="flex h-[60px] w-[60px] cursor-pointer items-center justify-center rounded-lg border-2 border-dashed transition-colors hover:border-primary hover:bg-primary/5"
        @click="handleOpenActivitySelect"
      >
        <IconifyIcon icon="ep:plus" class="text-xl text-gray-400" />
      </div>
    </ElTooltip>
  </div>

  <!-- 活动选择对话框 -->
  <PointTableSelect
    ref="pointTableSelectRef"
    :multiple="isMultiple"
    @change="handleActivitySelected"
  />
</template>
