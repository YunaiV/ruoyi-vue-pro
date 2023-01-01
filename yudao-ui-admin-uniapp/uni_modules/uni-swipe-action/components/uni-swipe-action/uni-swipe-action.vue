<template>
	<view>
		<slot></slot>
	</view>
</template>

<script>
	/**
	 * SwipeAction 滑动操作
	 * @description 通过滑动触发选项的容器
	 * @tutorial https://ext.dcloud.net.cn/plugin?id=181
	 */
	export default {
		name:"uniSwipeAction",
		data() {
			return {};
		},
		created() {
			this.children = [];
		},
		methods: {
			// 公开给用户使用，重制组件样式
			resize(){
				// wxs 会自己计算组件大小，所以无需执行下面代码
				// #ifndef APP-VUE || H5 || MP-WEIXIN
				this.children.forEach(vm=>{
					vm.init()
				})
				// #endif
			},
			// 公开给用户使用，关闭全部 已经打开的组件
			closeAll(){
				this.children.forEach(vm=>{
					// #ifdef APP-VUE || H5 || MP-WEIXIN
					vm.is_show = 'none'
					// #endif

					// #ifndef APP-VUE || H5 || MP-WEIXIN
					vm.close()
					// #endif
				})
			},
			closeOther(vm) {
				if (this.openItem && this.openItem !== vm) {
					// #ifdef APP-VUE || H5 || MP-WEIXIN
					this.openItem.is_show = 'none'
					// #endif

					// #ifndef APP-VUE || H5 || MP-WEIXIN
					this.openItem.close()
					// #endif
				}
				// 记录上一个打开的 swipe-action-item ,用于 auto-close
				this.openItem = vm
			}
		}
	};
</script>

<style></style>
