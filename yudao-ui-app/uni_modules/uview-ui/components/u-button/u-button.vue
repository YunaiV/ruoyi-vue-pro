<template>
    <!-- #ifndef APP-NVUE -->
    <button
        :hover-start-time="Number(hoverStartTime)"
        :hover-stay-time="Number(hoverStayTime)"
        :form-type="formType"
        :open-type="openType"
        :app-parameter="appParameter"
        :hover-stop-propagation="hoverStopPropagation"
        :send-message-title="sendMessageTitle"
        :send-message-path="sendMessagePath"
        :lang="lang"
        :data-name="dataName"
        :session-from="sessionFrom"
        :send-message-img="sendMessageImg"
        :show-message-card="showMessageCard"
        @getphonenumber="getphonenumber"
        @getuserinfo="getuserinfo"
        @error="error"
        @opensetting="opensetting"
        @launchapp="launchapp"
        :hover-class="!disabled && !loading ? 'u-button--active' : ''"
        class="u-button u-reset-button"
        :style="[baseColor, $u.addStyle(customStyle)]"
        @tap="clickHandler"
        :class="bemClass"
    >
        <template v-if="loading">
            <u-loading-icon
                :mode="loadingMode"
                :size="loadingSize * 1.15"
                :color="loadingColor"
            ></u-loading-icon>
            <text
                class="u-button__loading-text"
                :style="[{ fontSize: textSize + 'px' }]"
                >{{ loadingText || text }}</text
            >
        </template>
        <template v-else>
            <u-icon
                v-if="icon"
                :name="icon"
                :color="iconColorCom"
                :size="textSize * 1.35"
                :customStyle="{ marginRight: '2px' }"
            ></u-icon>
            <slot>
                <text
                    class="u-button__text"
                    :style="[{ fontSize: textSize + 'px' }]"
                    >{{ text }}</text
                >
            </slot>
        </template>
    </button>
    <!-- #endif -->

    <!-- #ifdef APP-NVUE -->
    <view
        :hover-start-time="Number(hoverStartTime)"
        :hover-stay-time="Number(hoverStayTime)"
        class="u-button"
        :hover-class="
            !disabled && !loading && !color && (plain || type === 'info')
                ? 'u-button--active--plain'
                : !disabled && !loading && !plain
                ? 'u-button--active'
                : ''
        "
        @tap="clickHandler"
        :class="bemClass"
        :style="[baseColor, $u.addStyle(customStyle)]"
    >
        <template v-if="loading">
            <u-loading-icon
                :mode="loadingMode"
                :size="loadingSize * 1.15"
                :color="loadingColor"
            ></u-loading-icon>
            <text
                class="u-button__loading-text"
                :style="[nvueTextStyle]"
                :class="[plain && `u-button__text--plain--${type}`]"
                >{{ loadingText || text }}</text
            >
        </template>
        <template v-else>
            <u-icon
                v-if="icon"
                :name="icon"
                :color="iconColorCom"
                :size="textSize * 1.35"
            ></u-icon>
            <text
                class="u-button__text"
                :style="[
                    {
                        marginLeft: icon ? '2px' : 0,
                    },
                    nvueTextStyle,
                ]"
                :class="[plain && `u-button__text--plain--${type}`]"
                >{{ text }}</text
            >
        </template>
    </view>
    <!-- #endif -->
</template>

