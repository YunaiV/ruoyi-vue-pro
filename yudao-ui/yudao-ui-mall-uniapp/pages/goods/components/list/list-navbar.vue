<!-- 页面；暂时没用到  -->
<template>
  <su-fixed
    alway
    :bgStyles="{ background: '#fff' }"
    :val="0"
    noNav
    :opacity="false"
    placeholder
    index="10090"
  >
    <su-status-bar />
    <view
      class="ui-bar ss-flex ss-col-center ss-row-between ss-p-x-20"
      :style="[{ height: sys_navBar - sys_statusBar + 'px' }]"
    >
      <!-- 左 -->
      <view class="left-box">
        <text
          class="_icon-back back-icon"
          @tap="toBack"
          :style="[{ color: state.iconColor }]"
        ></text>
      </view>
      <!-- 中 -->
      <uni-search-bar
        class="ss-flex-1"
        radius="33"
        :placeholder="placeholder"
        cancelButton="none"
        :focus="true"
        v-model="state.searchVal"
        @confirm="onSearch"
      />
      <!-- 右 -->
      <view class="right">
        <text class="sicon-more" :style="[{ color: state.iconColor }]" @tap="showMenuTools" />
      </view>
      <!-- #ifdef MP -->
      <view :style="[capsuleStyle]"></view>
      <!-- #endif -->
    </view>
  </su-fixed>
</template>

<script setup>
  import { reactive } from 'vue';
  import sheep from '@/sheep';
  import { showMenuTools } from '@/sheep/hooks/useModal';

  const sys_statusBar = sheep.$platform.device.statusBarHeight;
  const sys_navBar = sheep.$platform.navbar;
  const capsuleStyle = {
    width: sheep.$platform.capsule.width + 'px',
    height: sheep.$platform.capsule.height + 'px',
    margin: '0 ' + (sheep.$platform.device.windowWidth - sheep.$platform.capsule.right) + 'px',
  };

  const state = reactive({
    iconColor: '#000',
    searchVal: '',
  });

  const props = defineProps({
    placeholder: {
      type: String,
      default: '搜索内容',
    },
  });

  const emits = defineEmits(['searchConfirm']);

  // 返回
  const toBack = () => {
    sheep.$router.back();
  };

  // 搜索
  const onSearch = () => {
    emits('searchConfirm', state.searchVal);
  };

  const onTab = (item) => {};
</script>

<style lang="scss" scoped>
  .back-icon {
    font-size: 40rpx;
  }
  .sicon-more {
    font-size: 48rpx;
  }
</style>
