<script setup lang="ts">
// TODO @芋艿：后续合并到 diy-editor 里，并不是通用的；

import { IconifyIcon } from '@vben/icons';
import { cloneDeep } from '@vben/utils';

import { useVModel } from '@vueuse/core';
// 拖拽组件
import VueDraggable from 'vuedraggable';

// 拖拽组件封装
defineOptions({ name: 'Draggable' });

// 定义属性
const props = defineProps({
  // 绑定值
  modelValue: {
    type: Array,
    default: () => [],
  },
  // 空的元素：点击添加按钮时，创建元素并添加到列表；默认为空对象
  emptyItem: {
    type: Object,
    default: () => ({}),
  },
  // 数量限制：默认为0，表示不限制
  limit: {
    type: Number,
    default: Number.MAX_VALUE,
  },
  // 最小数量：默认为1
  min: {
    type: Number,
    default: 1,
  },
});
// 定义事件
const emit = defineEmits(['update:modelValue']);
const formData = useVModel(props, 'modelValue', emit);

// 处理添加
const handleAdd = () => formData.value.push(cloneDeep(props.emptyItem || {}));
// 处理删除
const handleDelete = (index: number) => formData.value.splice(index, 1);
</script>

<template>
  <el-text type="info" size="small"> 拖动左上角的小圆点可对其排序 </el-text>
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
          class="-m-2 mb-1 flex flex-row items-center justify-between p-2"
          style="background-color: var(--app-content-bg-color)"
        >
          <el-tooltip content="拖动排序">
            <IconifyIcon
              icon="ic:round-drag-indicator"
              class="drag-icon cursor-move"
              style="color: #8a909c"
            />
          </el-tooltip>
          <el-tooltip content="删除">
            <IconifyIcon
              icon="ep:delete"
              class="cursor-pointer text-red-500"
              v-if="formData.length > min"
              @click="handleDelete(index)"
            />
          </el-tooltip>
        </div>
        <!-- 内容区 -->
        <slot :element="element" :index="index"></slot>
      </div>
    </template>
  </VueDraggable>
  <el-tooltip :disabled="limit < 1" :content="`最多添加${limit}个`">
    <el-button
      type="primary"
      plain
      class="mt-1 w-full"
      :disabled="limit > 0 && formData.length >= limit"
      @click="handleAdd"
    >
      <IconifyIcon icon="ep:plus" /><span>添加</span>
    </el-button>
  </el-tooltip>
</template>

<style scoped lang="scss"></style>
