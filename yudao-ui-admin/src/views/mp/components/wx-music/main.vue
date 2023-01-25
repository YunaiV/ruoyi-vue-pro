<!--
  【微信消息 - 音乐】
-->
<template>
  <div>
    <el-link type="success" :underline="false" target="_blank" :href="hqMusicUrl ? hqMusicUrl : musicUrl">
      <div class="avue-card__body" style="padding:10px;background-color: #fff;border-radius: 5px">
        <div class="avue-card__avatar">
          <img :src="thumbMediaUrl" alt=""/>
        </div>
        <div class="avue-card__detail">
          <div class="avue-card__title" style="margin-bottom:unset">{{ title }}</div>
          <div class="avue-card__info" style="height: unset">{{ description }}</div>
        </div>
      </div>
    </el-link>
  </div>
</template>

<script>

export default {
  name: "wxMusic",
  props: {
    title: {
      required: false,
      type: String
    },
    description: {
      required: false,
      type: String
    },
    musicUrl: {
      required: false,
      type: String
    },
    hqMusicUrl: {
      required: false,
      type: String
    },
    thumbMediaUrl: {
      required: true,
      type: String
    },
  }
};
</script>

<style lang="scss" scoped>
/* 因为 joolun 实现依赖 avue 组件，该页面使用了 card.scc  */
@import '../wx-msg/card.scss';
</style>
