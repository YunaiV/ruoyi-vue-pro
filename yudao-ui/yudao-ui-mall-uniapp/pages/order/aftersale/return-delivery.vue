<template>
	<s-layout title="退货物流">
		<view>
			<form @submit="subRefund" report-submit='true'>
				<view class='apply-return'>
					<view class='list borRadius14'>
						<view class='item acea-row row-between-wrapper' style="display: flex;align-items: center;">
							<view>物流公司</view>
							<view v-if="state.expresses.length>0" style="flex:1">
								<picker mode='selector' class='num' @change="bindPickerChange" :value="state.expressIndex"
									:range="state.expresses" range-key="name">
									<view class="picker acea-row row-between-wrapper" style="display: flex;justify-content: space-between;">
										<view class='reason'>{{ state.expresses[state.expressIndex].name }}</view>
										<text class='iconfont _icon-forward' />
									</view>
								</picker>
							</view>
						</view>
						<view class='item textarea acea-row row-between' style="display: flex;align-items: center;">
							<view>物流单号</view>
							<input placeholder='请填写物流单号' class='num' name="logisticsNo"
                     placeholder-class='placeholder' />
						</view>
						<button class='returnBnt bg-color ss-reset-button ui-BG-Main-Gradient sub-btn'
							form-type="submit"
							style="background: linear-gradient(90deg,var(--ui-BG-Main),var(--ui-BG-Main-gradient))!important">提交</button>
					</view>
				</view>
			</form>
		</view>
	</s-layout>
</template>

<script setup>
	import { onLoad } from '@dcloudio/uni-app';
	import { reactive } from 'vue';
  import sheep from '@/sheep';
  import AfterSaleApi from '@/sheep/api/trade/afterSale';
  import DeliveryApi from '@/sheep/api/trade/delivery';

  const state = reactive({
    id: 0, // 售后编号
		expressIndex: 0, // 选中的 expresses 下标
		expresses: [], // 可选的快递列表
	})

	function bindPickerChange(e) {
		state.expressIndex = e.detail.value;
	}

	async function subRefund(e) {
    let data = {
      id: state.id,
      logisticsId: state.expresses[state.expressIndex].id,
      logisticsNo: e.detail.value.logisticsNo,
    };
    const { code } = await AfterSaleApi.deliveryAfterSale(data);
    if (code !== 0) {
      return;
    }
    uni.showToast({
      title: '填写退货成功',
    });
    sheep.$router.go('/pages/order/aftersale/detail', { id: state.id });
	}

  // 获得快递物流列表
	async function getExpressList() {
    const { code, data } = await DeliveryApi.getDeliveryExpressList();
    if (code !== 0) {
      return;
    }
    state.expresses = data;
	}

	onLoad(options => {
    if (!options.id) {
      sheep.$helper.toast(`缺少订单信息，请检查`);
      return
    }
    state.id = options.id;
    // 获得快递物流列表
    getExpressList();
	})
</script>
<style lang="scss" scoped>
	.apply-return {
		padding: 20rpx 30rpx 70rpx 30rpx;
	}

	.apply-return .list {
		background-color: #fff;
		margin-top: 18rpx;
		padding: 0 24rpx 70rpx 24rpx;
	}

	.apply-return .list .item {
		min-height: 90rpx;
		border-bottom: 1rpx solid #eee;
		font-size: 30rpx;
		color: #333;
	}

	.apply-return .list .item .num {
		color: #282828;
		margin-left: 27rpx;
		// width: 227rpx;
		// text-align: right;
	}

	.apply-return .list .item .num .picker .reason {
		width: 385rpx;
	}

	.apply-return .list .item .num .picker .iconfont {
		color: #666;
		font-size: 30rpx;
		margin-top: 2rpx;
	}

	.apply-return .list .item.textarea {
		padding: 24rpx 0;
	}

	.apply-return .list .item textarea {
		height: 100rpx;
		font-size: 30rpx;
	}

	.apply-return .list .item .placeholder {
		color: #bbb;
	}

	.apply-return .list .item .title {
		height: 95rpx;
		width: 100%;
	}

	.apply-return .list .item .title .tip {
		font-size: 30rpx;
		color: #bbb;
	}

	.apply-return .list .item .upload {
		padding-bottom: 36rpx;
	}

	.apply-return .list .item .upload .pictrue {
		border-radius: 14rpx;
		margin: 22rpx 23rpx 0 0;
		width: 156rpx;
		height: 156rpx;
		position: relative;
		font-size: 24rpx;
		color: #bbb;
	}

	.apply-return .list .item .upload .pictrue:nth-of-type(4n) {
		margin-right: 0;
	}

	.apply-return .list .item .upload .pictrue image {
		width: 100%;
		height: 100%;
		border-radius: 14rpx;
	}

	.apply-return .list .item .upload .pictrue .icon-guanbi1 {
		position: absolute;
		font-size: 45rpx;
		top: -10rpx;
		right: -10rpx;
	}

	.apply-return .list .item .upload .pictrue .icon-icon25201 {
		color: #bfbfbf;
		font-size: 50rpx;
	}

	.apply-return .list .item .upload .pictrue:nth-last-child(1) {
		border: 1rpx solid #ddd;
		box-sizing: border-box;
	}

	.apply-return .returnBnt {
		font-size: 32rpx;
		color: #fff;
		width: 100%;
		height: 86rpx;
		border-radius: 50rpx;
		text-align: center;
		line-height: 86rpx;
		margin: 43rpx auto;
	}
</style>