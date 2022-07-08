<template>
    <view
        class="u-rate"
        :id="elId"
        ref="u-rate"
        :style="[$u.addStyle(customStyle)]"
    >
        <view
            class="u-rate__content"
            @touchmove.stop="touchMove"
            @touchend.stop="touchEnd"
        >
            <view
                class="u-rate__content__item"
                v-for="(item, index) in Number(count)"
                :key="index"
                :class="[elClass]"
            >
                <view
                    class="u-rate__content__item__icon-wrap"
                    ref="u-rate__content__item__icon-wrap"
                    @tap.stop="clickHandler($event, index + 1)"
                >
                    <u-icon
                        :name="
                            Math.floor(activeIndex) > index
                                ? activeIcon
                                : inactiveIcon
                        "
                        :color="
                            disabled
                                ? '#c8c9cc'
                                : Math.floor(activeIndex) > index
                                ? activeColor
                                : inactiveColor
                        "
                        :custom-style="{
                            padding: `0 ${$u.addUnit(gutter / 2)}`,
                        }"
                        :size="size"
                    ></u-icon>
                </view>
                <view
                    v-if="allowHalf"
                    @tap.stop="clickHandler($event, index + 1)"
                    class="u-rate__content__item__icon-wrap u-rate__content__item__icon-wrap--half"
                    :style="[{
                        width: $u.addUnit(rateWidth / 2),
                    }]"
                    ref="u-rate__content__item__icon-wrap"
                >
                    <u-icon
                        :name="
                            Math.ceil(activeIndex) > index
                                ? activeIcon
                                : inactiveIcon
                        "
                        :color="
                            disabled
                                ? '#c8c9cc'
                                : Math.ceil(activeIndex) > index
                                ? activeColor
                                : inactiveColor
                        "
                        :custom-style="{
                            padding: `0 ${$u.addUnit(gutter / 2)}`
                        }"
                        :size="size"
                    ></u-icon>
                </view>
            </view>
        </view>
    </view>
</template>

