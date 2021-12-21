<template>
	<view class="uni-swipe">
		<!-- 在微信小程序 app vue端 h5 使用wxs 实现-->
		<!-- #ifdef APP-VUE || MP-WEIXIN || H5 -->
		<view class="uni-swipe_content">
			<view :data-disabled="disabled" :data-position="pos" :change:prop="swipe.sizeReady" :prop="pos" class="uni-swipe_move-box selector-query-hock move-hock"
			 @touchstart="swipe.touchstart" @touchmove="swipe.touchmove" @touchend="swipe.touchend" @change="change">
				<view class="uni-swipe_box">
					<slot />
				</view>
				<view ref="selector-button-hock" class="uni-swipe_button-group selector-query-hock move-hock">
					<!-- 使用 touchend 解决 ios 13 不触发按钮事件的问题-->
					<view v-for="(item,index) in options" :data-button="btn" :key="index" :style="{
		          backgroundColor: item.style && item.style.backgroundColor ? item.style.backgroundColor : '#C7C6CD',
		          fontSize: item.style && item.style.fontSize ? item.style.fontSize : '16px'
		        }"
					 class="uni-swipe_button button-hock" @touchend="onClick(index,item)"><text class="uni-swipe_button-text" :style="{color: item.style && item.style.color ? item.style.color : '#FFFFFF',}">{{ item.text }}</text></view>
				</view>
			</view>
		</view>
		<!-- #endif -->

		<!--  app nvue端 使用 bindingx -->
		<!-- #ifdef APP-NVUE -->
		<view ref="selector-box-hock" class="uni-swipe_content" @horizontalpan="touchstart" @touchend="touchend">
			<view ref="selector-button-hock" class="uni-swipe_button-group selector-query-hock move-hock" :style="{width:right+'px'}">
				<view ref="button-hock" v-for="(item,index) in options" :key="index" :style="{
		  backgroundColor: item.style && item.style.backgroundColor ? item.style.backgroundColor : '#C7C6CD',left: right+'px'}"
				 class="uni-swipe_button " @click.stop="onClick(index,item)"><text class="uni-swipe_button-text" :style="{color: item.style && item.style.color ? item.style.color : '#FFFFFF',fontSize: item.style && item.style.fontSize ? item.style.fontSize : '16px'}">{{ item.text }}</text></view>
			</view>
			<view ref='selector-content-hock' class="uni-swipe_move-box selector-query-hock">
				<view class="uni-swipe_box">
					<slot />
				</view>
			</view>
		</view>
		<!-- #endif -->

		<!-- 在非 app 端、非微信小程序、支付宝小程序、h5端使用 js -->
		<!-- #ifndef APP-PLUS || MP-WEIXIN || MP-ALIPAY || H5 -->
		<view class="uni-swipe_content">
			<view ref="selector-button-hock" class="uni-swipe_button-group selector-query-hock move-hock">
				<view v-for="(item,index) in options" :data-button="btn" :key="index" :style="{
		    backgroundColor: item.style && item.style.backgroundColor ? item.style.backgroundColor : '#C7C6CD',
		    fontSize: item.style && item.style.fontSize ? item.style.fontSize : '16px'
		  }"
				 class="uni-swipe_button button-hock" @click.stop="onClick(index,item)"><text class="uni-swipe_button-text" :style="{color: item.style && item.style.color ? item.style.color : '#FFFFFF',}">{{ item.text }}</text></view>
			</view>
			<view ref='selector-content-hock' class="selector-query-hock" @touchstart="touchstart" @touchmove="touchmove"
			 @touchend="touchend" :class="{'ani':uniShow}" :style="{transform:moveLeft}">
				<view class="uni-swipe_move-box"  >
					<view class="uni-swipe_box">
						<slot />
					</view>
				</view>
			</view>
		</view>
		<!-- #endif -->
		<!-- #ifdef MP-ALIPAY -->
		<view class="uni-swipe-box" @touchstart="touchstart" @touchmove="touchmove" @touchend="touchend">
			<view class="viewWidth-hook">
				<movable-area v-if="viewWidth !== 0" class="movable-area" :style="{width:(viewWidth-buttonWidth)+'px'}">
					<movable-view class="movable-view" direction="horizontal" :animation="!transition" :style="{width:viewWidth+'px'}"
					 :class="[transition?'transition':'']" :x="x" :disabled="disabledView" @change="onChange">
						<view class="movable-view-box">
							<slot></slot>
						</view>
					</movable-view>
				</movable-area>
			</view>
			<view ref="selector-button-hock" class="uni-swipe_button-group viewWidth-hook">
				<view v-for="(item,index) in options" :data-button="btn" :key="index" :style="{
				  backgroundColor: item.style && item.style.backgroundColor ? item.style.backgroundColor : '#C7C6CD',
				  fontSize: item.style && item.style.fontSize ? item.style.fontSize : '16px'
				}"
				 class="uni-swipe_button button-hock" @click.stop="onClick(index,item)"><text class="uni-swipe_button-text" :style="{color: item.style && item.style.color ? item.style.color : '#FFFFFF',}">{{ item.text }}</text></view>
			</view>
		</view>
		<!-- #endif -->
	</view>
