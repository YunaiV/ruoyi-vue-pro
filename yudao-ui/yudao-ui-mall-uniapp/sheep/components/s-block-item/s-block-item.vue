<template>
  <view>
    <!-- 基础组件：搜索框 -->
    <s-search-block v-if="type === 'SearchBar'" :data="data" :styles="styles" :navbar="false" />
    <!-- 基础组件：公告栏 -->
    <s-notice-block v-if="type === 'NoticeBar'" :data="data" />
    <!-- 基础组件：菜单导航 -->
    <s-menu-button v-if="type === 'MenuSwiper'" :data="data" :styles="styles" />
    <!-- 基础组件：列表导航 -->
    <s-menu-list v-if="type === 'MenuList'" :data="data" />
    <!-- 基础组件：宫格导航 -->
    <s-menu-grid v-if="type === 'MenuGrid'" :data="data" :styles="styles" />
    <!-- 基础组件：弹窗广告 -->
    <s-popup-image v-if="type === 'Popover'" :data="data" />
    <!-- 基础组件：悬浮按钮 -->
    <s-float-menu v-if="type === 'FloatingActionButton'" :data="data" />

    <!-- 图文组件：图片展示 -->
    <s-image-block v-if="type === 'ImageBar'" :data="data" :styles="styles" />
    <!-- 图文组件：图片轮播 -->
    <s-image-banner v-if="type === 'Carousel'" :data="data" :styles="styles" />
    <!-- 基础组件：标题栏 -->
    <s-title-block v-if="type === 'TitleBar'" :data="data" :styles="styles" />
    <!-- 图文组件：广告魔方 -->
    <s-image-cube v-if="type === 'MagicCube'" :data="data" :styles="styles" />
    <!-- 图文组件：视频播放 -->
    <s-video-block v-if="type === 'VideoPlayer'" :data="data" :styles="styles" />
    <!-- 基础组件：分割线 -->
    <s-line-block v-if="type === 'Divider'" :data="data" />
    <!-- 图文组件：热区 -->
    <s-hotzone-block v-if="type === 'HotZone'" :data="data" :styles="styles" />

    <!-- 商品组件：商品卡片 -->
    <s-goods-card v-if="type === 'ProductCard'" :data="data" :styles="styles" />
    <!-- 商品组件：商品栏 -->
    <s-goods-shelves v-if="type === 'ProductList'" :data="data" :styles="styles" />

    <!-- 营销组件：拼团 -->
    <s-groupon-block v-if="type === 'PromotionCombination'" :data="data" :styles="styles" />
    <!-- 营销组件：秒杀 -->
    <s-seckill-block v-if="type === 'PromotionSeckill'" :data="data" :styles="styles" />
    <!-- 营销组件：积分商城 -->
    <s-point-block v-if="type === 'PromotionPoint'" :data="data" :styles="styles" />
    <!-- 营销组件：小程序直播（暂时没有这个功能） -->
    <s-live-block v-if="type === 'MpLive'" :data="data" :styles="styles" />
    <!-- 营销组件：优惠券 -->
    <s-coupon-block v-if="type === 'CouponCard'" :data="data" :styles="styles" />
    <!-- 营销组件：文章 -->
    <s-richtext-block v-if="type === 'PromotionArticle'" :data="data" :styles="styles" />

    <!-- 用户组件：用户卡片 -->
    <s-user-card v-if="type === 'UserCard'" :data="data" :styles="styles" />
    <!-- 用户组件：用户订单 -->
    <s-order-card v-if="type === 'UserOrder'" :data="data" :styles="styles" />
    <!-- 用户组件：用户资产 -->
    <s-wallet-card v-if="type === 'UserWallet'" :data="data" :styles="styles" />
    <!-- 用户组件：用户卡券 -->
    <s-coupon-card v-if="type === 'UserCoupon'" :data="data" :styles="styles" />
  </view>
</template>

<script setup>
  /**
   * 装修组件 - 组件集
   */
  const props = defineProps({
    type: {
      type: String,
      default: '',
    },
    data: {
      type: Object,
      default() {},
    },
    styles: {
      type: Object,
      default() {},
    },
  });
  function onSearch() {}
</script>

<style></style>