<script>
	import props from './props.js';

	// #ifdef APP-NVUE
	const dom = weex.requireModule("dom");
	// #endif
	/**
	 * rate 评分
	 * @description 该组件一般用于满意度调查，星型评分的场景
	 * @tutorial https://www.uviewui.com/components/rate.html
	 * @property {String | Number}	value			用于v-model双向绑定选中的星星数量 (默认 1 )
	 * @property {String | Number}	count			最多可选的星星数量 （默认 5 ）
	 * @property {Boolean}			disabled		是否禁止用户操作 （默认 false ）
	 * @property {Boolean}			readonly		是否只读 （默认 false ）
	 * @property {String | Number}	size			星星的大小，单位px （默认 18 ）
	 * @property {String}			inactiveColor	未选中星星的颜色 （默认 '#b2b2b2' ）
	 * @property {String}			activeColor		选中的星星颜色 （默认 '#FA3534' ）
	 * @property {String | Number}	gutter			星星之间的距离 （默认 4 ）
	 * @property {String | Number}	minCount		最少选中星星的个数 （默认 1 ）
	 * @property {Boolean}			allowHalf		是否允许半星选择 （默认 false ）
	 * @property {String}			activeIcon		选中时的图标名，只能为uView的内置图标 （默认 'star-fill' ）
	 * @property {String}			inactiveIcon	未选中时的图标名，只能为uView的内置图标 （默认 'star' ）
	 * @property {Boolean}			touchable		是否可以通过滑动手势选择评分 （默认 'true' ）
	 * @property {Object}			customStyle		组件的样式，对象形式
	 * @event {Function} change 选中的星星发生变化时触发
	 * @example <u-rate :count="count" :value="2"></u-rate>
	 */
	export default {
		name: "u-rate",
		mixins: [uni.$u.mpMixin, uni.$u.mixin,props],
		data() {
			return {
				// 生成一个唯一id，否则一个页面多个评分组件，会造成冲突
				elId: uni.$u.guid(),
				elClass: uni.$u.guid(),
				rateBoxLeft: 0, // 评分盒子左边到屏幕左边的距离，用于滑动选择时计算距离
				activeIndex: this.value,
				rateWidth: 0, // 每个星星的宽度
				// 标识是否正在滑动，由于iOS事件上touch比click先触发，导致快速滑动结束后，接着触发click，导致事件混乱而出错
				moving: false,
			};
		},
		watch: {
			value(val) {
				this.activeIndex = val;
			},
			activeIndex: 'emitEvent'
		},
		methods: {
			init() {
				uni.$u.sleep().then(() => {
					this.getRateItemRect();
					this.getRateIconWrapRect();
				})
			},
			// 获取评分组件盒子的布局信息
			async getRateItemRect() {
				await uni.$u.sleep();
				// uView封装的获取节点的方法，详见文档
				// #ifndef APP-NVUE
				this.$uGetRect("#" + this.elId).then((res) => {
					this.rateBoxLeft = res.left;
				});
				// #endif
				// #ifdef APP-NVUE
				dom.getComponentRect(this.$refs["u-rate"], (res) => {
					this.rateBoxLeft = res.size.left;
				});
				// #endif
			},
			// 获取单个星星的尺寸
			getRateIconWrapRect() {
				// uView封装的获取节点的方法，详见文档
				// #ifndef APP-NVUE
				this.$uGetRect("." + this.elClass).then((res) => {
					this.rateWidth = res.width;
				});
				// #endif
				// #ifdef APP-NVUE
				dom.getComponentRect(
					this.$refs["u-rate__content__item__icon-wrap"][0],
					(res) => {
						this.rateWidth = res.size.width;
					}
				);
				// #endif
			},
			// 手指滑动
			touchMove(e) {
				// 如果禁止通过手动滑动选择，返回
				if (!this.touchable) {
					return;
				}
				this.preventEvent(e);
				const x = e.changedTouches[0].pageX;
				this.getActiveIndex(x);
			},
			// 停止滑动
			touchEnd(e) {
				// 如果禁止通过手动滑动选择，返回
				if (!this.touchable) {
					return;
				}
				this.preventEvent(e);
				const x = e.changedTouches[0].pageX;
				this.getActiveIndex(x);
			},
			// 通过点击，直接选中
			clickHandler(e, index) {
				// ios上，moving状态取消事件触发
				if (uni.$u.os() === "ios" && this.moving) {
					return;
				}
				this.preventEvent(e);
				let x = 0;
				// 点击时，在nvue上，无法获得点击的坐标，所以无法实现点击半星选择
				// #ifndef APP-NVUE
				x = e.changedTouches[0].pageX;
				// #endif
				// #ifdef APP-NVUE
				// nvue下，无法通过点击获得坐标信息，这里通过元素的位置尺寸值模拟坐标
				x = index * this.rateWidth + this.rateBoxLeft;
				// #endif
				this.getActiveIndex(x,true);
			},
			// 发出事件
			emitEvent() {
				// 发出change事件
				this.$emit("change", this.activeIndex);
				// 同时修改双向绑定的value的值
				this.$emit("input", this.activeIndex);
			},
			// 获取当前激活的评分图标
			getActiveIndex(x,isClick = false) {
				if (this.disabled || this.readonly) {
					return;
				}
				// 判断当前操作的点的x坐标值，是否在允许的边界范围内
				const allRateWidth = this.rateWidth * this.count + this.rateBoxLeft;
				// 如果小于第一个图标的左边界，设置为最小值，如果大于所有图标的宽度，则设置为最大值
				x = uni.$u.range(this.rateBoxLeft, allRateWidth, x) - this.rateBoxLeft
				// 滑动点相对于评分盒子左边的距离
				const distance = x;
				// 滑动的距离，相当于多少颗星星
				let index;
				// 判断是否允许半星
				if (this.allowHalf) {
					index = Math.floor(distance / this.rateWidth);
					// 取余，判断小数的区间范围
					const decimal = distance % this.rateWidth;
					if (decimal <= this.rateWidth / 2 && decimal > 0) {
						index += 0.5;
					} else if (decimal > this.rateWidth / 2) {
						index++;
					}
				} else {
					index = Math.floor(distance / this.rateWidth);
					// 取余，判断小数的区间范围
					const decimal = distance % this.rateWidth;
					// 非半星时，只有超过了图标的一半距离，才认为是选择了这颗星
					if (isClick){
						if (decimal > 0) index++;
					} else {
						if (decimal > this.rateWidth / 2) index++;
					}

				}
				this.activeIndex = Math.min(index, this.count);
				// 对最少颗星星的限制
				if (this.activeIndex < this.minCount) {
					this.activeIndex = this.minCount;
				}

				// 设置延时为了让click事件在touchmove之前触发
				setTimeout(() => {
					this.moving = true;
				}, 10);
				// 一定时间后，取消标识为移动中状态，是为了让click事件无效
				setTimeout(() => {
					this.moving = false;
				}, 10);
			},
		},
		mounted() {
			this.init();
		},
	};
</script>

<style lang="scss" scoped>
@import "../../libs/css/components.scss";
$u-rate-margin: 0 !default;
$u-rate-padding: 0 !default;
$u-rate-item-icon-wrap-half-top: 0 !default;
$u-rate-item-icon-wrap-half-left: 0 !default;

.u-rate {
    @include flex;
    align-items: center;
    margin: $u-rate-margin;
    padding: $u-rate-padding;
    /* #ifndef APP-NVUE */
    touch-action: none;
    /* #endif */

    &__content {
        @include flex;

		&__item {
		    position: relative;

		    &__icon-wrap {
		        &--half {
		            position: absolute;
		            overflow: hidden;
		            top: $u-rate-item-icon-wrap-half-top;
		            left: $u-rate-item-icon-wrap-half-left;
		        }
		    }
		}
    }
}

.u-icon {
    /* #ifndef APP-NVUE */
    box-sizing: border-box;
    /* #endif */
}
</style>
