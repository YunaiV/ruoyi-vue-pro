<script setup lang="ts">
import type { PropType } from 'vue';

import { computed, getCurrentInstance, inject, onMounted, ref } from 'vue';

import { IconifyIcon } from '@vben/icons';

import {
  Button,
  Dropdown,
  Empty,
  Form,
  InputNumber,
  Menu,
  message,
  Tooltip,
} from 'ant-design-vue';

import {
  createKnowledgeDocumentList,
  updateKnowledgeDocument,
} from '#/api/ai/knowledge/document';
import { splitContent } from '#/api/ai/knowledge/segment';

const props = defineProps({
  modelValue: {
    type: Object as PropType<any>,
    required: true,
  },
});

const emit = defineEmits(['update:modelValue']);
const parent = inject('parent', null); // 获取父组件实例

const modelData = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val),
}); // 表单数据

const splitLoading = ref(false); // 分段加载状态
const currentFile = ref<any>(null); // 当前选中的文件
const submitLoading = ref(false); // 提交按钮加载状态

/** 选择文件 */
async function selectFile(index: number) {
  currentFile.value = modelData.value.list[index];
  await splitContentFile(currentFile.value);
}

/** 获取文件分段内容 */
async function splitContentFile(file: any) {
  if (!file || !file.url) {
    message.warning('文件 URL 不存在');
    return;
  }

  splitLoading.value = true;
  try {
    // 调用后端分段接口，获取文档的分段内容、字符数和 Token 数
    file.segments = await splitContent(
      file.url,
      modelData.value.segmentMaxTokens,
    );
  } catch (error) {
    console.error('获取分段内容失败:', file, error);
  } finally {
    splitLoading.value = false;
  }
}
/** 处理预览分段 */
async function handleAutoSegment() {
  // 如果没有选中文件，默认选中第一个
  if (
    !currentFile.value &&
    modelData.value.list &&
    modelData.value.list.length > 0
  ) {
    currentFile.value = modelData.value.list[0];
  }
  // 如果没有选中文件，提示请先选择文件
  if (!currentFile.value) {
    message.warning('请先选择文件');
    return;
  }

  // 获取分段内容
  await splitContentFile(currentFile.value);
}

/** 上一步按钮处理 */
function handlePrevStep() {
  const parentEl = parent || getCurrentInstance()?.parent;
  if (parentEl && typeof parentEl.exposed?.goToPrevStep === 'function') {
    parentEl.exposed.goToPrevStep();
  }
}

/** 保存操作 */
async function handleSave() {
  // 保存前验证
  if (
    !currentFile?.value?.segments ||
    currentFile.value.segments.length === 0
  ) {
    message.warning('请先预览分段内容');
    return;
  }

  // 设置按钮加载状态
  submitLoading.value = true;
  try {
    if (modelData.value.id) {
      // 修改场景
      await updateKnowledgeDocument({
        id: modelData.value.id,
        segmentMaxTokens: modelData.value.segmentMaxTokens,
      });
    } else {
      // 新增场景
      const data = await createKnowledgeDocumentList({
        knowledgeId: modelData.value.knowledgeId,
        segmentMaxTokens: modelData.value.segmentMaxTokens,
        list: modelData.value.list.map((item: any) => ({
          name: item.name,
          url: item.url,
        })),
      });
      modelData.value.list.forEach((document: any, index: number) => {
        document.id = data[index];
      });
    }

    // 进入下一步
    const parentEl = parent || getCurrentInstance()?.parent;
    if (parentEl && typeof parentEl.exposed?.goToNextStep === 'function') {
      parentEl.exposed.goToNextStep();
    }
  } catch (error: any) {
    console.error('保存失败:', modelData.value, error);
  } finally {
    // 关闭按钮加载状态
    submitLoading.value = false;
  }
}

