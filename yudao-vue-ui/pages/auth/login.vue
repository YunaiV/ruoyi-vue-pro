<template>
	<view class="app">
		<!-- 左下角的环 -->
		<view class="left-bottom-sign"></view>
		<!-- 右上角的折角 -->
		<view class="right-top-sign"></view>
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
				<view class="input-item">
					<text class="title">手机号码</text>
					<view class="row">
						<input v-model="mobile" type="number" maxlength="11" 
							placeholder="请输入手机号码"	placeholder-style="color: #909399"/>
					</view>
				</view>
				<!-- 判断使用验证码还是密码 -->
				<view class="input-item" v-if="loginType == 'code'">
					<text class="title">验证码</text>
					<view class="row">
						<input v-model="code" type="number" maxlength="6"
							placeholder="请输入手机验证码" placeholder-style="color: #909399">
						<mix-code :mobile="mobile" templateCode="SMS_194050994"></mix-code>
					</view>					
				</view>
				<view class="input-item" v-else>
					<text class="title">密码</text>
					<view class="row">
						<input v-model="password" type="password" maxlength="16" 
							placeholder="请输入密码"	placeholder-style="color: #909399"/>
					</view>
				</view>
				<mix-button ref="confirmBtn" text="立即登录" marginTop="60rpx" @onConfirm="mobileLogin"></mix-button>
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
		
		<!-- Loading 框 -->
		<mix-loading v-if="isLoading"></mix-loading>
	</view>
</template>

<script>
	import { checkStr } from '@/common/js/util'
	import { login, smsLogin } from '@/api/system/auth.js'
	import loginMpWx from './mixin/login-mp-wx.js'
	import loginAppWx from './mixin/login-app-wx.js'

	export default{
		mixins: [loginMpWx, loginAppWx],
		data(){
			return {
				loginType: 'code', // 登录方式，code 验证码；password 密码
				mobile: '',
				code: '',
				password: '',
				agreement: true,
			}
		},
		onLoad() {
		},
		methods: {
			// 手机号登录
			async mobileLogin() {
				// 参数校验 TODO 芋艿：表单校验的支持
				if (!this.agreement) {
					this.$util.msg('请阅读并同意用户服务及隐私协议');
					this.$refs.confirmBtn.stop();
					return;
				}
				const {mobile, code, password} = this;
				if (!checkStr(mobile, 'mobile')){
					this.$util.msg('请输入正确的手机号码');
					this.$refs.confirmBtn.stop();
					return;
				}
				if (this.loginType === 'code' && !checkStr(code, 'mobileCode')) {
					this.$util.msg('验证码格式错误');
					this.$refs.confirmBtn.stop();
					return;
				}
				if (this.loginType === 'password' && !checkStr(password, 'pwd')) {
					this.$util.msg('密码格式错误');
					this.$refs.confirmBtn.stop();
					return;
				}
				
				// 执行登陆
				try {
					const data = this.loginType === 'code' ? await smsLogin(mobile, code)
						: await login(mobile, password);
					// 登陆成功
					this.loginSuccessCallBack(data);
				} finally {
					this.$refs.confirmBtn.stop();
				}
			},
			// 登陆成功的处理逻辑
			loginSuccessCallBack(data){
				this.$util.msg('登录成功');
				this.$store.commit('setToken', data);
				// TODO 芋艿：如果当前页是第一页，则无法返回。期望是能够回到首页
				setTimeout(()=>{
					uni.navigateBack();
				}, 1000)
			},
			navBack() {
				uni.navigateBack();
			},
			setLoginType(loginType) {
				this.loginType = loginType;
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
		}
	}
</script>

<style>
	page{
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
		position:relative;
		left: -12rpx;
	}
	.left-bottom-sign {
		position: absolute;
		left: -270rpx;
		bottom: -320rpx;
		border: 100rpx solid #d0d1fd;
		border-radius: 50%;
		padding: 180rpx;
	}
	.right-top-sign {
		position:absolute;
		top: 80rpx;
		right: -30rpx;
		z-index: 95;
		&:before, &:after{
			display:block;
			content:"";
			width: 400rpx;
			height: 80rpx;
			background: #b4f3e2;
		}
		&:before{
			transform: rotate(50deg);
			border-top-right-radius: 50px;
		}
		&:after{
			position: absolute;
			right: -198rpx;
			top: 0;
			transform: rotate(-50deg);
			border-top-left-radius: 50px;
		}
	}
	
	/** 手机登录部分 */
	.input-content {
		padding: 0 60rpx;
		.input-item {
			display:flex;
			flex-direction: column;
			align-items:flex-start;
			justify-content: center;
			padding: 0 30rpx;
			background: #f8f6fc;
			height: 120rpx;
			border-radius: 4px;
			margin-bottom: 50rpx;
			&:last-child{
				margin-bottom: 0;
			}
			.row{
				width: 100%;
			}
			.title{
				height: 50rpx;
				line-height: 56rpx;
				font-size: 26rpx;
				color: #606266;
			}
			input {
				flex: 1;
				height: 60rpx;
				font-size: 30rpx;
				color: #303133;
				width: 100%;
			}	
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
