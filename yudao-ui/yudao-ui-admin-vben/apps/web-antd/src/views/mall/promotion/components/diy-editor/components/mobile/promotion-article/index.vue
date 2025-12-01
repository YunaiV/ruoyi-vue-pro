<script setup lang="ts">
import type { PromotionArticleProperty } from './config';

import type { MallArticleApi } from '#/api/mall/promotion/article';

import { ref, watch } from 'vue';

import { getArticle } from '#/api/mall/promotion/article';

/** 营销文章 */
defineOptions({ name: 'PromotionArticle' });

const props = defineProps<{ property: PromotionArticleProperty }>(); // 定义属性

const article = ref<MallArticleApi.Article>(); // 商品列表

watch(
  () => props.property.id,
  async () => {
    if (props.property.id) {
      article.value = await getArticle(props.property.id);
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
