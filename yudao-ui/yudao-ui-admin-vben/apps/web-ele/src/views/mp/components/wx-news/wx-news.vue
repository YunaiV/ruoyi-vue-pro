<script lang="ts" setup>
/** 微信消息 - 图文 */
defineOptions({ name: 'WxNews' });

const props = withDefaults(
  defineProps<{
    articles?: any[] | null;
  }>(),
  {
    articles: null,
  },
);

defineExpose({
  articles: props.articles,
});
</script>

<template>
  <div class="mx-auto flex w-full flex-col gap-[10px] bg-white">
    <div v-for="(article, index) in articles" :key="index">
      <!-- 头条 -->
      <a v-if="index === 0" :href="article.url" target="_blank">
        <div class="mx-auto w-full">
          <div class="relative w-full bg-[#acadae]">
            <img
              :src="article.picUrl"
              :preview="false"
              class="w-[100px] object-cover"
            />
            <div
              class="absolute bottom-0 left-0 ml-[10px] inline-block w-[98%] whitespace-normal p-[1%] text-base text-white"
            >
              <span>{{ article.title }}</span>
            </div>
          </div>
        </div>
      </a>
      <!-- 二条/三条等等 -->
      <a v-else :href="article.url" target="_blank">
        <div class="bg-white">
          <div class="relative box-border p-[10px]">
            <div class="flex items-center">
              <div class="flex-1 text-sm">{{ article.title }}</div>

              <img
                :src="article.picUrl"
                class="h-[70px] w-[70px] object-cover"
                alt="文章图片"
              />
            </div>
          </div>
        </div>
      </a>
    </div>
  </div>
</template>
