<template>
	<view class="app">
		<!-- 左上角的 x 关闭 -->
		<view class="back-btn mix-icon icon-guanbi" @click="navBack"></view>
				
		<!-- 用户协议 -->
		<view class="agreement center">
			<text class="mix-icon icon-xuanzhong" :class="{active: agreement}" @click="checkAgreement"></text>
			<text @click="checkAgreement">请认真阅读并同意</text>
			<text class="title" @click="navToAgreementDetail(1)">《用户服务协议》</text>
			<text class="title" @click="navToAgreementDetail(2)">《隐私权政策》</text>
		</view>
		<!-- 登录表单 -->
		<view class="wrapper">
			<view class="left-top-sign">LOGIN</view>
			<view class="welcome">手机登录/注册</view>
			<!-- 手机验证码登录 -->
			<view class="input-content">
				<u--form labelPosition="left" :model="form" :rules="rules" ref="form" errorType="toast">
					<u-form-item prop="mobile" borderBottom>
						<u--input type="number" v-model="form.mobile" placeholder="请输入手机号" border="none"></u--input>
					</u-form-item>
					<!-- 判断使用验证码还是密码 -->
					<u-form-item prop="code" borderBottom v-if="loginType == 'code'">
						<u--input type="number" v-model="form.code" placeholder="请输入验证码" border="none"></u--input>
						<u-button slot="right" @tap="getCode" :text="uCode.tips" type="success" size="mini" :disabled="uCode.disabled"></u-button>
						<u-code ref="uCode" @change="codeChange" seconds="60" @start="uCode.disabled === true" @end="uCode.disabled === false"></u-code>
					</u-form-item>
					<u-form-item prop="password" borderBottom v-else>
						<u--input password v-model="form.password" placeholder="请输入密码" border="none"></u--input>
					</u-form-item>
				</u--form>
				
				<u-button class="login-button" text="立即登录" type="error" shape="circle" @click="mobileLogin"
					:loading="loading"></u-button>
				<!-- 切换登陆 -->
				<view class="login-type" v-if="loginType == 'code'" @click="setLoginType('password')">账号密码登录</view>
				<view class="login-type" v-else @click="setLoginType('code')">免密登录</view>
			</view>
			
			<!-- 快捷登录 -->
			<!-- #ifdef APP-PLUS || MP-WEIXIN -->
			<view class="other-wrapper">
				<view class="line center">
					<text class="title">快捷登录</text>
				</view>
				<view class="list row">
					<!-- #ifdef MP-WEIXIN -->
					<view class="item column center" @click="mpWxGetUserInfo">
						<image class="icon" src="/static/icon/login-wx.png"></image>
					</view>
					<!-- #endif -->
					
					<!-- #ifdef APP-PLUS -->
					<view class="item column center" style="width: 180rpx;" @click="loginByWxApp">
						<image class="icon" src="/static/icon/login-wx.png"></image>
						<text>微信登录</text>
					</view>
					<!-- #endif -->
				</view>
			</view>
			<!-- #endif -->
		</view>
	</view>
</template>

