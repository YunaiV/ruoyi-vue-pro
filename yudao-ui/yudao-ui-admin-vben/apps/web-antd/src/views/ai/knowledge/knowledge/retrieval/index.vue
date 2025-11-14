<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { Page } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';

import {
  Button,
  Card,
  Empty,
  InputNumber,
  message,
  Textarea,
} from 'ant-design-vue';

import { getKnowledge } from '#/api/ai/knowledge/knowledge';
import { searchKnowledgeSegment } from '#/api/ai/knowledge/segment';

/** 文档召回测试 */
defineOptions({ name: 'KnowledgeDocumentRetrieval' });

const route = useRoute(); // 路由
const router = useRouter(); // 路由

const loading = ref(false); // 加载状态
const segments = ref<any[]>([]); // 召回结果
const queryParams = reactive({
  id: undefined,
  content: '',
  topK: 10,
  similarityThreshold: 0.5,
});

/** 调用文档召回测试接口 */
async function getRetrievalResult() {
  if (!queryParams.content) {
    message.warning('请输入查询文本');
    return;
  }

  loading.value = true;
  segments.value = [];

  try {
    const data = await searchKnowledgeSegment({
      knowledgeId: queryParams.id,
      content: queryParams.content,
      topK: queryParams.topK,
      similarityThreshold: queryParams.similarityThreshold,
    });
    segments.value = data || [];
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
}

/** 展开/收起段落内容 */
function toggleExpand(segment: any) {
  segment.expanded = !segment.expanded;
}

/** 获取知识库信息 */
async function getKnowledgeInfo(id: number) {
  try {
    const knowledge = await getKnowledge(id);
    if (knowledge) {
      queryParams.topK = knowledge.topK || queryParams.topK;
      queryParams.similarityThreshold =
        knowledge.similarityThreshold || queryParams.similarityThreshold;
    }
  } catch {}
}

/** 初始化 */
onMounted(() => {
  // 如果知识库 ID 不存在，显示错误提示并关闭页面
  if (!route.query.id) {
    message.error('知识库 ID 不存在，无法进行召回测试');
    router.back();
    return;
  }
  queryParams.id = route.query.id as any;

  // 获取知识库信息并设置默认值
  getKnowledgeInfo(queryParams.id as any);
});
</script>
<template>
  <Page auto-content-height>
    <div class="flex w-full gap-4">
      <Card class="w-3/4 flex-1">
        <div class="mb-15">
          <h3 class="m-2 text-lg font-semibold leading-none tracking-tight">
            召回测试
          </h3>
          <div class="m-2 text-sm text-gray-500">
            根据给定的查询文本测试召回效果。
          </div>
        </div>
        <div>
          <div class="relative m-2">
            <Textarea
              v-model:value="queryParams.content"
              :rows="8"
              placeholder="请输入文本"
            />
            <div class="absolute bottom-2 right-2 text-sm text-gray-400">
              {{ queryParams.content?.length }} / 200
            </div>
          </div>
          <div class="m-2 flex items-center">
            <span class="w-16 text-gray-500">topK:</span>
            <InputNumber
              v-model:value="queryParams.topK"
              :min="1"
              :max="20"
              class="w-full"
            />
          </div>
          <div class="m-2 flex items-center">
            <span class="w-16 text-gray-500">相似度:</span>
            <InputNumber
              v-model:value="queryParams.similarityThreshold"
              class="w-full"
              :min="0"
              :max="1"
              :precision="2"
              :step="0.01"
            />
          </div>
          <div class="flex justify-end">
            <Button
              type="primary"
              @click="getRetrievalResult"
              :loading="loading"
            >
              测试
            </Button>
          </div>
        </div>
      </Card>
      <Card class="min-w-300 flex-1">
        <!-- 加载中状态 -->
        <template v-if="loading">
          <div class="flex h-72 items-center justify-center">
            <Empty description="正在检索中..." />
          </div>
        </template>

        <!-- 有段落 -->
        <template v-else-if="segments.length > 0">
          <div class="mb-15 font-bold">{{ segments.length }} 个召回段落</div>
          <div>
            <div
              v-for="(segment, index) in segments"
              :key="index"
              class="p-15 mb-20 rounded border border-solid border-gray-200"
            >
              <div class="mb-5 flex justify-between text-sm text-gray-500">
                <span>
                  分段({{ segment.id }}) · {{ segment.contentLength }} 字符数 ·
                  {{ segment.tokens }} Token
                </span>
                <span
                  class="rounded-full bg-blue-50 py-4 text-sm font-bold text-blue-500"
                >
                  score: {{ segment.score }}
                </span>
              </div>
              <div
                class="mb-10 overflow-hidden whitespace-pre-wrap rounded bg-gray-50 p-10 text-sm transition-all duration-100"
                :class="{
                  'max-h-50 line-clamp-2': !segment.expanded,
                  'max-h-[1500px]': segment.expanded,
                }"
              >
                {{ segment.content }}
              </div>
              <div class="flex items-center justify-between">
                <div class="flex items-center text-sm text-gray-500">
                  <IconifyIcon icon="lucide:file-text" class="mr-5" />
                  <span>{{ segment.documentName || '未知文档' }}</span>
                </div>
                <Button size="small" @click="toggleExpand(segment)">
                  {{ segment.expanded ? '收起' : '展开' }}
                  <span
                    class="mr-5"
                    :class="
                      segment.expanded
                        ? 'lucide:chevron-up'
                        : 'lucide:chevron-down'
                    "
                  ></span>
                </Button>
              </div>
            </div>
          </div>
        </template>

        <!-- 无召回结果 -->
        <template v-else>
          <div class="flex h-72 items-center justify-center">
            <Empty description="暂无召回结果" />
          </div>
        </template>
      </Card>
    </div>
  </Page>
</template>
