<template>
  <view class="container">
    <view class="user-info">
      <view class="info-item">
        <view class="label">头像：</view>
        <view class="info" @click="handleAvatarClick">
          <u-avatar size="60" :src="userInfo.avatar"></u-avatar>
          <u-icon class="btn" name="arrow-right"></u-icon>
        </view>
      </view>
      <view class="info-item">
        <view class="label">昵称：</view>
        <view class="info">
          <view class="value">{{ userInfo.nickname }}</view>
          <u-icon class="btn" name="edit-pen"></u-icon>
        </view>
      </view>
      <view class="info-item">
        <view class="label">手机：</view>
        <view class="info">
          <view class="value">{{ userInfo.mobile }}</view>
          <u-icon class="btn" name="edit-pen"></u-icon>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { getUserInfo, updateAvatar } from '../../common/api'

export default {
  data() {
    return {
      userInfo: {
        nickname: '',
        avatar: '',
        mobile: ''
      },
      avatarFiles: []
    }
  },
  onLoad() {
    this.loadUserInfoData()
  },
  methods: {
    loadUserInfoData() {
      getUserInfo()
        .then(res => {
          this.userInfo = res.data
        })
        .catch(err => {
          //console.log(err)
        })
    },
    handleAvatarClick() {
      uni.chooseImage({
        success: chooseImageRes => {
          const tempFilePaths = chooseImageRes.tempFilePaths
          console.log(tempFilePaths)
          updateAvatar(tempFilePaths[0])
            .then(res => {
              console.log(res)
            })
            .catch(err => {
              //console.log(err)
            })
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.user-info {
  .info-item {
    padding: 20rpx 60rpx;
    border-bottom: $custom-border-style;
    @include flex-space-between;
    .label {
      font-size: 30rpx;
    }
    .info {
      @include flex-right;
      .value {
        font-size: 30rpx;
      }
      .btn {
        margin-left: 30rpx;
      }
    }
  }
}
</style>
