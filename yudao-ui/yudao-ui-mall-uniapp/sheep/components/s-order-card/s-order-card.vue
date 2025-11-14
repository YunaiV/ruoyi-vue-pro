<!-- 装修用户组件：用户订单 -->
<template>
  <view class="ss-order-menu-wrap ss-flex ss-col-center" :style="[style, { marginLeft: `${data.space}px` }]">
    <view
      class="menu-item ss-flex-1 ss-flex-col ss-row-center ss-col-center"
      v-for="item in orderMap"
      :key="item.title"
      @tap="sheep.$router.go(item.path, { type: item.value })"
    >
      <uni-badge
        class="uni-badge-left-margin"
        :text="numData.orderCount[item.count]"
        absolute="rightTop"
        size="small"
      >
        <image class="item-icon" :src="sheep.$url.static(item.icon)" mode="aspectFit" />
      </uni-badge>
      <view class="menu-title ss-m-t-28">{{ item.title }}</view>
    </view>
  </view>
</template>

<script setup>
  /**
   * 装修组件 - 订单菜单组
   */
  import sheep from '@/sheep';
  import { computed } from 'vue';

  const orderMap = [
    {
      title: '待付款',
      value: '1',
      icon: '/static/img/shop/order/no_pay.png',
      path: '/pages/order/list',
      type: 'unpaid',
      count: 'unpaidCount',
    },
    {
      title: '待收货',
      value: '3',
      icon: '/static/img/shop/order/no_take.png',
      path: '/pages/order/list',
      type: 'noget',
      count: 'deliveredCount',
    },
    {
      title: '待评价',
      value: '4',
      icon: '/static/img/shop/order/no_comment.png',
      path: '/pages/order/list',
      type: 'nocomment',
      count: 'uncommentedCount',
    },
    {
      title: '售后单',
      value: '0',
      icon: '/static/img/shop/order/change_order.png',
      path: '/pages/order/aftersale/list',
      type: 'aftersale',
      count: 'afterSaleCount',
    },
    {
      title: '全部订单',
      value: '0',
      icon: '/static/img/shop/order/all_order.png',
      path: '/pages/order/list',
    },
  ];
  // 接收参数
  const props = defineProps({
  	// 装修数据
  	data: {
  	  type: Object,
  	  default: () => ({}),
  	},
  	// 装修样式
  	styles: {
  	  type: Object,
  	  default: () => ({}),
  	},
  });
  // 设置角标
  const numData = computed(() => sheep.$store('user').numData);
  // 设置背景样式
  const style = computed(() => {
    // 直接从 props.styles 解构
    const { bgType, bgImg, bgColor } = props.styles; 
    // 根据 bgType 返回相应的样式
    return {
  		background: bgType === 'img'
  			? `url(${bgImg}) no-repeat top center / 100% 100%`
  			: bgColor
  	};
  });
</script>

<style lang="scss" scoped>
  .ss-order-menu-wrap {
    .menu-item {
      height: 160rpx;
      position: relative;
      z-index: 10;
      .menu-title {
        font-size: 24rpx;
        line-height: 24rpx;
        color: #333333;
      }
      .item-icon {
        width: 44rpx;
        height: 44rpx;
      }
      .num-icon {
        position: absolute;
        right: 18rpx;
        top: 18rpx;
        // width: 40rpx;
        padding: 0 8rpx;
        height: 26rpx;
        background: #ff4d4f;
        border-radius: 13rpx;
        color: #fefefe;
        display: flex;
        align-items: center;
        .num {
          font-size: 24rpx;
          transform: scale(0.8);
        }
      }
    }
  }
</style>
