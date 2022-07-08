<template>
  <view class="container">
    <view class="auth-header">
      <view class="auth-logo">
        <u-avatar size="100" icon="github-circle-fill" fontSize="100"></u-avatar>
      </view>
    </view>

    <view class="auth-box">
      <view class="btn-group">
        <!-- #ifdef MP-WEIXIN -->
        <u-button class="auth-btn" open-type="getPhoneNumber" type="primary" @getphonenumber="getPhoneNumber">一键登录</u-button>
        <navigator class="reg-login-link" url="/pages/login/mobile" open-type="navigate" hover-class="none">手机号登录/注册 &gt;</navigator>
        <!-- #endif -->

        <!-- #ifndef MP-WEIXIN -->
        <u-button type="primary" text="手机号登录/注册" @click="handleJump"></u-button>
        <!-- #endif -->
      </view>
      <view class="auth-footer">
        <view>登录即表示同意<text class="lk-text">《用户协议》</text> 和 <text class="lk-text">《隐私政策》</text></view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {}
  },
  onLoad() {},
  onReady() {},
  methods: {
    getPhoneNumber(e) {
      let phoneCode = e.detail.code
      if (!e.detail.code) {
        uni.showModal({
          title: '授权失败',
          content: '您已拒绝获取绑定手机号登录授权，可以使用其他手机号验证登录',
          cancelText: '知道了',
          confirmText: '验证登录',
          confirmColor: '#3C9CFFFF',
          success: res => {
            if (res.confirm) {
              uni.$u.route('/pages/login/mobile')
            } else if (res.cancel) {
              //console.log('用户点击取消')
            }
          }
        })
      } else {
        uni.login({
          provider: 'weixin',
          success: res => {
            this.$store.dispatch('Login', { type: 2, data: { phoneCode: phoneCode, loginCode: res.code } }).then(res => {
              uni.$u.toast('登录成功')
              setTimeout(() => {
                uni.switchTab({
                  url: '/pages/user/user'
                })
              }, 300)
            })
          }
        })
      }
    },
    handleJump() {
      uni.$u.route('/pages/login/mobile')
    }
  }
}
</script>

<style lang="scss" scoped>
.container {
  height: calc(100vh - 70px);
  @include flex-space-between(column);
}

.auth-header {
  flex: 2;
  @include flex-center;
  .auth-logo {
    @include flex-center(column);
  }
}

.auth-box {
  @include flex-center(column);

  .btn-group {
    width: 600rpx;
    margin-bottom: 200rpx;
    .auth-btn {
      height: 90rpx;
      font-size: 32rpx;
    }
  }

  .reg-login-link {
    margin-top: 32rpx;
    text-align: center;
    color: #636363;
    font-size: 30rpx;
  }

  .auth-footer {
    font-size: 26rpx;
    color: #939393;
    .lk-text {
      color: $u-primary;
      text-decoration: $u-primary;
    }
  }
}
</style>
