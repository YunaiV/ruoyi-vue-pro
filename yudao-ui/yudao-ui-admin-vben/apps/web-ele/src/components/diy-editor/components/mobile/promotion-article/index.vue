<script setup lang="ts">
import type { PromotionArticleProperty } from './config';

import type { MallArticleApi } from '#/api/mall/promotion/article';

import { ref, watch } from 'vue';

import * as ArticleApi from '#/api/mall/promotion/article/index';

/** 营销文章 */
defineOptions({ name: 'PromotionArticle' });
// 定义属性
const props = defineProps<{ property: PromotionArticleProperty }>();
// 商品列表
const article = ref<MallArticleApi.Article>();

watch(
  () => props.property.id,
  async () => {
    if (props.property.id) {
      article.value = await ArticleApi.getArticle(props.property.id);
    }
  },
  {
    immediate: true,
  },
);
</script>
<template>
  <div class="min-h-[30px]" v-dompurify-html="article?.content"></div>
</template>

<style scoped lang="scss"></style>
