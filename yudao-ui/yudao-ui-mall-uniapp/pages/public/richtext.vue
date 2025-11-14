<!-- 文章展示 -->
<template>
  <s-layout :bgStyle="{ color: '#FFF' }" :title="state.title" class="set-wrap">
    <view class="ss-p-30 richtext"><mp-html :content="state.content"></mp-html></view>
  </s-layout>
</template>

<script setup>
  import { onLoad } from '@dcloudio/uni-app';
  import { reactive } from 'vue';
  import ArticleApi from '@/sheep/api/promotion/article';

  const state = reactive({
    title: '',
    content: '',
  });

  async function getRichTextContent(id, title) {
    const { code, data } = await ArticleApi.getArticle(id, title);
    if (code !== 0) {
      return;
    }
    state.content = data.content;
    // 标题不一致时，修改标题
    if (state.title !== data.title) {
      state.title = data.title;
      uni.setNavigationBarTitle({
        title: state.title,
      });
    }
  }

  onLoad((options) => {
    if (options.title) {
      state.title = options.title;
      uni.setNavigationBarTitle({
        title: state.title,
      });
    }
    getRichTextContent(options.id, options.title);
  });
</script>

<style lang="scss" scoped>
  .set-title {
    margin: 0 30rpx;
  }

  :deep() {
    image {
      display: block;
    }
  }
</style>
