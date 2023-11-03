<template>
	<view class="uni-cursor-point">
		<view v-if="popMenu && (leftBottom||rightBottom||leftTop||rightTop) && content.length > 0" :class="{
        'uni-fab--leftBottom': leftBottom,
        'uni-fab--rightBottom': rightBottom,
        'uni-fab--leftTop': leftTop,
        'uni-fab--rightTop': rightTop
      }" class="uni-fab">
			<view :class="{
          'uni-fab__content--left': horizontal === 'left',
          'uni-fab__content--right': horizontal === 'right',
          'uni-fab__content--flexDirection': direction === 'vertical',
          'uni-fab__content--flexDirectionStart': flexDirectionStart,
          'uni-fab__content--flexDirectionEnd': flexDirectionEnd,
		  'uni-fab__content--other-platform': !isAndroidNvue
        }" :style="{ width: boxWidth, height: boxHeight, backgroundColor: styles.backgroundColor }"
				class="uni-fab__content" elevation="5">
				<view v-if="flexDirectionStart || horizontalLeft" class="uni-fab__item uni-fab__item--first" />
				<view v-for="(item, index) in content" :key="index" :class="{ 'uni-fab__item--active': isShow }"
					class="uni-fab__item" @click="_onItemClick(index, item)">
					<image :src="item.active ? item.selectedIconPath : item.iconPath" class="uni-fab__item-image"
						mode="aspectFit" />
					<text class="uni-fab__item-text"
						:style="{ color: item.active ? styles.selectedColor : styles.color }">{{ item.text }}</text>
				</view>
				<view v-if="flexDirectionEnd || horizontalRight" class="uni-fab__item uni-fab__item--first" />
			</view>
		</view>
		<view :class="{
		  'uni-fab__circle--leftBottom': leftBottom,
		  'uni-fab__circle--rightBottom': rightBottom,
		  'uni-fab__circle--leftTop': leftTop,
		  'uni-fab__circle--rightTop': rightTop,
		  'uni-fab__content--other-platform': !isAndroidNvue
		}" class="uni-fab__circle uni-fab__plus" :style="{ 'background-color': styles.buttonColor }" @click="_onClick">
			<uni-icons class="fab-circle-icon" type="plusempty" :color="styles.iconColor" size="32"
				:class="{'uni-fab__plus--active': isShow && content.length > 0}"></uni-icons>
			<!-- <view class="fab-circle-v"  :class="{'uni-fab__plus--active': isShow && content.length > 0}"></view>
			<view class="fab-circle-h" :class="{'uni-fab__plus--active': isShow  && content.length > 0}"></view> -->
		</view>
	</view>
</template>

