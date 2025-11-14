<script setup lang="ts">
import { computed, ref } from 'vue';

import { IconifyIcon } from '@vben/icons';

import { Tooltip } from 'ant-design-vue';

const props = defineProps<{
  segments: {
    content: string;
    documentId: number;
    documentName: string;
    id: number;
  }[];
}>();

const document = ref<null | {
  id: number;
  segments: {
    content: string;
    id: number;
  }[];
  title: string;
}>(null); // 知识库文档列表
const dialogVisible = ref(false); // 知识引用详情弹窗
const documentRef = ref<HTMLElement>(); // 知识引用详情弹窗 Ref

/** 按照 document 聚合 segments */
const documentList = computed(() => {
  if (!props.segments) return [];

  const docMap = new Map();
  props.segments.forEach((segment) => {
    if (!docMap.has(segment.documentId)) {
      docMap.set(segment.documentId, {
        id: segment.documentId,
        title: segment.documentName,
        segments: [],
      });
    }
    docMap.get(segment.documentId).segments.push({
      id: segment.id,
      content: segment.content,
    });
  });
  return [...docMap.values()];
});

/** 点击 document 处理 */
function handleClick(doc: any) {
  document.value = doc;
  dialogVisible.value = true;
}
</script>

<template>
  <!-- 知识引用列表 -->
  <div
    v-if="segments && segments.length > 0"
    class="mt-2 rounded-lg bg-gray-50 p-2"
  >
    <div class="mb-2 flex items-center text-sm text-gray-400">
      <IconifyIcon icon="lucide:file-text" class="mr-1" /> 知识引用
    </div>
    <div class="flex flex-wrap gap-2">
      <div
        v-for="(doc, index) in documentList"
        :key="index"
        class="bg-card cursor-pointer rounded-lg p-2 px-3 transition-all hover:bg-blue-50"
        @click="handleClick(doc)"
      >
        <div class="mb-1 text-sm text-gray-600">
          {{ doc.title }}
          <span class="ml-1 text-xs text-gray-300">
            （{{ doc.segments.length }} 条）
          </span>
        </div>
      </div>
    </div>
  </div>
  <Tooltip placement="topLeft" trigger="click">
    <div ref="documentRef"></div>
    <template #title>
      <div class="mb-3 text-base font-bold">{{ document?.title }}</div>
      <div class="max-h-[60vh] overflow-y-auto">
        <div
          v-for="(segment, index) in document?.segments"
          :key="index"
          class="border-b-solid border-b-gray-200 p-3 last:border-b-0"
        >
          <div
            class="mb-2 block w-fit rounded-sm px-2 py-1 text-xs text-gray-400"
          >
            分段 {{ segment.id }}
          </div>
          <div class="mt-2 text-sm leading-[1.6] text-gray-600">
            {{ segment.content }}
          </div>
        </div>
      </div>
    </template>
  </Tooltip>
</template>
