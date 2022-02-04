<template>
	<view class="mescroll-empty" :class="{ 'empty-fixed': option.fixed }" :style="{ 'z-index': option.zIndex, top: option.top }">
		<mix-empty :type="option.type" :backgroundColor="option.backgroundColor"></mix-empty>
	</view>
</template>

<script>
	import mixEmpty from '@/components/mix-empty/mix-empty.vue'
	export default {
		components: {
			mixEmpty
		},
		props: {
			// empty的配置项: 默认为GlobalOption.up.empty
			option: {
				type: Object,
				default() {
					return {};
				}
			}
		}
	};
</script>

<style scoped lang="scss">
	
</style>