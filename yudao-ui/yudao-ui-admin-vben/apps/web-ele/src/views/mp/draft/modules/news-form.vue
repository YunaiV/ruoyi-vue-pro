<script lang="ts" setup>
import type { MpDraftApi } from '#/api/mp/draft';

import { computed, ref } from 'vue';

import { confirm } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';

import {
  ElAside,
  ElButton,
  ElCol,
  ElContainer,
  ElInput,
  ElMain,
  ElRow,
} from 'element-plus';

import { createEmptyNewsItem } from '#/api/mp/draft';
import { Tinymce as RichTextarea } from '#/components/tinymce';

import CoverSelect from './cover-select.vue';

defineOptions({ name: 'NewsForm' });

const props = defineProps<{
  isCreating: boolean;
  modelValue: MpDraftApi.NewsItem[] | null;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', v: MpDraftApi.NewsItem[]): void;
}>();

const newsList = computed<MpDraftApi.NewsItem[]>({
  get() {
    return props.modelValue === null
      ? [createEmptyNewsItem()]
      : props.modelValue;
  },
  set(val) {
    emit('update:modelValue', val);
  },
});

const activeNewsIndex = ref(0);
const activeNewsItem = computed(() => {
  const item = newsList.value[activeNewsIndex.value];
  if (!item) {
    return createEmptyNewsItem();
  }
  return item;
});

/** 将图文向下移动 */
function moveDownNews(index: number) {
  const current = newsList.value[index];
  const next = newsList.value[index + 1];
  if (current && next) {
    newsList.value[index] = next;
    newsList.value[index + 1] = current;
    activeNewsIndex.value = index + 1;
  }
}

/** 将图文向上移动 */
function moveUpNews(index: number) {
  const current = newsList.value[index];
  const prev = newsList.value[index - 1];
  if (current && prev) {
    newsList.value[index] = prev;
    newsList.value[index - 1] = current;
    activeNewsIndex.value = index - 1;
  }
}

/** 删除指定 index 的图文 */
async function removeNews(index: number) {
  await confirm('确定删除该图文吗?');
  newsList.value.splice(index, 1);
  if (activeNewsIndex.value === index) {
    activeNewsIndex.value = 0;
  }
}

/** 添加一个图文 */
function plusNews() {
  newsList.value.push(createEmptyNewsItem());
  activeNewsIndex.value = newsList.value.length - 1;
}
</script>

<template>
  <ElContainer>
    <ElAside width="40%">
      <div class="mx-auto mb-[10px] w-[60%] border border-[#eaeaea] p-[10px]">
        <div v-for="(news, index) in newsList" :key="index">
          <div
            class="group relative mx-auto mb-[10px] w-full cursor-pointer border-[2px] bg-white"
            v-if="index === 0"
            :class="
              activeNewsIndex === index
                ? 'border-green-500'
                : 'border-transparent'
            "
            @click="activeNewsIndex = index"
          >
            <div class="relative w-full bg-[#acadae]">
              <img
                class="max-h-[200px] min-h-[100px] w-full object-cover"
                :src="news.thumbUrl"
              />
              <div
                class="absolute bottom-0 left-0 mb-[5px] ml-[5px] inline-block h-[25px] w-[100%] overflow-hidden text-ellipsis whitespace-nowrap p-[1%] text-[18px] text-white"
              >
                {{ news.title }}
              </div>
            </div>
            <div
              class="absolute bottom-0 right-[-45px] top-0 flex flex-col justify-center gap-[10px] py-[5px] text-center"
              v-if="newsList.length > 1"
            >
              <ElButton
                type="info"
                circle
                size="small"
                @click="() => moveDownNews(index)"
              >
                <IconifyIcon icon="lucide:arrow-down" />
              </ElButton>
              <ElButton
                v-if="isCreating"
                type="danger"
                circle
                size="small"
                class="!ml-0"
                @click="() => removeNews(index)"
              >
                <IconifyIcon icon="lucide:trash-2" />
              </ElButton>
            </div>
          </div>
          <div
            class="group relative mx-auto mb-[10px] cursor-pointer border-[2px] bg-white"
            v-if="index > 0"
            :class="
              activeNewsIndex === index
                ? 'border-green-500'
                : 'border-transparent'
            "
            @click="activeNewsIndex = index"
          >
            <div class="relative flex items-center justify-between">
              <div
                class="mb-[5px] ml-[5px] h-[25px] flex-1 overflow-hidden text-ellipsis whitespace-nowrap p-[1%] text-[16px]"
              >
                {{ news.title }}
              </div>
              <img
                class="block h-[90px] w-[90px] object-cover"
                :src="news.thumbUrl"
              />
            </div>
            <div
              class="absolute bottom-0 right-[-45px] top-0 flex flex-col justify-center gap-[10px] py-[5px] text-center"
            >
              <ElButton
                v-if="newsList.length > index + 1"
                circle
                type="info"
                size="small"
                @click="() => moveDownNews(index)"
              >
                <IconifyIcon icon="lucide:arrow-down" />
              </ElButton>
              <ElButton
                v-if="index > 0"
                type="info"
                circle
                size="small"
                class="!ml-0"
                @click="() => moveUpNews(index)"
              >
                <IconifyIcon icon="lucide:arrow-up" />
              </ElButton>
              <ElButton
                v-if="isCreating"
                type="danger"
                size="small"
                circle
                class="!ml-0"
                @click="() => removeNews(index)"
              >
                <IconifyIcon icon="lucide:trash-2" />
              </ElButton>
            </div>
          </div>
        </div>
        <ElRow
          justify="center"
          class="mt-1.5 border-t border-gray-200 pt-1.5 text-center"
        >
          <ElButton
            type="primary"
            circle
            @click="plusNews"
            v-if="newsList.length < 8 && isCreating"
          >
            <IconifyIcon icon="lucide:plus" />
          </ElButton>
        </ElRow>
      </div>
    </ElAside>
    <ElMain>
      <div v-if="newsList.length > 0 && activeNewsItem">
        <!-- 标题、作者、原文地址 -->
        <ElRow :gutter="20" class="mb-5 last:mb-0">
          <ElInput
            v-model="activeNewsItem.title"
            placeholder="请输入标题（必填）"
          />
          <ElInput
            v-model="activeNewsItem.author"
            placeholder="请输入作者"
            class="mt-1.5"
          />
          <ElInput
            v-model="activeNewsItem.contentSourceUrl"
            placeholder="请输入原文地址"
            class="mt-1.5"
          />
        </ElRow>
        <!-- 封面和摘要 -->
        <ElRow :gutter="20" class="mb-5 last:mb-0">
          <ElCol :span="12">
            <CoverSelect
              v-model="activeNewsItem"
              :is-first="activeNewsIndex === 0"
            />
          </ElCol>
          <ElCol :span="12">
            <p>摘要:</p>
            <ElInput
              :rows="8"
              type="textarea"
              v-model="activeNewsItem.digest"
              placeholder="请输入摘要"
              class="inline-block w-full align-top"
              maxlength="120"
            />
          </ElCol>
        </ElRow>
        <!--富文本编辑器组件-->
        <ElRow class="mb-5 last:mb-0">
          <RichTextarea v-model="activeNewsItem.content" />
        </ElRow>
      </div>
    </ElMain>
  </ElContainer>
</template>
