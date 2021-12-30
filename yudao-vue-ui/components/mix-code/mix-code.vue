<template>
	<view class="mix-get-code" @click="getCode">
		<view v-if="loading" class="loading">
			<mix-icon-loading size="28rpx" color="#0083ff"></mix-icon-loading>
		</view>
		<text class="text" :class="{disabled: timeDown > 0}">
			{{ timeDown > 0 ? '重新获取 ' + timeDown + 's' : '获取验证码' }}
		</text>
	</view>
</template>

<script>
	/**
	 * 手机验证码
	 * @prop mobile 手机号
	 * @prop templateCode 短信模版id
	 */
	import {checkStr} from '@/common/js/util'
	export default {
		//获取手机验证码
		name: 'MixMobileCode',
		data() {
			return {
				loading: false,
				timeDown: ''
			}
		},
		props: {
			mobile: {
				type: String,
				default: ''
			},
			templateCode: {
				type: String,
				default: ''
			},
			action: {
				type: String,
				default: '用户注册' //设置支付密码
			}
		},
		methods: {
			//获取验证码
			async getCode(){
				if(this.timeDown > 0){
					return;
				}
				this.$util.throttle(()=>{
					const mobile = this.mobile || this.$store.state.userInfo.username;;
					if(!checkStr(mobile, 'mobile')){
						this.$util.msg('手机号码格式不正确');
						return;
					}
					this.loading = true;
					this.$request('smsCode', 'send', {
						mobile,
						action: this.action, //uni短信必填
						TemplateCode: this.templateCode, //阿里云必填
					}).then(response=>{
						this.$util.msg(response.msg);
						this.loading = false;
						if(response.status === 1){
							this.countDown(60);
						}
					}).catch(err=>{
						this.$util.msg('验证码发送失败');
						this.loading = false;
						console.log(err);
					})
				}, 2000)
			},
			//倒计时
			countDown(timer){
				timer --;
				this.timeDown = timer;
				if(timer > 0){
					setTimeout(()=>{
						this.countDown(timer);
					}, 1000)
				}
			},
		}
	}
</script>

<style scoped lang="scss">
	.mix-get-code{
		flex-shrink: 0;
		display: flex;
		justify-content: space-between;
		align-items: center;
		height: 36rpx;
		
		&:before{
			content: '';
			width: 0;
			height: 40;
			border-right: 1px solid #f0f0f0;
		}
		.loading{
			margin-right: 8rpx;
		}
		.text{
			line-height: 28rpx;
			font-size: 26rpx;
			color: #40a2ff;
			
			&.disabled{
				color: #ccc;
			}
		}
	}
</style>
