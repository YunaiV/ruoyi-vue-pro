<!-- 下单界面，收货地址 or 自提门店的选择组件 -->
<template>
  <view class="allAddress" :style="state.isPickUp ? '' : 'padding-top:10rpx;'">
    <view class="nav flex flex-wrap">
      <view
        class="item font-color"
        :class="state.deliveryType === 1 ? 'on' : 'on2'"
        @tap="switchDeliveryType(1)"
        v-if="state.isPickUp"
      />
      <view
        class="item font-color"
        :class="state.deliveryType === 2 ? 'on' : 'on2'"
        @tap="switchDeliveryType(2)"
        v-if="state.isPickUp"
      />
    </view>
    <!-- 情况一：收货地址的选择 -->
    <view
      class="address flex flex-wrap flex-center ss-row-between"
      @tap="onSelectAddress"
      v-if="state.deliveryType === 1"
      :style="state.isPickUp ? '' : 'border-top-left-radius: 14rpx;border-top-right-radius: 14rpx;'"
    >
      <view class="addressCon" v-if="state.addressInfo.name">
        <view class="name"
          >{{ state.addressInfo.name }}
          <text class="phone">{{ state.addressInfo.mobile }}</text>
        </view>
        <view class="flex flex-wrap">
          <text class="default font-color" v-if="state.addressInfo.defaultStatus">[默认]</text>
          <text class="line2">
            {{ state.addressInfo.areaName }} {{ state.addressInfo.detailAddress }}
          </text>
        </view>
      </view>
      <view class="addressCon" v-else>
        <view class="setaddress">设置收货地址</view>
      </view>
      <view class="iconfont">
        <view class="ss-rest-button">
          <text class="_icon-forward" />
        </view>
      </view>
    </view>
    <!-- 情况二：门店的选择 -->
    <view
      class="address flex flex-wrap flex-center ss-row-between"
      v-if="state.deliveryType === 2"
      @tap="onSelectAddress"
    >
      <view class="addressCon" v-if="state.pickUpInfo.name">
        <view class="name"
          >{{ state.pickUpInfo.name }}
          <text class="phone">{{ state.pickUpInfo.phone }}</text>
        </view>
        <view class="line1">
          {{ state.pickUpInfo.areaName }}{{ ', ' + state.pickUpInfo.detailAddress }}
        </view>
      </view>
      <view class="addressCon" v-else>
        <view class="setaddress">选择自提门店</view>
      </view>
      <view class="iconfont">
        <view class="ss-rest-button">
          <text class="_icon-forward" />
        </view>
      </view>
    </view>
    <view class="line">
      <image :src="sheep.$url.static('/static/img/shop/line.png')" />
    </view>
  </view>
</template>

<script setup>
  import { computed } from 'vue';
  import sheep from '@/sheep';
  import { isEmpty } from 'lodash-es';

  const props = defineProps({
    modelValue: {
      type: Object,
      default() {},
    },
  });
  const emits = defineEmits(['update:modelValue']);

  // computed 解决父子组件双向数据同步
  const state = computed({
    get() {
      return new Proxy(props.modelValue, {
        set(obj, name, val) {
          emits('update:modelValue', {
            ...obj,
            [name]: val,
          });
          return true;
        },
      });
    },
    set(val) {
      emits('update:modelValue', val);
    },
  });

  // 选择地址
  function onSelectAddress() {
    let emitName = 'SELECT_ADDRESS';
    let addressPage = '/pages/user/address/list?type=select';
    if (state.value.deliveryType === 2) {
      emitName = 'SELECT_PICK_UP_INFO';
      addressPage = '/pages/user/goods_details_store/index';
    }
    uni.$once(emitName, (e) => {
      changeConsignee(e.addressInfo);
    });
    sheep.$router.go(addressPage);
  }

  // 更改收货人地址&计算订单信息
  async function changeConsignee(addressInfo = {}) {
    if (!isEmpty(addressInfo)) {
      if (state.value.deliveryType === 1) {
        state.value.addressInfo = addressInfo;
      }
      if (state.value.deliveryType === 2) {
        state.value.pickUpInfo = addressInfo;
      }
    }
  }

  // 收货方式切换
  const switchDeliveryType = (type) => {
    state.value.deliveryType = type;
  };
</script>

<style scoped lang="scss">
  .allAddress .font-color {
    color: #e93323 !important;
  }
  .line2 {
    width: 504rpx;
  }
  .textR {
    text-align: right;
  }

  .line {
    width: 100%;
    height: 3rpx;
  }

  .line image {
    width: 100%;
    height: 100%;
    display: block;
  }

  .address {
    padding: 28rpx;
    background-color: #fff;
    box-sizing: border-box;
  }

  .address .addressCon {
    width: 596rpx;
    font-size: 26rpx;
    color: #666;
  }

  .address .addressCon .name {
    font-size: 30rpx;
    color: #282828;
    font-weight: bold;
    margin-bottom: 10rpx;
  }

  .address .addressCon .name .phone {
    margin-left: 50rpx;
  }

  .address .addressCon .default {
    margin-right: 12rpx;
  }

  .address .addressCon .setaddress {
    color: #333;
    font-size: 28rpx;
  }

  .address .iconfont {
    font-size: 35rpx;
    color: #707070;
  }

  .allAddress {
    width: 100%;
    background: linear-gradient(to bottom, #e93323 0%, #f5f5f5 100%);
    // background-image: linear-gradient(to bottom, #e93323 0%, #f5f5f5 100%);
    // background-image: -webkit-linear-gradient(to bottom, #e93323 0%, #f5f5f5 100%);
    // background-image: -moz-linear-gradient(to bottom, #e93323 0%, #f5f5f5 100%);
    //padding: 100rpx 30rpx 0 30rpx;
    padding-top: 100rpx;
    padding-bottom: 10rpx;
  }

  .allAddress .nav {
    width: 690rpx;
    margin: 0 auto;
  }

  .allAddress .nav .item {
    width: 334rpx;
  }

  .allAddress .nav .item.on {
    position: relative;
    width: 230rpx;
  }

  .allAddress .nav .item.on::before {
    position: absolute;
    bottom: 0;
    content: '快递配送';
    font-size: 28rpx;
    display: block;
    height: 0;
    width: 336rpx;
    border-width: 0 20rpx 80rpx 0;
    border-style: none solid solid;
    border-color: transparent transparent #fff;
    z-index: 2;
    border-radius: 14rpx 36rpx 0 0;
    text-align: center;
    line-height: 80rpx;
  }

  .allAddress .nav .item:nth-of-type(2).on::before {
    content: '到店自提';
    border-width: 0 0 80rpx 20rpx;
    border-radius: 36rpx 14rpx 0 0;
  }

  .allAddress .nav .item.on2 {
    position: relative;
  }

  .allAddress .nav .item.on2::before {
    position: absolute;
    bottom: 0;
    content: '到店自提';
    font-size: 28rpx;
    display: block;
    height: 0;
    width: 401rpx;
    border-width: 0 0 60rpx 60rpx;
    border-style: none solid solid;
    border-color: transparent transparent #f7c1bd;
    border-radius: 36rpx 14rpx 0 0;
    text-align: center;
    line-height: 60rpx;
  }

  .allAddress .nav .item:nth-of-type(1).on2::before {
    content: '快递配送';
    border-width: 0 60rpx 60rpx 0;
    border-radius: 14rpx 36rpx 0 0;
  }

  .allAddress .address {
    width: 690rpx;
    max-height: 180rpx;
    margin: 0 auto;
  }

  .allAddress .line {
    width: 100%;
    margin: 0 auto;
  }
</style>
