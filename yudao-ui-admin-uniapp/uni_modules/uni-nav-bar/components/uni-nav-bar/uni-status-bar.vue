<template>
	<view :style="{ height: statusBarHeight }" class="uni-status-bar">
		<slot />
	</view>
</template>

<script>
	export default {
		name: 'UniStatusBar',
		data() {
			return {
				statusBarHeight: 20
			}
		},
		mounted() {
			this.statusBarHeight = uni.getSystemInfoSync().statusBarHeight + 'px'
		}
	}
</script>

<style lang="scss" >
	.uni-status-bar {
		// width: 750rpx;
		height: 20px;
		// height: var(--status-bar-height);
	}
</style>
