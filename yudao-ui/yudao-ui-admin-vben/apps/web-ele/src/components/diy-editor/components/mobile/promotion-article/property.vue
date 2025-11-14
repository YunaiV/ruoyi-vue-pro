<script setup lang="ts">
import type { PromotionArticleProperty } from './config';

import type { MallArticleApi } from '#/api/mall/promotion/article';

import { onMounted, ref } from 'vue';

import { useVModel } from '@vueuse/core';
import { ElForm, ElFormItem, ElOption, ElSelect } from 'element-plus';

import * as ArticleApi from '#/api/mall/promotion/article/index';
import ComponentContainerProperty from '#/components/diy-editor/components/component-container-property.vue';

// 营销文章属性面板
defineOptions({ name: 'PromotionArticleProperty' });

const props = defineProps<{ modelValue: PromotionArticleProperty }>();
const emit = defineEmits(['update:modelValue']);
const formData = useVModel(props, 'modelValue', emit);
// 文章列表
const articles = ref<MallArticleApi.Article[]>([]);

// 加载中
const loading = ref(false);
// 查询文章列表
const queryArticleList = async (title?: string) => {
  loading.value = true;
  const { list } = await ArticleApi.getArticlePage({
    title,
    pageNo: 1,
    pageSize: 10,
  });
  articles.value = list;
  loading.value = false;
};

// 初始化
onMounted(() => {
  queryArticleList();
});
</script>

<template>
  <ComponentContainerProperty v-model="formData.style">
    <ElForm label-width="40px" :model="formData">
      <ElFormItem label="文章" prop="id">
        <ElSelect
          v-model="formData.id"
          placeholder="请选择文章"
          class="w-full"
          filterable
          remote
          :remote-method="queryArticleList"
          :loading="loading"
        >
          <ElOption
            v-for="article in articles"
            :key="article.id"
            :label="article.title"
            :value="article.id"
          />
        </ElSelect>
      </ElFormItem>
    </ElForm>
  </ComponentContainerProperty>
</template>

<style scoped lang="scss"></style>
