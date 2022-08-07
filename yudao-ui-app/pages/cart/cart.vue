<template>
  <view class="container">
    <view class="cart-empty">
      <u-empty mode="car" width="500rpx" height="500rpx" icon="/static/images/empty/cart.png"></u-empty>
    </view>

    <view v-if="!hasLogin" class="login-tips-box">
      <view class="login-tips">
        <navigator url="/pages/login/social" open-type="navigate" hover-class="none">
          <text class="login-link">登录</text>
        </navigator>
        <text>查看购物车</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      title: ''
    }
  },
  onLoad() {},
  methods: {},
  computed: {
    hasLogin() {
      return this.$store.getters.hasLogin
    }
  }
}
</script>

<style lang="scss" scoped>
.cart-empty {
  padding-top: 100rpx;
}

.login-tips-box {
  padding-top: 100rpx;
  .login-tips {
    @include flex-center;
    color: #939393;
    font-size: 28rpx;
    letter-spacing: 5rpx;
  }
  .login-link {
    color: $u-primary;
  }
}
</style>
