<template>
    <view
        class="u-subsection"
        ref="u-subsection"
        :class="[`u-subsection--${mode}`]"
        :style="[$u.addStyle(customStyle), wrapperStyle]"
    >
        <view
            class="u-subsection__bar"
            ref="u-subsection__bar"
            :style="[barStyle]"
            :class="[
                mode === 'button' && 'u-subsection--button__bar',
                current === 0 &&
                    mode === 'subsection' &&
                    'u-subsection__bar--first',
                current > 0 &&
                    current < list.length - 1 &&
                    mode === 'subsection' &&
                    'u-subsection__bar--center',
                current === list.length - 1 &&
                    mode === 'subsection' &&
                    'u-subsection__bar--last',
            ]"
        ></view>
        <view
            class="u-subsection__item"
            :class="[
                `u-subsection__item--${index}`,
                index < list.length - 1 &&
                    'u-subsection__item--no-border-right',
                index === 0 && 'u-subsection__item--first',
                index === list.length - 1 && 'u-subsection__item--last',
            ]"
            :ref="`u-subsection__item--${index}`"
            :style="[itemStyle(index)]"
            @tap="clickHandler(index)"
            v-for="(item, index) in list"
            :key="index"
        >
            <text
                class="u-subsection__item__text"
                :style="[textStyle(index)]"
                >{{ getText(item) }}</text
            >
        </view>
    </view>
</template>

<script>
// #ifdef APP-NVUE
const dom = uni.requireNativePlugin("dom");
const animation = uni.requireNativePlugin("animation");
// #endif
import props from "./props.js";
/**
 * Subsection 分段器
 * @description 该分段器一般用于用户从几个选项中选择某一个的场景
 * @tutorial https://www.uviewui.com/components/subsection.html
 * @property {Array}			list			tab的数据
 * @property {String ｜ Number}	current			当前活动的tab的index（默认 0 ）
 * @property {String}			activeColor		激活时的颜色（默认 '#3c9cff' ）
 * @property {String}			inactiveColor	未激活时的颜色（默认 '#303133' ）
 * @property {String}			mode			模式选择，mode=button为按钮形式，mode=subsection时为分段模式（默认 'button' ）
 * @property {String ｜ Number}	fontSize		字体大小，单位px（默认 12 ）
 * @property {Boolean}			bold			激活选项的字体是否加粗（默认 true ）
 * @property {String}			bgColor			组件背景颜色，mode为button时有效（默认 '#eeeeef' ）
 * @property {Object}			customStyle		定义需要用到的外部样式
 * @property {String}	        keyName	        从`list`元素对象中读取的键名（默认 'name' ）
 *
 * @event {Function} change		分段器选项发生改变时触发  回调 index：选项的index索引值，从0开始
 * @example <u-subsection :list="list" :current="curNow" @change="sectionChange"></u-subsection>
 */
