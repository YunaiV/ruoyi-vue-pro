<template>
  <view class="container">
    <view class="unp-header">
      <view class="unp-logo">
        <u-avatar size="80" icon="github-circle-fill" fontSize="80"></u-avatar>
      </view>
    </view>

    <view class="unp-box">
      <u--form class="unp-form" labelPosition="left" :model="formData" :rules="rules" ref="form">
        <u-form-item label="账号" prop="username" borderBottom ref="item-username">
          <u-input type="text" maxlength="20" v-model="formData.username" clearable placeholder="账号由数字和字母组成" border="none" @change="handleUsernameChange"></u-input>
        </u-form-item>

        <u-gap height="20"></u-gap>

        <u-form-item label="验证码" prop="code" labelWidth="80" borderBottom>
          <u--input type="number" maxlength="6" v-model="formData.code" border="none" placeholder="请填写验证码"></u--input>
          <u-button slot="right" @tap="getCode" :text="tips" type="success" size="mini" :disabled="codeDisabled"></u-button>
          <u-code ref="uCode" @change="codeChange" seconds="60" @start="codeDisabled = true" @end="codeDisabled = false"></u-code>
        </u-form-item>

        <u-gap height="20"></u-gap>

        <u-form-item label="密码" prop="password" borderBottom ref="item-password">
          <u-input :type="inputType" maxlength="20" v-model="formData.password" placeholder="新密码由数字、字母和符号组成" border="none" @change="handlePasswordChange">
            <template slot="suffix">
              <u-icon v-if="inputType === 'password'" size="20" color="#666666" name="eye-fill" @click="inputType = 'text'"></u-icon>
              <u-icon v-if="inputType === 'text'" size="20" color="#666666" name="eye-off" @click="inputType = 'password'"></u-icon>
            </template>
          </u-input>
        </u-form-item>

        <view class="lk-group">
          <!-- 占位 -->
        </view>

        <u-button type="error" text="重置密码" customStyle="margin-top: 50px" @click="handleSubmit"></u-button>

        <u-gap height="20"></u-gap>
        <u-button type="info" text="返回" @click="navigateBack()"></u-button>
      </u--form>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      codeDisabled: false,
      tips: '',
      inputType: 'password',
      formData: {
        username: '',
        code: '',
        password: ''
      },
      rules: {
        username: {
          type: 'string',
          max: 20,
          required: true,
          message: '请输入您的账号',
          trigger: ['blur', 'change']
        },
        code: {
          type: 'number',
          max: 6,
          required: true,
          message: '请输入验证码',
          trigger: ['blur', 'change']
        },
        password: {
          type: 'string',
          max: 20,
          required: true,
          message: '请输入您的新密码',
          trigger: ['blur', 'change']
        }
      }
    }
  },
  onLoad() {},
  methods: {
    handleUsernameChange(e) {
      let str = uni.$u.trim(e, 'all')
      this.$nextTick(() => {
        this.formData.username = str
      })
    },
    handlePasswordChange(e) {
      let str = uni.$u.trim(e, 'all')
      this.$nextTick(() => {
        this.formData.password = str
      })
    },
    codeChange(text) {
      this.tips = text
    },
    getCode() {
      if (this.$refs.uCode.canGetCode) {
        // 模拟向后端请求验证码
        uni.showLoading({
          title: '正在获取验证码'
        })
        setTimeout(() => {
          uni.hideLoading()
          // 这里此提示会被this.start()方法中的提示覆盖
          uni.$u.toast('验证码已发送')
          // 通知验证码组件内部开始倒计时
          this.$refs.uCode.start()
        }, 2000)
      } else {
        uni.$u.toast('倒计时结束后再发送')
      }
    },
    handleSubmit() {
      this.$refs.form
        .validate()
        .then(res => {
          uni.$u.toast('点击了重置密码')
        })
    },
    navigateBack() {
      uni.navigateBack()
    }
  }
}
</script>

<style lang="scss" scoped>
.unp-header {
  height: 400rpx;
  @include flex-center;
  .unp-logo {
    @include flex-center;
  }
}

.unp-box {
  @include flex-center(column);
  .unp-form {
    width: 560rpx;
  }
}

.lk-group {
  height: 40rpx;
  margin-top: 40rpx;
  @include flex-space-between;
  font-size: 12rpx;

  color: $u-primary;
  text-decoration: $u-primary;
}
</style>
