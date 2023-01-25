<!--
  - Copyright (C) 2018-2019
  - All rights reserved, Designed By www.joolun.com
  【微信消息 - 图文】
  芋道源码：
  ① 代码优化，补充注释，提升阅读性
-->
<template>
  <div class="news-home">
    <div v-for="(article, index) in articles" :key="index" class="news-div">
      <!-- 头条 -->
      <a target="_blank" :href="article.url" v-if="index === 0">
        <div class="news-main">
          <div class="news-content">
            <img class="material-img" :src="article.picUrl" width="280px" height="120px"/>
            <div class="news-content-title">
              <span>{{article.title}}</span>
            </div>
          </div>
        </div>
      </a>
      <!-- 二条/三条等等 -->
      <a target="_blank" :href="article.url" v-else>
        <div class="news-main-item">
          <div class="news-content-item">
            <div class="news-content-item-title">{{article.title}}</div>
            <div class="news-content-item-img">
              <img class="material-img" :src="article.picUrl" height="100%"/>
            </div>
          </div>
        </div>
      </a>
    </div>
  </div>
</template>

<script>
export default {
  name: "wxNews",
  props: {
    articles: {
      type: Array // title - 标题；description - 描述；picUrl - 图片连接；url - 跳转链接
    }
  },
  // created() {
  //   console.log(this.articles)
  // },
};
</script>

<style lang="scss" scoped>
  .news-home {
    background-color: #FFFFFF;
    width: 100%;
    margin: auto;
  }
  .news-main {
    width: 100%;
    margin: auto;
  }
  .news-content {
    background-color: #acadae;
    width: 100%;
    position: relative;
  }
  .news-content-title {
    display: inline-block;
    font-size: 12px;
    color: #FFFFFF;
    position: absolute;
    left: 0;
    bottom: 0;
    background-color: black;
    width: 98%;
    padding: 1%;
    opacity: 0.65;
    white-space: normal;
    box-sizing: unset!important
  }
  .news-main-item {
    background-color: #FFFFFF;
    padding: 5px 0;
    border-top: 1px solid #eaeaea;
  }
  .news-content-item {
    position: relative;
  }
  .news-content-item-title {
    display: inline-block;
    font-size: 10px;
    width: 70%;
    margin-left: 1%;
    white-space: normal
  }
  .news-content-item-img {
    display: inline-block;
    width: 25%;
    background-color: #acadae;
    margin-right: 1%;
  }
  .material-img {
    width: 100%;
  }
</style>
