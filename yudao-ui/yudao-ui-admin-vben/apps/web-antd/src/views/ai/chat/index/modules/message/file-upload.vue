<script setup lang="ts">
import { computed, onUnmounted, ref, watch } from 'vue';

import { IconifyIcon } from '@vben/icons';
import { formatFileSize, getFileIcon } from '@vben/utils';

import { message } from 'ant-design-vue';

import { useUpload } from '#/components/upload/use-upload';

export interface FileItem {
  name: string;
  size: number;
  url?: string;
  uploading?: boolean;
  progress?: number;
  raw?: File;
}

const props = withDefaults(
  defineProps<{
    acceptTypes?: string;
    disabled?: boolean;
    limit?: number;
    maxSize?: number;
    modelValue?: string[];
  }>(),
  {
    modelValue: () => [],
    limit: 5,
    maxSize: 10,
    acceptTypes:
      '.jpg,.jpeg,.png,.gif,.webp,.pdf,.doc,.docx,.txt,.xls,.xlsx,.ppt,.pptx,.csv,.md',
    disabled: false,
  },
);

const emit = defineEmits<{
  'update:modelValue': [value: string[]];
  uploadError: [error: any];
  uploadSuccess: [file: FileItem];
}>();

const fileInputRef = ref<HTMLInputElement>();
const fileList = ref<FileItem[]>([]);
const uploadedUrls = ref<string[]>([]);
const showTooltip = ref(false);
const hideTimer = ref<NodeJS.Timeout | null>(null);
const { httpRequest } = useUpload();

/** 监听 v-model 变化 */
watch(
  () => props.modelValue,
  (newVal) => {
    uploadedUrls.value = [...newVal];
    if (newVal.length === 0) {
      fileList.value = [];
    }
  },
  { immediate: true, deep: true },
);

/** 是否有文件 */
const hasFiles = computed(() => fileList.value.length > 0);

/** 是否达到上传限制 */
const isLimitReached = computed(() => fileList.value.length >= props.limit);

/** 触发文件选择 */
function triggerFileInput() {
  fileInputRef.value?.click();
}

/** 显示 tooltip */
function showTooltipHandler() {
  if (hideTimer.value) {
    clearTimeout(hideTimer.value);
    hideTimer.value = null;
  }
  showTooltip.value = true;
}

/** 隐藏 tooltip */
function hideTooltipHandler() {
  hideTimer.value = setTimeout(() => {
    showTooltip.value = false;
    hideTimer.value = null;
  }, 300);
}

/** 处理文件选择 */
async function handleFileSelect(event: Event) {
  const target = event.target as HTMLInputElement;
  const files = [...(target.files || [])];
  if (files.length === 0) {
    return;
  }

  if (files.length + fileList.value.length > props.limit) {
    message.error(`最多只能上传 ${props.limit} 个文件`);
    target.value = '';
    return;
  }

  for (const file of files) {
    if (file.size > props.maxSize * 1024 * 1024) {
      message.error(`文件 ${file.name} 大小超过 ${props.maxSize}MB`);
      continue;
    }

    const fileItem: FileItem = {
      name: file.name,
      size: file.size,
      uploading: true,
      progress: 0,
      raw: file,
    };
    fileList.value.push(fileItem);
    await uploadFile(fileItem);
  }

  target.value = '';
}

/** 上传文件 */
async function uploadFile(fileItem: FileItem) {
  try {
    const progressInterval = setInterval(() => {
      if (fileItem.progress! < 90) {
        fileItem.progress = (fileItem.progress || 0) + Math.random() * 10;
      }
    }, 100);

    const response = await httpRequest(fileItem.raw!);
    clearInterval(progressInterval);

    fileItem.uploading = false;
    fileItem.progress = 100;

    // 调试日志
    console.warn('上传响应:', response);

    // 兼容不同的返回格式：{ url: '...' } 或 { data: '...' } 或直接是字符串
    const fileUrl =
      (response as any)?.url || (response as any)?.data || response;
    fileItem.url = fileUrl;

    console.warn('提取的文件 URL:', fileUrl);

    // 只有当 URL 有效时才添加到列表
    if (fileUrl && typeof fileUrl === 'string') {
      uploadedUrls.value.push(fileUrl);
      emit('uploadSuccess', fileItem);
      updateModelValue();
    } else {
      throw new Error('上传返回的 URL 无效');
    }
  } catch (error) {
    fileItem.uploading = false;
    message.error(`文件 ${fileItem.name} 上传失败`);
    emit('uploadError', error);

    const index = fileList.value.indexOf(fileItem);
    if (index !== -1) {
      removeFile(index);
    }
  }
}

