<template>
  <view class="container">
    <view class="auth-header">
      <view class="auth-logo">
        <u-avatar size="100" icon="github-circle-fill" fontSize="100"></u-avatar>
      </view>
    </view>

    <view class="auth-box">
      <!-- 登录方式选择 -->
      <view class="mode-section">
        <u-subsection class="subsection" mode="subsection" fontSize="15" :list="loginModeList" :current="currentModeIndex" @change="handleModeChange"></u-subsection>
      </view>
      <u-gap height="40"></u-gap>

      <!-- 登录表单 -->
      <u--form labelPosition="left" :model="formData" :rules="rules" ref="form">
        <u-form-item label="手机号" prop="mobile" labelWidth="60" borderBottom ref="item-mobile">
          <u-input type="number" maxlength="11" v-model="formData.mobile" clearable placeholder="请填写手机号" border="none"></u-input>
        </u-form-item>

        <u-gap height="20"></u-gap>

        <u-form-item v-if="currentModeIndex === 0" label="密码" prop="password" labelWidth="60" borderBottom ref="item-password">
          <u-input :type="inputType" maxlength="16" v-model="formData.password" placeholder="请填写密码" border="none">
            <template slot="suffix">
              <u-icon v-if="inputType === 'password'" size="20" color="#666666" name="eye-fill" @click="inputType = 'text'"></u-icon>
              <u-icon v-if="inputType === 'text'" size="20" color="#666666" name="eye-off" @click="inputType = 'password'"></u-icon>
            </template>
          </u-input>
        </u-form-item>

        <u-form-item v-else label="验证码" prop="code" labelWidth="60" borderBottom>
          <u--input type="number" maxlength="4" v-model="formData.code" border="none" placeholder="请填写验证码"></u--input>
          <u-button slot="right" @tap="getCode" :text="codeTips" type="success" size="mini" :disabled="codeDisabled"></u-button>
          <u-code ref="uCode" @change="codeChange" seconds="60" @start="codeDisabled = true" @end="codeDisabled = false"></u-code>
        </u-form-item>

        <view class="btn-group">
          <u-button class="auth-btn" type="primary" customStyle="margin-top: 50px" @click="handleSubmit">立即登录</u-button>
        </view>
      </u--form>
    </view>
  </view>
</template>

<script>
import { sendSmsCode } from '../../api/auth'

export default {
  data() {
    return {
      currentModeIndex: 0,
      loginModeList: ['密码登录', '验证码登录'],
      inputType: 'password',
      codeDisabled: false,
      codeTips: '',
      formData: {
        mobile: '',
        password: '',
        code: ''
      },
      rules: {
        mobile: [
          {
            type: 'integer',
            required: true,
            message: '请填写手机号',
            trigger: ['blur', 'change']
          },
          {
            // 自定义验证函数，见上说明
            validator: (rule, value, callback) => {
              // 上面有说，返回true表示校验通过，返回false表示不通过
              // uni.$u.test.mobile()就是返回true或者false的
              return uni.$u.test.mobile(value)
            },
            message: '手机号码不正确',
            // 触发器可以同时用blur和change
            trigger: ['change', 'blur']
          }
        ],
        password: {
          type: 'string',
          min: 4,
          max: 16,
          required: true,
          message: '密码长度4-16位密码',
          trigger: ['blur', 'change']
        },
        code: {
          type: 'integer',
          len: 4,
          required: true,
          message: '请填写4位验证码',
          trigger: ['blur', 'change']
        }
      }
    }
  },
  onLoad() {},
  onReady() {
    // 如果需要兼容微信小程序，并且校验规则中含有方法等，只能通过setRules方法设置规则
    this.$refs.form.setRules(this.rules)
  },
  methods: {
    handleModeChange(index) {
      if (index !== this.currentModeIndex) {
        this.currentModeIndex = index
        this.$refs.form.clearValidate()
      }
    },
    codeChange(text) {
      this.codeTips = text
    },
    getCode() {
      const mobile = this.formData.mobile
      if (!mobile) {
        uni.$u.toast('请填写手机号')
      } else if (!uni.$u.test.mobile(mobile)) {
        uni.$u.toast('手机号格式不正确')
      } else if (this.$refs.uCode.canGetCode) {
        // 模拟向后端请求验证码
        uni.showLoading({
          title: '正在获取验证码'
        })

        //scene:1登陆获取验证码场景
        sendSmsCode({ mobile: mobile, scene: 1 }).then(res => {
          //console.log(res)
          uni.hideLoading()
          uni.$u.toast('验证码已发送')
          // 通知验证码组件内部开始倒计时
          this.$refs.uCode.start()
        })
      } else {
        uni.$u.toast('倒计时结束后再发送')
      }
    },
    handleSubmit() {
      this.$refs.form.validate().then(res => {
        uni.login({
          provider: 'weixin',
          success: res => {
            let data = this.formData
            data.socialType = 34 //WECHAT_MINI_APP 先指定固定值
            data.socialCode = res.code
            data.socialState = Math.random() // 该参数没有实际意义暂时传随机数
            this.mobileLogin(data)
          },
          fail: res => {
            this.mobileLogin(this.formData)
          }
        })
      })
    },
    mobileLogin(data){
      this.$store.dispatch('Login', { type: this.currentModeIndex, data: data }).then(res => {
        uni.$u.toast('登录成功')
        setTimeout(() => {
          uni.switchTab({
            url: '/pages/user/user'
          })
        }, 300)
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.auth-header {
  height: 400rpx;
  @include flex-center;
  .auth-logo {
    @include flex-center(column);
  }
}

.auth-box {
  @include flex-center(column);

  .mode-section {
    width: 600rpx;
    .subsection {
      height: 60rpx;
    }
  }
  .btn-group {
    width: 600rpx;
    .auth-btn {
      height: 90rpx;
      font-size: 32rpx;
    }
  }
}
</style>
