<script setup lang="ts">
import { computed } from 'vue';

import { IconifyIcon } from '@vben/icons';
import { getFileIcon, getFileNameFromUrl, getFileTypeClass } from '@vben/utils';

const props = defineProps<{
  attachmentUrls?: string[];
}>();

/** 过滤掉空值的附件列表 */
const validAttachmentUrls = computed(() => {
  return (props.attachmentUrls || []).filter((url) => url && url.trim());
});

/** 点击文件 */
function handleFileClick(url: string) {
  window.open(url, '_blank');
}
</script>

<template>
  <div v-if="validAttachmentUrls.length > 0" class="mt-2">
    <div class="flex flex-wrap gap-2">
      <div
        v-for="(url, index) in validAttachmentUrls"
        :key="index"
        class="max-w-70 flex min-w-40 cursor-pointer items-center rounded-lg border border-transparent bg-gray-100 p-3 transition-all duration-200 hover:-translate-y-1 hover:bg-gray-200 hover:shadow-lg"
        @click="handleFileClick(url)"
      >
        <div class="mr-3 flex-shrink-0">
          <div
            class="flex h-8 w-8 items-center justify-center rounded-md bg-gradient-to-br font-bold text-white"
            :class="getFileTypeClass(getFileNameFromUrl(url))"
          >
            <IconifyIcon
              :icon="getFileIcon(getFileNameFromUrl(url))"
              :size="20"
            />
          </div>
        </div>
        <div class="min-w-0 flex-1">
          <div
            class="mb-1 overflow-hidden text-ellipsis whitespace-nowrap text-sm font-medium leading-tight text-gray-800"
            :title="getFileNameFromUrl(url)"
          >
            {{ getFileNameFromUrl(url) }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
