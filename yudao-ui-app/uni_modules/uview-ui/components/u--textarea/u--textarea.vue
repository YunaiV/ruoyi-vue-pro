<template>
	<uvTextarea
		:value="value"
		:placeholder="placeholder"
		:height="height"
		:confirmType="confirmType"
		:disabled="disabled"
		:count="count"
		:focus="focus"
		:autoHeight="autoHeight"
		:fixed="fixed"
		:cursorSpacing="cursorSpacing"
		:cursor="cursor"
		:showConfirmBar="showConfirmBar"
		:selectionStart="selectionStart"
		:selectionEnd="selectionEnd"
		:adjustPosition="adjustPosition"
		:disableDefaultPadding="disableDefaultPadding"
		:holdKeyboard="holdKeyboard"
		:maxlength="maxlength"
		:border="border"
		:customStyle="customStyle"
		:formatter="formatter"
		:ignoreCompositionEvent="ignoreCompositionEvent"
		@focus="e => $emit('focus')"
		@blur="e => $emit('blur')"
		@linechange="e => $emit('linechange', e)"
		@confirm="e => $emit('confirm')"
		@input="e => $emit('input', e)"
		@keyboardheightchange="e => $emit('keyboardheightchange')"
	></uvTextarea>
</template>

<script>
	/**
	 * 此组件存在的理由是，在nvue下，u--textarea被uni-app官方占用了，u-textarea在nvue中相当于textarea组件
	 * 所以在nvue下，取名为u--textarea，内部其实还是u-textarea.vue，只不过做一层中转
	 */
	import uvTextarea from '../u-textarea/u-textarea.vue';
	import props from '../u-textarea/props.js'
	export default {
		name: 'u--textarea',
		mixins: [uni.$u.mpMixin, props, uni.$u.mixin],
		components: {
			uvTextarea
		},
	}
</script>