/** 初始化 */
onMounted(async () => {
  // 确保 segmentMaxTokens 存在
  if (!modelData.value.segmentMaxTokens) {
    modelData.value.segmentMaxTokens = 500;
  }
  // 如果没有选中文件，默认选中第一个
  if (
    !currentFile.value &&
    modelData.value.list &&
    modelData.value.list.length > 0
  ) {
    currentFile.value = modelData.value.list[0];
  }

  // 如果有选中的文件，获取分段内容
  if (currentFile.value) {
    await splitContentFile(currentFile.value);
  }
});
</script>

<template>
  <div>
    <!-- 上部分段设置部分 -->
    <div class="mb-5">
      <div class="mb-5 flex items-center justify-between">
        <div class="flex items-center text-base font-bold">
          分段设置
          <Tooltip placement="top">
            <template #title>
              系统会自动将文档内容分割成多个段落，您可以根据需要调整分段方式和内容。
            </template>
            <IconifyIcon
              icon="lucide:circle-alert"
              class="ml-1 text-gray-400"
            />
          </Tooltip>
        </div>
        <div>
          <Button type="primary" size="small" @click="handleAutoSegment">
            预览分段
          </Button>
        </div>
      </div>
      <div class="mb-5">
        <Form :label-col="{ span: 5 }">
          <Form.Item label="最大 Token 数">
            <InputNumber
              v-model:value="modelData.segmentMaxTokens"
              :min="1"
              :max="2048"
            />
          </Form.Item>
        </Form>
      </div>
    </div>
    <div class="mb-2.5">
      <div class="mb-2.5 text-base font-bold">分段预览</div>
      <!-- 文件选择器 -->
      <div class="mb-2.5">
        <Dropdown
          v-if="modelData.list && modelData.list.length > 0"
          trigger="click"
        >
          <div class="flex cursor-pointer items-center">
            <IconifyIcon icon="lucide:file-text" class="mr-1" />
            <span>{{ currentFile?.name || '请选择文件' }}</span>
            <span
              v-if="currentFile?.segments"
              class="ml-1 text-sm text-gray-500"
            >
              ({{ currentFile.segments.length }}个分片)
            </span>
            <IconifyIcon icon="lucide:chevron-down" class="ml-1" />
          </div>
          <template #overlay>
            <Menu>
              <Menu.Item
                v-for="(file, index) in modelData.list"
                :key="index"
                @click="selectFile(index)"
              >
                {{ file.name }}
                <span v-if="file.segments" class="ml-1 text-sm text-gray-500">
                  ({{ file.segments.length }}个分片)
                </span>
              </Menu.Item>
            </Menu>
          </template>
        </Dropdown>
        <div v-else class="text-gray-400">暂无上传文件</div>
      </div>
      <!-- 文件内容预览 -->
      <div class="max-h-[600px] overflow-y-auto rounded-md p-4">
        <div v-if="splitLoading" class="flex items-center justify-center py-5">
          <IconifyIcon icon="lucide:loader" class="is-loading" />
          <span class="ml-2.5">正在加载分段内容...</span>
        </div>
        <template
          v-else-if="
            currentFile &&
            currentFile.segments &&
            currentFile.segments.length > 0
          "
        >
          <div
            v-for="(segment, index) in currentFile.segments"
            :key="index"
            class="mb-2.5"
          >
            <div class="mb-1 text-sm text-gray-500">
              分片-{{ index + 1 }} · {{ segment.contentLength || 0 }} 字符数 ·
              {{ segment.tokens || 0 }} Token
            </div>
            <div class="bg-card rounded-md p-2">
              {{ segment.content }}
            </div>
          </div>
        </template>
        <Empty v-else description="暂无预览内容" />
      </div>
    </div>
    <!-- 添加底部按钮 -->
    <div class="mt-5 flex justify-between">
      <div>
        <Button v-if="!modelData.id" @click="handlePrevStep">上一步</Button>
      </div>
      <div>
        <Button type="primary" :loading="submitLoading" @click="handleSave">
          保存并处理
        </Button>
      </div>
    </div>
  </div>
</template>
