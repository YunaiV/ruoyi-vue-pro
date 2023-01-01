<template>
	<view class="u-index-list">
		<!-- #ifdef APP-NVUE -->
		<list
			:scrollTop="scrollTop"
			enable-back-to-top
			:offset-accuracy="1"
			:style="{
				maxHeight: $u.addUnit(scrollViewHeight)
			}"
			@scroll="scrollHandler"
			ref="uList"
		>
			<cell
				v-if="$slots.header"
				ref="header"
			>
				<slot name="header" />
			</cell>
			<slot />
			<cell v-if="$slots.footer">
				<slot name="footer" />
			</cell>
		</list>
		<!-- #endif -->
		<!-- #ifndef APP-NVUE -->
		<scroll-view
			:scrollTop="scrollTop"
			:scrollIntoView="scrollIntoView"
			:offset-accuracy="1"
			:style="{
				maxHeight: $u.addUnit(scrollViewHeight)
			}"
			scroll-y
			@scroll="scrollHandler"
			ref="uList"
		>
			<view v-if="$slots.header">
				<slot name="header" />
			</view>
			<slot />
			<view v-if="$slots.footer">
				<slot name="footer" />
			</view>
		</scroll-view>
		<!-- #endif -->
		<view
			class="u-index-list__letter"
			ref="u-index-list__letter"
			:style="{ top: $u.addUnit(letterInfo.top || 100) }"
			@touchstart="touchStart"
			@touchmove.stop.prevent="touchMove"
			@touchend.stop.prevent="touchEnd"
			@touchcancel.stop.prevent="touchEnd"
		>
			<view
				class="u-index-list__letter__item"
				v-for="(item, index) in uIndexList"
				:key="index"
				:style="{
					backgroundColor: activeIndex === index ? activeColor : 'transparent'
				}"
			>
				<text
					class="u-index-list__letter__item__index"
					:style="{color: activeIndex === index ? '#fff' : inactiveColor}"
				>{{ item }}</text>
			</view>
		</view>
		<u-transition
			mode="fade"
			:show="touching"
			:customStyle="{
				position: 'fixed',
				right: '50px',
				top: $u.addUnit(indicatorTop),
				zIndex: 2
			}"
		>
			<view
				class="u-index-list__indicator"
				:class="['u-index-list__indicator--show']"
				:style="{
					height: $u.addUnit(indicatorHeight),
					width: $u.addUnit(indicatorHeight)
				}"
			>
				<text class="u-index-list__indicator__text">{{ uIndexList[activeIndex] }}</text>
			</view>
		</u-transition>
	</view>
</template>

