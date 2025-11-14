<script lang="ts" setup>
import type { MallSeckillActivityApi } from '#/api/mall/promotion/seckill/seckillActivity';

import { computed, ref, watch } from 'vue';

import { IconifyIcon } from '@vben/icons';

import { ElImage, ElTooltip } from 'element-plus';

import * as SeckillActivityApi from '#/api/mall/promotion/seckill/seckillActivity';
import SeckillTableSelect from '#/views/mall/promotion/seckill/components/seckill-table-select.vue';

// 活动橱窗，一般用于装修时使用
// 提供功能：展示活动列表、添加活动、删除活动
defineOptions({ name: 'SeckillShowcase' });

const props = defineProps({
  modelValue: {
    type: [Number, Array],
    required: true,
  },
  // 限制数量：默认不限制
  limit: {
    type: Number,
    default: Number.MAX_VALUE,
  },
  disabled: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(['update:modelValue', 'change']);

// 计算是否可以添加
const canAdd = computed(() => {
  // 情况一：禁用时不可以添加
  if (props.disabled) return false;
  // 情况二：未指定限制数量时，可以添加
  if (!props.limit) return true;
  // 情况三：检查已添加数量是否小于限制数量
  return Activitys.value.length < props.limit;
});

// 拼团活动列表
const Activitys = ref<MallSeckillActivityApi.SeckillActivity[]>([]);

watch(
  () => props.modelValue,
  async () => {
    let ids;
    if (Array.isArray(props.modelValue)) {
      ids = props.modelValue;
    } else {
      ids = props.modelValue ? [props.modelValue] : [];
    }
    // 不需要返显
    if (ids.length === 0) {
      Activitys.value = [];
      return;
    }
    // 只有活动发生变化之后，才会查询活动
    if (
      Activitys.value.length === 0 ||
      Activitys.value.some(
        (seckillActivity) => !ids.includes(seckillActivity.id!),
      )
    ) {
      Activitys.value = await SeckillActivityApi.getSeckillActivityListByIds(
        ids as number[],
      );
    }
  },
  { immediate: true },
);

/** 活动表格选择对话框 */
const seckillActivityTableSelectRef = ref();
// 打开对话框
const openSeckillActivityTableSelect = () => {
  seckillActivityTableSelectRef.value.open(Activitys.value);
};

/**
 * 选择活动后触发
 * @param activityVOs 选中的活动列表
 */
const handleActivitySelected = (
  activityVOs:
    | MallSeckillActivityApi.SeckillActivity
    | MallSeckillActivityApi.SeckillActivity[],
) => {
  Activitys.value = Array.isArray(activityVOs) ? activityVOs : [activityVOs];
  emitActivityChange();
};

/**
 * 删除活动
 * @param index 活动索引
 */
const handleRemoveActivity = (index: number) => {
  Activitys.value.splice(index, 1);
  emitActivityChange();
};
const emitActivityChange = () => {
  if (props.limit === 1) {
    const seckillActivity =
      Activitys.value.length > 0 ? Activitys.value[0] : null;
    emit('update:modelValue', seckillActivity?.id || 0);
    emit('change', seckillActivity);
  } else {
    emit(
      'update:modelValue',
      Activitys.value.map((seckillActivity) => seckillActivity.id),
    );
    emit('change', Activitys.value);
  }
};
</script>
<template>
  <div class="gap-8px flex flex-wrap items-center">
    <div
      v-for="(seckillActivity, index) in Activitys"
      :key="seckillActivity.id"
      class="select-box spu-pic"
    >
      <ElTooltip :content="seckillActivity.name">
        <div class="relative h-full w-full">
          <ElImage :src="seckillActivity.picUrl" class="h-full w-full" />
          <IconifyIcon
            v-show="!disabled"
            class="del-icon"
            icon="ep:circle-close-filled"
            @click="handleRemoveActivity(index)"
          />
        </div>
      </ElTooltip>
    </div>
    <ElTooltip content="选择活动" v-if="canAdd">
      <div class="select-box" @click="openSeckillActivityTableSelect">
        <IconifyIcon icon="ep:plus" />
      </div>
    </ElTooltip>
  </div>
  <!-- 拼团活动选择对话框（表格形式） -->
  <SeckillTableSelect
    ref="seckillActivityTableSelectRef"
    :multiple="limit !== 1"
    @change="handleActivitySelected"
  />
</template>

<style lang="scss" scoped>
.select-box {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px;
  cursor: pointer;
  border: 1px dashed var(--el-border-color-darker);
  border-radius: 8px;
}

.spu-pic {
  position: relative;
}

.del-icon {
  position: absolute;
  top: -10px;
  right: -10px;
  z-index: 1;
  width: 20px !important;
  height: 20px !important;
}
</style>