<script>
import button from "../../libs/mixin/button.js";
import openType from "../../libs/mixin/openType.js";
import props from "./props.js";
/**
 * button 按钮
 * @description Button 按钮
 * @tutorial https://www.uviewui.com/components/button.html
 *
 * @property {Boolean}			hairline				是否显示按钮的细边框 (默认 true )
 * @property {String}			type					按钮的预置样式，info，primary，error，warning，success (默认 'info' )
 * @property {String}			size					按钮尺寸，large，normal，mini （默认 normal）
 * @property {String}			shape					按钮形状，circle（两边为半圆），square（带圆角） （默认 'square' ）
 * @property {Boolean}			plain					按钮是否镂空，背景色透明 （默认 false）
 * @property {Boolean}			disabled				是否禁用 （默认 false）
 * @property {Boolean}			loading					按钮名称前是否带 loading 图标(App-nvue 平台，在 ios 上为雪花，Android上为圆圈) （默认 false）
 * @property {String | Number}	loadingText				加载中提示文字
 * @property {String}			loadingMode				加载状态图标类型 （默认 'spinner' ）
 * @property {String | Number}	loadingSize				加载图标大小 （默认 15 ）
 * @property {String}			openType				开放能力，具体请看uniapp稳定关于button组件部分说明
 * @property {String}			formType				用于 <form> 组件，点击分别会触发 <form> 组件的 submit/reset 事件
 * @property {String}			appParameter			打开 APP 时，向 APP 传递的参数，open-type=launchApp时有效 （注：只微信小程序、QQ小程序有效）
 * @property {Boolean}			hoverStopPropagation	指定是否阻止本节点的祖先节点出现点击态，微信小程序有效（默认 true ）
 * @property {String}			lang					指定返回用户信息的语言，zh_CN 简体中文，zh_TW 繁体中文，en 英文（默认 en ）
 * @property {String}			sessionFrom				会话来源，openType="contact"时有效
 * @property {String}			sendMessageTitle		会话内消息卡片标题，openType="contact"时有效
 * @property {String}			sendMessagePath			会话内消息卡片点击跳转小程序路径，openType="contact"时有效
 * @property {String}			sendMessageImg			会话内消息卡片图片，openType="contact"时有效
 * @property {Boolean}			showMessageCard			是否显示会话内消息卡片，设置此参数为 true，用户进入客服会话会在右下角显示"可能要发送的小程序"提示，用户点击后可以快速发送小程序消息，openType="contact"时有效（默认false）
 * @property {String}			dataName				额外传参参数，用于小程序的data-xxx属性，通过target.dataset.name获取
 * @property {String | Number}	throttleTime			节流，一定时间内只能触发一次 （默认 0 )
 * @property {String | Number}	hoverStartTime			按住后多久出现点击态，单位毫秒 （默认 0 )
 * @property {String | Number}	hoverStayTime			手指松开后点击态保留时间，单位毫秒 （默认 200 )
 * @property {String | Number}	text					按钮文字，之所以通过props传入，是因为slot传入的话（注：nvue中无法控制文字的样式）
 * @property {String}			icon					按钮图标
 * @property {String}			iconColor				按钮图标颜色
 * @property {String}			color					按钮颜色，支持传入linear-gradient渐变色
 * @property {Object}			customStyle				定义需要用到的外部样式
 *
 * @event {Function}	click			非禁止并且非加载中，才能点击
 * @event {Function}	getphonenumber	open-type="getPhoneNumber"时有效
 * @event {Function}	getuserinfo		用户点击该按钮时，会返回获取到的用户信息，从返回参数的detail中获取到的值同uni.getUserInfo
 * @event {Function}	error			当使用开放能力时，发生错误的回调
 * @event {Function}	opensetting		在打开授权设置页并关闭后回调
 * @event {Function}	launchapp		打开 APP 成功的回调
 * @example <u-button>月落</u-button>
 */
