<template>
	<uni-popup ref="popup" type="bottom">
		<view class="content">
			<view v-if="data.title" class="cell b-b center title">
				<text >{{ data.title }}</text>
			</view>
			<view class="cell b-b center" v-for="(item, index) in data.list" :key="index" @click="confirm(item)">
				<text>{{ item.text }}</text>
			</view>
			<view class="cell center cancel-btn" @click="close">
				<text>取消</text>
			</view>
		</view>
	</uni-popup>
</template>

<script>
	/**
	 * 底部选择菜单
	 */
	export default {
		data() {
			return {
				data: {}
			};
		},
		methods: {
			//选择回调
			confirm(item){
				this.$util.throttle(()=>{
					this.$emit('onConfirm', item)
				})
				this.close();
			},
			open(data){
				this.data = data;
				this.$refs.popup.open();
			},
			close(){
				this.$refs.popup.close();
			}
		}
	}
</script>

<style scoped lang="scss">
	.content{
		background-color: #fff;
		border-radius: 16rpx 16rpx 0 0;
		overflow: hidden;
	}
	.cell{
		min-height: 88rpx;
		font-size: 32rpx;
		color: #333;
		position: relative;
		
		&:after{
			position: absolute;
			z-index: 3;
			left: 0;
			top: auto;
			bottom: 0;
			right: 0;
			height: 0;
			content: '';
			transform: scaleY(.5);
			border-bottom: 1px solid #f5f5f5;
		}
		&:last-child{
			height: 96rpx;
			border-top: 12rpx solid #f7f7f7;
		}
		&.title{
			height: 100rpx;
			font-size: 28rpx;
			color: #999;
		}
	}


</style>
