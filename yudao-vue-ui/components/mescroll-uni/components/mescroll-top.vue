<!-- 回到顶部的按钮 -->
<template>
	<image
		v-if="mOption.src"
		class="mescroll-totop"
		:class="[value ? 'mescroll-totop-in' : 'mescroll-totop-out', {'mescroll-totop-safearea': mOption.safearea}]"
		:style="{'z-index':mOption.zIndex, 'left': left, 'right': right, 'bottom':addUnit(mOption.bottom), 'width':addUnit(mOption.width), 'border-radius':addUnit(mOption.radius)}"
		:src="mOption.src"
		mode="widthFix"
		@click="toTopClick"
	/>
</template>

<script>
export default {
	props: {
		// up.toTop的配置项
		option: Object,
		// 是否显示
		value: false
	},
	computed: {
		// 支付宝小程序需写成计算属性,prop定义default仍报错
		mOption(){
			return this.option || {}
		},
		// 优先显示左边
		left(){
			return this.mOption.left ? this.addUnit(this.mOption.left) : 'auto';
		},
		// 右边距离 (优先显示左边)
		right() {
			return this.mOption.left ? 'auto' : this.addUnit(this.mOption.right);
		}
	},
	methods: {
		addUnit(num){
			if(!num) return 0;
			if(typeof num === 'number') return num + 'rpx';
			return num
		},
		toTopClick() {
			this.$emit('input', false); // 使v-model生效
			this.$emit('click'); // 派发点击事件
		}
	}
};
</script>

<style>
/* 回到顶部的按钮 */
.mescroll-totop {
	z-index: 9990;
	position: fixed !important; /* 加上important避免编译到H5,在多mescroll中定位失效 */
	right: 20rpx;
	bottom: 120rpx;
	width: 72rpx;
	height: auto;
	border-radius: 50%;
	opacity: 0;
	transition: opacity 0.5s; /* 过渡 */
	margin-bottom: var(--window-bottom); /* css变量 */
}

/* 适配 iPhoneX */
@supports (bottom: constant(safe-area-inset-bottom)) or (bottom: env(safe-area-inset-bottom)) {
	.mescroll-totop-safearea {
		margin-bottom: calc(var(--window-bottom) + constant(safe-area-inset-bottom)); /* window-bottom + 适配 iPhoneX */
		margin-bottom: calc(var(--window-bottom) + env(safe-area-inset-bottom));
	}
}

/* 显示 -- 淡入 */
.mescroll-totop-in {
	opacity: 1;
}

/* 隐藏 -- 淡出且不接收事件*/
.mescroll-totop-out {
	opacity: 0;
	pointer-events: none;
}
</style>
