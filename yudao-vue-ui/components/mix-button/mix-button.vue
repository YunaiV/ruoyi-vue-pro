<template>
	<view 
		class="mix-btn-content" 
		:class="{
				disabled: loading || disabled || dead, 
			}"
		:style="[
			{marginTop: marginTop}
		]"
		@click="confirm"
	>
		<image v-if="loading" class="loading-icon" src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADIAAAAyCAYAAAAeP4ixAAAE3UlEQVRoQ82Z7ZEQNwyGrQoCFQQqgFRAqCBcBSEVQCqAqyBJBTkqACoAKsilAo4KQioQ8+zIO16tvbaWM4P/7lrSq2/JkiYeVX2SUnpmLP4SkTez2MkswgbitaN/MQvMTCBo/xcH5IOI/DxDeWEgqvq0cJfLloZV9X1K6VEEiKo+NNpvo5YLAYm4SxSIqt5JKf2TUrqXUvosIncjlosCqbnLTUrpJxH5XDI+AeRlSumF0Qi7YBTIn4VblXLjYgiynggQVcUKWAOrcB6LCK45fKJAYPixQh1rYBWss5wgELIbqZrzSkSIw9AJATEBW1Z5IyIXUSCqShZ7Z/f+Tyk9LBUyiuYMEMyP5n+oMFldYtQiqopLka04OxedBsSsgun/rjC5EZH7o65lqTzT+WTW2CSN00CKtgKCtBXVoGtoHL6/iciVql6llH51glAflliwdEu85QBf7vUEt8SQvPttXMv5a6YJcUy+BrIJUvp2yX9JnQ1apeuVsdZNtwacvi1nx02744GUubwUDuvAGAutpm9ofRXKwOQMdJWta0L9VzAg4123rKGqWBbZyJr5/C4iyLQcD+R5SumPA/NilZci8qpwDx/43cbQ3COncZQD390xRVAkfX8G/+elUj0Q/JWYeNDxVf5BI9cmFFpHW6vWB3ydTEWq3cWFWQyF+nrywRS5i9tq+rVsgtlqKbaUsav9HiD/XVWxABbKSYBfqC9YoJkMmnXEtIJf5sGoJlM3SEeBWOdLhS/jgOuXxKfv5TzdbkE010ETviWH1m0C8YnmrVlhky1biukCyRct8AD0Y0Hs1lzL6hf0yYpPpzaNlqlyYL+PMht1szP/DVvkDPFveWcBYnHg24mWHPRTSx35no5YtqADjZxmEYsQqf1rsYJSy/R7RJaO4BIgrbbkUCYRmeKWqkrrMgoiy7gA6bUlNUCfRMTn+681xnJfVdFwr7PwvC4AAnqWCrU6UROOKvtkZsayVD+qGGL2ZnUPA5QntRYR1jTNLnWU84z/pvj5DEF7NMNArKmjraZ1oAM+NZp6wcwjyFb/nnHbYSCWFmmtyyBfR9eexnrf3ZBGzKKkoT4L2l0gB8MN92c2jdCnNGym0pZCjtp4NM9scLQsO72+aRRDBKcclHMQrssscthN7IAUQ74fbkrerG4gfusPN8afoc63TEyFKK661amNumz9WmmYGsKQ4/e8xA7AYUJr300AqsodVkMIVxt3SSjw8fWtutWJLB92A79VYt/ibLYb1suxh1rrj1sV7fbGpelt7IZHOQdxh9XSSnMECAM/brQrhJUlGzKsQFz7s8aT3Su3L4eLa/sfi+dnhw2fXdZy7Uo3Dip7LVzvXnYtt43cZLhKs9p9SrBxI7t1ex3Uy/XO5MSRb/83a88OEHo8rJxdhonzcUSG8t9uHWkRVlWSQrk429WUIyAWX34ZPrT/rcl0CkjjLXHnGj0gBqZ8NK0+441Y6SwQ1p1lq1IN1kEgfhl+qsiGgVSCtPnKNALErFI+QRym43CL0mghCNDyTYPfmhoMAMG6BH5uTcLviCGLVBYVh69Mo0BqhTW6EwgBMYb5rb078gaBlC8B4a46DMTAEKDXvZ4qAiS7Mu3L1MFqJAVWpr4ytYa1HOF5yiKjDFq91uj9yH9TgZgbLiPB7O3LF2dInShyDo35AAAAAElFTkSuQmCC"></image>
		<text v-if="icon" class="mix-icon" :class="icon" :style="{fontSize: iconSize + 'rpx'}"></text>
		<text class="mix-text">{{ text }}</text>
	</view>
</template>

<script>
	/**
	 * 按钮组件
	 * @prop text 按钮显示文字
	 * @prop icon 按钮图标
	 * @prop iconSize 按钮显示文字
	 * @prop isConfirm 点击后是否处理loading状态
	 * @prop disabled 按钮禁用
	 * @prop marginTop 按钮上边距
	 */
	let stopTimer = null;
	export default {
		name: 'MixButton',
		data() {
			return {
				dead: false,
				loading: false
			};
		},
		props: {
			text: {
				type: String,
				default: '提交'
			},
			icon: {
				type: String,
				default: ''
			},
			iconSize: {
				type: Number,
				default: 32
			},
			isConfirm: {
				type: Boolean,
				default: true
			},
			disabled: {
				type: Boolean,
				default: false
			},
			marginTop: {
				type: String,
				default: '0rpx'
			}
		},
		methods: {
			stop(){
				if(stopTimer){
					clearTimeout(stopTimer);
					stopTimer = null;
				}
				this.loading = false;
			},
			death(){
				this.loading = false;
				this.dead = true;
			},
			confirm(){
				if(this.dead){
					return;
				}
				if(this.loading || this.disabled){
					return;
				}
				if(this.isConfirm){
					this.loading = true;
					
					stopTimer = setTimeout(()=>{
						this.loading = false;
						clearTimeout(stopTimer);
						stopTimer = null;
					}, 10000)
				}
				this.$emit('onConfirm');
			}
		}
	}
</script>

<style scoped lang='scss'>
	.mix-btn-content{
		display: flex;
		align-items: center;
		justify-content: center;
		width: 610rpx;
		height: 88rpx;
		margin: 0 auto;
		font-size: 32rpx;
		color: #fff;
		border-radius: 100rpx;
		background-color: $base-color;
		position: relative;
		
		&:after{
			content: '';
			position: absolute;
			left: 50%;
			top: 25%;
			transform: translateX(-50%);
			width: 85%;
			height: 85%;
			background: linear-gradient(131deg, rgba(255,115,138,1) 0%, rgba(255,83,111,1) 100%);
			border-radius: 100rpx;
			opacity: 0.4;
			filter:blur(10rpx);
		}
		&.disabled {
			opacity: .65;
		}
		.mix-text{
			position: relative;
			z-index: 1;
		}
		.mix-icon{
			position: relative;
			z-index: 1;
			margin-right: 8rpx;
		}
		.loading-icon{
			width: 34rpx;
			height: 34rpx;
			transform-origin:50% 50%;
			margin-right: 16rpx;
			animation: rotate 2s linear infinite;
			position: relative;
			z-index: 1;
		}
	}
	@keyframes rotate{
		from {
			transform:rotate(0deg)
		}
		to {
			transform:rotate(360deg)
		}
	}
</style>
