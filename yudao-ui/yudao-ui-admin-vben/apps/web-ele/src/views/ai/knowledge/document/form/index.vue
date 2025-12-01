<script setup lang="ts">
import {
  getCurrentInstance,
  onBeforeUnmount,
  onMounted,
  provide,
  ref,
} from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { Page } from '@vben/common-ui';
import { useTabs } from '@vben/hooks';
import { IconifyIcon } from '@vben/icons';

import { ElCard } from 'element-plus';

import { getKnowledgeDocument } from '#/api/ai/knowledge/document';

import ProcessStep from './modules/process-step.vue';
import SplitStep from './modules/split-step.vue';
import UploadStep from './modules/upload-step.vue';

const route = useRoute();
const router = useRouter();

const uploadDocumentRef = ref();
const documentSegmentRef = ref();
const processCompleteRef = ref();
const currentStep = ref(0); // 步骤控制
const steps = [
  { title: '上传文档' },
  { title: '文档分段' },
  { title: '处理并完成' },
];
const formData = ref({
  knowledgeId: undefined, // 知识库编号
  id: undefined, // 编辑的文档编号(documentId)
  segmentMaxTokens: 500, // 分段最大 token 数
  list: [] as Array<{
    count?: number; // 段落数量
    id: number; // 文档编号
    name: string; // 文档名称
    process?: number; // 处理进度
    segments: Array<{
      content?: string;
      contentLength?: number;
      tokens?: number;
    }>;
    url: string; // 文档 URL
  }>, // 用于存储上传的文件列表
}); // 表单数据

provide('parent', getCurrentInstance()); // 提供 parent 给子组件使用

const tabs = useTabs();

/** 返回列表页 */
function handleBack() {
  // 关闭当前页签
  tabs.closeCurrentTab();
  // 跳转到列表页
  router.push({
    name: 'AiKnowledgeDocument',
    query: {
      knowledgeId: route.query.knowledgeId,
    },
  });
}

/** 初始化数据 */
async function initData() {
  if (route.query.knowledgeId) {
    formData.value.knowledgeId = route.query.knowledgeId as any;
  }
  // 【修改场景】从路由参数中获取文档 ID
  const documentId = route.query.id;
  if (documentId) {
    // 获取文档信息
    formData.value.id = documentId as any;
    const document = await getKnowledgeDocument(documentId as any);
    formData.value.segmentMaxTokens = document.segmentMaxTokens;
    formData.value.list = [
      {
        id: document.id,
        name: document.name,
        url: document.url,
        segments: [],
      },
    ];
    // 进入下一步
    goToNextStep();
  }
}

/** 切换到下一步 */
function goToNextStep() {
  if (currentStep.value < steps.length - 1) {
    currentStep.value++;
  }
}

/** 切换到上一步 */
function goToPrevStep() {
  if (currentStep.value > 0) {
    currentStep.value--;
  }
}

/** 添加组件卸载前的清理代码 */
onBeforeUnmount(() => {
  // 清理所有的引用
  uploadDocumentRef.value = null;
  documentSegmentRef.value = null;
  processCompleteRef.value = null;
});

/** 暴露方法给子组件使用 */
defineExpose({
  goToNextStep,
  goToPrevStep,
  handleBack,
});

/** 初始化 */
onMounted(async () => {
  await initData();
});
</script>

<template>
  <Page auto-content-height>
    <div class="mx-auto">
      <!-- 头部导航栏 -->
      <div
        class="absolute left-0 right-0 top-0 z-10 flex h-12 items-center border-b bg-card px-4"
      >
        <!-- 左侧标题 -->
        <div class="flex w-48 items-center overflow-hidden">
          <IconifyIcon
            icon="lucide:arrow-left"
            class="size-5 flex-shrink-0 cursor-pointer"
            @click="handleBack"
          />
          <span class="ml-2.5 truncate text-base">
            {{ formData.id ? '编辑知识库文档' : '创建知识库文档' }}
          </span>
        </div>

        <!-- 步骤条 -->
        <div class="flex h-full flex-1 items-center justify-center">
          <div class="flex h-full w-96 items-center justify-between">
            <div
              v-for="(step, index) in steps"
              :key="index"
              class="relative mx-4 flex h-full cursor-pointer items-center"
              :class="[
                currentStep === index
                  ? 'border-b-2 border-solid border-blue-500 text-blue-500'
                  : 'text-gray-500',
              ]"
            >
              <div
                class="mr-2 flex h-7 w-7 items-center justify-center rounded-full border-2 border-solid text-base"
                :class="[
                  currentStep === index
                    ? 'border-blue-500 bg-blue-500 text-white'
                    : 'border-gray-300 bg-white text-gray-500',
                ]"
              >
                {{ index + 1 }}
              </div>
              <span class="whitespace-nowrap text-base font-bold">
                {{ step.title }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 主体内容 -->
      <ElCard :body-style="{ padding: '10px' }" class="mb-4">
        <div class="mt-12">
          <!-- 第一步：上传文档 -->
          <div v-if="currentStep === 0" class="mx-auto w-[560px]">
            <UploadStep v-model="formData" ref="uploadDocumentRef" />
          </div>
          <!-- 第二步：文档分段 -->
          <div v-if="currentStep === 1" class="mx-auto w-[560px]">
            <SplitStep v-model="formData" ref="documentSegmentRef" />
          </div>

          <!-- 第三步：处理并完成 -->
          <div v-if="currentStep === 2" class="mx-auto w-[560px]">
            <ProcessStep v-model="formData" ref="processCompleteRef" />
          </div>
        </div>
      </ElCard>
    </div>
  </Page>
</template>
