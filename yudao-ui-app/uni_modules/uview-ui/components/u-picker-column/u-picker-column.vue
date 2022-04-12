<template>
	<picker-view-column>
		<view class="u-picker-column">

		</view>
	</picker-view-column>
</template>

<script>
	import props from './props.js';
	/**
	 * PickerColumn 
	 * @description 
	 * @tutorial url
	 * @property {String}
	 * @event {Function}
	 * @example
	 */
	export default {
		name: 'u-picker-column',
		mixins: [uni.$u.mpMixin, uni.$u.mixin,props],
	}
</script>

<style lang="scss" scoped>
	@import "../../libs/css/components.scss";
</style>
