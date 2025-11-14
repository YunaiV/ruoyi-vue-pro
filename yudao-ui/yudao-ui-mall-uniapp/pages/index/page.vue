<!-- 自定义页面：支持装修 -->
<template>
  <s-layout
    :title="state.name"
    navbar="custom"
    :bgStyle="state.page"
    :navbarStyle="state.navigationBar"
    onShareAppMessage
    showLeftButton
  >
    <s-block v-for="(item, index) in state.components" :key="index" :styles="item.property.style">
      <s-block-item :type="item.id" :data="item.property" :styles="item.property.style" />
    </s-block>
  </s-layout>
</template>

<script setup>
  import { reactive } from 'vue';
  import { onLoad, onPageScroll } from '@dcloudio/uni-app';
  import DiyApi from '@/sheep/api/promotion/diy';

  const state = reactive({
    name: '',
    components: [],
    navigationBar: {},
    page: {},
  });
  onLoad(async (options) => {
    let id = options.id

    // #ifdef MP
    // 小程序预览自定义页面
    if (options.scene) {
      const sceneParams = decodeURIComponent(options.scene).split('=');
      id = sceneParams[1];
    }
    // #endif

    const { code, data } = await DiyApi.getDiyPage(id);
    if (code === 0) {
      state.name = data.name;
      state.components = data.property?.components;
      state.navigationBar = data.property?.navigationBar;
      state.page = data.property?.page;
    }
  });

  onPageScroll(() => {});
</script>

<style></style>
