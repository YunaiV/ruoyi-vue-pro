<!-- 商品详情 - 优惠劵领取 -->
<template>
  <su-popup
    :show="show"
    type="bottom"
    round="20"
    @close="emits('close')"
    showClose
    backgroundColor="#f2f2f2"
  >
    <view class="model-box">
      <view class="title ss-m-t-16 ss-m-l-20 ss-flex">优惠券</view>
      <scroll-view
        class="model-content"
        scroll-y
        :scroll-with-animation="false"
        :enable-back-to-top="true"
      >
        <view class="subtitle ss-m-l-20">可使用优惠券</view>
        <view v-for="item in state.couponInfo" :key="item.id">
          <s-coupon-list :data="item">
            <template #default>
              <button
                class="ss-reset-button card-btn ss-flex ss-row-center ss-col-center"
                :class="!item.canTake ? 'boder-btn' : ''"
                @click.stop="getBuy(item.id)"
                :disabled="!item.canTake"
              >
                {{ item.canTake ? '立即领取' : '已领取' }}
              </button>
            </template>
          </s-coupon-list>
        </view>
      </scroll-view>
    </view>
  </su-popup>
</template>
<script setup>
  import { computed, reactive } from 'vue';

  const props = defineProps({
    modelValue: {
      type: Object,
      default() {},
    },
    show: {
      type: Boolean,
      default: false,
    },
  });

  const emits = defineEmits(['get', 'close']);

  const state = reactive({
    couponInfo: computed(() => props.modelValue)
  });

  // 领取优惠劵
  const getBuy = (id) => {
    emits('get', id);
  };
</script>
<style lang="scss" scoped>
  .model-box {
    height: 60vh;
    .title {
      font-size: 36rpx;
      height: 80rpx;
      font-weight: bold;
      color: #333333;
    }
    .subtitle {
      font-size: 26rpx;
      font-weight: 500;
      color: #333333;
    }
  }
  .model-content {
    height: 54vh;
  }
  .modal-footer {
    width: 100%;
    height: 120rpx;
    background: #fff;
  }
  .confirm-btn {
    width: 710rpx;
    margin-left: 20rpx;
    height: 80rpx;
    background: linear-gradient(90deg, var(--ui-BG-Main), var(--ui-BG-Main-gradient));
    border-radius: 40rpx;
    color: #fff;
  }
  // 优惠券按钮
  .card-btn {
    // width: 144rpx;
    padding: 0 16rpx;
    height: 50rpx;
    border-radius: 40rpx;
    background: linear-gradient(90deg, var(--ui-BG-Main), var(--ui-BG-Main-gradient));
    color: #ffffff;
    font-size: 24rpx;
    font-weight: 400;
  }
  .boder-btn {
    background: linear-gradient(90deg, var(--ui-BG-Main-opacity-4), var(--ui-BG-Main-light));
    color: #fff !important;
  }
</style>