<script>
	let platform = 'other'
	// #ifdef APP-NVUE
	platform = uni.getSystemInfoSync().platform
	// #endif

	/**
	 * Fab 悬浮按钮
	 * @description 点击可展开一个图形按钮菜单
	 * @tutorial https://ext.dcloud.net.cn/plugin?id=144
	 * @property {Object} pattern 可选样式配置项
	 * @property {Object} horizontal = [left | right] 水平对齐方式
	 * 	@value left 左对齐
	 * 	@value right 右对齐
	 * @property {Object} vertical = [bottom | top] 垂直对齐方式
	 * 	@value bottom 下对齐
	 * 	@value top 上对齐
	 * @property {Object} direction = [horizontal | vertical] 展开菜单显示方式
	 * 	@value horizontal 水平显示
	 * 	@value vertical 垂直显示
	 * @property {Array} content 展开菜单内容配置项
	 * @property {Boolean} popMenu 是否使用弹出菜单
	 * @event {Function} trigger 展开菜单点击事件，返回点击信息
	 * @event {Function} fabClick 悬浮按钮点击事件
	 */
	export default {
		name: 'UniFab',
		emits: ['fabClick', 'trigger'],
		props: {
			pattern: {
				type: Object,
				default () {
					return {}
				}
			},
			horizontal: {
				type: String,
				default: 'left'
			},
			vertical: {
				type: String,
				default: 'bottom'
			},
			direction: {
				type: String,
				default: 'horizontal'
			},
			content: {
				type: Array,
				default () {
					return []
				}
			},
			show: {
				type: Boolean,
				default: false
			},
			popMenu: {
				type: Boolean,
				default: true
			}
		},
		data() {
			return {
				fabShow: false,
				isShow: false,
				isAndroidNvue: platform === 'android',
				styles: {
					color: '#3c3e49',
					selectedColor: '#007AFF',
					backgroundColor: '#fff',
					buttonColor: '#007AFF',
					iconColor: '#fff'
				}
			}
		},
		computed: {
			contentWidth(e) {
				return (this.content.length + 1) * 55 + 15 + 'px'
			},
			contentWidthMin() {
				return '55px'
			},
			// 动态计算宽度
			boxWidth() {
				return this.getPosition(3, 'horizontal')
			},
			// 动态计算高度
			boxHeight() {
				return this.getPosition(3, 'vertical')
			},
			// 计算左下位置
			leftBottom() {
				return this.getPosition(0, 'left', 'bottom')
			},
			// 计算右下位置
			rightBottom() {
				return this.getPosition(0, 'right', 'bottom')
			},
			// 计算左上位置
			leftTop() {
				return this.getPosition(0, 'left', 'top')
			},
			rightTop() {
				return this.getPosition(0, 'right', 'top')
			},
			flexDirectionStart() {
				return this.getPosition(1, 'vertical', 'top')
			},
			flexDirectionEnd() {
				return this.getPosition(1, 'vertical', 'bottom')
			},
			horizontalLeft() {
				return this.getPosition(2, 'horizontal', 'left')
			},
			horizontalRight() {
				return this.getPosition(2, 'horizontal', 'right')
			}
		},
		watch: {
			pattern: {
				handler(val, oldVal) {
					this.styles = Object.assign({}, this.styles, val)
				},
				deep: true
			}
		},
		created() {
			this.isShow = this.show
			if (this.top === 0) {
				this.fabShow = true
			}
			// 初始化样式
			this.styles = Object.assign({}, this.styles, this.pattern)
		},
		methods: {
			_onClick() {
				this.$emit('fabClick')
				if (!this.popMenu) {
					return
				}
				this.isShow = !this.isShow
			},
			open() {
				this.isShow = true
			},
			close() {
				this.isShow = false
			},
			/**
			 * 按钮点击事件
			 */
			_onItemClick(index, item) {
				this.$emit('trigger', {
					index,
					item
				})
			},
			/**
			 * 获取 位置信息
			 */
			getPosition(types, paramA, paramB) {
				if (types === 0) {
					return this.horizontal === paramA && this.vertical === paramB
				} else if (types === 1) {
					return this.direction === paramA && this.vertical === paramB
				} else if (types === 2) {
					return this.direction === paramA && this.horizontal === paramB
				} else {
					return this.isShow && this.direction === paramA ? this.contentWidth : this.contentWidthMin
				}
			}
		}
	}
</script>

