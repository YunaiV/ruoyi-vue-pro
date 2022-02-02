export default{
	// #ifdef APP-PLUS
	methods: {
		/**
		 * 微信App登录
		 *  "openId": "o0yywwGWxtBCvBuE8vH4Naof0cqU",
		 *	"nickName": "S .",
		 *	"gender": 1,
		 *	"city": "临沂",
		 *	"province": "山东",
		 *	"country": "中国",
		 *	"avatarUrl": "http://thirdwx.qlogo.cn/mmopen/vi_32/xqpCtHRBBmdlf201Fykhtx7P7JcicIbgV3Weic1oOvN6iaR3tEbuu74f2fkKQWXvzK3VDgNTZzgf0g8FqPvq8LCNQ/132",
		 *	"unionId": "oYqy4wmMcs78x9P-tsyMeM3MQ1PU"
		 */
		loginByWxApp(userInfoData){
			if(!this.agreement){
				this.$util.msg('请阅读并同意用户服务及隐私协议');
				return;
			}
			this.$util.throttle(async ()=>{
				let [err, res] = await uni.login({
					provider: 'weixin'
				})
				if(err){
					console.log(err);
					return;
				}
				uni.getUserInfo({
					provider: 'weixin',
					success: async res=>{
						const response = await this.$request('user', 'loginByWeixin', {
							userInfo: res.userInfo,
						}, {
							showLoading: true
						});
						if(response.status === 0){
							this.$util.msg(response.msg);
							return;
						}
						if(response.hasBindMobile && response.data.token){
							this.loginSuccessCallBack({
								token: response.data.token,
								tokenExpired: response.data.tokenExpired
							});
						}else{
							this.navTo('/pages/auth/bindMobile?data='+JSON.stringify(response.data))
						}
						plus.oauth.getServices(oauthRes=>{
							oauthRes[0].logout(logoutRes => {
								console.log(logoutRes);
							}, error => {
								console.log(error);
							})
						})
					},
					fail(err) {
						console.log(err);
					}
				})
			})
		}
	}
	// #endif
}