<script>
	import { checkStr } from '@/common/js/util'
	import { login, smsLogin, sendSmsCode } from '@/api/system/auth.js'
	import loginMpWx from './mixin/login-mp-wx.js'
	import loginAppWx from './mixin/login-app-wx.js'

	export default{
		mixins: [loginMpWx, loginAppWx],
		data() {
			return {
				agreement: true,
				loginType: 'code', // 登录方式，code 验证码；password 密码
				loading: false, // 表单提交
				rules: {
					mobile: [{
						required: true, 
						message: '请输入手机号'
					}, {
						validator: (rule, value, callback) => {
							return uni.$u.test.mobile(value);
						},
						message: '手机号码不正确'
					}],
					code: [],
					password: []
				},
				form: {
					mobile: '',
					code: '',
					password: '',
				},
				uCode: {
					tips: '',
					disable: false,
				}
			}
		},
		onLoad() {
			this.setLoginType(this.loginType);
		},
		methods: {
			// 手机号登录
			mobileLogin() {
				if (!this.agreement) {
					this.$util.msg('请阅读并同意用户服务及隐私协议');
					return;
				}
				this.$refs.form.validate().then(() => {
					this.loading = true;
					// 执行登陆
					const { mobile, code, password} = this.form;
					const loginPromise = this.loginType == 'password' ? login(mobile, password) :
						smsLogin(mobile, code);
					loginPromise.then(data => {
						// 登陆成功
						this.loginSuccessCallBack(data);
					}).catch(errors => {
					}).finally(() => {
						this.loading = false;
					})
				}).catch(errors => {
				});
			},
			// 登陆成功的处理逻辑
			loginSuccessCallBack(data) {
				this.$util.msg('登录成功');
				this.$store.commit('setToken', data);
				// TODO 芋艿：如果当前页是第一页，则无法返回。期望是能够回到首页
				setTimeout(()=>{
					uni.navigateBack();
				}, 1000)
			},
			navBack() {
				uni.navigateBack({
					delta: 1
				});
			},
			setLoginType(loginType) {
				this.loginType = loginType;
				// 修改校验规则
				this.rules.code = [];
				this.rules.password = [];
				if (loginType == 'code') {
					this.rules.code = [{
						required: true,
						message: '请输入验证码'
					}, {
						min: 4,
						max: 6,
						message: '验证码不正确'
					}];
				} else {
					this.rules.password = [{
						required: true,
						message: '请输入密码'
					}, {
						min: 4,
						max: 16,
						message: '密码不正确'
					}]
				}
			},
			//同意协议
			checkAgreement(){
				this.agreement = !this.agreement;
			},
			//打开协议
			navToAgreementDetail(type){
				this.navTo('/pages/public/article?param=' + JSON.stringify({
					module: 'article',
					operation: 'getAgreement',
					data: {
						type
					}
				}))
			},
			codeChange(text) {
				this.uCode.tips = text;
			},
			getCode() {
				// 处于发送中，则跳过
				if (!this.$refs.uCode.canGetCode) {
					return;
				}
				// 校验手机号
				this.$refs.form.validateField('mobile', errors => {
					if (errors.length > 0) {
						uni.$u.toast(errors[0].message);
						return;
					}
					// 发送验证码 TODO 芋艿，枚举类
					sendSmsCode(this.form.mobile, 1).then(data => {
						uni.$u.toast('验证码已发送');
						// 通知验证码组件内部开始倒计时
						this.$refs.uCode.start();
					}).catch(erros => {
					})
				})
			},
		}
	}
</script>

<style>
	page {
		background: #fff;
	}
</style>
<style scoped lang='scss'>
	.app {
		padding-top: 15vh;
		position:relative;
		width: 100vw;
		height: 100vh;
		overflow: hidden;
		background: #fff;
	}
	.wrapper {
		position:relative;
		z-index: 90;
		padding-bottom: 40rpx;
		.welcome {
			position:relative;
			left: 50rpx;
			top: -90rpx;
			font-size: 46rpx;
			color: #555;
			text-shadow: 1px 0px 1px rgba(0,0,0,.3);
		}
	}
	.back-btn {
		position:absolute;
		left: 20rpx;
		top: calc(var(--status-bar-height) + 20rpx);
		z-index: 90;
		padding: 20rpx;
		font-size: 32rpx;
		color: #606266;
	}
	.left-top-sign {
		font-size: 120rpx;
		color: #f8f8f8;
		position: relative;
		left: -12rpx;
	}
	
	/** 手机登录部分 */
	.input-content {
		padding: 0 60rpx;
		.login-button {
			margin-top: 30rpx;
		}
		.login-type {
			display: flex;
			justify-content: flex-end;
			font-size: 13px;
			color: #40a2ff;
			margin-top: 20rpx;
		}
	}
	
	/* 其他登录方式 */
	.other-wrapper{
		display: flex;
		flex-direction: column;
		align-items: center;
		padding-top: 20rpx;
		margin-top: 80rpx;
		.line{
			margin-bottom: 40rpx;
			.title {
				margin: 0 32rpx;
				font-size: 24rpx;
				color: #606266;
			}
			&:before, &:after{
				content: '';
				width: 160rpx;
				height: 0;
				border-top: 1px solid #e0e0e0;
			}
		}
		.item{
			font-size: 24rpx;
			color: #606266;
			background-color: #fff;
			border: 0;
			&:after{
				border: 0;
			}
		}
		.icon{
			width: 90rpx;
			height: 90rpx;
			margin: 0 24rpx 16rpx;
		}
	}
	.agreement{
		position: absolute;
		left: 0;
		bottom: 6vh;
		z-index: 1;
		width: 750rpx;
		height: 90rpx;
		font-size: 24rpx;
		color: #999;
		.mix-icon{
			font-size: 36rpx;
			color: #ccc;
			margin-right: 8rpx;
			margin-top: 1px;
			&.active{
				color: $base-color;
			}
		}
		.title {
			color: #40a2ff;
		}
	}
</style>
