<template>
  <view>
    <!-- md卡片：竖向，一行放两个，图上内容下 -->
    <view v-if="size === 'md'" class="md-goods-card ss-flex-col" :style="[elStyles]" @tap="onClick">
      <view class="icon-box ss-flex">
        <image class="icon" :src="state.liveStatus[data.status].img"></image>
        <view class="title ss-m-l-16">{{ state.liveStatus[data.status].title }}</view>
      </view>
      <img class="md-img-box" :src="sheep.$url.cdn(data.feeds_img)" referrerpolicy="no-referrer">
      <view class="md-goods-content">
        <view class="md-goods-title ss-line-1" :style="[{ color: titleColor }]">
          {{ data.name }}
        </view>
        <view class="md-goods-subtitle ss-m-t-14 ss-line-1" :style="[{ color: subTitleColor }]">
          主播：{{ data.anchor_name }}
        </view>
      </view>
    </view>
    <!-- sl卡片：竖向型，一行放一个，图片上内容下边 -->
    <view v-if="size === 'sl'" class="sl-goods-card ss-flex-col" :style="[elStyles]" @tap="onClick">
      <view class="icon-box ss-flex">
        <image class="icon" :src="state.liveStatus[data.status].img"></image>
        <view class="title ss-m-l-16">{{ state.liveStatus[data.status].title }}</view>
      </view>
      <img class="sl-img-box" :src="sheep.$url.cdn(data.feeds_img)" referrerpolicy="no-referrer">
      <view class="sl-goods-content">
        <view class="sl-goods-title ss-line-1" :style="[{ color: titleColor }]">
          {{ data.name }}
        </view>
        <view class="sl-goods-subtitle ss-m-t-14 ss-line-1" :style="[{ color: subTitleColor }]">
          主播：{{ data.anchor_name }}
        </view>
      </view>
    </view>
  </view>
</template>
<script setup>
  import { computed, reactive } from 'vue';
  import sheep from '@/sheep';
  /**
   * 直播卡片
   *
   * @property {String} img 											- 图片
   * @property {String} title 										- 标题
   * @property {Number} titleWidth = 0								- 标题宽度，默认0，单位rpx
   * @property {String} skuText 										- 规格
   * @property {String | Number} score 								- 积分
   * @property {String | Number} price 								- 价格
   * @property {String | Number} originalPrice 						- 单购价
   * @property {String} priceColor 									- 价格颜色
   * @property {Number | String} num									- 数量
   *
   */
  const props = defineProps({
    goodsFields: {
      type: [Array, Object],
      default() {
        return {};
      },
    },
    tagStyle: {
      type: Object,
      default: {},
    },
    data: {
      type: Object,
      default: {},
    },
    size: {
      type: String,
      default: 'sl',
    },
    background: {
      type: String,
      default: '',
    },
    topRadius: {
      type: Number,
      default: 0,
    },
    bottomRadius: {
      type: Number,
      default: 0,
    },
    titleColor: {
      type: String,
      default: '#333',
    },
    subTitleColor: {
      type: String,
      default: '#999999',
    },
  });
  // 组件样式
  const elStyles = computed(() => {
    return {
      background: props.background,
      'border-top-left-radius': props.topRadius + 'px',
      'border-top-right-radius': props.topRadius + 'px',
      'border-bottom-left-radius': props.bottomRadius + 'px',
      'border-bottom-right-radius': props.bottomRadius + 'px',
    };
  });
  const state = reactive({
    liveStatus: {
      101: {
        img: sheep.$url.static('/static/img/shop/app/mplive/living.png'),
        title: '直播中',
      },
      102: {
        img: sheep.$url.static('/static/img/shop/app/mplive/start.png'),
        title: '未开始',
      },
      103: {
        img: sheep.$url.static('/static/img/shop/app/mplive/ended.png'),
        title: '已结束',
      },
    },
  });
  const emits = defineEmits(['click', 'getHeight']);
  const onClick = () => {
    emits('click');
  };
</script>

<style lang="scss" scoped>
  // md
  .md-goods-card {
    overflow: hidden;
    width: 100%;
    height: 424rpx;
    position: relative;
    z-index: 1;
    background-color: $white;
    .icon-box {
      position: absolute;
      left: 20rpx;
      top: 10rpx;
      width: 136rpx;
      height: 40rpx;
      background: rgba(#000000, 0.5);
      border-radius: 20rpx;
      z-index: 1;
      .icon {
        width: 40rpx;
        height: 40rpx;
        border-radius: 20rpx 0px 20rpx 20rpx;
      }
      .title {
        font-size: 24rpx;
        font-weight: 500;
        color: #ffffff;
      }
    }
    .md-goods-content {
      position: absolute;
      left: 0;
      bottom: 0;
      padding: 20rpx;
      width: 100%;
      background: linear-gradient(360deg, rgba(0, 0, 0, 0.8) 0%, rgba(0, 0, 0, 0.02) 100%);
    }

    .md-img-box {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .md-goods-title {
      font-size: 26rpx;
      color: #333;
      width: 100%;
    }
    .md-goods-subtitle {
      font-size: 24rpx;
      font-weight: 400;
      color: #999999;
    }
  }
  .sl-goods-card {
    overflow: hidden;
    position: relative;
    z-index: 1;
    width: 100%;
    height: 400rpx;
    background-color: $white;
    .icon-box {
      position: absolute;
      left: 20rpx;
      top: 10rpx;
      width: 136rpx;
      height: 40rpx;
      background: rgba(#000000, 0.5);
      border-radius: 20rpx;
      z-index: 1;
      .icon {
        width: 40rpx;
        height: 40rpx;
        border-radius: 20rpx 0px 20rpx 20rpx;
      }
      .title {
        font-size: 24rpx;
        font-weight: 500;
        color: #ffffff;
      }
    }
    .sl-goods-content {
      position: absolute;
      left: 0;
      bottom: 0;
      padding: 20rpx;
      width: 100%;
      background: linear-gradient(360deg, rgba(0, 0, 0, 0.8) 0%, rgba(0, 0, 0, 0.02) 100%);
    }

    .sl-img-box {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .sl-goods-title {
      font-size: 26rpx;
      color: #333;
      width: 100%;
    }
    .sl-goods-subtitle {
      font-size: 24rpx;
      font-weight: 400;
      color: #999999;
    }
  }
</style>
