<template>
	<view v-if="show" class="uni-noticebar" :style="{ backgroundColor: backgroundColor }" @click="onClick">
		<uni-icons v-if="showIcon === true || showIcon === 'true'" class="uni-noticebar-icon" type="sound"
			:color="color" size="22" />
		<view ref="textBox" class="uni-noticebar__content-wrapper"
			:class="{'uni-noticebar__content-wrapper--scrollable':scrollable, 'uni-noticebar__content-wrapper--single':!scrollable && (single || moreText)}">
			<view :id="elIdBox" class="uni-noticebar__content"
				:class="{'uni-noticebar__content--scrollable':scrollable, 'uni-noticebar__content--single':!scrollable && (single || moreText)}">
				<text :id="elId" ref="animationEle" class="uni-noticebar__content-text"
					:class="{'uni-noticebar__content-text--scrollable':scrollable,'uni-noticebar__content-text--single':!scrollable && (single || showGetMore)}"
					:style="{color:color, width:wrapWidth+'px', 'animationDuration': animationDuration, '-webkit-animationDuration': animationDuration ,animationPlayState: webviewHide?'paused':animationPlayState,'-webkit-animationPlayState':webviewHide?'paused':animationPlayState, animationDelay: animationDelay, '-webkit-animationDelay':animationDelay}">{{text}}</text>
			</view>
		</view>
		<view v-if="showGetMore === true || showGetMore === 'true'" class="uni-noticebar__more uni-cursor-point"
			@click="clickMore">
			<text v-if="moreText.length > 0" :style="{ color: moreColor }" class="uni-noticebar__more-text">{{ moreText }}</text>
			<uni-icons v-else type="right" :color="moreColor" size="16" />
		</view>
		<view class="uni-noticebar-close uni-cursor-point" v-if="(showClose === true || showClose === 'true') && (showGetMore === false || showGetMore === 'false')">
			<uni-icons
				type="closeempty" :color="color" size="16" @click="close" />
		</view>
	</view>
</template>

<script>
	// #ifdef APP-NVUE
	const dom = weex.requireModule('dom');
	const animation = weex.requireModule('animation');
	// #endif

	/**
	 * NoticeBar 自定义导航栏
	 * @description 通告栏组件
	 * @tutorial https://ext.dcloud.net.cn/plugin?id=30
	 * @property {Number} speed 文字滚动的速度，默认100px/秒
	 * @property {String} text 显示文字
	 * @property {String} backgroundColor 背景颜色
	 * @property {String} color 文字颜色
	 * @property {String} moreColor 查看更多文字的颜色
	 * @property {String} moreText 设置“查看更多”的文本
	 * @property {Boolean} single = [true|false] 是否单行
	 * @property {Boolean} scrollable = [true|false] 是否滚动，为true时，NoticeBar为单行
	 * @property {Boolean} showIcon = [true|false] 是否显示左侧喇叭图标
	 * @property {Boolean} showClose = [true|false] 是否显示左侧关闭按钮
	 * @property {Boolean} showGetMore = [true|false] 是否显示右侧查看更多图标，为true时，NoticeBar为单行
	 * @event {Function} click 点击 NoticeBar 触发事件
	 * @event {Function} close 关闭 NoticeBar 触发事件
	 * @event {Function} getmore 点击”查看更多“时触发事件
	 */

	export default {
		name: 'UniNoticeBar',
		emits: ['click', 'getmore', 'close'],
		props: {
			text: {
				type: String,
				default: ''
			},
			moreText: {
				type: String,
				default: ''
			},
			backgroundColor: {
				type: String,
				default: '#FFF9EA'
			},
			speed: {
				// 默认1s滚动100px
				type: Number,
				default: 100
			},
			color: {
				type: String,
				default: '#FF9A43'
			},
			moreColor: {
				type: String,
				default: '#FF9A43'
			},
			single: {
				// 是否单行
				type: [Boolean, String],
				default: false
			},
			scrollable: {
				// 是否滚动，添加后控制单行效果取消
				type: [Boolean, String],
				default: false
			},
			showIcon: {
				// 是否显示左侧icon
				type: [Boolean, String],
				default: false
			},
			showGetMore: {
				// 是否显示右侧查看更多
				type: [Boolean, String],
				default: false
			},
			showClose: {
				// 是否显示左侧关闭按钮
				type: [Boolean, String],
				default: false
			}
		},
		data() {
			const elId = `Uni_${Math.ceil(Math.random() * 10e5).toString(36)}`
			const elIdBox = `Uni_${Math.ceil(Math.random() * 10e5).toString(36)}`
			return {
				textWidth: 0,
				boxWidth: 0,
				wrapWidth: '',
				webviewHide: false,
				// #ifdef APP-NVUE
				stopAnimation: false,
				// #endif
				elId: elId,
				elIdBox: elIdBox,
				show: true,
				animationDuration: 'none',
				animationPlayState: 'paused',
				animationDelay: '0s'
			}
		},
		mounted() {
			// #ifdef APP-PLUS
			var pages = getCurrentPages();
			var page = pages[pages.length - 1];
			var currentWebview = page.$getAppWebview();
			currentWebview.addEventListener('hide', () => {
				this.webviewHide = true
			})
			currentWebview.addEventListener('show', () => {
				this.webviewHide = false
			})
			// #endif
			this.$nextTick(() => {
				this.initSize()
			})
		},
		// #ifdef APP-NVUE
		beforeDestroy() {
			this.stopAnimation = true
		},
		// #endif
		methods: {
			initSize() {
				if (this.scrollable) {
					// #ifndef APP-NVUE
					let query = [],
						boxWidth = 0,
						textWidth = 0;
					let textQuery = new Promise((resolve, reject) => {
						uni.createSelectorQuery()
							// #ifndef MP-ALIPAY
							.in(this)
							// #endif
							.select(`#${this.elId}`)
							.boundingClientRect()
							.exec(ret => {
								this.textWidth = ret[0].width
								resolve()
							})
					})
					let boxQuery = new Promise((resolve, reject) => {
						uni.createSelectorQuery()
							// #ifndef MP-ALIPAY
							.in(this)
							// #endif
							.select(`#${this.elIdBox}`)
							.boundingClientRect()
							.exec(ret => {
								this.boxWidth = ret[0].width
								resolve()
							})
					})
					query.push(textQuery)
					query.push(boxQuery)
					Promise.all(query).then(() => {
						this.animationDuration = `${this.textWidth / this.speed}s`
						this.animationDelay = `-${this.boxWidth / this.speed}s`
						setTimeout(() => {
							this.animationPlayState = 'running'
						}, 1000)
					})
					// #endif
					// #ifdef APP-NVUE
					dom.getComponentRect(this.$refs['animationEle'], (res) => {
						let winWidth = uni.getSystemInfoSync().windowWidth
						this.textWidth = res.size.width
						animation.transition(this.$refs['animationEle'], {
							styles: {
								transform: `translateX(-${winWidth}px)`
							},
							duration: 0,
							timingFunction: 'linear',
							delay: 0
						}, () => {
							if (!this.stopAnimation) {
								animation.transition(this.$refs['animationEle'], {
									styles: {
										transform: `translateX(-${this.textWidth}px)`
									},
									timingFunction: 'linear',
									duration: (this.textWidth - winWidth) / this.speed * 1000,
									delay: 1000
								}, () => {
									if (!this.stopAnimation) {
										this.loopAnimation()
									}
								});
							}
						});
					})
					// #endif
				}
				// #ifdef APP-NVUE
				if (!this.scrollable && (this.single || this.moreText)) {
					dom.getComponentRect(this.$refs['textBox'], (res) => {
						this.wrapWidth = res.size.width
					})
				}
				// #endif
			},
			loopAnimation() {
				// #ifdef APP-NVUE
				animation.transition(this.$refs['animationEle'], {
					styles: {
						transform: `translateX(0px)`
					},
					duration: 0
				}, () => {
					if (!this.stopAnimation) {
						animation.transition(this.$refs['animationEle'], {
							styles: {
								transform: `translateX(-${this.textWidth}px)`
							},
							duration: this.textWidth / this.speed * 1000,
							timingFunction: 'linear',
							delay: 0
						}, () => {
							if (!this.stopAnimation) {
								this.loopAnimation()
							}
						});
					}
				});
				// #endif
			},
			clickMore() {
				this.$emit('getmore')
			},
			close() {
				this.show = false;
				this.$emit('close')
			},
			onClick() {
				this.$emit('click')
			}
		}
	}