</template>
<script src="./index.wxs" module="swipe" lang="wxs"></script>
<script>
	// #ifdef APP-VUE|| MP-WEIXIN || H5
	import mpwxs from './mpwxs'
	// #endif

	// #ifdef APP-NVUE
	import bindingx from './bindingx.js'
	// #endif

	// #ifndef APP-PLUS|| MP-WEIXIN || MP-ALIPAY ||  H5
	import mixins from './mpother'
	// #endif

	// #ifdef MP-ALIPAY
	import mpalipay from './mpalipay'
	// #endif

	/**
	 * SwipeActionItem 滑动操作子组件
	 * @description 通过滑动触发选项的容器
	 * @tutorial https://ext.dcloud.net.cn/plugin?id=181
	 * @property {Boolean} show = [true|false] 开启关闭组件，auto-close = false 时生效
	 * @property {Boolean} disabled = [true|false] 是否禁止滑动
	 * @property {Boolean} autoClose = [true|false] 其他组件开启的时候，当前组件是否自动关闭
	 * @property {Array} options 组件选项内容及样式
	 * @event {Function} click 点击选项按钮时触发事件，e = {content,index} ，content（点击内容）、index（下标)
	 * @event {Function} change 组件打开或关闭时触发，true：开启状态；false：关闭状态
	 */

	export default {
		// #ifdef APP-VUE|| MP-WEIXIN||H5
		mixins: [mpwxs],
		// #endif

		// #ifdef APP-NVUE
		mixins: [bindingx],
		// #endif

		// #ifndef APP-PLUS|| MP-WEIXIN || MP-ALIPAY ||  H5
		mixins: [mixins],
		// #endif

		// #ifdef MP-ALIPAY
		mixins: [mpalipay],
		// #endif

		props: {
			/**
			 * 按钮内容
			 */
			options: {
				type: Array,
				default () {
					return []
				}
			},
			/**
			 * 禁用
			 */
			disabled: {
				type: Boolean,
				default: false
			},
			/**
			 * 变量控制开关
			 */
			show: {
				type: Boolean,
				default: false
			},
			/**
			 * 是否自动关闭
			 */
			autoClose: {
				type: Boolean,
				default: true
			}
		},
		inject: ['swipeaction']


	}
</script>
<style lang="scss" scoped>
	.uni-swipe {
		overflow: hidden;
	}

	.uni-swipe-box {
		position: relative;
		width: 100%;
	}

	.uni-swipe_content {
		flex: 1;
		position: relative;
	}

	.uni-swipe_move-box {
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		position: relative;
		flex-direction: row;
	}

	.uni-swipe_box {
		/* #ifndef APP-NVUE */
		display: flex;
		flex-direction: row;
		width: 100%;
		flex-shrink: 0;
		/* #endif */
		/* #ifdef APP-NVUE */
		flex: 1;
		/* #endif */
		font-size: 14px;
		background-color: #fff;
	}

	.uni-swipe_button-group {
		/* #ifndef APP-VUE|| MP-WEIXIN||H5 */
		position: absolute;
		top: 0;
		right: 0;
		bottom: 0;
		z-index: 0;
		/* #endif */
		/* #ifndef APP-NVUE */
		display: flex;
		flex-shrink: 0;
		/* #endif */
		flex-direction: row;
	}

	.uni-swipe_button {
		/* #ifdef APP-NVUE */
		position: absolute;
		left: 0;
		top: 0;
		bottom: 0;
		/* #endif */
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		flex-direction: row;
		justify-content: center;
		align-items: center;
		padding: 0 20px;
	}

	.uni-swipe_button-text {
		/* #ifndef APP-NVUE */
		flex-shrink: 0;
		/* #endif */
		font-size: 14px;
	}

	.ani {
		transition-property: transform;
		transition-duration: 0.3s;
		transition-timing-function: cubic-bezier(0.165, 0.84, 0.44, 1);
	}

	/* #ifdef MP-ALIPAY */
	.movable-area {
		width: 300px;
		height: 100%;
		height: 45px;
	}

	.movable-view {
		position: relative;
		width: 160%;
		height: 45px;
		z-index: 2;
	}
	.transition {
		transition: all 0.3s;
	}

	.movable-view-box {
		width: 100%;
		height: 100%;
		background-color: #fff;
	}
	/* #endif */
</style>
