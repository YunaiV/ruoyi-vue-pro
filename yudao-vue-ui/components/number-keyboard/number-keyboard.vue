<template>
	<view class="number-keyboard-component numberkeyboard">
		<view class="keys">
			<view 
				class="key button" 
				v-for="num in config.loop" 
				:key="num.key" 
				@tap="number(num.number)"
				hover-class="hover-dark" 
				:hover-stay-time="50"
			>{{num.number}}</view>
			<view class="key button" @tap="del" style="background: #e2e7eb;"></view>
			<view class="key button" @tap="number(0)">0</view>
			<view class="key button" @tap="del" style="background: #e2e7eb;">
				<image style="width: 50rpx;height: 50rpx;" src="data:image/png;base64,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"></image>
			</view>
		</view>
	</view>
</template>

<script>
	/**
	 * 数字键盘
	 * @event onChange 输入值改变时触发
	 */
	export default {
		name: 'number-keyboard',
		data() {
			return {
				val: '',
				config: {
					loop: [
						{number: 1,key: 'number-1'},
						{number: 2,key: 'number-2'},
						{number: 3,key: 'number-3'},
						{number: 4,key: 'number-4'},
						{number: 5,key: 'number-5'},
						{number: 6,key: 'number-6'},
						{number: 7,key: 'number-7'},
						{number: 8,key: 'number-8'},
						{number: 9,key: 'number-9'}
					]
				},
			};
		},
		props: {
			length: {
				type: Number,
				default: 6
			}
		},
		methods: {
			del() {
				if(this.val.length > 0){
					this.val = this.val.slice(0, this.val.length - 1);
					this.$emit('onChange', this.val)
				}
			},
			number(number) {
				if(this.val.length < this.length){
					this.val = this.val + number;
					this.$emit('onChange', this.val)
				}
			}
		}
	};
</script>

<style scoped="scoped" lang="scss">
	.number-keyboard-component {
		width: 100%;
		border-top: 1px solid #f1f4f4;

		.title {
			display: flex;
			justify-content: center;
			align-items: center;
			height: 60rpx;
			background: #f0f0f0;
		}

		.keys {
			display: flex;
			flex-direction: row;
			flex-wrap: wrap;

			.key {
				width: 250rpx;
				height: 110rpx;
				display: flex;
				justify-content: center;
				align-items: center;
				border-right: 1px solid #f1f4f4;
				box-sizing: border-box;
				font-size: 36rpx;
				font-weight: 700;
				color: #333;
			}

			.key:nth-child(n + 4) {
				border-top: 1px solid #f1f4f4;
				box-sizing: border-box;
			}
		}

		.container {
			width: 100%;

			.count-text {
				justify-content: center;
				align-items: center;
				display: flex;
				font-size: 18px;
			}
		}

		.numberkeyboard-popup {
			width: 100%;
			z-index: 10;

			.count-text {
				position: fixed;
				top: -100px;
				font-size: 28px;
				color: #ffffff;
				width: 100%;
				text-align: center;
				font-weight: 500;

				span {
					padding: 0 40px;
					border-radius: 30px;
					background: -webkit-linear-gradient(left top, #999999, #777777);
				}
			}

			.desc-text {
				position: fixed;
				top: -40px;
				font-size: 12px;
				color: #ffffff;
				width: 100%;
				text-align: center;
				font-weight: 100;
			}

			.number {
				height: 70px;
				line-height: 70px;
				font-size: 25px;
				font-weight: 400;
				color: #666666;
				border-right: 1px solid #999999;
				box-sizing: border-box;
			}

			.number:nth-child(n + 4) {
				border-top: 1px solid #999999;
				box-sizing: border-box;
			}

			.number:active {
				background: #04be02;
				color: #ffffff;
				border: 0px solid #000000;
				box-shadow: 0 0 2px 2px #09bb07;
				box-sizing: border-box;
			}


			.del {
				height: 140px;
				vertical-align: middle;
				line-height: 140px;
				border-bottom: 0.1px solid #999999;
				box-sizing: border-box;
			}
			.confirm {
				font-size: 15px;
				height: 140px;
				line-height: 140px;
				color: #ffffff;
			}
		}
	}
</style>
