<template>
	<view
		v-if="inited"
		class="u-transition"
		ref="u-transition"
		@tap="clickHandler"
		:class="classes"
		:style="[mergeStyle]"
		@touchmove="noop"
	>
		<slot />
	</view>
</template>

<script>
import props from './props.js';
// 组件的methods方法，由于内容较长，写在外部文件中通过mixin引入
import transition from "./transition.js";
/**
 * transition  动画组件
 * @description
 * @tutorial
 * @property {String}			show			是否展示组件 （默认 false ）
 * @property {String}			mode			使用的动画模式 （默认 'fade' ）
 * @property {String | Number}	duration		动画的执行时间，单位ms （默认 '300' ）
 * @property {String}			timingFunction	使用的动画过渡函数 （默认 'ease-out' ）
 * @property {Object}			customStyle		自定义样式
 * @event {Function} before-enter	进入前触发
 * @event {Function} enter			进入中触发
 * @event {Function} after-enter	进入后触发
 * @event {Function} before-leave	离开前触发
 * @event {Function} leave			离开中触发
 * @event {Function} after-leave	离开后触发
 * @example
 */
export default {
	name: 'u-transition',
	data() {
		return {
			inited: false, // 是否显示/隐藏组件
			viewStyle: {}, // 组件内部的样式
			status: '', // 记录组件动画的状态
			transitionEnded: false, // 组件是否结束的标记
			display: false, // 组件是否展示
			classes: '', // 应用的类名
		}
	},
	computed: {
	    mergeStyle() {
	        const { viewStyle, customStyle } = this
	        return {
	            // #ifndef APP-NVUE
	            transitionDuration: `${this.duration}ms`,
	            // display: `${this.display ? '' : 'none'}`,
				transitionTimingFunction: this.timingFunction,
	            // #endif
				// 避免自定义样式影响到动画属性，所以写在viewStyle前面
	            ...uni.$u.addStyle(customStyle),
	            ...viewStyle
	        }
	    }
	},
	// 将mixin挂在到组件中，uni.$u.mixin实际上为一个vue格式对象
	mixins: [uni.$u.mpMixin, uni.$u.mixin, transition, props],
	watch: {
		show: {
			handler(newVal) {
				// vue和nvue分别执行不同的方法
				// #ifdef APP-NVUE
				newVal ? this.nvueEnter() : this.nvueLeave()
				// #endif
				// #ifndef APP-NVUE
				newVal ? this.vueEnter() : this.vueLeave()
				// #endif
			},
			// 表示同时监听初始化时的props的show的意思
			immediate: true
		}
	}
}
</script>

<style lang="scss" scoped>
@import '../../libs/css/components.scss';

/* #ifndef APP-NVUE */
// vue版本动画相关的样式抽离在外部文件
@import './vue.ani-style.scss';
/* #endif */

.u-transition {}
</style>
