<!-- 装修基础组件：菜单导航（金刚区） -->
<template>
	<!-- 包裹层 -->
	<view class="ui-swiper" :class="[props.mode, props.ui]"
		:style="[bgStyle, { height: swiperHeight + (menuList.length > 1 ? 50 : 0) + 'rpx' }]">
		<!-- 轮播 -->
		<swiper :circular="props.circular" :current="state.cur" :autoplay="props.autoplay" :interval="props.interval"
			:duration="props.duration" :style="[{ height: swiperHeight + 'rpx' }]" @change="swiperChange">
			<swiper-item v-for="(arr, index) in menuList" :key="index" :class="{ cur: state.cur == index }">
				<!-- 宫格 -->
				<view class="grid-wrap">
					<view v-for="(item, index) in arr" :key="index"
						class="grid-item ss-flex ss-flex-col ss-col-center ss-row-center"
						:style="[{ width: `${100 * (1 / data.column)}%`, height: '200rpx' }]" hover-class="ss-hover-btn"
						@tap="sheep.$router.go(item.url)">
						<view class="menu-box ss-flex ss-flex-col ss-col-center ss-row-center">
							<view v-if="item.badge.show" class="tag-box"
								:style="[{ background: item.badge.bgColor, color: item.badge.textColor }]">
								{{ item.badge.text }}
							</view>
							<image v-if="item.iconUrl" class="menu-icon" :style="[
                  {
                    width: props.iconSize + 'rpx',
                    height: props.iconSize + 'rpx',
                  },
                ]" :src="sheep.$url.cdn(item.iconUrl)" mode="aspectFill"></image>
							<view v-if="data.layout === 'iconText'" class="menu-title"
								:style="[{ color: item.titleColor }]">
								{{ item.title }}
							</view>
						</view>
					</view>
				</view>
			</swiper-item>
		</swiper>
		<!-- 指示点 -->
		<template v-if="menuList.length > 1">
			<view class="ui-swiper-dot" :class="props.dotStyle" v-if="props.dotStyle != 'tag'">
				<view class="line-box" v-for="(item, index) in menuList.length" :key="index"
					:class="[state.cur == index ? 'cur' : '', props.dotCur]"></view>
			</view>
			<view class="ui-swiper-dot" :class="props.dotStyle" v-if="props.dotStyle == 'tag'">
				<view class="ui-tag radius" :class="[props.dotCur]" style="pointer-events: none">
					<view style="transform: scale(0.7)">{{ state.cur + 1 }} / {{ menuList.length }}</view>
				</view>
			</view>
		</template>
	</view>
</template>

