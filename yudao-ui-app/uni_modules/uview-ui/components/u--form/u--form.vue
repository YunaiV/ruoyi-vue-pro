<template>
	<uvForm
		ref="uForm"
		:model="model"
		:rules="rules"
		:errorType="errorType"
		:borderBottom="borderBottom"
		:labelPosition="labelPosition"
		:labelWidth="labelWidth"
		:labelAlign="labelAlign"
		:labelStyle="labelStyle"
		:customStyle="customStyle"
	>
		<slot />
	</uvForm>
</template>

<script>
	/**
	 * 此组件存在的理由是，在nvue下，u-form被uni-app官方占用了，u-form在nvue中相当于form组件
	 * 所以在nvue下，取名为u--form，内部其实还是u-form.vue，只不过做一层中转
	 */
	import uvForm from '../u-form/u-form.vue';
	import props from '../u-form/props.js'
	export default {
		// #ifdef MP-WEIXIN
		name: 'u-form',
		// #endif
		// #ifndef MP-WEIXIN
		name: 'u--form',
		// #endif
		mixins: [uni.$u.mpMixin, props, uni.$u.mixin],
		components: {
			uvForm
		},
		created() {
			this.children = []
		},
		methods: {
			// 手动设置校验的规则，如果规则中有函数的话，微信小程序中会过滤掉，所以只能手动调用设置规则
			setRules(rules) {
				this.$refs.uForm.setRules(rules)
			},
			validate() {
				/**
				 * 在微信小程序中，通过this.$parent拿到的父组件是u--form，而不是其内嵌的u-form
				 * 导致在u-form组件中，拿不到对应的children数组，从而校验无效，所以这里每次调用u-form组件中的
				 * 对应方法的时候，在小程序中都先将u--form的children赋值给u-form中的children
				 */
				// #ifdef MP-WEIXIN
				this.setMpData()
				// #endif
				return this.$refs.uForm.validate()
			},
			validateField(value, callback) {
				// #ifdef MP-WEIXIN
				this.setMpData()
				// #endif
				return this.$refs.uForm.validateField(value, callback)
			},
			resetFields() {
				// #ifdef MP-WEIXIN
				this.setMpData()
				// #endif
				return this.$refs.uForm.resetFields()
			},
			clearValidate(props) {
				// #ifdef MP-WEIXIN
				this.setMpData()
				// #endif
				return this.$refs.uForm.clearValidate(props)
			},
			setMpData() {
				this.$refs.uForm.children = this.children
			}
		},
	}
</script>
