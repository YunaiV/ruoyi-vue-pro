<script setup lang="ts">
import { IconifyIcon } from '@vben/icons';
import { cloneDeep } from '@vben/utils';

import { useVModel } from '@vueuse/core';
import { Button, Tooltip } from 'ant-design-vue';
import VueDraggable from 'vuedraggable';

/** 拖拽组件封装 */
defineOptions({ name: 'Draggable' });

/** 定义属性 */
const props = defineProps({
  modelValue: {
    type: Array,
    default: () => [],
  }, // 绑定值
  emptyItem: {
    type: Object,
    default: () => ({}),
  }, // 空的元素：点击添加按钮时，创建元素并添加到列表；默认为空对象
  limit: {
    type: Number,
    default: Number.MAX_VALUE,
  }, // 数量限制：默认为 0，表示不限制
  min: {
    type: Number,
    default: 1,
  }, // 最小数量：默认为1
});

const emit = defineEmits(['update:modelValue']);

const formData = useVModel(props, 'modelValue', emit);

/** 处理添加 */
function handleAdd() {
  return formData.value.push(cloneDeep(props.emptyItem || {}));
}

/** 处理删除 */
const handleDelete = function (index: number) {
  return formData.value.splice(index, 1);
};
</script>

<template>
  <div class="text-sm text-gray-500">拖动左上角的小圆点可对其排序</div>
  <VueDraggable
    :list="formData"
    :force-fallback="true"
    :animation="200"
    handle=".drag-icon"
    class="mt-2"
    item-key="index"
  >
    <template #item="{ element, index }">
      <div class="mb-1 flex flex-col gap-1 rounded border border-gray-200 p-2">
        <!-- 操作按钮区 -->
        <div
          class="-m-2 mb-1 flex flex-row items-center justify-between rounded-t bg-secondary p-2"
        >
          <Tooltip title="拖动排序">
            <IconifyIcon
              icon="lucide:move"
              class="drag-icon cursor-move text-gray-500"
            />
          </Tooltip>
          <Tooltip title="删除">
            <IconifyIcon
              icon="ep:delete"
              class="cursor-pointer text-red-500"
              v-if="formData.length > min"
              @click="handleDelete(index)"
            />
          </Tooltip>
        </div>
        <!-- 内容区 -->
        <slot :element="element" :index="index"></slot>
      </div>
    </template>
  </VueDraggable>
  <Tooltip :title="limit < Number.MAX_VALUE ? `最多添加${limit}个` : undefined">
    <Button
      type="primary"
      ghost
      class="mt-1 w-full"
      :disabled="limit > 0 && formData.length >= limit"
      @click="handleAdd"
    >
      <template #icon>
        <IconifyIcon icon="lucide:plus" />
      </template>
      添加
    </Button>
  </Tooltip>
</template>
