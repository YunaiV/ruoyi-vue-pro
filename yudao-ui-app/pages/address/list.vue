<template>
  <view class="container">
    <view class="address-list" v-for="(item, index) in addressList" :key="item.id">
      <view class="address-item" @click="handleClick" @longpress="handleLongPress(item)">
        <view class="left">
          <u-avatar :text="item.name ? item.name.slice(0, 1) : 'U'" fontSize="18" randomBgColor></u-avatar>
        </view>
        <view class="middle">
          <view class="info">
            <view class="name">{{ item.name }}</view>
            <view class="mobile">{{ item.mobile }}</view>
            <u-tag class="type" v-if="item.type === 1" text="默认" plain size="mini" type="success"></u-tag>
          </view>
          <view class="detail">
            <u--text :lines="2" size="14" color="#939393" :text="item.detailAddress"></u--text>
          </view>
        </view>
        <navigator class="right" :url="`/pages/address/update?addressId=${item.id}`" open-type="navigate" hover-class="none">
          <u-icon name="edit-pen" size="28"></u-icon>
        </navigator>
      </view>
    </view>

    <navigator class="fixed-btn-box" url="/pages/address/create" open-type="navigate" hover-class="none">
      <u-button type="primary" size="large" text="新增地址"></u-button>
    </navigator>
    <u-safe-bottom customStyle="background: #ffffff"></u-safe-bottom>
  </view>
</template>

<script>
import { getAddressList, deleteAddress } from '../../api/address'

export default {
  data() {
    return {
      addressList: []
    }
  },
  onLoad() {},
  onShow() {
    this.loadAddressListData()
  },
  methods: {
    loadAddressListData() {
      getAddressList().then(res => {
        this.addressList = res.data
      })
    },
    handleLongPress(item) {
      uni.showModal({
        title: '提示',
        content: `删除收件人[${item.name}${item.mobile}]\n地址：${item.detailAddress.slice(0, 5)}......${item.detailAddress.slice(-6)}？`,
        success: res => {
          if (res.confirm) {
            deleteAddress({ id: item.id }).then(res => {
              uni.$u.toast('地址已删除')
              this.loadAddressListData();
            })
          } else if (res.cancel) {
            //console.log('用户点击取消')
          }
        }
      })
    },
    handleClick(){
      // TODO 提交订单时选择地址逻辑
    }
  }
}
</script>

<style lang="scss" scoped>
.address-list {
  .address-item {
    padding: 10rpx 0;
    border-bottom: $custom-border-style;
    @include flex-space-between;
    .left {
      margin: 20rpx;
    }
    .middle {
      flex: 1;
      margin: 20rpx;
      @include flex(column);
      .info {
        @include flex-left;
        .name {
          font-size: 30rpx;
          font-weight: 700;
        }
        .mobile {
          font-size: 28rpx;
          margin-left: 15rpx;
        }
        .type {
          margin-left: 15rpx;
        }
      }
      .detail {
        margin-top: 10rpx;
      }
    }
    .right {
      margin: 20rpx;
    }
  }
}

.fixed-btn-box {
  position: fixed;
  bottom: 0;
  left: 0;
  width: 750rpx;
}
</style>
