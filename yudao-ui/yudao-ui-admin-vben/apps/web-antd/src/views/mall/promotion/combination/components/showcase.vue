<!-- 拼团活动橱窗组件：用于展示和选择拼团活动 -->
<script lang="ts" setup>
import type { MallCombinationActivityApi } from '#/api/mall/promotion/combination/combinationActivity';

import { computed, ref, watch } from 'vue';

import { CloseCircleFilled, PlusOutlined } from '@vben/icons';

import { Image, Tooltip } from 'ant-design-vue';

import { getCombinationActivityListByIds } from '#/api/mall/promotion/combination/combinationActivity';

import CombinationTableSelect from './table-select.vue';

interface CombinationShowcaseProps {
  modelValue?: number | number[];
  limit?: number;
  disabled?: boolean;
}

const props = withDefaults(defineProps<CombinationShowcaseProps>(), {
  modelValue: undefined,
  limit: Number.MAX_VALUE,
  disabled: false,
});

const emit = defineEmits(['update:modelValue', 'change']);

const activityList = ref<MallCombinationActivityApi.CombinationActivity[]>([]); // 已选择的活动列表
const combinationTableSelectRef =
  ref<InstanceType<typeof CombinationTableSelect>>(); // 活动选择表格组件引用
const isMultiple = computed(() => props.limit !== 1); // 是否为多选模式

/** 计算是否可以添加 */
const canAdd = computed(() => {
  if (props.disabled) {
    return false;
  }
  if (!props.limit) {
    return true;
  }
  return activityList.value.length < props.limit;
});

/** 监听 modelValue 变化，加载活动详情 */
watch(
  () => props.modelValue,
  async (newValue) => {
    // eslint-disable-next-line unicorn/no-nested-ternary
    const ids = Array.isArray(newValue) ? newValue : newValue ? [newValue] : [];
    if (ids.length === 0) {
      activityList.value = [];
      return;
    }
    // 只有活动发生变化时才重新查询
    if (
      activityList.value.length === 0 ||
      activityList.value.some((activity) => !ids.includes(activity.id!))
    ) {
      activityList.value = await getCombinationActivityListByIds(ids);
    }
  },
  { immediate: true },
);

/** 打开活动选择对话框 */
function handleOpenActivitySelect() {
  combinationTableSelectRef.value?.open(activityList.value);
}

/** 选择活动后触发 */
function handleActivitySelected(
  activities:
    | MallCombinationActivityApi.CombinationActivity
    | MallCombinationActivityApi.CombinationActivity[],
) {
  activityList.value = Array.isArray(activities) ? activities : [activities];
  emitActivityChange();
}

/** 删除活动 */
function handleRemoveActivity(index: number) {
  activityList.value.splice(index, 1);
  emitActivityChange();
}

/** 触发变更事件 */
function emitActivityChange() {
  if (props.limit === 1) {
    const activity =
      activityList.value.length > 0 ? activityList.value[0] : null;
    emit('update:modelValue', activity?.id || 0);
    emit('change', activity);
  } else {
    emit(
      'update:modelValue',
      activityList.value.map((activity) => activity.id!),
    );
    emit('change', activityList.value);
  }
}
</script>

<template>
  <div class="flex flex-wrap items-center gap-2">
    <!-- 已选活动列表 -->
    <div
      v-for="(activity, index) in activityList"
      :key="activity.id"
      class="group relative h-[60px] w-[60px] overflow-hidden rounded-lg"
    >
      <Tooltip :title="activity.name">
        <div class="relative h-full w-full">
          <!-- TODO @芋艿 -->
          <Image
            :src="activity.picUrl"
            class="h-full w-full rounded-lg object-cover"
          />
          <!-- 删除按钮 -->
          <CloseCircleFilled
            v-if="!disabled"
            class="absolute -right-2 -top-2 cursor-pointer text-xl text-red-500 opacity-0 transition-opacity hover:text-red-600 group-hover:opacity-100"
            @click="handleRemoveActivity(index)"
          />
        </div>
      </Tooltip>
    </div>

    <!-- 添加活动按钮 -->
    <Tooltip v-if="canAdd" title="选择活动">
      <div
        class="flex h-[60px] w-[60px] cursor-pointer items-center justify-center rounded-lg border-2 border-dashed transition-colors hover:border-primary hover:bg-primary/5"
        @click="handleOpenActivitySelect"
      >
        <PlusOutlined class="text-xl text-gray-400" />
      </div>
    </Tooltip>
  </div>

  <!-- 活动选择对话框 -->
  <CombinationTableSelect
    ref="combinationTableSelectRef"
    :multiple="isMultiple"
    @change="handleActivitySelected"
  />
</template>
