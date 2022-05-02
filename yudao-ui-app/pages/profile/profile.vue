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
        <view v-if="!nameEditOn" class="info">
          <view class="value">{{ userInfo.nickname }}</view>
          <u-icon
            class="btn"
            name="edit-pen"
            @click="
              tempName = userInfo.nickname
              nameEditOn = true
            "
          ></u-icon>
        </view>
        <view v-else class="name-edit">
          <u--input maxlength="10" border="bottom" v-model="tempName"></u--input>
          <view class="edit-btn-group">
            <u-tag class="edit-btn" text="保存" plain size="mini" type="primary" @click="handleSaveBtnClick"></u-tag>
            <u-tag
              class="edit-btn"
              text="取消"
              plain
              size="mini"
              type="info"
              @click="
                tempName = ''
                nameEditOn = false
              "
            ></u-tag>
          </view>
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
import { getUserInfo, updateAvatar, updateNickname } from '../../api/user'

export default {
  data() {
    return {
      userInfo: {
        nickname: '',
        avatar: '',
        mobile: ''
      },
      avatarFiles: [],
      nameEditOn: false,
      tempName: ''
    }
  },
  onLoad() {
    this.loadUserInfoData()
  },
  methods: {
    loadUserInfoData() {
      getUserInfo().then(res => {
        this.userInfo = res.data
      })
    },
    handleAvatarClick() {
      uni.chooseImage({
        success: chooseImageRes => {
          const tempFilePaths = chooseImageRes.tempFilePaths
          updateAvatar(tempFilePaths[0]).then(res => {
            this.userInfo.avatar = res.data
            this.$store.commit('SET_USER_INFO', this.userInfo)
          })
        }
      })
    },
    handleSaveBtnClick() {
      updateNickname({ nickname: this.tempName }).then(res => {
        this.nameEditOn = false;
        this.userInfo.nickname = this.tempName
        this.$store.commit('SET_USER_INFO', this.userInfo)
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
    .name-edit {
      @include flex-left;
      .edit-btn-group {
        @include flex;
        .edit-btn {
          margin-left: 20rpx;
        }
      }
    }
  }
}
</style>
