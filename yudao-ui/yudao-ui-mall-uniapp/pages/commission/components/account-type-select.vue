<!-- 提现方式的 select 组件 -->
<template>
  <su-popup :show="show" class="ss-checkout-counter-wrap" @close="hideModal">
    <view class="ss-modal-box bg-white ss-flex-col">
      <view class="modal-header ss-flex-col ss-col-left">
        <text class="modal-title ss-m-b-20">选择提现方式</text>
      </view>
      <view class="modal-content ss-flex-1 ss-p-b-100">
        <radio-group @change="onChange">
          <label
            class="container-list ss-p-l-34 ss-p-r-24 ss-flex ss-col-center ss-row-center"
            v-for="(item, index) in typeList"
            :key="index"
          >
            <view class="container-icon ss-flex ss-m-r-20">
              <image :src="sheep.$url.static(item.icon)" />
            </view>
            <view class="ss-flex-1">{{ item.title }}</view>
            <radio
              :value="item.value"
              color="var(--ui-BG-Main)"
              :checked="item.value === state.currentValue"
              :disabled="!methods.includes(parseInt(item.value))"
            />
          </label>
        </radio-group>
      </view>
      <view class="modal-footer ss-flex ss-row-center ss-col-center">
        <button class="ss-reset-button save-btn" @tap="onConfirm">确定</button>
      </view>
    </view>
  </su-popup>
</template>

<script setup>
  import { reactive } from 'vue';
  import sheep from '@/sheep';

  const props = defineProps({
    modelValue: {
      type: Object,
      default() {},
    },
    show: {
      type: Boolean,
      default: false,
    },
    methods: {
      // 开启的提现方式
      type: Array,
      default: [],
    },
  });
  const emits = defineEmits(['update:modelValue', 'change', 'close']);
  const state = reactive({
    currentValue: '',
  });

  const typeList = [
    {
      icon: '/static/img/shop/pay/wallet.png',
      title: '钱包余额',
      value: '1',
    },
    {
      icon: '/static/img/shop/pay/bank.png',
      title: '银行卡转账',
      value: '2',
    },
    {
      icon: '/static/img/shop/pay/wechat.png',
      title: '微信收款码', // 微信手动转账
      value: '3',
    },
    {
      icon: '/static/img/shop/pay/alipay.png',
      title: '支付宝收款码', // 支付宝手动转账
      value: '4',
    },
    {
      icon: '/static/img/shop/pay/wechat_api.png',
      title: '微信零钱', // 微信 API 转账
      value: '5',
    },
    {
      icon: '/static/img/shop/pay/alipay_api.png',
      title: '支付宝余额', // 支付宝 API 转账
      value: '6',
    },
  ];

  function onChange(e) {
    state.currentValue = e.detail.value;
  }

  const onConfirm = async () => {
    if (state.currentValue === '') {
      sheep.$helper.toast('请选择提现方式');
      return;
    }
    // 赋值
    emits('update:modelValue', {
      type: state.currentValue,
    });
    // 关闭弹窗
    emits('close');
  };

  const hideModal = () => {
    emits('close');
  };
</script>

<style lang="scss" scoped>
  .ss-modal-box {
    border-radius: 30rpx 30rpx 0 0;
    max-height: 1000rpx;

    .modal-header {
      position: relative;
      padding: 60rpx 40rpx 40rpx;

      .modal-title {
        font-size: 32rpx;
        font-weight: bold;
      }

      .close-icon {
        position: absolute;
        top: 10rpx;
        right: 20rpx;
        font-size: 46rpx;
        opacity: 0.2;
      }
    }

    .modal-content {
      overflow-y: auto;

      .container-list {
        height: 96rpx;
        border-bottom: 2rpx solid rgba(#dfdfdf, 0.5);
        font-size: 28rpx;
        font-weight: 500;
        color: #333333;

        .container-icon {
          width: 36rpx;
          height: 36rpx;
        }
      }
    }

    .modal-footer {
      height: 120rpx;

      .save-btn {
        width: 710rpx;
        height: 80rpx;
        border-radius: 40rpx;
        background: linear-gradient(90deg, var(--ui-BG-Main), var(--ui-BG-Main-gradient));
        color: $white;
      }
    }
  }

  image {
    width: 100%;
    height: 100%;
  }
</style>
