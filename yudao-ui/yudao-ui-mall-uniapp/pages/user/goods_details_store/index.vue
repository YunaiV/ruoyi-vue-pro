<template>
  <s-layout :bgStyle="{ color: '#FFF' }" title="选择自提门店">
    <view class="storeBox" ref="container">
      <view
        class="storeBox-box"
        v-for="(item, index) in state.storeList"
        :key="index"
        @tap="checked(item)"
      >
        <view class="store-img">
          <image :src="item.logo" class="img" />
        </view>
        <view class="store-cent-left">
          <view class="store-name">{{ item.name }}</view>
          <view class="store-address line1">
            {{ item.areaName }}{{ ', ' + item.detailAddress }}
          </view>
        </view>
        <view class="row-right ss-flex-col ss-col-center">
          <view>
            <!-- #ifdef H5 -->
            <a class="store-phone" :href="'tel:' + item.phone">
              <view class="iconfont">
                <view class="ss-rest-button">
                  <text class="_icon-forward" />
                </view>
              </view>
            </a>
            <!-- #endif -->
            <!-- #ifdef MP -->
            <view class="store-phone" @click="call(item.phone)">
              <view class="iconfont">
                <view class="ss-rest-button">
                  <text class="_icon-forward" />
                </view>
              </view>
            </view>
            <!-- #endif -->
          </view>
          <view class="store-distance ss-flex ss-row-center" @tap.stop="showMaoLocation(item)">
            <text class="addressTxt" v-if="item.distance">
              距离{{ item.distance.toFixed(2) }}千米
            </text>
            <text class="addressTxt" v-else>查看地图</text>
            <view class="iconfont">
              <view class="ss-rest-button">
                <text class="_icon-forward" />
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>
  </s-layout>
</template>

<script setup>
  import DeliveryApi from '@/sheep/api/trade/delivery';
  import { onMounted, reactive } from 'vue';
  import { onLoad } from '@dcloudio/uni-app';
  import sheep from '@/sheep';

  const LONGITUDE = 'user_longitude';
  const LATITUDE = 'user_latitude';
  const state = reactive({
    loaded: false,
    loading: false,
    storeList: [],
    system_store: {},
    locationShow: false,
    user_latitude: 0,
    user_longitude: 0,
  });

  const call = (phone) => {
    uni.makePhoneCall({
      phoneNumber: phone,
    });
  };
  const selfLocation = () => {
    // #ifdef H5
    const jsWxSdk = sheep.$platform.useProvider('wechat').jsWxSdk;
    if (jsWxSdk.isWechat()) {
      jsWxSdk.getLocation((res) => {
        state.user_latitude = res.latitude;
        state.user_longitude = res.longitude;
        uni.setStorageSync(LATITUDE, res.latitude);
        uni.setStorageSync(LONGITUDE, res.longitude);
        getList();
      });
    } else {
      // #endif
      uni.getLocation({
        type: 'gcj02',
        success: (res) => {
          try {
            state.user_latitude = res.latitude;
            state.user_longitude = res.longitude;
            uni.setStorageSync(LATITUDE, res.latitude);
            uni.setStorageSync(LONGITUDE, res.longitude);
          } catch (e) {
            console.error(e);
          }
          getList();
        },
        complete: () => {
          getList();
        },
      });
      // #ifdef H5
    }
    // #endif
  };
  const showMaoLocation = (e) => {
    // #ifdef H5
    const jsWxSdk = sheep.$platform.useProvider('wechat').jsWxSdk;
    if (jsWxSdk.isWechat()) {
      jsWxSdk.openLocation({
        latitude: Number(e.latitude),
        longitude: Number(e.longitude),
        name: e.name,
        address: `${e.areaName}-${e.detailAddress}`,
      });
    } else {
      // #endif
      uni.openLocation({
        latitude: Number(e.latitude),
        longitude: Number(e.longitude),
        name: e.name,
        address: `${e.areaName}-${e.detailAddress}`,
        success: function () {
          console.log('success');
        },
      });
      // #ifdef H5
    }
    // #endif
  };

  /**
   * 选中门店
   */
  const checked = (addressInfo) => {
    uni.$emit('SELECT_PICK_UP_INFO', {
      addressInfo,
    });
    sheep.$router.back();
  };

  /**
   * 获取门店列表数据
   */
  const getList = async () => {
    if (state.loading || state.loaded) {
      return;
    }
    state.loading = true;
    const { data, code } = await DeliveryApi.getDeliveryPickUpStoreList({
      latitude: state.user_latitude,
      longitude: state.user_longitude,
    });
    if (code !== 0) {
      return;
    }
    state.loading = false;
    state.storeList = data;
  };

  onMounted(() => {
    if (state.user_latitude && state.user_longitude) {
      getList();
    } else {
      selfLocation();
      getList();
    }
  });
  onLoad(() => {
    try {
      state.user_latitude = uni.getStorageSync(LATITUDE);
      state.user_longitude = uni.getStorageSync(LONGITUDE);
    } catch (e) {
      console.error(e);
    }
  });
</script>
<style lang="scss" scoped>
  .line1 {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .geoPage {
    position: fixed;
    width: 100%;
    height: 100%;
    top: 0;
    z-index: 10000;
  }

  .storeBox {
    width: 100%;
    background-color: #fff;
    padding: 0 30rpx;
  }

  .storeBox-box {
    width: 100%;
    height: auto;
    display: flex;
    align-items: center;
    padding: 23rpx 0;
    justify-content: space-between;
    border-bottom: 1px solid #eee;
  }

  .store-cent {
    display: flex;
    align-items: center;
    width: 80%;
  }

  .store-cent-left {
    //width: 45%;
    flex: 2;
  }

  .store-img {
    flex: 1;
    width: 120rpx;
    height: 120rpx;
    border-radius: 6rpx;
    margin-right: 22rpx;
  }

  .store-img .img {
    width: 100%;
    height: 100%;
  }

  .store-name {
    color: #282828;
    font-size: 30rpx;
    margin-bottom: 22rpx;
    font-weight: 800;
  }

  .store-address {
    color: #666666;
    font-size: 24rpx;
  }

  .store-phone {
    width: 50rpx;
    height: 50rpx;
    color: #fff;
    border-radius: 50%;
    display: block;
    text-align: center;
    line-height: 48rpx;
    background-color: #e83323;
    margin-bottom: 22rpx;
    text-decoration: none;
  }

  .store-distance {
    font-size: 22rpx;
    color: #e83323;
  }

  .iconfont {
    font-size: 20rpx;
  }

  .row-right {
    flex: 2;
    //display: flex;
    //flex-direction: column;
    //align-items: flex-end;
    //width: 33.5%;
  }
</style>
