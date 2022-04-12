<template>
	<view
		class="u-notice"
		@tap="clickHandler"
	>
		<slot name="icon">
			<view
				class="u-notice__left-icon"
				v-if="icon"
			>
				<u-icon
					:name="icon"
					:color="color"
					size="19"
				></u-icon>
			</view>
		</slot>
		<view
			class="u-notice__content"
			ref="u-notice__content"
		>
			<text
				ref="u-notice__content__text"
				class="u-notice__content__text"
				:style="[textStyle]"
			>{{text}}</text>
		</view>
		<view
			class="u-notice__right-icon"
			v-if="['link', 'closable'].includes(mode)"
		>
			<u-icon
				v-if="mode === 'link'"
				name="arrow-right"
				:size="17"
				:color="color"
			></u-icon>
			<u-icon
				v-if="mode === 'closable'"
				@click="close"
				name="close"
				:size="16"
				:color="color"
			></u-icon>
		</view>
	</view>
</template>
<script>
	import props from './props.js';
	// #ifdef APP-NVUE
	const animation = uni.requireNativePlugin('animation')
	const dom = uni.requireNativePlugin('dom')
	// #endif
	/**
	 * RowNotice 滚动通知中的水平滚动模式
	 * @description 水平滚动
	 * @tutorial https://www.uviewui.com/components/noticeBar.html
	 * @property {String | Number}	text			显示的内容，字符串
	 * @property {String}			icon			是否显示左侧的音量图标 (默认 'volume' )
	 * @property {String}			mode			通告模式，link-显示右箭头，closable-显示右侧关闭图标
	 * @property {String}			color			文字颜色，各图标也会使用文字颜色 (默认 '#f9ae3d' )
	 * @property {String}			bgColor			背景颜色 (默认 ''#fdf6ec' )
	 * @property {String | Number}	fontSize		字体大小，单位px (默认 14 )
	 * @property {String | Number}	speed			水平滚动时的滚动速度，即每秒滚动多少px(rpx)，这有利于控制文字无论多少时，都能有一个恒定的速度  (默认 80 )
	 * 
	 * @event {Function} click 点击通告文字触发
	 * @event {Function} close 点击右侧关闭图标触发
	 * @example 
	 */
	export default {
		name: 'u-row-notice',
		mixins: [uni.$u.mpMixin, uni.$u.mixin,props],
		data() {
			return {
				animationDuration: '0', // 动画执行时间
				animationPlayState: 'paused', // 动画的开始和结束执行
				// nvue下，内容发生变化，导致滚动宽度也变化，需要标志为是否需要重新计算宽度
				// 不能在内容变化时直接重新计算，因为nvue的animation模块上一次的滚动不是刚好结束，会有影响
				nvueInit: true,
				show: true
			};
		},
		watch: {
			text: {
				immediate: true,
				handler(newValue, oldValue) {
					// #ifdef APP-NVUE
					this.nvueInit = true
					// #endif
					// #ifndef APP-NVUE
					this.vue()
					// #endif
					
					if(!uni.$u.test.string(newValue)) {
						uni.$u.error('noticebar组件direction为row时，要求text参数为字符串形式')
					}
				}
			},
			fontSize() {
				// #ifdef APP-NVUE
				this.nvueInit = true
				// #endif
				// #ifndef APP-NVUE
				this.vue()
				// #endif
			},
			speed() {
				// #ifdef APP-NVUE
				this.nvueInit = true
				// #endif
				// #ifndef APP-NVUE
				this.vue()
				// #endif
			}
		},
		computed: {
			// 文字内容的样式
			textStyle() {
				let style = {}
				style.color = this.color
				style.animationDuration = this.animationDuration
				style.animationPlayState = this.animationPlayState
				style.fontSize = uni.$u.addUnit(this.fontSize)
				return style
			},
		},
		mounted() {
			// #ifdef APP-PLUS
			// 在APP上(含nvue)，监听当前webview是否处于隐藏状态(进入下一页时即为hide状态)
			// 如果webivew隐藏了，为了节省性能的损耗，应停止动画的执行，同时也是为了保持进入下一页返回后，滚动位置保持不变
			var pages = getCurrentPages()
			var page = pages[pages.length - 1]
			var currentWebview = page.$getAppWebview()
			currentWebview.addEventListener('hide', () => {
				this.webviewHide = true
			})
			currentWebview.addEventListener('show', () => {
				this.webviewHide = false
			})
			// #endif

			this.init()
		},
		methods: {
			init() {
				// #ifdef APP-NVUE
				this.nvue()
				// #endif

				// #ifndef APP-NVUE
				this.vue()
				// #endif
				
				if(!uni.$u.test.string(this.text)) {
					uni.$u.error('noticebar组件direction为row时，要求text参数为字符串形式')
				}
			},
			// vue版处理
			async vue() {
				// #ifndef APP-NVUE
				let boxWidth = 0,
					textWidth = 0
				// 进行一定的延时
				await uni.$u.sleep()
				// 查询盒子和文字的宽度
				textWidth = (await this.$uGetRect('.u-notice__content__text')).width
				boxWidth = (await this.$uGetRect('.u-notice__content')).width
				// 根据t=s/v(时间=路程/速度)，这里为何不需要加上#u-notice-box的宽度，因为中设置了.u-notice-content样式中设置了padding-left: 100%
				// 恰巧计算出来的结果中已经包含了#u-notice-box的宽度
				this.animationDuration = `${textWidth / uni.$u.getPx(this.speed)}s`
				// 这里必须这样开始动画，否则在APP上动画速度不会改变
				this.animationPlayState = 'paused'
				setTimeout(() => {
					this.animationPlayState = 'running'
				}, 10)
				// #endif
			},
			// nvue版处理
			async nvue() {
				// #ifdef APP-NVUE
				this.nvueInit = false
				let boxWidth = 0,
					textWidth = 0
				// 进行一定的延时
				await uni.$u.sleep()
				// 查询盒子和文字的宽度
				textWidth = (await this.getNvueRect('u-notice__content__text')).width
				boxWidth = (await this.getNvueRect('u-notice__content')).width
				// 将文字移动到盒子的右边沿，之所以需要这么做，是因为nvue不支持100%单位，否则可以通过css设置
				animation.transition(this.$refs['u-notice__content__text'], {
					styles: {
						transform: `translateX(${boxWidth}px)`
					},
				}, () => {
					// 如果非禁止动画，则开始滚动
					!this.stopAnimation && this.loopAnimation(textWidth, boxWidth)
				});
				// #endif
			},
			loopAnimation(textWidth, boxWidth) {
				// #ifdef APP-NVUE
				animation.transition(this.$refs['u-notice__content__text'], {
					styles: {
						// 目标移动终点为-textWidth，也即当文字的最右边贴到盒子的左边框的位置
						transform: `translateX(-${textWidth}px)`
					},
					// 滚动时间的计算为，时间 = 路程(boxWidth + textWidth) / 速度，最后转为毫秒
					duration: (boxWidth + textWidth) / uni.$u.getPx(this.speed) * 1000,
					delay: 10
				}, () => {
					animation.transition(this.$refs['u-notice__content__text'], {
						styles: {
							// 重新将文字移动到盒子的右边沿
							transform: `translateX(${this.stopAnimation ? 0 : boxWidth}px)`
						},
					}, () => {
						// 如果非禁止动画，则继续下一轮滚动
						if (!this.stopAnimation) {
							// 判断是否需要初始化计算尺寸
							if (this.nvueInit) {
								this.nvue()
							} else {
								this.loopAnimation(textWidth, boxWidth)
							}
						}
					});
				})
				// #endif
			},
			getNvueRect(el) {
				// #ifdef APP-NVUE
				// 返回一个promise
				return new Promise(resolve => {
					dom.getComponentRect(this.$refs[el], (res) => {
						resolve(res.size)
					})
				})
				// #endif
			},
			// 点击通告栏
			clickHandler(index) {
				this.$emit('click')
			},
			// 点击右侧按钮，需要判断点击的是关闭图标还是箭头图标
			close() {
				this.$emit('close')
			}
		},
		// #ifdef APP-NVUE
		beforeDestroy() {
			this.stopAnimation = true
		},
		// #endif
	};
</script>

<style lang="scss" scoped>
	@import "../../libs/css/components.scss";

	.u-notice {
		@include flex;
		align-items: center;
		justify-content: space-between;

		&__left-icon {
			align-items: center;
			margin-right: 5px;
		}

		&__right-icon {
			margin-left: 5px;
			align-items: center;
		}

		&__content {
			text-align: right;
			flex: 1;
			@include flex;
			flex-wrap: nowrap;
			overflow: hidden;

			&__text {
				font-size: 14px;
				color: $u-warning;
				/* #ifndef APP-NVUE */
				// 这一句很重要，为了能让滚动左右连接起来
				padding-left: 100%;
				word-break: keep-all;
				white-space: nowrap;
				animation: u-loop-animation 10s linear infinite both;
				/* #endif */
			}
		}

	}

	@keyframes u-loop-animation {
		0% {
			transform: translate3d(0, 0, 0);
		}

		100% {
			transform: translate3d(-100%, 0, 0);
		}
	}
</style>