<script setup>
	/**
	 * 轮播menu
	 *
	 * @property {Boolean} circular = false  		- 是否采用衔接滑动，即播放到末尾后重新回到开头
	 * @property {Boolean} autoplay = true  		- 是否自动切换
	 * @property {Number} interval = 5000  			- 自动切换时间间隔
	 * @property {Number} duration = 500  			- 滑动动画时长,app-nvue不支持
	 * @property {Array} list = [] 					- 轮播数据
	 * @property {String} ui = ''  					- 样式class
	 * @property {String} mode  					- 模式
	 * @property {String} dotStyle  				- 指示点样式
	 * @property {String} dotCur= 'ui-BG-Main' 		- 当前指示点样式,默认主题色
	 * @property {String} bg  						- 背景
	 *
	 * @property {String|Number} col = 4  			- 一行数量
	 * @property {String|Number} row = 1 			- 几行
	 * @property {String} hasBorder 				- 是否有边框
	 * @property {String} borderColor 				- 边框颜色
	 * @property {String} background		  		- 背景
	 * @property {String} hoverClass 				- 按压样式类
	 * @property {String} hoverStayTime 		  	- 动画时间
	 *
	 * @property {Array} list 		  				- 导航列表
	 * @property {Number} iconSize 		  			- 图标大小
	 * @property {String} color 		  			- 标题颜色
	 *
	 */

	import {
		reactive,
		computed
	} from 'vue';
	import sheep from '@/sheep';

	// 数据
	const state = reactive({
		cur: 0,
	});

	// 接收参数

	const props = defineProps({
		// 装修数据
		data: {
			type: Object,
			default: () => ({}),
		},
		// 装修样式
		styles: {
			type: Object,
			default: () => ({}),
		},
		circular: {
			type: Boolean,
			default: true,
		},
		autoplay: {
			type: Boolean,
			default: false,
		},
		interval: {
			type: Number,
			default: 5000,
		},
		duration: {
			type: Number,
			default: 500,
		},
		ui: {
			type: String,
			default: '',
		},
		mode: {
			//default
			type: String,
			default: 'default',
		},
		dotStyle: {
			type: String,
			default: 'long', //default long tag
		},
		dotCur: {
			type: String,
			default: 'ui-BG-Main',
		},
		height: {
			type: Number,
			default: 300,
		},
		// 是否有边框
		hasBorder: {
			type: Boolean,
			default: true,
		},
		// 边框颜色
		borderColor: {
			type: String,
			default: 'red',
		},
		background: {
			type: String,
			default: 'blue',
		},
		hoverClass: {
			type: String,
			default: 'ss-hover-class', //'none'为没有hover效果
		},
		// 一排宫格数
		col: {
			type: [Number, String],
			default: 3,
		},
		iconSize: {
			type: Number,
			default: 80,
		},
		color: {
			type: String,
			default: '#000',
		},
	});

	// 设置背景样式
	const bgStyle = computed(() => {
		// 直接从 props.styles 解构
		const {
			bgType,
			bgImg,
			bgColor
		} = props.styles;

		// 根据 bgType 返回相应的样式
		return {
			background: bgType === 'img' ? `url(${bgImg}) no-repeat top center / 100% 100%` : bgColor
		};
	});

	// 生成数据
	const menuList = computed(() => splitData(props.data.list, props.data.row * props.data.column));
	const swiperHeight = computed(() => props.data.row * (props.data.layout === 'iconText' ? 200 : 180));
	const windowWidth = sheep.$platform.device.windowWidth;

	// current 改变时会触发 change 事件
	const swiperChange = (e) => {
		state.cur = e.detail.current;
	};

	// 重组数据
	const splitData = (oArr = [], length = 1) => {
		let arr = [];
		let minArr = [];
		oArr.forEach((c) => {
			if (minArr.length === length) {
				minArr = [];
			}
			if (minArr.length === 0) {
				arr.push(minArr);
			}
			minArr.push(c);
		});

		return arr;
	};
</script>

<style lang="scss" scoped>
	.grid-wrap {
		width: 100%;
		display: flex;
		position: relative;
		box-sizing: border-box;
		overflow: hidden;
		flex-wrap: wrap;
		align-items: center;
	}

	.menu-box {
		position: relative;
		z-index: 1;
		transform: translate(0, 0);

		.tag-box {
			position: absolute;
			z-index: 2;
			top: 0;
			right: -6rpx;
			font-size: 2em;
			line-height: 1;
			padding: 0.4em 0.6em 0.3em;
			transform: scale(0.4) translateX(0.5em) translatey(-0.6em);
			transform-origin: 100% 0;
			border-radius: 200rpx;
			white-space: nowrap;
		}

		.menu-icon {
			transform: translate(0, 0);
			width: 80rpx;
			height: 80rpx;
			padding-bottom: 10rpx;
		}

		.menu-title {
			font-size: 24rpx;
			color: #333;
		}
	}

	::v-deep(.ui-swiper) {
		position: relative;
		z-index: 1;

		.ui-swiper-dot {
			position: absolute;
			width: 100%;
			bottom: 20rpx;
			height: 30rpx;
			display: flex;
			align-items: center;
			justify-content: center;
			z-index: 2;

			&.default .line-box {
				display: inline-flex;
				border-radius: 50rpx;
				width: 6px;
				height: 6px;
				border: 2px solid transparent;
				margin: 0 10rpx;
				opacity: 0.3;
				position: relative;
				justify-content: center;
				align-items: center;

				&.cur {
					width: 8px;
					height: 8px;
					opacity: 1;
					border: 0px solid transparent;
				}

				&.cur::after {
					content: '';
					border-radius: 50rpx;
					width: 4px;
					height: 4px;
					background-color: #fff;
				}
			}

			&.long .line-box {
				display: inline-block;
				border-radius: 100rpx;
				width: 6px;
				height: 6px;
				margin: 0 10rpx;
				opacity: 0.3;
				position: relative;

				&.cur {
					width: 24rpx;
					opacity: 1;
				}

				&.cur::after {}
			}

			&.line {
				bottom: 20rpx;

				.line-box {
					display: inline-block;
					width: 30px;
					height: 3px;
					opacity: 0.3;
					position: relative;

					&.cur {
						opacity: 1;
					}
				}
			}

			&.tag {
				justify-content: flex-end;
				position: absolute;
				bottom: 20rpx;
				right: 20rpx;
			}
		}
	}
</style>