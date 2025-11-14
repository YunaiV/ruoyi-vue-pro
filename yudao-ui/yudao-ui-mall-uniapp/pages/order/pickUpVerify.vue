<template>
  <view class="order-details">
    <!--  自提商品核销  -->
    <view v-if="orderInfo.deliveryType === 2 && orderInfo.payStatus" class="writeOff borRadius14">
      <view class="title">核销信息</view>
      <view class="grayBg flex-center">
        <view class="pictrue">
          <image
            v-if="!!painterImageUrl"
            :src="painterImageUrl"
            :style="{ width: `${state.qrcodeSize}px`, height: `${state.qrcodeSize}px` }"
            :show-menu-by-longpress="true"
          />
        </view>
      </view>
      <view class="gear">
        <image :src="sheep.$url.static('/static/img/shop/writeOff.png')"></image>
      </view>
      <view class="num">{{ orderInfo.pickUpVerifyCode }}</view>
      <view class="rules">
        <view class="item">
          <view class="rulesTitle flex flex-wrap align-center"> 核销时间 </view>
          <view class="info">
            每日：
            <text class="time">{{ systemStore.openingTime }} - {{ systemStore.closingTime }}</text>
          </view>
        </view>
        <view class="item">
          <view class="rulesTitle flex flex-wrap align-center">
            <text class="iconfont icon-shuoming1"></text>
            使用说明
          </view>
          <view class="info">可将二维码出示给店员扫描或提供数字核销码</view>
        </view>
      </view>
    </view>
    <view
      v-if="orderInfo.deliveryType === 2"
      class="map flex flex-wrap align-center ss-row-between borRadius14"
    >
      <view>自提地址信息</view>
      <view class="place cart-color flex flex-wrap flex-center" @tap="showMaoLocation">
        查看位置
      </view>
    </view>
    <!--  海报画板：默认隐藏只用来生成海报。生成方式为主动调用  -->
    <l-painter
      v-if="showPainter"
      isCanvasToTempFilePath
      pathType="url"
      @success="setPainterImageUrl"
      hidden
      ref="painterRef"
    />
  </view>
</template>

<script setup>
  import sheep from '@/sheep';
  import { reactive, ref } from 'vue';

  const props = defineProps({
    orderInfo: {
      type: Object,
      default() {},
    },
    systemStore: {
      type: Object,
      default() {},
    },
  });
  const state = reactive({
    qrcodeSize: 145,
  });

  /**
   * 打开地图
   */
  const showMaoLocation = () => {
    if (!props.systemStore.latitude || !props.systemStore.longitude) {
      sheep.$helper.toast('缺少经纬度信息无法查看地图!');
      return;
    }
    uni.openLocation({
      latitude: props.systemStore.latitude,
      longitude: props.systemStore.longitude,
      scale: 8,
      name: props.systemStore.name,
      address: props.systemStore.areaName + props.systemStore.detailAddress,
    });
  };

  /**
   * 拨打电话
   */
  const makePhone = () => {
    uni.makePhoneCall({
      phoneNumber: props.systemStore.phone,
    });
  };

  const painterRef = ref(); // 海报画板
  const painterImageUrl = ref(); // 海报 url
  const showPainter = ref(true);
  // 渲染海报
  const renderPoster = async (poster) => {
    await painterRef.value.render(poster);
  };
  // 获得生成的图片
  const setPainterImageUrl = (path) => {
    painterImageUrl.value = path;
    showPainter.value = false;
  };
  /**
   * 生成核销二维码
   */
  const markCode = (text) => {
    renderPoster({
      css: {
        width: `${state.qrcodeSize}px`,
        height: `${state.qrcodeSize}px`,
      },
      views: [
        {
          type: 'qrcode',
          text: text,
          css: {
            width: `${state.qrcodeSize}px`,
            height: `${state.qrcodeSize}px`,
          },
        },
      ],
    });
  };
  defineExpose({
    markCode,
  });
</script>

<style scoped lang="scss">
  .borRadius14 {
    border-radius: 14rpx !important;
  }
  .cart-color {
    color: #e93323 !important;
    border: 1px solid #e93323 !important;
  }
  .order-details {
    border-radius: 10rpx;
    margin: 0 20rpx 20rpx 20rpx;
  }
  .order-details .writeOff {
    background-color: #fff;
    margin-top: 15rpx;
    padding-bottom: 50rpx;
  }

  .order-details .writeOff .title {
    font-size: 30rpx;
    color: #282828;
    height: 87rpx;
    border-bottom: 1px solid #f0f0f0;
    padding: 0 24rpx;
    line-height: 87rpx;
  }

  .order-details .writeOff .grayBg {
    background-color: #f2f5f7;
    width: 590rpx;
    height: 384rpx;
    border-radius: 20rpx 20rpx 0 0;
    margin: 50rpx auto 0 auto;
    padding-top: 55rpx;
  }

  .order-details .writeOff .grayBg .pictrue {
    width: 290rpx;
    height: 290rpx;
  }

  .order-details .writeOff .grayBg .pictrue image {
    width: 100%;
    height: 100%;
  }

  .order-details .writeOff .gear {
    width: 590rpx;
    height: 30rpx;
    margin: 0 auto;
  }

  .order-details .writeOff .gear image {
    width: 100%;
    height: 100%;
  }

  .order-details .writeOff .num {
    background-color: #f0c34c;
    width: 590rpx;
    height: 84rpx;
    color: #282828;
    font-size: 48rpx;
    margin: 0 auto;
    border-radius: 0 0 20rpx 20rpx;
    text-align: center;
    padding-top: 4rpx;
  }

  .order-details .writeOff .rules {
    margin: 46rpx 30rpx 0 30rpx;
    border-top: 1px solid #f0f0f0;
    padding-top: 10rpx;
  }

  .order-details .writeOff .rules .item {
    margin-top: 20rpx;
  }

  .order-details .writeOff .rules .item .rulesTitle {
    font-size: 28rpx;
    color: #282828;
  }

  .order-details .writeOff .rules .item .rulesTitle .iconfont {
    font-size: 30rpx;
    color: #333;
    margin-right: 8rpx;
    margin-top: 5rpx;
  }

  .order-details .writeOff .rules .item .info {
    font-size: 28rpx;
    color: #999;
    margin-top: 7rpx;
  }

  .order-details .writeOff .rules .item .info .time {
    margin-left: 20rpx;
  }

  .order-details .map {
    height: 86rpx;
    font-size: 30rpx;
    color: #282828;
    line-height: 86rpx;
    border-bottom: 1px solid #f0f0f0;
    margin-top: 15rpx;
    background-color: #fff;
    padding: 0 24rpx;
  }

  .order-details .map .place {
    font-size: 26rpx;
    width: 176rpx;
    height: 50rpx;
    border-radius: 25rpx;
    line-height: 50rpx;
    text-align: center;
  }
</style>
