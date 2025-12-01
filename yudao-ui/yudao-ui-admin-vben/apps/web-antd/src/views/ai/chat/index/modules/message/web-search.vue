<script setup lang="ts">
import type { AiChatMessageApi } from '#/api/ai/chat/message';

import { ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';

defineProps<{
  webSearchPages?: AiChatMessageApi.WebSearchPage[];
}>();

const isExpanded = ref(false); // 默认收起
const selectedResult = ref<AiChatMessageApi.WebSearchPage | null>(null); // 选中的搜索结果
const iconLoadError = ref<Record<number, boolean>>({}); // 记录图标加载失败

const [Drawer, drawerApi] = useVbenDrawer({
  title: '联网搜索详情',
  closable: true,
  footer: true,
  onCancel() {
    drawerApi.close();
  },
  onConfirm() {
    if (selectedResult.value?.url) {
      window.open(selectedResult.value.url, '_blank');
    }
    drawerApi.close();
  },
});

/** 切换展开/收起 */
function toggleExpanded() {
  isExpanded.value = !isExpanded.value;
}

/** 点击搜索结果 */
function handleClick(result: AiChatMessageApi.WebSearchPage) {
  selectedResult.value = result;
  drawerApi.open();
}

/** 图标加载失败处理 */
function handleIconError(index: number) {
  iconLoadError.value[index] = true;
}
</script>

<template>
  <div v-if="webSearchPages && webSearchPages.length > 0" class="mt-2.5">
    <!-- 标题栏：可点击展开/收起 -->
    <div
      class="mb-2 flex cursor-pointer items-center justify-between text-sm text-gray-600 transition-colors hover:text-blue-500"
      @click="toggleExpanded"
    >
      <div class="flex items-center gap-1.5">
        <IconifyIcon icon="lucide:search" :size="14" />
        <span>联网搜索结果 ({{ webSearchPages.length }} 条)</span>
      </div>
      <IconifyIcon
        :icon="isExpanded ? 'lucide:chevron-up' : 'lucide:chevron-down'"
        class="text-xs transition-transform duration-200"
        :size="12"
      />
    </div>

    <!-- 可展开的搜索结果列表 -->
    <div
      v-show="isExpanded"
      class="flex flex-col gap-2 transition-all duration-200 ease-in-out"
    >
      <div
        v-for="(page, index) in webSearchPages"
        :key="index"
        class="cursor-pointer rounded-md bg-white p-2.5 transition-all hover:bg-blue-50"
        @click="handleClick(page)"
      >
        <div class="flex items-start gap-2">
          <!-- 网站图标 -->
          <div class="mt-0.5 h-4 w-4 flex-shrink-0">
            <img
              v-if="page.icon && !iconLoadError[index]"
              :src="page.icon"
              :alt="page.name"
              class="h-full w-full rounded-sm object-contain"
              @error="handleIconError(index)"
            />
            <IconifyIcon
              v-else
              icon="lucide:link"
              class="h-full w-full text-gray-600"
            />
          </div>
          <!-- 内容区域 -->
          <div class="min-w-0 flex-1">
            <!-- 网站名称 -->
            <div class="mb-1 truncate text-xs text-gray-400">
              {{ page.name }}
            </div>
            <!-- 主标题 -->
            <div
              class="mb-1 line-clamp-2 text-sm font-medium leading-snug text-blue-600"
            >
              {{ page.title }}
            </div>
            <!-- 描述 -->
            <div class="mb-1 line-clamp-2 text-xs leading-snug text-gray-600">
              {{ page.snippet }}
            </div>
            <!-- URL -->
            <div class="truncate text-xs text-green-700">
              {{ page.url }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 联网搜索详情 Drawer -->
    <Drawer class="w-[600px]" cancel-text="关闭" confirm-text="访问原文">
      <div v-if="selectedResult">
        <!-- 标题区域 -->
        <div class="mb-4 flex items-start gap-3">
          <div class="mt-0.5 h-6 w-6 flex-shrink-0">
            <img
              v-if="selectedResult.icon"
              :src="selectedResult.icon"
              :alt="selectedResult.name"
              class="h-full w-full rounded-sm object-contain"
            />
            <IconifyIcon
              v-else
              icon="lucide:link"
              class="h-full w-full text-gray-600"
            />
          </div>
          <div class="min-w-0 flex-1">
            <div class="mb-2 text-lg font-bold text-gray-900">
              {{ selectedResult.title }}
            </div>
            <div class="mb-1 text-sm text-gray-500">
              {{ selectedResult.name }}
            </div>
            <div class="break-all text-sm text-green-700">
              {{ selectedResult.url }}
            </div>
          </div>
        </div>
        <!-- 内容区域 -->
        <div class="space-y-4">
          <!-- 简短描述 -->
          <div>
            <div class="mb-2 text-sm font-semibold text-gray-900">简短描述</div>
            <div
              class="rounded-lg bg-gray-50 p-3 text-sm leading-relaxed text-gray-700"
            >
              {{ selectedResult.snippet }}
            </div>
          </div>
          <!-- 内容摘要 -->
          <div v-if="selectedResult.summary">
            <div class="mb-2 text-sm font-semibold text-gray-900">内容摘要</div>
            <div
              class="max-h-[50vh] overflow-y-auto whitespace-pre-wrap rounded-lg bg-gray-50 p-3 text-sm leading-relaxed text-gray-900"
            >
              {{ selectedResult.summary }}
            </div>
          </div>
        </div>
      </div>
    </Drawer>
  </div>
</template>