export default {
    name: "u-button",
    // #ifdef MP
    mixins: [uni.$u.mpMixin, uni.$u.mixin, button, openType, props],
    // #endif
    // #ifndef MP
    mixins: [uni.$u.mpMixin, uni.$u.mixin, props],
    // #endif
    data() {
        return {};
    },
    computed: {
        // 生成bem风格的类名
        bemClass() {
            // this.bem为一个computed变量，在mixin中
            if (!this.color) {
                return this.bem(
                    "button",
                    ["type", "shape", "size"],
                    ["disabled", "plain", "hairline"]
                );
            } else {
                // 由于nvue的原因，在有color参数时，不需要传入type，否则会生成type相关的类型，影响最终的样式
                return this.bem(
                    "button",
                    ["shape", "size"],
                    ["disabled", "plain", "hairline"]
                );
            }
        },
        loadingColor() {
            if (this.plain) {
                // 如果有设置color值，则用color值，否则使用type主题颜色
                return this.color
                    ? this.color
                    : uni.$u.config.color[`u-${this.type}`];
            }
            if (this.type === "info") {
                return "#c9c9c9";
            }
            return "rgb(200, 200, 200)";
        },
        iconColorCom() {
            // 如果是镂空状态，设置了color就用color值，否则使用主题颜色，
            // u-icon的color能接受一个主题颜色的值
			if (this.iconColor) return this.iconColor;
			if (this.plain) {
                return this.color ? this.color : this.type;
            } else {
                return this.type === "info" ? "#000000" : "#ffffff";
            }
        },
        baseColor() {
            let style = {};
            if (this.color) {
                // 针对自定义了color颜色的情况，镂空状态下，就是用自定义的颜色
                style.color = this.plain ? this.color : "white";
                if (!this.plain) {
                    // 非镂空，背景色使用自定义的颜色
                    style["background-color"] = this.color;
                }
                if (this.color.indexOf("gradient") !== -1) {
                    // 如果自定义的颜色为渐变色，不显示边框，以及通过backgroundImage设置渐变色
                    // weex文档说明可以写borderWidth的形式，为什么这里需要分开写？
                    // 因为weex是阿里巴巴为了部门业绩考核而做的你懂的东西，所以需要这么写才有效
                    style.borderTopWidth = 0;
                    style.borderRightWidth = 0;
                    style.borderBottomWidth = 0;
                    style.borderLeftWidth = 0;
                    if (!this.plain) {
                        style.backgroundImage = this.color;
                    }
                } else {
                    // 非渐变色，则设置边框相关的属性
                    style.borderColor = this.color;
                    style.borderWidth = "1px";
                    style.borderStyle = "solid";
                }
            }
            return style;
        },
        // nvue版本按钮的字体不会继承父组件的颜色，需要对每一个text组件进行单独的设置
        nvueTextStyle() {
            let style = {};
            // 针对自定义了color颜色的情况，镂空状态下，就是用自定义的颜色
            if (this.type === "info") {
                style.color = "#323233";
            }
            if (this.color) {
                style.color = this.plain ? this.color : "white";
            }
            style.fontSize = this.textSize + "px";
            return style;
        },
        // 字体大小
        textSize() {
            let fontSize = 14,
                { size } = this;
            if (size === "large") fontSize = 16;
            if (size === "normal") fontSize = 14;
            if (size === "small") fontSize = 12;
            if (size === "mini") fontSize = 10;
            return fontSize;
        },
    },
    methods: {
        clickHandler() {
            // 非禁止并且非加载中，才能点击
            if (!this.disabled && !this.loading) {
				// 进行节流控制，每this.throttle毫秒内，只在开始处执行
				uni.$u.throttle(() => {
					this.$emit("click");
				}, this.throttleTime);
            }
        },
        // 下面为对接uniapp官方按钮开放能力事件回调的对接
        getphonenumber(res) {
            this.$emit("getphonenumber", res);
        },
        getuserinfo(res) {
            this.$emit("getuserinfo", res);
        },
        error(res) {
            this.$emit("error", res);
        },
        opensetting(res) {
            this.$emit("opensetting", res);
        },
        launchapp(res) {
            this.$emit("launchapp", res);
        },
    },
};
</script>

<style lang="scss" scoped>
@import "../../libs/css/components.scss";

/* #ifndef APP-NVUE */
@import "./vue.scss";
/* #endif */

/* #ifdef APP-NVUE */
@import "./nvue.scss";
/* #endif */

$u-button-u-button-height: 40px !default;
$u-button-text-font-size: 15px !default;
$u-button-loading-text-font-size: 15px !default;
$u-button-loading-text-margin-left: 4px !default;
$u-button-large-width: 100% !default;
$u-button-large-height: 50px !default;
$u-button-normal-padding: 0 12px !default;
$u-button-large-padding: 0 15px !default;
$u-button-normal-font-size: 14px !default;
$u-button-small-min-width: 60px !default;
$u-button-small-height: 30px !default;
$u-button-small-padding: 0px 8px !default;
$u-button-mini-padding: 0px 8px !default;
$u-button-small-font-size: 12px !default;
$u-button-mini-height: 22px !default;
$u-button-mini-font-size: 10px !default;
$u-button-mini-min-width: 50px !default;
$u-button-disabled-opacity: 0.5 !default;
$u-button-info-color: #323233 !default;
$u-button-info-background-color: #fff !default;
$u-button-info-border-color: #ebedf0 !default;
$u-button-info-border-width: 1px !default;
$u-button-info-border-style: solid !default;
$u-button-success-color: #fff !default;
$u-button-success-background-color: $u-success !default;
$u-button-success-border-color: $u-button-success-background-color !default;
$u-button-success-border-width: 1px !default;
$u-button-success-border-style: solid !default;
$u-button-primary-color: #fff !default;
$u-button-primary-background-color: $u-primary !default;
$u-button-primary-border-color: $u-button-primary-background-color !default;
$u-button-primary-border-width: 1px !default;
$u-button-primary-border-style: solid !default;
$u-button-error-color: #fff !default;
$u-button-error-background-color: $u-error !default;
$u-button-error-border-color: $u-button-error-background-color !default;
$u-button-error-border-width: 1px !default;
$u-button-error-border-style: solid !default;
$u-button-warning-color: #fff !default;
$u-button-warning-background-color: $u-warning !default;
$u-button-warning-border-color: $u-button-warning-background-color !default;
$u-button-warning-border-width: 1px !default;
$u-button-warning-border-style: solid !default;
$u-button-block-width: 100% !default;
$u-button-circle-border-top-right-radius: 100px !default;
$u-button-circle-border-top-left-radius: 100px !default;
$u-button-circle-border-bottom-left-radius: 100px !default;
$u-button-circle-border-bottom-right-radius: 100px !default;
$u-button-square-border-top-right-radius: 3px !default;
$u-button-square-border-top-left-radius: 3px !default;
$u-button-square-border-bottom-left-radius: 3px !default;
$u-button-square-border-bottom-right-radius: 3px !default;
$u-button-icon-min-width: 1em !default;
$u-button-plain-background-color: #fff !default;
$u-button-hairline-border-width: 0.5px !default;

