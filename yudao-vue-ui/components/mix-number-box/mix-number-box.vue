<template>
	<view class="uni-numbox">
		<view class="uni-numbox-minus" 
			@click="_calcValue('subtract')"
		>
			<text class="mix-icon icon--jianhao" :class="minDisabled?'uni-numbox-disabled': ''" ></text>
		</view>
		<input
			class="uni-numbox-value" 
			type="number" 
			:disabled="inputDisabled"
			:value="inputValue" 
			@blur="_onBlur"
		>
		
		<view 
			class="uni-numbox-plus" 
			@click="_calcValue('add')"
		>
			<text class="mix-icon icon-jia2" :class="maxDisabled?'uni-numbox-disabled': ''" ></text>
		</view>
	</view>
</template>
<script>
	/**
	 * index 当前行下标
	 * value 默认值
	 * min 最小值
	 * max 最大值
	 * step 步进值
	 * disabled 禁用
	 */
	export default {
		name: 'uni-number-box',
		props: {
			index: {
				type: Number,
				default: 0
			},
			value: {
				type: Number,
				default: 1
			},
			min: {
				type: Number,
				default: -Infinity
			},
			max: {
				type: Number,
				default: 99
			},
			step: {
				type: Number,
				default: 1
			},
			inputDisabled: {
				type: Boolean,
				default: false
			}
		},
		data() {
			return {
				inputValue: this.value,
			}
		},
		created(){
			
		},
		computed: {
			maxDisabled(){
				return this.inputValue >= this.max;
			},
			minDisabled(){
				return this.inputValue <= this.min;
			},
		},
		watch: {
			inputValue(number) {
				const data = {
					number: number,
					index: this.index
				}
				this.$emit('eventChange', data);
			},
		},
		methods: {
			_calcValue(type) {
				let value = this.inputValue;
				let newValue = 0;
				let step = this.step;
				
				if(type === 'subtract'){
					newValue = value - step;
					
					if(newValue < this.min){
						newValue = this.min
						if(this.min > 1){
							this.$api.msg(this.limit_message);
						}
					}
				}else if(type === 'add'){
					newValue = value + step;
					
					if(newValue > this.max){
						newValue = this.max
					}
				}
				if(newValue === value){
					return;
				}
				this.inputValue = newValue;
			},
			_onBlur(event) {
				let value = event.detail.value;
				let constValue = value;
				if (!value) {
					this.inputValue = 0;
					return
				}
				value = +value;
				if (value > this.max) {
					value = this.max;
				} else if (value < this.min) {
					value = this.min
				}
				
				if(constValue != value){
					this.inputValue = constValue;
					this.$nextTick(()=>{
						this.inputValue = value
					})
				}else{
					this.inputValue = value
				}
			}
		}
	}
</script>
<style>
	.uni-numbox {
		display: flex;
		justify-content: flex-start;
		flex-direction: row;
		align-items: center;
		height: 50rpx;
	}

	.uni-numbox-minus,
	.uni-numbox-plus {
		display: flex;
		align-items: center;
		justify-content: center;
		width: 50rpx;
		height: 100%;
		line-height: 1;
		background-color: #f7f7f7;
	}
	.uni-numbox-minus .mix-icon,
	.uni-numbox-plus .mix-icon{
		font-size: 32rpx;
		color: #333;
	}

	.uni-numbox-value {
		display: flex;
		align-items: center;
		justify-content: center;
		background-color: #fff;
		width: 60rpx;
		height: 50rpx;
		min-height: 50rpx;
		text-align: center;
		font-size: 28rpx;
		color: #333;
	}

	.uni-numbox-disabled.mix-icon {
		color: #C0C4CC;
	}
</style>
