<template>
	<view class="mix-timeline">
		<view class="cell" v-for="(item, index) in list" :key="index">
			<view class="left column center">
				<text class="time">{{ item.time | date('H:i') }}</text>
				<text class="date">{{ item.time | date('m/d') }}</text>
			</view>
			<view class="cen center">
				<view class="circle center"></view>
			</view>
			<view class="right column">
				<text class="title">{{ item.title }}</text>
				<text v-if="item.tip" class="tip">{{ item.tip }}</text>
			</view>
		</view>
	</view>
</template>

<script>
	/**
	 * 时间轴
	 * {
	 *	title: 标题
	 *	tip: 小字
	 *	time: 时间戳
	 *	}  
	 */
	export default {
		data() {
			return {
				
			};
		},
		props: {
			list: {
				type: Array,
				default(){
					return []
				}
			}
		}
	}
</script>

<style scoped lang="scss">
	.mix-timeline{
		
	}
	.cell{
		display: flex;
		align-items: flex-start;
		width: 100%;
		padding: 0 30rpx 0;
		
		&:first-child .circle{
			&:before{
				background-color: $base-color;
			}
			&:after{
				content: '';
				position: absolute;
				width: 28rpx;
				height: 28rpx;
				background-color: #f9e0eb;
				border-radius: 100rpx;
			}
		}
		&:last-child .right:before{
			display: none;
		}
	}
	.left{
		
		.time{
			font-size: 26rpx;
			color: #333;
			line-height: 44rpx;
		}
		.date{
			font-size: 20rpx;
			color: #333;
		}
	}
	.cen{
		width: 80rpx;
		height: 44rpx;
		
		.circle{
			width: 16rpx;
			height: 16rpx;
			position: relative;
			z-index: 1;
			
			&:before{
				content: '';
				width: 16rpx;
				height: 16rpx;
				background-color: #ddd;
				border-radius: 100rpx;
				position: relative;
				z-index: 5;
			}
		}
	}
	.right{
		flex: 1;
		padding-bottom: 30rpx;
		position: relative;
		min-height: 96rpx;
		
		&:before{
			content: '';
			width: 2rpx;
			position: absolute;
			left: 0;
			top: 0;
			bottom: 0;
			background-color: #ddd;
			transform: translate(-42rpx, 22rpx);
		}
		.title{
			font-size: 28rpx;
			color: #333;
			line-height: 44rpx;
			font-weight: 700;
		}
		.tip{
			margin-top: 6rpx;
			font-size: 24rpx;
			color: #999;
			line-height: 36rpx;
		}
	}
	


</style>
