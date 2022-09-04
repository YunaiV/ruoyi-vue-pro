<template>
	<view v-if="visibleSync" :class="{ 'uni-drawer--visible': showDrawer }" class="uni-drawer" @touchmove.stop.prevent="clear">
		<view class="uni-drawer__mask" :class="{ 'uni-drawer__mask--visible': showDrawer && mask }" @tap="close('mask')" />
		<view class="uni-drawer__content" :class="{'uni-drawer--right': rightMode,'uni-drawer--left': !rightMode, 'uni-drawer__content--visible': showDrawer}" :style="{width:drawerWidth+'px'}">
			<slot />
		</view>
		<!-- #ifdef H5 -->
		<keypress @esc="close('mask')" />
		<!-- #endif -->
	</view>
</template>

<script>
	// #ifdef H5
	import keypress from './keypress.js'
	// #endif
	/**
	 * Drawer 抽屉
	 * @description 抽屉侧滑菜单
	 * @tutorial https://ext.dcloud.net.cn/plugin?id=26
	 * @property {Boolean} mask = [true | false] 是否显示遮罩
	 * @property {Boolean} maskClick = [true | false] 点击遮罩是否关闭
	 * @property {Boolean} mode = [left | right] Drawer 滑出位置
	 * 	@value left 从左侧滑出
	 * 	@value right 从右侧侧滑出
	 * @property {Number} width 抽屉的宽度 ，仅 vue 页面生效
	 * @event {Function} close 组件关闭时触发事件
	 */
	export default {
		name: 'UniDrawer',
		components: {
			// #ifdef H5
			keypress
			// #endif
		},
		emits:['change'],
		props: {
			/**
			 * 显示模式（左、右），只在初始化生效
			 */
			mode: {
				type: String,
				default: ''
			},
			/**
			 * 蒙层显示状态
			 */
			mask: {
				type: Boolean,
				default: true
			},
			/**
			 * 遮罩是否可点击关闭
			 */
			maskClick:{
				type: Boolean,
				default: true
			},
			/**
			 * 抽屉宽度
			 */
			width: {
				type: Number,
				default: 220
			}
		},
		data() {
			return {
				visibleSync: false,
				showDrawer: false,
				rightMode: false,
				watchTimer: null,
				drawerWidth: 220
			}
		},
		created() {
			// #ifndef APP-NVUE
			this.drawerWidth = this.width
			// #endif
			this.rightMode = this.mode === 'right'
		},
		methods: {
			clear(){},
			close(type) {
				// fixed by mehaotian 抽屉尚未完全关闭或遮罩禁止点击时不触发以下逻辑
				if((type === 'mask' && !this.maskClick) || !this.visibleSync) return
				this._change('showDrawer', 'visibleSync', false)
			},
			open() {
				// fixed by mehaotian 处理重复点击打开的事件
				if(this.visibleSync) return
				this._change('visibleSync', 'showDrawer', true)
			},
			_change(param1, param2, status) {
				this[param1] = status
				if (this.watchTimer) {
					clearTimeout(this.watchTimer)
				}
				this.watchTimer = setTimeout(() => {
					this[param2] = status
					this.$emit('change',status)
				}, status ? 50 : 300)
			}
		}
	}
</script>

<style lang="scss" >
	$uni-mask: rgba($color: #000000, $alpha: 0.4) ;
	// 抽屉宽度
	$drawer-width: 220px;

	.uni-drawer {
		/* #ifndef APP-NVUE */
		display: block;
		/* #endif */
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		overflow: hidden;
		z-index: 999;
	}

	.uni-drawer__content {
		/* #ifndef APP-NVUE */
		display: block;
		/* #endif */
		position: absolute;
		top: 0;
		width: $drawer-width;
		bottom: 0;
		background-color: $uni-bg-color;
		transition: transform 0.3s ease;
	}

	.uni-drawer--left {
		left: 0;
		/* #ifdef APP-NVUE */
		transform: translateX(-$drawer-width);
		/* #endif */
		/* #ifndef APP-NVUE */
		transform: translateX(-100%);
		/* #endif */
	}

	.uni-drawer--right {
		right: 0;
		/* #ifdef APP-NVUE */
		transform: translateX($drawer-width);
		/* #endif */
		/* #ifndef APP-NVUE */
		transform: translateX(100%);
		/* #endif */
	}

	.uni-drawer__content--visible {
		transform: translateX(0px);
	}


	.uni-drawer__mask {
		/* #ifndef APP-NVUE */
		display: block;
		/* #endif */
		opacity: 0;
		position: absolute;
		top: 0;
		left: 0;
		bottom: 0;
		right: 0;
		background-color: $uni-mask;
		transition: opacity 0.3s;
	}

	.uni-drawer__mask--visible {
		/* #ifndef APP-NVUE */
		display: block;
		/* #endif */
		opacity: 1;
	}
</style>