<style lang="scss" >
	$uni-shadow-base:0 1px 5px 2px rgba($color: #000000, $alpha: 0.3) !default;

	.uni-fab {
		position: fixed;
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		justify-content: center;
		align-items: center;
		z-index: 10;
		border-radius: 45px;
		box-shadow: $uni-shadow-base;
	}

	.uni-cursor-point {
		/* #ifdef H5 */
		cursor: pointer;
		/* #endif */
	}

	.uni-fab--active {
		opacity: 1;
	}

	.uni-fab--leftBottom {
		left: 15px;
		bottom: 30px;
		/* #ifdef H5 */
		left: calc(15px + var(--window-left));
		bottom: calc(30px + var(--window-bottom));
		/* #endif */
		// padding: 10px;
	}

	.uni-fab--leftTop {
		left: 15px;
		top: 30px;
		/* #ifdef H5 */
		left: calc(15px + var(--window-left));
		top: calc(30px + var(--window-top));
		/* #endif */
		// padding: 10px;
	}

	.uni-fab--rightBottom {
		right: 15px;
		bottom: 30px;
		/* #ifdef H5 */
		right: calc(15px + var(--window-right));
		bottom: calc(30px + var(--window-bottom));
		/* #endif */
		// padding: 10px;
	}

	.uni-fab--rightTop {
		right: 15px;
		top: 30px;
		/* #ifdef H5 */
		right: calc(15px + var(--window-right));
		top: calc(30px + var(--window-top));
		/* #endif */
		// padding: 10px;
	}

	.uni-fab__circle {
		position: fixed;
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		justify-content: center;
		align-items: center;
		width: 55px;
		height: 55px;
		background-color: #3c3e49;
		border-radius: 45px;
		z-index: 11;
		// box-shadow: $uni-shadow-base;
	}

	.uni-fab__circle--leftBottom {
		left: 15px;
		bottom: 30px;
		/* #ifdef H5 */
		left: calc(15px + var(--window-left));
		bottom: calc(30px + var(--window-bottom));
		/* #endif */
	}

	.uni-fab__circle--leftTop {
		left: 15px;
		top: 30px;
		/* #ifdef H5 */
		left: calc(15px + var(--window-left));
		top: calc(30px + var(--window-top));
		/* #endif */
	}

	.uni-fab__circle--rightBottom {
		right: 15px;
		bottom: 30px;
		/* #ifdef H5 */
		right: calc(15px + var(--window-right));
		bottom: calc(30px + var(--window-bottom));
		/* #endif */
	}

	.uni-fab__circle--rightTop {
		right: 15px;
		top: 30px;
		/* #ifdef H5 */
		right: calc(15px + var(--window-right));
		top: calc(30px + var(--window-top));
		/* #endif */
	}

	.uni-fab__circle--left {
		left: 0;
	}

	.uni-fab__circle--right {
		right: 0;
	}

	.uni-fab__circle--top {
		top: 0;
	}

	.uni-fab__circle--bottom {
		bottom: 0;
	}

	.uni-fab__plus {
		font-weight: bold;
	}

	// .fab-circle-v {
	// 	position: absolute;
	// 	width: 2px;
	// 	height: 24px;
	// 	left: 0;
	// 	top: 0;
	// 	right: 0;
	// 	bottom: 0;
	// 	/* #ifndef APP-NVUE */
	// 	margin: auto;
	// 	/* #endif */
	// 	background-color: white;
	// 	transform: rotate(0deg);
	// 	transition: transform 0.3s;
	// }

	// .fab-circle-h {
	// 	position: absolute;
	// 	width: 24px;
	// 	height: 2px;
	// 	left: 0;
	// 	top: 0;
	// 	right: 0;
	// 	bottom: 0;
	// 	/* #ifndef APP-NVUE */
	// 	margin: auto;
	// 	/* #endif */
	// 	background-color: white;
	// 	transform: rotate(0deg);
	// 	transition: transform 0.3s;
	// }

	.fab-circle-icon {
		transform: rotate(0deg);
		transition: transform 0.3s;
		font-weight: 200;
	}

	.uni-fab__plus--active {
		transform: rotate(135deg);
	}

	.uni-fab__content {
		/* #ifndef APP-NVUE */
		box-sizing: border-box;
		display: flex;
		/* #endif */
		flex-direction: row;
		border-radius: 55px;
		overflow: hidden;
		transition-property: width, height;
		transition-duration: 0.2s;
		width: 55px;
		border-color: #DDDDDD;
		border-width: 1rpx;
		border-style: solid;
	}

	.uni-fab__content--other-platform {
		border-width: 0px;
		box-shadow: $uni-shadow-base;
	}

	.uni-fab__content--left {
		justify-content: flex-start;
	}

	.uni-fab__content--right {
		justify-content: flex-end;
	}

	.uni-fab__content--flexDirection {
		flex-direction: column;
		justify-content: flex-end;
	}

	.uni-fab__content--flexDirectionStart {
		flex-direction: column;
		justify-content: flex-start;
	}

	.uni-fab__content--flexDirectionEnd {
		flex-direction: column;
		justify-content: flex-end;
	}

	.uni-fab__item {
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		flex-direction: column;
		justify-content: center;
		align-items: center;
		width: 55px;
		height: 55px;
		opacity: 0;
		transition: opacity 0.2s;
	}

	.uni-fab__item--active {
		opacity: 1;
	}

	.uni-fab__item-image {
		width: 20px;
		height: 20px;
		margin-bottom: 4px;
	}

	.uni-fab__item-text {
		color: #FFFFFF;
		font-size: 12px;
		line-height: 12px;
		margin-top: 2px;
	}

	.uni-fab__item--first {
		width: 55px;
	}
</style>
