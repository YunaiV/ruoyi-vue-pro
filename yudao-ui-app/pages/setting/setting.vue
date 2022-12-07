<template>
  <view class="container">
    <u-gap height="20"></u-gap>
    <u-cell-group class="setting-list" :border="false">
      <u-cell class="setting-item" icon="lock" title="修改密码" isLink></u-cell>
      <u-cell class="setting-item" icon="phone" title="换绑手机" isLink></u-cell>
      <u-cell v-if="hasLogin" class="setting-item" icon="minus-circle" title="用户登出" @click="logout" isLink></u-cell>
    </u-cell-group>
  </view>
</template>

<script>
import UGap from '../../uni_modules/uview-ui/components/u-gap/u-gap'

export default {
  components: { UGap },
  data() {
    return {}
  },
  computed: {
    hasLogin() {
      return this.$store.getters.hasLogin
    }
  },
  onLoad() {},
  methods: {
    logout() {
      uni.showModal({
        title: '提示',
        content: '您确定要退出登录吗',
        success: res => {
          if (res.confirm) {
            this.$store.dispatch('Logout').then(res => {
              uni.switchTab({
                url: '/pages/user/user'
              })
            })
          } else if (res.cancel) {
            //console.log('用户点击取消')
          }
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.setting-list {
  padding: 10rpx 0;
  background-color: #fff;
  border-radius: 15rpx;

  .setting-item {
    padding: 10rpx 0;

    &:last-child {
      border-bottom: none;
    }
  }
}
</style>
