<script>
	import Vue from 'vue'
	import { getAuthToken } from '@/common/js/util.js'
	let __timerId = 0;
	export default {
		onLaunch() {
			uni.getSystemInfo({
				success: e=> {
					this.initSize(e);
				}
			})
			this.initLogin();
		},
		methods: {
			// 初始化登陆状态
			async initLogin(){
				const token = getAuthToken()
				if (!token) {
					return;
				}
				// 通过设置 Token 的方式，触发加载用户信息
				this.$store.commit('setToken', {
					token
				});
			},
			/**
			 * 存储设备信息 参考colorUI
			 * @param {Object} 
			 */
			initSize(e){
				const systemInfo = e;
				let navigationBarHeight;
				let custom = {};
				// #ifndef MP
				custom = {height: 36,width: 88};
				navigationBarHeight = 44;
				// #endif
				// #ifdef MP
				custom = wx.getMenuButtonBoundingClientRect();
				navigationBarHeight = custom.bottom + custom.top - e.statusBarHeight * 2;
				// #endif	
				systemInfo.custom = custom;
				systemInfo.navigationBarHeight = navigationBarHeight;
				Vue.prototype.systemInfo = systemInfo;
			},
			//打开全局定时器
			openTimer(){
				this.closeTimer();
				__timerId = setInterval(()=>{
					this.$store.commit('setStateAttr', {
						key: 'timerIdent',
						val: !this.$store.state.timerIdent
					})
				}, 1000)
			},
			//关闭定时器
			closeTimer(){
				if(__timerId != 0){
					clearInterval(__timerId);
					__timerId = 0;
				}
			},
		},
		onShow() {
			console.log('app show');
			this.openTimer();
		},
		onHide() {
			this.closeTimer();
		}
	}
</script>

<style lang="scss">
	/*每个页面公共css */
	@import "@/uni_modules/uview-ui/index.scss";
	@import url("./common/css/common.css");
	@import url("./common/css/icon.css");
</style>