<script>
	const indexList = () => {
		const indexList = [];
		const charCodeOfA = 'A'.charCodeAt(0);
		for (let i = 0; i < 26; i++) {
			indexList.push(String.fromCharCode(charCodeOfA + i));
		}
		return indexList;
	}
	import props from './props.js';
	// #ifdef APP-NVUE
	// 由于weex为阿里的KPI业绩考核的产物，所以不支持百分比单位，这里需要通过dom查询组件的宽度
	const dom = uni.requireNativePlugin('dom')
	// #endif
	/**
	 * IndexList 索引列表
	 * @description  通过折叠面板收纳内容区域
	 * @tutorial https://uviewui.com/components/indexList.html
	 * @property {String}			inactiveColor	右边锚点非激活的颜色 ( 默认 '#606266' )
	 * @property {String}			activeColor		右边锚点激活的颜色 ( 默认 '#5677fc' )
	 * @property {Array}			indexList		索引字符列表，数组形式
	 * @property {Boolean}			sticky			是否开启锚点自动吸顶 ( 默认 true )
	 * @property {String | Number}	customNavHeight	自定义导航栏的高度 ( 默认 0 )
	 * */ 
	export default {
		name: 'u-index-list',
		mixins: [uni.$u.mpMixin, uni.$u.mixin, props],
		// #ifdef MP-WEIXIN
		// 将自定义节点设置成虚拟的，更加接近Vue组件的表现，能更好的使用flex属性
		options: {
			virtualHost: true
		},
		// #endif
		data() {
			return {
				// 当前正在被选中的字母索引
				activeIndex: -1,
				touchmoveIndex: 1,
				// 索引字母的信息
				letterInfo: {
					height: 0,
					itemHeight: 0,
					top: 0
				},
				// 设置字母指示器的高度，后面为了让指示器跟随字母，并将尖角部分指向字母的中部，需要依赖此值
				indicatorHeight: 50,
				// 字母放大指示器的top值，为了让其指向当前激活的字母
				// indicatorTop: 0
				// 当前是否正在被触摸状态
				touching: false,
				// 滚动条顶部top值
				scrollTop: 0,
				// scroll-view的高度
				scrollViewHeight: 0,
				// 系统信息
				sys: uni.$u.sys(),
				scrolling: false,
				scrollIntoView: '',
			}
		},
		computed: {
			// 如果有传入外部的indexList锚点数组则使用，否则使用内部生成A-Z字母
			uIndexList() {
				return this.indexList.length ? this.indexList : indexList()
			},
			// 字母放大指示器的top值，为了让其指向当前激活的字母
			indicatorTop() {
				const {
					top,
					itemHeight
				} = this.letterInfo
				return Math.floor(top + itemHeight * this.activeIndex + itemHeight / 2 - this.indicatorHeight / 2)
			}
		},
		watch: {
			// 监听字母索引的变化，重新设置尺寸
			uIndexList: {
				immediate: true,
				handler() {
					uni.$u.sleep().then(() => {
						this.setIndexListLetterInfo()
					})
				}
			}
		},
		created() {
			this.children = []
			this.anchors = []
			this.init()
		},
		mounted() {
			this.setIndexListLetterInfo()
		},
		methods: {
			init() {
				// 设置列表的高度为整个屏幕的高度
				//减去this.customNavHeight，并将this.scrollViewHeight设置为maxHeight
				//解决当u-index-list组件放在tabbar页面时,scroll-view内容较少时，还能滚动
				this.scrollViewHeight = this.sys.windowHeight - this.customNavHeight
			},
			// 索引列表被触摸
			touchStart(e) {
				// 获取触摸点信息
				const touchStart = e.changedTouches[0]
				if (!touchStart) return
				this.touching = true
				const {
					pageY
				} = touchStart
				// 根据当前触摸点的坐标，获取当前触摸的为第几个字母
				const currentIndex = this.getIndexListLetter(pageY)
				this.setValueForTouch(currentIndex)
			},
			// 索引字母列表被触摸滑动中
			touchMove(e) {
				// 获取触摸点信息
				let touchMove = e.changedTouches[0]
				if (!touchMove) return;

				// 滑动结束后迅速开始第二次滑动时候 touching 为 false 造成不显示 indicator 问题
				if (!this.touching) {
					this.touching = true
				}
				const {
					pageY
				} = touchMove
				const currentIndex = this.getIndexListLetter(pageY)
				this.setValueForTouch(currentIndex)
			},
			// 触摸结束
			touchEnd(e) {
				// 延时一定时间后再隐藏指示器，为了让用户看的更直观，同时也是为了消除快速切换u-transition的show带来的影响
				uni.$u.sleep(300).then(() => {
					this.touching = false
				})
			},
			// 获取索引列表的尺寸以及单个字符的尺寸信息
			getIndexListLetterRect() {
				return new Promise(resolve => {
					// 延时一定时间，以获取dom尺寸
					// #ifndef APP-NVUE
					this.$uGetRect('.u-index-list__letter').then(size => {
						resolve(size)
					})
					// #endif

					// #ifdef APP-NVUE
					const ref = this.$refs['u-index-list__letter']
					dom.getComponentRect(ref, res => {
						resolve(res.size)
					})
					// #endif
				})
			},
			// 设置indexList索引的尺寸信息
			setIndexListLetterInfo() {
				this.getIndexListLetterRect().then(size => {
					const {
						height
					} = size
					const sys = uni.$u.sys()
					const windowHeight = sys.windowHeight
					let customNavHeight = 0
					// 消除各端导航栏非原生和原生导致的差异，让索引列表字母对屏幕垂直居中
					if (this.customNavHeight == 0) {
						// #ifdef H5
						customNavHeight = sys.windowTop
						// #endif
						// #ifndef H5
						// 在非H5中，为原生导航栏，其高度不算在windowHeight内，这里设置为负值，后面相加时变成减去其高度的一半
						customNavHeight = -(sys.statusBarHeight + 44)
						// #endif
					} else {
						customNavHeight = uni.$u.getPx(this.customNavHeight)
					}
					this.letterInfo = {
						height,
						// 为了让字母列表对屏幕绝对居中，让其对导航栏进行修正，也即往上偏移导航栏的一半高度
						top: (windowHeight - height) / 2 + customNavHeight / 2,
						itemHeight: Math.floor(height / this.uIndexList.length)
					}
				})
			},
			// 获取当前被触摸的索引字母
			getIndexListLetter(pageY) {
				const {
					top,
					height,
					itemHeight
				} = this.letterInfo
				// 对H5的pageY进行修正，这是由于uni-app自作多情在H5中将触摸点的坐标跟H5的导航栏结合导致的问题
				// #ifdef H5
				pageY += uni.$u.sys().windowTop
				// #endif
				// 对第一和最后一个字母做边界处理，因为用户可能在字母列表上触摸到两端的尽头后依然继续滑动
				if (pageY < top) {
					return 0
				} else if (pageY >= top + height) {
					// 如果超出了，取最后一个字母
					return this.uIndexList.length - 1
				} else {
					// 将触摸点的Y轴偏移值，减去索引字母的top值，除以每个字母的高度，即可得到当前触摸点落在哪个字母上
					return Math.floor((pageY - top) / itemHeight);
				}
			},
			// 设置各项由触摸而导致变化的值
			setValueForTouch(currentIndex) {
				// 如果偏移量太小，前后得出的会是同一个索引字母，为了防抖，进行返回
				if (currentIndex === this.activeIndex) return
				this.activeIndex = currentIndex
				// #ifndef APP-NVUE || MP-WEIXIN
				// 在非nvue中，由于anchor和item都在u-index-item中，所以需要对index-item进行偏移
				this.scrollIntoView = `u-index-item-${this.uIndexList[currentIndex].charCodeAt(0)}`
				// #endif
				// #ifdef MP-WEIXIN
				// 微信小程序下，scroll-view的scroll-into-view属性无法对slot中的内容的id生效，只能通过设置scrollTop的形式去移动滚动条
				this.scrollTop = this.children[currentIndex].top
				// #endif
				// #ifdef APP-NVUE
				// 在nvue中，由于cell和header为同级元素，所以实际是需要对header(anchor)进行偏移
				const anchor = `u-index-anchor-${this.uIndexList[currentIndex]}`
				dom.scrollToElement(this.anchors[currentIndex].$refs[anchor], {
					offset: 0,
					animated: false
				})
				// #endif
			},
			getHeaderRect() {
				// 获取header slot的高度，因为list组件中获取元素的尺寸是没有top值的
				return new Promise(resolve => {
					dom.getComponentRect(this.$refs.header, res => {
						resolve(res.size)
					})
				})
			},
			// scroll-view的滚动事件
			async scrollHandler(e) {
				if (this.touching || this.scrolling) return
				// 每过一定时间取样一次，减少资源损耗以及可能带来的卡顿
				this.scrolling = true
				uni.$u.sleep(10).then(() => {
					this.scrolling = false
				})
				let scrollTop = 0
				const len = this.children.length
				let children = this.children
				const anchors = this.anchors
				// #ifdef APP-NVUE
				// nvue下获取的滚动条偏移为负数，需要转为正数
				scrollTop = Math.abs(e.contentOffset.y)
				// 获取header slot的尺寸信息
				const header = await this.getHeaderRect()
				// item的top值，在nvue下，模拟出的anchor的top，类似非nvue下的index-item的top
				let top = header.height
				// 由于list组件无法获取cell的top值，这里通过header slot和各个item之间的height，模拟出类似非nvue下的位置信息
				children = this.children.map((item, index) => {
					const child = {
						height: item.height,
						top
					}
					// 进行累加，给下一个item提供计算依据
					top += item.height + anchors[index].height
					return child
				})
				// #endif
				// #ifndef APP-NVUE
				// 非nvue通过detail获取滚动条位移
				scrollTop = e.detail.scrollTop
				// #endif
				for (let i = 0; i < len; i++) {
					const item = children[i],
						nextItem = children[i + 1]
					// 如果滚动条高度小于第一个item的top值，此时无需设置任意字母为高亮
					if (scrollTop <= children[0].top || scrollTop >= children[len - 1].top + children[len -
							1].height) {
						this.activeIndex = -1
						break
					} else if (!nextItem) { 
						// 当不存在下一个item时，意味着历遍到了最后一个
						this.activeIndex = len - 1
						break
					} else if (scrollTop > item.top && scrollTop < nextItem.top) {
						this.activeIndex = i
						break
					}
				}
			},
		},
	}
</script>

<style lang="scss" scoped>
	@import "../../libs/css/components.scss";

	.u-index-list {

		&__letter {
			position: fixed;
			right: 0;
			text-align: center;
			z-index: 3;
			padding: 0 6px;

			&__item {
				width: 16px;
				height: 16px;
				border-radius: 100px;
				margin: 1px 0;
				@include flex;
				align-items: center;
				justify-content: center;

				&--active {
					background-color: $u-primary;
				}

				&__index {
					font-size: 12px;
					text-align: center;
					line-height: 12px;
				}
			}
		}

		&__indicator {
			width: 50px;
			height: 50px;
			border-radius: 100px 100px 0 100px;
			text-align: center;
			color: #ffffff;
			background-color: #c9c9c9;
			transform: rotate(-45deg);
			@include flex;
			justify-content: center;
			align-items: center;

			&__text {
				font-size: 28px;
				line-height: 28px;
				font-weight: bold;
				color: #fff;
				transform: rotate(45deg);
				text-align: center;
			}
		}
	}
</style>
