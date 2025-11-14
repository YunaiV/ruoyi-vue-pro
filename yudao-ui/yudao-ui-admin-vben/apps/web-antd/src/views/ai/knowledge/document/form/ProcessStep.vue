<script setup lang="ts">
import { computed, inject, onBeforeUnmount, onMounted, ref } from 'vue';

import { IconifyIcon } from '@vben/icons';

import { Button, Progress } from 'ant-design-vue';

import { getKnowledgeSegmentProcessList } from '#/api/ai/knowledge/segment';

const props = defineProps({
  modelValue: {
    type: Object,
    required: true,
  },
});

const emit = defineEmits(['update:modelValue']);
const parent = inject('parent') as any;
const pollingTimer = ref<null | number>(null); // 轮询定时器 ID，用于跟踪和清除轮询进程

/** 判断文件处理是否完成 */
function isProcessComplete(file: any) {
  return file.progress === 100;
}

/** 判断所有文件是否都处理完成 */
const allProcessComplete = computed(() => {
  return props.modelValue.list.every((file: any) => isProcessComplete(file));
});

/** 完成按钮点击事件处理 */
function handleComplete() {
  if (parent?.exposed?.handleBack) {
    parent.exposed.handleBack();
  }
}

/** 获取文件处理进度 */
async function getProcessList() {
  try {
    // 1. 调用 API 获取处理进度
    const documentIds = props.modelValue.list
      .filter((item: any) => item.id)
      .map((item: any) => item.id);
    if (documentIds.length === 0) {
      return;
    }
    const result = await getKnowledgeSegmentProcessList(documentIds);

    // 2.1更新进度
    const updatedList = props.modelValue.list.map((file: any) => {
      const processInfo = result.find(
        (item: any) => item.documentId === file.id,
      );
      if (processInfo) {
        // 计算进度百分比：已嵌入数量 / 总数量 * 100
        const progress =
          processInfo.embeddingCount && processInfo.count
            ? Math.floor((processInfo.embeddingCount / processInfo.count) * 100)
            : 0;
        return {
          ...file,
          progress,
          count: processInfo.count || 0,
        };
      }
      return file;
    });

    // 2.2 更新数据
    emit('update:modelValue', {
      ...props.modelValue,
      list: updatedList,
    });

    // 3. 如果未完成，继续轮询
    if (!updatedList.every((file: any) => isProcessComplete(file))) {
      pollingTimer.value = window.setTimeout(getProcessList, 3000);
    }
  } catch (error) {
    // 出错后也继续轮询
    console.error('获取处理进度失败:', error);
    pollingTimer.value = window.setTimeout(getProcessList, 5000);
  }
}

/** 组件挂载时开始轮询 */
onMounted(() => {
  // 1. 初始化进度为 0
  const initialList = props.modelValue.list.map((file: any) => ({
    ...file,
    progress: 0,
  }));

  emit('update:modelValue', {
    ...props.modelValue,
    list: initialList,
  });

  // 2. 开始轮询获取进度
  getProcessList();
});

/** 组件卸载前清除轮询 */
onBeforeUnmount(() => {
  // 1. 清除定时器
  if (pollingTimer.value) {
    clearTimeout(pollingTimer.value);
    pollingTimer.value = null;
  }
});
</script>

<template>
  <div>
    <!-- 文件处理列表 -->
    <div class="mt-4 grid grid-cols-1 gap-2">
      <div
        v-for="(file, index) in modelValue.list"
        :key="index"
        class="flex items-center rounded-sm border-l-4 border-l-blue-500 px-3 py-1 shadow-sm transition-all duration-300 hover:bg-blue-50"
      >
        <!-- 文件图标和名称 -->
        <div class="mr-2 flex min-w-48 items-center">
          <IconifyIcon icon="lucide:file-text" class="mr-2 text-blue-500" />
          <span class="break-all text-sm text-gray-600">
            {{ file.name }}
          </span>
        </div>

        <!-- 处理进度 -->
        <div class="flex-1">
          <Progress
            :percent="file.progress || 0"
            :size="10"
            :status="isProcessComplete(file) ? 'success' : 'active'"
          />
        </div>

        <!-- 分段数量 -->
        <div class="ml-2 text-sm text-gray-400">
          分段数量：{{ file.count ? file.count : '-' }}
        </div>
      </div>
    </div>

    <!-- 底部完成按钮 -->
    <div class="mt-5 flex justify-end">
      <Button
        :type="allProcessComplete ? 'primary' : 'default'"
        :disabled="!allProcessComplete"
        @click="handleComplete"
      >
        完成
      </Button>
    </div>
  </div>
</template>