/** 删除文件 */
function removeFile(index: number) {
  const removedFile = fileList.value[index];
  fileList.value.splice(index, 1);
  if (removedFile?.url) {
    const urlIndex = uploadedUrls.value.indexOf(removedFile.url);
    if (urlIndex !== -1) {
      uploadedUrls.value.splice(urlIndex, 1);
    }
  }
  updateModelValue();
}

/** 更新 v-model */
function updateModelValue() {
  emit('update:modelValue', [...uploadedUrls.value]);
}

/** 清空文件 */
function clearFiles() {
  fileList.value = [];
  uploadedUrls.value = [];
  updateModelValue();
}

defineExpose({
  triggerFileInput,
  clearFiles,
});

onUnmounted(() => {
  if (hideTimer.value) {
    clearTimeout(hideTimer.value);
  }
});
</script>

<template>
  <div
    v-if="!disabled"
    class="relative inline-block"
    @mouseenter="showTooltipHandler"
    @mouseleave="hideTooltipHandler"
  >
    <!-- 文件上传按钮 -->
    <button
      type="button"
      class="relative flex h-8 w-8 items-center justify-center rounded-full border-0 bg-transparent text-gray-600 transition-all duration-200 hover:bg-gray-100"
      :class="{ 'text-blue-500 hover:bg-blue-50': hasFiles }"
      :disabled="isLimitReached"
      @click="triggerFileInput"
    >
      <IconifyIcon icon="lucide:paperclip" :size="16" />
      <!-- 文件数量徽章 -->
      <span
        v-if="hasFiles"
        class="absolute -right-1 -top-1 flex h-4 min-w-4 items-center justify-center rounded-full bg-red-500 px-1 text-[10px] font-medium leading-none text-white"
      >
        {{ fileList.length }}
      </span>
    </button>

    <!-- 隐藏的文件输入框 -->
    <input
      ref="fileInputRef"
      type="file"
      multiple
      style="display: none"
      :accept="acceptTypes"
      @change="handleFileSelect"
    />

    <!-- Hover 显示的文件列表 -->
    <div
      v-if="hasFiles && showTooltip"
      class="absolute bottom-[calc(100%+8px)] left-1/2 z-[1000] min-w-[240px] max-w-[320px] -translate-x-1/2 rounded-lg border border-gray-200 bg-white p-2 shadow-lg duration-200 animate-in fade-in slide-in-from-bottom-1"
      @mouseenter="showTooltipHandler"
      @mouseleave="hideTooltipHandler"
    >
      <!-- Tooltip 箭头 -->
      <div
        class="absolute -bottom-[5px] left-1/2 h-0 w-0 -translate-x-1/2 border-l-[5px] border-r-[5px] border-t-[5px] border-l-transparent border-r-transparent border-t-gray-200"
      >
        <div
          class="absolute bottom-[1px] left-1/2 h-0 w-0 -translate-x-1/2 border-l-[4px] border-r-[4px] border-t-[4px] border-l-transparent border-r-transparent border-t-white"
        ></div>
      </div>
      <!-- 文件列表 -->
      <div
        class="scrollbar-thin scrollbar-track-transparent scrollbar-thumb-gray-300 hover:scrollbar-thumb-gray-400 scrollbar-thumb-rounded-sm max-h-[200px] overflow-y-auto"
      >
        <div
          v-for="(file, index) in fileList"
          :key="index"
          class="mb-1 flex items-center justify-between rounded-md bg-gray-50 p-2 text-xs transition-all duration-200 last:mb-0 hover:bg-gray-100"
          :class="{ 'opacity-70': file.uploading }"
        >
          <div class="flex min-w-0 flex-1 items-center">
            <IconifyIcon
              :icon="getFileIcon(file.name)"
              class="mr-2 flex-shrink-0 text-blue-500"
            />
            <span
              class="mr-1 flex-1 overflow-hidden text-ellipsis whitespace-nowrap font-medium text-gray-900"
            >
              {{ file.name }}
            </span>
            <span class="flex-shrink-0 text-[11px] text-gray-500">
              ({{ formatFileSize(file.size) }})
            </span>
          </div>
          <div class="ml-2 flex flex-shrink-0 items-center gap-1">
            <div
              v-if="file.uploading"
              class="h-1 w-[60px] overflow-hidden rounded-full bg-gray-200"
            >
              <div
                class="h-full bg-blue-500 transition-all duration-300"
                :style="{ width: `${file.progress || 0}%` }"
              ></div>
            </div>
            <button
              v-else-if="!disabled"
              type="button"
              class="flex h-5 w-5 items-center justify-center rounded text-red-500 hover:bg-red-50"
              @click="removeFile(index)"
            >
              <IconifyIcon icon="lucide:x" :size="12" />
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
