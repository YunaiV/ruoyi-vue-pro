<template>
	<view class="container">

    <view class="unp-header">
      <view class="unp-logo">
        <u-avatar size="80" icon="github-circle-fill" fontSize="80"></u-avatar>
      </view>
    </view>

    <view class="unp-box">
      <!-- 登录方式选择 -->
      <view class="mode-section">
        <u-subsection mode="subsection" fontSize="15" :list="loginModeList" :current="currentModeIndex" @change="handleModeChange"></u-subsection>
      </view>
      <u-gap height="40"></u-gap>

      <!-- 登录表单 -->
      <u--form class="unp-form" labelPosition="left" :model="formData" :rules="rules" ref="form">
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

        <u-button type="primary" text="登录" customStyle="margin-top: 50px" @click="handleSubmit"></u-button>

        <u-gap height="20"></u-gap>
        <u-button type="info" text="返回" @click="navigateBack()"></u-button>

      </u--form>

    </view>

	</view>
</template>

<script>
import {passwordLogin,sendSmsCode,smsLogin} from "../../common/api";

  export default {
		data() {
			return {
        //租户ID
        agent: 1,
        currentModeIndex: 0,
        loginModeList: ['密码登录', '验证码登录'],
        inputType: 'password',
        codeDisabled: false,
        codeTips: '',
        formData: {
          mobile: '15601691234',
          password: '',
          code: ''
        },
        rules: {
          'mobile': [
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
                return uni.$u.test.mobile(value);
              },
              message: '手机号码不正确',
              // 触发器可以同时用blur和change
              trigger: ['change','blur'],
            }
          ],
          'password': {
            type: 'string',
            min: 4,
            max: 16,
            required: true,
            message: '密码长度4-16位密码',
            trigger: ['blur', 'change']
          },
          'code': {
            type: 'integer',
            len: 4,
            required: true,
            message: '请填写4位验证码',
            trigger: ['blur', 'change']
          }
        }
			}
		},
		onLoad() {

		},onReady(){
      // 如果需要兼容微信小程序，并且校验规则中含有方法等，只能通过setRules方法设置规则
      this.$refs.form.setRules(this.rules)
    },
		methods: {
      handleModeChange(index) {
        if (index !== this.currentModeIndex){
          this.currentModeIndex = index;
          this.$refs.form.clearValidate();
        }
      },
      codeChange(text) {
        this.codeTips = text;
      },
      getCode() {
        const mobile = this.formData.mobile;
        if (!mobile) {
          uni.$u.toast('请填写手机号');
        } else if (!uni.$u.test.mobile(mobile)){
          uni.$u.toast('手机号格式不正确');
        } else if (this.$refs.uCode.canGetCode) {
          // 模拟向后端请求验证码
          uni.showLoading({
            title: '正在获取验证码'
          });

          //scene:1登陆获取验证码场景
          sendSmsCode({agent: 1, mobile:mobile, scene:1 }).then(res => {
            //console.log(res)
            uni.hideLoading();
            if (res.data.code === 0){
              // 这里此提示会被this.start()方法中的提示覆盖
              uni.$u.toast('验证码已发送');
              // 通知验证码组件内部开始倒计时
              this.$refs.uCode.start();
            } else {
              uni.$u.toast(res.data.msg);
            }
          }).catch(err => {
            uni.$u.toast('服务器接口请求异常');
          })
        } else {
          uni.$u.toast('倒计时结束后再发送');
        }
      },
      handleSubmit() {
        this.$refs.form.validate().then(res => {
          uni.$u.toast('登录');
          if (this.currentModeIndex === 0){
            passwordLogin({agent: 1, mobile:this.formData.mobile, password:this.formData.password}).then(res => {
              if (res.data.code === 0){
                uni.$u.toast('登录成功');
                // TODO 登录成功，保存toke
              } else {
                uni.$u.toast(res.data.msg);
                // TODO 登录失败
                }
            }).catch(err => {
              uni.$u.toast('服务器接口请求异常');
            })
          } else if (this.currentModeIndex === 1) {
            smsLogin({agent: 1, mobile:this.formData.mobile, code:this.formData.code})
          }
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
  display: flex;
  align-items: center;
  justify-content: center;
  .unp-logo {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
  }
}


.unp-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  .mode-section{
    width: 560rpx;
  }
  .unp-form{
    width: 560rpx;
  }
}

.lk-group {
  height: 40rpx;
  margin-top: 40rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12rpx;

  color: $u-primary;
  text-decoration: $u-primary;
}


</style>