.u-button {
    height: $u-button-u-button-height;
    position: relative;
    align-items: center;
    justify-content: center;
    @include flex;
    /* #ifndef APP-NVUE */
    box-sizing: border-box;
    /* #endif */
    flex-direction: row;

    &__text {
        font-size: $u-button-text-font-size;
    }

    &__loading-text {
        font-size: $u-button-loading-text-font-size;
        margin-left: $u-button-loading-text-margin-left;
    }

    &--large {
        /* #ifndef APP-NVUE */
        width: $u-button-large-width;
        /* #endif */
        height: $u-button-large-height;
        padding: $u-button-large-padding;
    }

    &--normal {
        padding: $u-button-normal-padding;
        font-size: $u-button-normal-font-size;
    }

    &--small {
        /* #ifndef APP-NVUE */
        min-width: $u-button-small-min-width;
        /* #endif */
        height: $u-button-small-height;
        padding: $u-button-small-padding;
        font-size: $u-button-small-font-size;
    }

    &--mini {
        height: $u-button-mini-height;
        font-size: $u-button-mini-font-size;
        /* #ifndef APP-NVUE */
        min-width: $u-button-mini-min-width;
        /* #endif */
        padding: $u-button-mini-padding;
    }

    &--disabled {
        opacity: $u-button-disabled-opacity;
    }

    &--info {
        color: $u-button-info-color;
        background-color: $u-button-info-background-color;
        border-color: $u-button-info-border-color;
        border-width: $u-button-info-border-width;
        border-style: $u-button-info-border-style;
    }

    &--success {
        color: $u-button-success-color;
        background-color: $u-button-success-background-color;
        border-color: $u-button-success-border-color;
        border-width: $u-button-success-border-width;
        border-style: $u-button-success-border-style;
    }

    &--primary {
        color: $u-button-primary-color;
        background-color: $u-button-primary-background-color;
        border-color: $u-button-primary-border-color;
        border-width: $u-button-primary-border-width;
        border-style: $u-button-primary-border-style;
    }

    &--error {
        color: $u-button-error-color;
        background-color: $u-button-error-background-color;
        border-color: $u-button-error-border-color;
        border-width: $u-button-error-border-width;
        border-style: $u-button-error-border-style;
    }

    &--warning {
        color: $u-button-warning-color;
        background-color: $u-button-warning-background-color;
        border-color: $u-button-warning-border-color;
        border-width: $u-button-warning-border-width;
        border-style: $u-button-warning-border-style;
    }

    &--block {
        @include flex;
        width: $u-button-block-width;
    }

    &--circle {
        border-top-right-radius: $u-button-circle-border-top-right-radius;
        border-top-left-radius: $u-button-circle-border-top-left-radius;
        border-bottom-left-radius: $u-button-circle-border-bottom-left-radius;
        border-bottom-right-radius: $u-button-circle-border-bottom-right-radius;
    }

    &--square {
        border-bottom-left-radius: $u-button-square-border-top-right-radius;
        border-bottom-right-radius: $u-button-square-border-top-left-radius;
        border-top-left-radius: $u-button-square-border-bottom-left-radius;
        border-top-right-radius: $u-button-square-border-bottom-right-radius;
    }

    &__icon {
        /* #ifndef APP-NVUE */
        min-width: $u-button-icon-min-width;
        line-height: inherit !important;
        vertical-align: top;
        /* #endif */
    }

    &--plain {
        background-color: $u-button-plain-background-color;
    }

    &--hairline {
        border-width: $u-button-hairline-border-width !important;
    }
}
</style>
