<template>
	<view class="uni-table-checkbox" @click="selected">
		<view v-if="!indeterminate" class="checkbox__inner" :class="{'is-checked':isChecked,'is-disable':isDisabled}">
			<view class="checkbox__inner-icon"></view>
		</view>
		<view v-else class="checkbox__inner checkbox--indeterminate">
			<view class="checkbox__inner-icon"></view>
		</view>
	</view>
</template>

<script>
	export default {
		name: 'TableCheckbox',
		emits:['checkboxSelected'],
		props: {
			indeterminate: {
				type: Boolean,
				default: false
			},
			checked: {
				type: [Boolean,String],
				default: false
			},
			disabled: {
				type: Boolean,
				default: false
			},
			index: {
				type: Number,
				default: -1
			},
			cellData: {
				type: Object,
				default () {
					return {}
				}
			}
		},
		watch:{
			checked(newVal){
				if(typeof this.checked === 'boolean'){
					this.isChecked = newVal
				}else{
					this.isChecked = true
				}
			},
			indeterminate(newVal){
				this.isIndeterminate = newVal
			}
		},
		data() {
			return {
				isChecked: false,
				isDisabled: false,
				isIndeterminate:false
			}
		},
		created() {
			if(typeof this.checked === 'boolean'){
				this.isChecked = this.checked
			}
			this.isDisabled = this.disabled
		},
		methods: {
			selected() {
				if (this.isDisabled) return
				this.isIndeterminate = false
				this.isChecked = !this.isChecked
				this.$emit('checkboxSelected', {
					checked: this.isChecked,
					data: this.cellData
				})
			}
		}
	}
</script>

<style lang="scss">
	$checked-color: #007aff;
	$border-color: #DCDFE6;
	$disable:0.4;

	.uni-table-checkbox {
		display: flex;
		flex-direction: row;
		align-items: center;
		justify-content: center;
		position: relative;
		margin: 5px 0;
		cursor: pointer;

		// 多选样式
		.checkbox__inner {
			/* #ifndef APP-NVUE */
			flex-shrink: 0;
			box-sizing: border-box;
			/* #endif */
			position: relative;
			width: 16px;
			height: 16px;
			border: 1px solid $border-color;
			border-radius: 2px;
			background-color: #fff;
			z-index: 1;

			.checkbox__inner-icon {
				position: absolute;
				/* #ifdef APP-NVUE */
				top: 2px;
				/* #endif */
				/* #ifndef APP-NVUE */
				top: 2px;
				/* #endif */
				left: 5px;
				height: 7px;
				width: 3px;
				border: 1px solid #fff;
				border-left: 0;
				border-top: 0;
				opacity: 0;
				transform-origin: center;
				transform: rotate(45deg);
				box-sizing: content-box;
			}

			&.checkbox--indeterminate {
				border-color: $checked-color;
				background-color: $checked-color;

				.checkbox__inner-icon {
					position: absolute;
					opacity: 1;
					transform: rotate(0deg);
					height: 2px;
					top: 0;
					bottom: 0;
					margin: auto;
					left: 0px;
					right: 0px;
					bottom: 0;
					width: auto;
					border: none;
					border-radius: 2px;
					transform: scale(0.5);
					background-color: #fff;
				}
			}
			&:hover{
				border-color: $checked-color;
			}
			// 禁用
			&.is-disable {
				/* #ifdef H5 */
				cursor: not-allowed;
				/* #endif */
				background-color: #F2F6FC;
				border-color: $border-color;
			}

			// 选中
			&.is-checked {
				border-color: $checked-color;
				background-color: $checked-color;

				.checkbox__inner-icon {
					opacity: 1;
					transform: rotate(45deg);
				}

				// 选中禁用
				&.is-disable {
					opacity: $disable;
				}
			}
			
		}
	}
</style>
