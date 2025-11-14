<!-- 装修营销组件：营销文章 -->
<template>
  <view
    class="richtext"
    :style="[
      {
        marginLeft: styles.marginLeft + 'px',
        marginRight: styles.marginRight + 'px',
        marginBottom: styles.marginBottom + 'px',
        marginTop: styles.marginTop + 'px',
        padding: styles.padding + 'px',
      },
    ]"
  >
    <mp-html :content="state.content"></mp-html>
  </view>
</template>
<script setup>
  import { reactive, onMounted } from 'vue';
  import ArticleApi from '@/sheep/api/promotion/article';

  const props = defineProps({
    data: {
      type: Object,
      default: {},
    },
    styles: {
      type: Object,
      default() {},
    },
  });

  const state = reactive({
    content: '',
  });

  onMounted(async () => {
    const { data } = await ArticleApi.getArticle(props.data.id);
    state.content = data.content;
  });
</script>
<style lang="scss" scoped>
  .richtext {
    line-height: 0;
  }
</style>
