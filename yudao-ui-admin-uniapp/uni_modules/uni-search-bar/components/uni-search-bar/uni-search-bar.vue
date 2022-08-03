<template>
	<view class="uni-searchbar">
		<view :style="{borderRadius:radius+'px',backgroundColor: bgColor}" class="uni-searchbar__box"
			@click="searchClick">
			<view class="uni-searchbar__box-icon-search">
				<slot name="searchIcon">
					<uni-icons color="#c0c4cc" size="18" type="search" />
				</slot>
			</view>
			<input v-if="show || searchVal" :focus="showSync" :disabled="readonly" :placeholder="placeholderText" :maxlength="maxlength"
				class="uni-searchbar__box-search-input" confirm-type="search" type="text" v-model="searchVal"
				@confirm="confirm" @blur="blur" @focus="emitFocus" />
			<text v-else class="uni-searchbar__text-placeholder">{{ placeholder }}</text>
			<view v-if="show && (clearButton==='always'||clearButton==='auto'&&searchVal!=='') &&!readonly"
				class="uni-searchbar__box-icon-clear" @click="clear">
				<slot name="clearIcon">
					<uni-icons color="#c0c4cc" size="20" type="clear" />
				</slot>
			</view>
		</view>
		<text @click="cancel" class="uni-searchbar__cancel"
			v-if="cancelButton ==='always' || show && cancelButton ==='auto'">{{cancelTextI18n}}</text>
	</view>
</template>

<script>
	import {
		initVueI18n
	} from '@dcloudio/uni-i18n'
	import messages from './i18n/index.js'
	const {
		t
	} = initVueI18n(messages)

	/**
	 * SearchBar 搜索栏
	 * @description 搜索栏组件，通常用于搜索商品、文章等
	 * @tutorial https://ext.dcloud.net.cn/plugin?id=866
	 * @property {Number} radius 搜索栏圆角
	 * @property {Number} maxlength 输入最大长度
	 * @property {String} placeholder 搜索栏Placeholder
	 * @property {String} clearButton = [always|auto|none] 是否显示清除按钮
	 * 	@value always 一直显示
	 * 	@value auto 输入框不为空时显示
	 * 	@value none 一直不显示
	 * @property {String} cancelButton = [always|auto|none] 是否显示取消按钮
	 * 	@value always 一直显示
	 * 	@value auto 输入框不为空时显示
	 * 	@value none 一直不显示
	 * @property {String} cancelText 取消按钮的文字
	 * @property {String} bgColor 输入框背景颜色
	 * @property {Boolean} focus 是否自动聚焦
	 * @property {Boolean} readonly 组件只读，不能有任何操作，只做展示
	 * @event {Function} confirm uniSearchBar 的输入框 confirm 事件，返回参数为uniSearchBar的value，e={value:Number}
	 * @event {Function} input uniSearchBar 的 value 改变时触发事件，返回参数为uniSearchBar的value，e=value
	 * @event {Function} cancel 点击取消按钮时触发事件，返回参数为uniSearchBar的value，e={value:Number}
	 * @event {Function} clear 点击清除按钮时触发事件，返回参数为uniSearchBar的value，e={value:Number}
	 * @event {Function} blur input失去焦点时触发事件，返回参数为uniSearchBar的value，e={value:Number}
	 */

	export default {
		name: "UniSearchBar",
		emits: ['input', 'update:modelValue', 'clear', 'cancel', 'confirm', 'blur', 'focus'],
		props: {
			placeholder: {
				type: String,
				default: ""
			},
			radius: {
				type: [Number, String],
				default: 5
			},
			clearButton: {
				type: String,
				default: "auto"
			},
			cancelButton: {
				type: String,
				default: "auto"
			},
			cancelText: {
				type: String,
				default: '取消'
			},
			bgColor: {
				type: String,
				default: "#F8F8F8"
			},
			maxlength: {
				type: [Number, String],
				default: 100
			},
			value: {
				type: [Number, String],
				default: ""
			},
			modelValue: {
				type: [Number, String],
				default: ""
			},
			focus: {
				type: Boolean,
				default: false
			},
			readonly: {
				type: Boolean,
				default: false
			}
		},
		data() {
			return {
				show: false,
				showSync: false,
				searchVal: ''
			}
		},
		computed: {
			cancelTextI18n() {
				return this.cancelText || t("uni-search-bar.cancel")
			},
			placeholderText() {
				return this.placeholder || t("uni-search-bar.placeholder")
			}
		},
		watch: {
			// #ifndef VUE3
			value: {
				immediate: true,
				handler(newVal) {
					this.searchVal = newVal
					if (newVal) {
						this.show = true
					}
				}
			},
			// #endif
			// #ifdef VUE3
			modelValue: {
				immediate: true,
				handler(newVal) {
					this.searchVal = newVal
					if (newVal) {
						this.show = true
					}
				}
			},
			// #endif
			focus: {
				immediate: true,
				handler(newVal) {
					if (newVal) {
						if(this.readonly) return
						this.show = true;
						this.$nextTick(() => {
							this.showSync = true
						})
					}
				}
			},
			searchVal(newVal, oldVal) {
				this.$emit("input", newVal)
				// #ifdef VUE3
				this.$emit("update:modelValue", newVal)
				// #endif
			}
		},
		methods: {
			searchClick() {
				if(this.readonly) return
				if (this.show) {
					return
				}
				this.show = true;
				this.$nextTick(() => {
					this.showSync = true
				})
			},
			clear() {
				this.$emit("clear", {
					value: this.searchVal
				})
				this.searchVal = ""
			},
			cancel() {
				if(this.readonly) return
				this.$emit("cancel", {
					value: this.searchVal
				});
				this.searchVal = ""
				this.show = false
				this.showSync = false
				// #ifndef APP-PLUS
				uni.hideKeyboard()
				// #endif
				// #ifdef APP-PLUS
				plus.key.hideSoftKeybord()
				// #endif
			},
			confirm() {
				// #ifndef APP-PLUS
				uni.hideKeyboard();
				// #endif
				// #ifdef APP-PLUS
				plus.key.hideSoftKeybord()
				// #endif
				this.$emit("confirm", {
					value: this.searchVal
				})
			},
			blur() {
				// #ifndef APP-PLUS
				uni.hideKeyboard();
				// #endif
				// #ifdef APP-PLUS
				plus.key.hideSoftKeybord()
				// #endif
				this.$emit("blur", {
					value: this.searchVal
				})
			},
			emitFocus(e) {
				this.$emit("focus", e.detail)
			}
		}
	};
