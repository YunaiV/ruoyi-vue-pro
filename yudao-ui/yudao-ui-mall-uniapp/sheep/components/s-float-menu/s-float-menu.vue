<!-- 装修基础组件：悬浮按钮 -->
<template>
  <!-- 模态背景：展开时显示，点击后折叠 -->
  <view class="modal-bg" v-if="fabRef?.isShow" @click="handleCollapseFab"></view>
  <!-- 悬浮按钮 -->
  <uni-fab
    ref="fabRef"
    horizontal="right"
    vertical="bottom"
    :direction="state.direction"
    :pattern="state.pattern"
    :content="state.content"
    @trigger="handleOpenLink"
  />
</template>
<script setup>
  /**
   * 悬浮按钮
   */

  import sheep from '@/sheep';
  import { reactive, ref, unref } from 'vue';
  import { onBackPress } from '@dcloudio/uni-app';

  // 定义属性
  const props = defineProps({
    data: {
      type: Object,
      default() {},
    }
  })

  // 悬浮按钮配置： https://uniapp.dcloud.net.cn/component/uniui/uni-fab.html#fab-props
  const state = reactive({
    // 可选样式配置项
    pattern: [],
    // 展开菜单内容配置项
    content: [],
    // 展开菜单显示方式：horizontal-水平显示，vertical-垂直显示
    direction: '',
  });

  // 悬浮按钮引用
  const fabRef = ref(null);
  // 按钮方向
  state.direction = props.data.direction;
  props.data?.list.forEach((item) => {
    // 按钮文字
    const text = props.data?.showText ? item.text : ''
    // 生成内容配置项
    state.content.push({ iconPath: sheep.$url.cdn(item.imgUrl), url: item.url, text });
    // 生成样式配置项
    state.pattern.push({ color: item.textColor });
  });

  // 处理链接跳转
  function handleOpenLink(e) {
    sheep.$router.go(e.item.url);
  }

  // 折叠
  function handleCollapseFab() {
    if (unref(fabRef)?.isShow) {
      unref(fabRef)?.close();
    }
  }

  // 按返回值后，折叠悬浮按钮
  onBackPress(() => {
    if (unref(fabRef)?.isShow) {
      unref(fabRef)?.close();
      return true;
    }
    return false;
  });
</script>
<style lang="scss" scoped>
  /* 模态背景 */
  .modal-bg {
    position: fixed;
    left: 0;
    top: 0;
    z-index: 11;
    width: 100%;
    height: 100%;
    background-color: rgba(#000000, 0.4);
  }
</style>