export default {
    name: "u-subsection",
    mixins: [uni.$u.mpMixin, uni.$u.mixin, props],
    data() {
        return {
            // 组件尺寸
            itemRect: {
                width: 0,
                height: 0,
            },
        };
    },
    watch: {
        list(newValue, oldValue) {
            this.init();
        },
        current: {
            immediate: true,
            handler(n) {
                // #ifdef APP-NVUE
                // 在安卓nvue上，如果通过translateX进行位移，到最后一个时，会导致右侧无法绘制圆角
                // 故用animation模块进行位移
                const ref = this.$refs?.["u-subsection__bar"]?.ref;
                // 不存在ref的时候(理解为第一次初始化时，需要渲染dom，进行一定延时再获取ref)，这里的100ms是经过测试得出的结果(某些安卓需要延时久一点)，勿随意修改
                uni.$u.sleep(ref ? 0 : 100).then(() => {
                    animation.transition(this.$refs["u-subsection__bar"].ref, {
                        styles: {
                            transform: `translateX(${
                                n * this.itemRect.width
                            }px)`,
                            transformOrigin: "center center",
                        },
                        duration: 300,
                    });
                });
                // #endif
            },
        },
    },
    computed: {
        wrapperStyle() {
            const style = {};
            // button模式时，设置背景色
            if (this.mode === "button") {
                style.backgroundColor = this.bgColor;
            }
            return style;
        },
        // 滑块的样式
        barStyle() {
            const style = {};
            style.width = `${this.itemRect.width}px`;
            style.height = `${this.itemRect.height}px`;
            // 通过translateX移动滑块，其移动的距离为索引*item的宽度
            // #ifndef APP-NVUE
            style.transform = `translateX(${
                this.current * this.itemRect.width
            }px)`;
            // #endif
            if (this.mode === "subsection") {
                // 在subsection模式下，需要动态设置滑块的圆角，因为移动滑块使用的是translateX，无法通过父元素设置overflow: hidden隐藏滑块的直角
                style.backgroundColor = this.activeColor;
            }
            return style;
        },
        // 分段器item的样式
        itemStyle(index) {
            return (index) => {
                const style = {};
                if (this.mode === "subsection") {
                    // 设置border的样式
                    style.borderColor = this.activeColor;
                    style.borderWidth = "1px";
                    style.borderStyle = "solid";
                }
                return style;
            };
        },
        // 分段器文字颜色
        textStyle(index) {
            return (index) => {
                const style = {};
                style.fontWeight =
                    this.bold && this.current === index ? "bold" : "normal";
                style.fontSize = uni.$u.addUnit(this.fontSize);
                // subsection模式下，激活时默认为白色的文字
                if (this.mode === "subsection") {
                    style.color =
                        this.current === index ? "#fff" : this.inactiveColor;
                } else {
                    // button模式下，激活时文字颜色默认为activeColor
                    style.color =
                        this.current === index
                            ? this.activeColor
                            : this.inactiveColor;
                }
                return style;
            };
        },
    },
    mounted() {
        this.init();
    },
    methods: {
        init() {
            uni.$u.sleep().then(() => this.getRect());
        },
		// 判断展示文本
		getText(item) {
			return typeof item === 'object' ? item[this.keyName] : item
		},
        // 获取组件的尺寸
        getRect() {
            // #ifndef APP-NVUE
            this.$uGetRect(".u-subsection__item--0").then((size) => {
                this.itemRect = size;
            });
            // #endif

            // #ifdef APP-NVUE
            const ref = this.$refs["u-subsection__item--0"][0];
            ref &&
                dom.getComponentRect(ref, (res) => {
                    this.itemRect = res.size;
                });
            // #endif
        },
        clickHandler(index) {
            this.$emit("change", index);
        },
    },
};
</script>

<style lang="scss" scoped>
@import "../../libs/css/components.scss";

.u-subsection {
    @include flex;
    position: relative;
    overflow: hidden;
	/* #ifndef APP-NVUE */
	width: 100%;
	box-sizing: border-box;
	/* #endif */

    &--button {
        height: 32px;
        background-color: rgb(238, 238, 239);
        padding: 3px;
        border-radius: 3px;
        align-items: stretch;

        &__bar {
            background-color: #ffffff;
            border-radius: 3px !important;
        }
    }

    &--subsection {
        height: 30px;
    }

    &__bar {
        position: absolute;
        /* #ifndef APP-NVUE */
        transition-property: transform, color;
        transition-duration: 0.3s;
        transition-timing-function: ease-in-out;
        /* #endif */

        &--first {
            border-top-left-radius: 3px;
            border-bottom-left-radius: 3px;
            border-top-right-radius: 0px;
            border-bottom-right-radius: 0px;
        }

        &--center {
            border-top-left-radius: 0px;
            border-bottom-left-radius: 0px;
            border-top-right-radius: 0px;
            border-bottom-right-radius: 0px;
        }

        &--last {
            border-top-left-radius: 0px;
            border-bottom-left-radius: 0px;
            border-top-right-radius: 3px;
            border-bottom-right-radius: 3px;
        }
    }

    &__item {
        @include flex;
        flex: 1;
        justify-content: center;
        align-items: center;
        // vue环境下，需要设置相对定位，因为滑块为绝对定位，item需要在滑块的上面
        position: relative;

        &--no-border-right {
            border-right-width: 0 !important;
        }

        &--first {
            border-top-left-radius: 3px;
            border-bottom-left-radius: 3px;
        }

        &--last {
            border-top-right-radius: 3px;
            border-bottom-right-radius: 3px;
        }

        &__text {
            font-size: 12px;
            line-height: 12px;
            @include flex;
            align-items: center;
            transition-property: color;
            transition-duration: 0.3s;
        }
    }
}
</style>
