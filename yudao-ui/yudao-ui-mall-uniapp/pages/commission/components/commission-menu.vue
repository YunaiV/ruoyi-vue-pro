<!-- 分销：商菜单栏 -->
<template>
  <view class="menu-box ss-flex-col">
    <view class="header-box">
      <image class="header-bg" :src="sheep.$url.static('/static/img/shop/commission/title1.png')" />
      <view class="ss-flex header-title">
        <view class="title">功能专区</view>
        <text class="cicon-forward"></text>
      </view>
    </view>
    <view class="menu-list ss-flex ss-flex-wrap">
      <view
        v-for="(item, index) in state.menuList"
        :key="index"
        class="item-box ss-flex-col ss-col-center"
        @tap="sheep.$router.go(item.path)"
      >
        <image
          class="menu-icon ss-m-b-10"
          :src="sheep.$url.static(item.img)"
          mode="aspectFill"
        ></image>
        <view>{{ item.title }}</view>
      </view>
    </view>
  </view>
</template>

<script setup>
  import sheep from '@/sheep';
  import { reactive } from 'vue';

  const state = reactive({
    menuList: [
      {
        img: '/static/img/shop/commission/commission_icon1.png',
        title: '我的团队',
        path: '/pages/commission/team',
      },
      {
        img: '/static/img/shop/commission/commission_icon2.png',
        title: '佣金明细',
        path: '/pages/commission/wallet',
      },
      {
        img: '/static/img/shop/commission/commission_icon3.png',
        title: '分销订单',
        path: '/pages/commission/order',
      },
      {
        img: '/static/img/shop/commission/commission_icon4.png',
        title: '推广商品',
        path: '/pages/commission/goods',
      },
      // {
      //   img: '/static/img/shop/commission/commission_icon5.png',
      //   title: '我的资料',
      //   path: '/pages/commission/apply',
      //   isAgentFrom: true,
      // },
      {
        img: '/static/img/shop/commission/commission_icon7.png',
        title: '邀请海报',
        path: 'action:showShareModal',
      },
      {
        img: '/static/img/shop/commission/commission_icon8.png',
        title: '推广排行',
        path: '/pages/commission/promoter',
      },
      {
        img: '/static/img/shop/commission/commission_icon9.png',
        title: '佣金排行',
        path: '/pages/commission/commission-ranking',
      },
    ],
  });
</script>

<style lang="scss" scoped>
  .menu-box {
    margin: 0 auto;
    width: 690rpx;
    margin-bottom: 20rpx;
    margin-top: 20rpx;
    border-radius: 12rpx;
    z-index: 3;
    position: relative;
  }

  .header-box {
    width: 690rpx;
    height: 76rpx;
    position: relative;

    .header-bg {
      width: 690rpx;
      height: 76rpx;
    }

    .header-title {
      position: absolute;
      left: 20rpx;
      top: 24rpx;
    }

    .title {
      font-size: 28rpx;
      font-weight: 500;
      color: #ffffff;
      line-height: 30rpx;
    }

    .cicon-forward {
      font-size: 30rpx;
      font-weight: 400;
      color: #ffffff;
      line-height: 30rpx;
    }
  }

  .menu-list {
    padding: 50rpx 0 10rpx 0;
    background: #fdfae9;
    border-radius: 0 0 12rpx 12rpx;
  }

  .item-box {
    width: 25%;
    margin-bottom: 40rpx;
  }

  .menu-icon {
    width: 68rpx;
    height: 68rpx;
    background: #ffffff;
    border-radius: 50%;
  }

  .menu-title {
    font-size: 26rpx;
    font-weight: 500;
    color: #ffffff;
  }
</style>
