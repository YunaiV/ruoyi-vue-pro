/**
 * mescroll-body写在子组件时,需通过mescroll的mixins补充子组件缺少的生命周期:
 * 当一个页面只有一个mescroll-body写在子组件时, 则使用mescroll-comp.js (参考 mescroll-comp 案例)
 * 当一个页面有多个mescroll-body写在子组件时, 则使用mescroll-more.js (参考 mescroll-more 案例)
 */
const MescrollCompMixin = {
	// 因为子组件无onPageScroll和onReachBottom的页面生命周期，需在页面传递进到子组件
	onPageScroll(e) {
		let item = this.$refs["mescrollItem"];
		if(item && item.mescroll) item.mescroll.onPageScroll(e);
	},
	onReachBottom() {
		let item = this.$refs["mescrollItem"];
		if(item && item.mescroll) item.mescroll.onReachBottom();
	},
	// 当down的native: true时, 还需传递此方法进到子组件
	onPullDownRefresh(){
		let item = this.$refs["mescrollItem"];
		if(item && item.mescroll) item.mescroll.onPullDownRefresh();
	}
}

export default MescrollCompMixin;
