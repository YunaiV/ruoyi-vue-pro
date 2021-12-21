<template>
	<uvImage 
		:src="src"
		:mode="mode"
		:width="width"
		:height="height"
		:shape="shape"
		:radius="radius"
		:lazyLoad="lazyLoad"
		:showMenuByLongpress="showMenuByLongpress"
		:loadingIcon="loadingIcon"
		:errorIcon="errorIcon"
		:showLoading="showLoading"
		:showError="showError"
		:fade="fade"
		:webp="webp"
		:duration="duration"
		:bgColor="bgColor"
		:customStyle="customStyle"
		@click="$emit('click')"
		@error="$emit('error')"
		@load="$emit('load')"
	></uvImage>
</template>

<script>
	/**
	 * 此组件存在的理由是，在nvue下，u-image被uni-app官方占用了，u-image在nvue中相当于image组件
	 * 所以在nvue下，取名为u--image，内部其实还是u-iamge.vue，只不过做一层中转
	 */
	import uvImage from '../u-image/u-image.vue';
	import props from '../u-image/props.js';
	export default {
		name: 'u--image',
		mixins: [uni.$u.mpMixin, props, uni.$u.mixin],
		components: {
			uvImage
		},
	}
</script>