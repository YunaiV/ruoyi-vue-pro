<!-- 物流追踪 -->
<template>
  <s-layout title="物流追踪">
    <view class="log-wrap">
      <!-- 商品信息 -->
      <view class="log-card ss-flex ss-m-20 ss-r-10" v-if="goodsImages.length > 0">
        <uni-swiper-dot :info="goodsImages" :current="state.current" mode="round">
          <swiper class="swiper-box">
            <swiper-item v-for="(item, index) in goodsImages" :key="index">
              <image class="log-card-img" :src="sheep.$url.static(item.image)" />
            </swiper-item>
          </swiper>
        </uni-swiper-dot>
        <view class="log-card-msg">
          <!-- TODO 芋艿：【物流】优化点：展示状态 -->
          <!--          <view class="ss-flex ss-m-b-8">-->
          <!--            <view>物流状态：</view>-->
          <!--            <view class="warning-color">{{ state.info.status_text }}</view>-->
          <!--          </view>-->
          <view class="ss-m-b-8">快递单号：{{ state.info.logisticsNo }}</view>
          <view>快递公司：{{ state.info.logisticsName }}</view>
        </view>
      </view>

      <!-- 物流轨迹 -->
      <view class="log-content ss-m-20 ss-r-10">
        <view
          class="log-content-box ss-flex"
          v-for="(item, index) in state.tracks"
          :key="item.title"
        >
          <view class="log-icon ss-flex-col ss-col-center ss-m-r-20">
            <text class="cicon-title" />
            <view v-if="state.tracks.length - 1 !== index" class="line" />
          </view>
          <view class="log-content-msg">
            <!-- TODO 芋艿：【物流】优化点：展示状态 -->
            <!--            <view class="log-msg-title ss-m-b-20">-->
            <!--              {{ item.status_text }}-->
            <!--            </view>-->
            <!--            <view class="log-msg-desc ss-m-b-16">{{ item.content }}</view>-->
            <view class="log-msg-desc ss-m-b-16">
              <highlight-number :content="item.content" @phone-click="handlePhoneClick" />
              <!--              <rich-text :nodes="item.content"></rich-text>-->
            </view>
            <view class="log-msg-date ss-m-b-40">
              {{ sheep.$helper.timeFormat(item.time, 'yyyy-mm-dd hh:MM:ss') }}
            </view>
          </view>
        </view>
      </view>
    </view>
  </s-layout>
</template>

<script setup>
  import sheep from '@/sheep';
  import { onLoad } from '@dcloudio/uni-app';
  import { computed, reactive } from 'vue';
  import OrderApi from '@/sheep/api/trade/order';
  import HighlightNumber from '@/pages/components/HighlightNumberText.vue';

  const state = reactive({
    info: [],
    tracks: [],
  });

  const goodsImages = computed(() => {
    let array = [];
    if (state.info.items) {
      state.info.items.forEach((item) => {
        array.push({
          image: item.picUrl,
        });
      });
    }
    return array;
  });

  async function getExpressDetail(id) {
    const { data } = await OrderApi.getOrderExpressTrackList(id);
    state.tracks = data;
  }

  async function getOrderDetail(id) {
    const { data } = await OrderApi.getOrderDetail(id);
    state.info = data;
  }

  onLoad((options) => {
    getExpressDetail(options.id);
    getOrderDetail(options.id);
  });

  function handlePhoneClick(data) {
    handleClick(data);
  }

  function handleClick(data) {
    const phoneNumber = data.phoneNumber;

    if (!phoneNumber) return;

    // 获取当前平台
    const platform = uni.getSystemInfoSync().platform.toLowerCase();

    if (platform === 'devtools') {
      uni.showToast({ title: '真机才可拨打电话', icon: 'none' });
      handleCopy(phoneNumber);
      return;
    }

    if (platform === 'wechat') {
      uni.showToast({ title: '请手动拨打', icon: 'none' });
      handleCopy(phoneNumber);
      return;
    }

    uni.makePhoneCall({
      phoneNumber: phoneNumber,
      success: () => {
        console.log('拨打电话成功');
      },
      fail: (err) => {
        console.error('拨打电话失败', err);
        uni.showToast({ title: '拨号失败，请手动拨打', icon: 'none' });
        handleCopy(phoneNumber);
      },
    });
  }
  function handleCopy(text) {
    uni.setClipboardData({
      data: text,
      success: () => {
        uni.showToast({ title: '已复制到剪贴板', icon: 'success' });
      },
      fail: () => {
        uni.showToast({ title: '复制失败', icon: 'none' });
      },
    });
  }
</script>

<style lang="scss" scoped>
  .highlight {
    color: red; /* 高亮颜色 */
    font-weight: bold;
  }

  .swiper-box {
    width: 200rpx;
    height: 200rpx;
  }

  .log-card {
    border-top: 2rpx solid rgba(#dfdfdf, 0.5);
    padding: 20rpx;
    background: #fff;
    margin-bottom: 20rpx;

    .log-card-img {
      width: 200rpx;
      height: 200rpx;
      margin-right: 20rpx;
    }

    .log-card-msg {
      font-size: 28rpx;
      font-weight: 500;
      width: 490rpx;
      color: #333333;

      .warning-color {
        color: #999;
      }
    }
  }

  .log-content {
    padding: 34rpx 20rpx 0rpx 20rpx;
    background: #fff;

    .log-content-box {
      align-items: stretch;
    }

    .log-icon {
      height: inherit;

      .cicon-title {
        color: #ccc;
        font-size: 40rpx;
      }

      .activity-color {
        color: #f0c785;
        font-size: 40rpx;
      }

      .info-color {
        color: #ccc;
        font-size: 40rpx;
      }

      .line {
        width: 1px;
        height: 100%;
        background: #d8d8d8;
      }
    }

    .log-content-msg {
      .log-msg-title {
        font-size: 28rpx;
        font-weight: bold;
        color: #333333;
      }

      .log-msg-desc {
        font-size: 24rpx;
        font-weight: 400;
        color: #333333;
        line-height: 36rpx;
      }

      .log-msg-date {
        font-size: 24rpx;
        font-weight: 500;
        color: #000000;
      }
    }
  }
</style>