</script>

<style lang="scss">
	$uni-searchbar-height: 36px;

	.uni-searchbar {
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		flex-direction: row;
		position: relative;
		padding: 10px;
		// background-color: #fff;
	}

	.uni-searchbar__box {
		/* #ifndef APP-NVUE */
		display: flex;
		box-sizing: border-box;
		/* #endif */
		overflow: hidden;
		position: relative;
		flex: 1;
		justify-content: center;
		flex-direction: row;
		align-items: center;
		height: $uni-searchbar-height;
		padding: 5px 8px 5px 0px;
	}

	.uni-searchbar__box-icon-search {
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		flex-direction: row;
		// width: 32px;
		padding: 0 8px;
		justify-content: center;
		align-items: center;
		color: #B3B3B3;
	}

	.uni-searchbar__box-search-input {
		flex: 1;
		font-size: 14px;
		color: #333;
	}

	.uni-searchbar__box-icon-clear {
		align-items: center;
		line-height: 24px;
		padding-left: 8px;
		/* #ifdef H5 */
		cursor: pointer;
		/* #endif */
	}

	.uni-searchbar__text-placeholder {
		font-size: 14px;
		color: #B3B3B3;
		margin-left: 5px;
	}

	.uni-searchbar__cancel {
		padding-left: 10px;
		line-height: $uni-searchbar-height;
		font-size: 14px;
		color: #333333;
		/* #ifdef H5 */
		cursor: pointer;
		/* #endif */
	}
</style>
