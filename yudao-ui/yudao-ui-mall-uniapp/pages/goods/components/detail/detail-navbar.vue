<!-- 商品详情：商品/评价/详情的 nav -->
<template>
  <su-fixed alway :bgStyles="{ background: '#fff' }" :val="0" noNav opacity :placeholder="false">
    <su-status-bar />
    <view
      class="ui-bar ss-flex ss-col-center ss-row-between ss-p-x-20"
      :style="[{ height: sys_navBar - sys_statusBar + 'px' }]"
    >
      <!-- 左 -->
      <view class="icon-box ss-flex">
        <view class="icon-button icon-button-left ss-flex ss-row-center" @tap="onClickLeft">
          <text class="sicon-back" v-if="hasHistory" />
          <text class="sicon-home" v-else />
        </view>
        <view class="line"></view>
        <view class="icon-button icon-button-right ss-flex ss-row-center" @tap="onClickRight">
          <text class="sicon-more" />
        </view>
      </view>
      <!-- 中 -->
      <view class="detail-tab-card ss-flex-1" :style="[{ opacity: state.tabOpacityVal }]">
        <view class="tab-box ss-flex ss-col-center ss-row-around">
          <view
            class="tab-item ss-flex-1 ss-flex ss-row-center ss-col-center"
            v-for="item in state.tabList"
            :key="item.value"
            @tap="onTab(item)"
          >
            <view class="tab-title" :class="state.curTab === item.value ? 'cur-tab-title' : ''">
              {{ item.label }}
            </view>
            <view v-show="state.curTab === item.value" class="tab-line"></view>
          </view>
        </view>
      </view>
      <!-- #ifdef MP -->
      <view :style="[capsuleStyle]"></view>
      <!-- #endif -->
    </view>
  </su-fixed>
</template>

<script setup>
  import { reactive } from 'vue';
  import { onPageScroll } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import throttle from '@/sheep/helper/throttle.js';
  import { showMenuTools } from '@/sheep/hooks/useModal';

  const sys_statusBar = sheep.$platform.device.statusBarHeight;
  const sys_navBar = sheep.$platform.navbar;
  const capsuleStyle = {
    width: sheep.$platform.capsule.width + 'px',
    height: sheep.$platform.capsule.height + 'px',
  };

  const state = reactive({
    tabOpacityVal: 0,
    curTab: 'goods',
    tabList: [
      {
        label: '商品',
        value: 'goods',
        to: 'detail-swiper-selector',
      },
      {
        label: '评价',
        value: 'comment',
        to: 'detail-comment-selector',
      },
      {
        label: '详情',
        value: 'detail',
        to: 'detail-content-selector',
      },
    ],
  });
  const emits = defineEmits(['clickLeft']);
  const hasHistory = sheep.$router.hasHistory();

  function onClickLeft() {
    if (hasHistory) {
      sheep.$router.back();
    } else {
      sheep.$router.go('/pages/index/index');
    }
    emits('clickLeft');
  }

  function onClickRight() {
    showMenuTools();
  }

  let commentCard = {
    top: 0,
    bottom: 0,
  };

  function getCommentCardNode() {
    return new Promise((res, rej) => {
      uni
        .createSelectorQuery()
        .select('.detail-comment-selector')
        .boundingClientRect((data) => {
          if (data) {
            commentCard.top = data.top;
            commentCard.bottom = data.top + data.height;
            res(data);
          } else {
            res(null);
          }
        })
        .exec();
    });
  }

  function onTab(tab) {
    let scrollTop = 0;
    if (tab.value === 'comment') {
      scrollTop = commentCard.top - sys_navBar + 1;
    } else if (tab.value === 'detail') {
      scrollTop = commentCard.bottom - sys_navBar + 1;
    }
    uni.pageScrollTo({
      scrollTop,
      duration: 200,
    });
  }

  onPageScroll((e) => {
    state.tabOpacityVal = e.scrollTop > sheep.$platform.navbar ? 1 : e.scrollTop * 0.01;
    if (commentCard.top === 0) {
      throttle(() => {
        getCommentCardNode();
      }, 50);
    }

    if (e.scrollTop < commentCard.top - sys_navBar) {
      state.curTab = 'goods';
    } else if (
      e.scrollTop >= commentCard.top - sys_navBar &&
      e.scrollTop <= commentCard.bottom - sys_navBar
    ) {
      state.curTab = 'comment';
    } else {
      state.curTab = 'detail';
    }
  });
</script>

<style lang="scss" scoped>
  .icon-box {
    box-shadow: 0px 0px 4rpx rgba(51, 51, 51, 0.08), 0px 4rpx 6rpx 2rpx rgba(102, 102, 102, 0.12);
    border-radius: 30rpx;
    width: 134rpx;
    height: 56rpx;
    margin-left: 8rpx;
    border: 1px solid rgba(#fff, 0.4);
    .line {
      width: 2rpx;
      height: 24rpx;
      background: #e5e5e7;
    }
    .sicon-back {
      font-size: 32rpx;
      color: #000;
    }
    .sicon-home {
      font-size: 32rpx;
      color: #000;
    }
    .sicon-more {
      font-size: 32rpx;
      color: #000;
    }
    .icon-button {
      width: 67rpx;
      height: 56rpx;
      &-left:hover {
        background: rgba(0, 0, 0, 0.16);
        border-radius: 30rpx 0px 0px 30rpx;
      }
      &-right:hover {
        background: rgba(0, 0, 0, 0.16);
        border-radius: 0px 30rpx 30rpx 0px;
      }
    }
  }
  .left-box {
    position: relative;
    width: 60rpx;
    height: 60rpx;
    display: flex;
    justify-content: center;
    align-items: center;
    .circle {
      position: absolute;
      left: 0;
      top: 0;
      width: 60rpx;
      height: 60rpx;
      background: rgba(#fff, 0.6);
      border: 1rpx solid #ebebeb;
      border-radius: 50%;
      box-sizing: border-box;
      z-index: -1;
    }
  }
  .right {
    position: relative;
    width: 60rpx;
    height: 60rpx;
    display: flex;
    justify-content: center;
    align-items: center;
    .circle {
      position: absolute;
      left: 0;
      top: 0;
      width: 60rpx;
      height: 60rpx;
      background: rgba(#ffffff, 0.6);
      border: 1rpx solid #ebebeb;
      box-sizing: border-box;
      border-radius: 50%;
      z-index: -1;
    }
  }
  .detail-tab-card {
    width: 50%;
    .tab-item {
      height: 80rpx;
      position: relative;
      z-index: 11;

      .tab-title {
        font-size: 30rpx;
      }

      .cur-tab-title {
        font-weight: $font-weight-bold;
      }

      .tab-line {
        width: 60rpx;
        height: 6rpx;
        border-radius: 6rpx;
        position: absolute;
        left: 50%;
        transform: translateX(-50%);
        bottom: 10rpx;
        background-color: var(--ui-BG-Main);
        z-index: 12;
      }
    }
  }
</style>