</script>

<style lang="scss" >
	.uni-noticebar {
		/* #ifndef APP-NVUE */
		display: flex;
		width: 100%;
		box-sizing: border-box;
		/* #endif */
		flex-direction: row;
		align-items: center;
		padding: 10px 12px;
		margin-bottom: 10px;
	}

	.uni-cursor-point {
		/* #ifdef H5 */
		cursor: pointer;
		/* #endif */
	}

	.uni-noticebar-close {
		margin-left: 8px;
		margin-right: 5px;
	}

	.uni-noticebar-icon {
		margin-right: 5px;
	}

	.uni-noticebar__content-wrapper {
		flex: 1;
		flex-direction: column;
		overflow: hidden;
	}

	.uni-noticebar__content-wrapper--single {
		/* #ifndef APP-NVUE */
		line-height: 18px;
		/* #endif */
	}

	.uni-noticebar__content-wrapper--single,
	.uni-noticebar__content-wrapper--scrollable {
		flex-direction: row;
	}

	/* #ifndef APP-NVUE */
	.uni-noticebar__content-wrapper--scrollable {
		position: relative;
		height: 18px;
	}

	/* #endif */

	.uni-noticebar__content--scrollable {
		/* #ifdef APP-NVUE */
		flex: 0;
		/* #endif */
		/* #ifndef APP-NVUE */
		flex: 1;
		display: block;
		overflow: hidden;
		/* #endif */
	}

	.uni-noticebar__content--single {
		/* #ifndef APP-NVUE */
		display: flex;
		flex: none;
		width: 100%;
		justify-content: center;
		/* #endif */
	}

	.uni-noticebar__content-text {
		font-size: 14px;
		line-height: 18px;
		/* #ifndef APP-NVUE */
		word-break: break-all;
		/* #endif */
	}

	.uni-noticebar__content-text--single {
		/* #ifdef APP-NVUE */
		lines: 1;
		/* #endif */
		/* #ifndef APP-NVUE */
		display: block;
		width: 100%;
		white-space: nowrap;
		/* #endif */
		overflow: hidden;
		text-overflow: ellipsis;
	}

	.uni-noticebar__content-text--scrollable {
		/* #ifdef APP-NVUE */
		lines: 1;
		padding-left: 750rpx;
		/* #endif */
		/* #ifndef APP-NVUE */
		position: absolute;
		display: block;
		height: 18px;
		line-height: 18px;
		white-space: nowrap;
		padding-left: 100%;
		animation: notice 10s 0s linear infinite both;
		animation-play-state: paused;
		/* #endif */
	}

	.uni-noticebar__more {
		/* #ifndef APP-NVUE */
		display: inline-flex;
		/* #endif */
		flex-direction: row;
		flex-wrap: nowrap;
		align-items: center;
		padding-left: 5px;
	}

	.uni-noticebar__more-text {
		font-size: 14px;
	}

	@keyframes notice {
		100% {
			transform: translate3d(-100%, 0, 0);
		}
	}
</style>
