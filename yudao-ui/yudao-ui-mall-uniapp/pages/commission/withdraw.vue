<!-- 分佣提现 -->
<template>
  <s-layout title="申请提现" class="withdraw-wrap" navbar="inner">
    <view class="page-bg"></view>
    <view
      class="wallet-num-box ss-flex ss-col-center ss-row-between"
      :style="[
        {
          marginTop: '-' + Number(statusBarHeight + 88) + 'rpx',
          paddingTop: Number(statusBarHeight + 108) + 'rpx',
        },
      ]"
    >
      <view class="">
        <view class="num-title">可提现金额（元）</view>
        <view class="wallet-num">{{ fen2yuan(state.brokerageInfo.brokeragePrice) }}</view>
      </view>
      <button
        class="ss-reset-button log-btn"
        @tap="sheep.$router.go('/pages/commission/wallet', { type: 2 })"
      >
        提现记录
      </button>
    </view>
    <!-- 提现输入卡片-->
    <view class="draw-card">
      <view class="bank-box ss-flex ss-col-center ss-row-between ss-m-b-30">
        <view class="name">提现至</view>
        <view class="bank-list ss-flex ss-col-center" @tap="onAccountSelect(true)">
          <view v-if="!state.accountInfo.type" class="empty-text">请选择提现方式</view>
          <view v-if="state.accountInfo.type === '1'" class="empty-text">钱包余额</view>
          <view v-if="state.accountInfo.type === '2'" class="empty-text">银行卡转账</view>
          <view v-if="state.accountInfo.type === '3'" class="empty-text">微信账户</view>
          <view v-if="state.accountInfo.type === '4'" class="empty-text">支付宝账户</view>
          <view v-if="state.accountInfo.type === '5'" class="empty-text">微信零钱</view>
          <text class="cicon-forward" />
        </view>
      </view>
      <!-- 提现金额 -->
      <view class="card-title">提现金额</view>
      <view class="input-box ss-flex ss-col-center border-bottom">
        <view class="unit">￥</view>
        <uni-easyinput
          :inputBorder="false"
          class="ss-flex-1 ss-p-l-10"
          v-model="state.accountInfo.price"
          type="number"
          placeholder="请输入提现金额"
        />
      </view>
      <!-- 提现账号 -->
      <view class="card-title" v-show="['2', '6'].includes(state.accountInfo.type)">
        提现账号
      </view>
      <view
        class="input-box ss-flex ss-col-center border-bottom"
        v-show="['2', '6'].includes(state.accountInfo.type)"
      >
        <view class="unit" />
        <uni-easyinput
          :inputBorder="false"
          class="ss-flex-1 ss-p-l-10"
          v-model="state.accountInfo.userAccount"
          placeholder="请输入提现账号"
        />
      </view>
      <!-- 收款码 -->
      <view class="card-title" v-show="['3', '4'].includes(state.accountInfo.type)">收款码</view>
      <view
        class="input-box ss-flex ss-col-center"
        v-show="['3', '4'].includes(state.accountInfo.type)"
      >
        <view class="unit" />
        <view class="upload-img">
          <s-uploader
            v-model:url="state.accountInfo.qrCodeUrl"
            fileMediatype="image"
            limit="1"
            mode="grid"
            :imageStyles="{ width: '168rpx', height: '168rpx' }"
            @success="(payload) => (state.accountInfo.qrCodeUrl = payload.tempFilePaths[0])"
          />
        </view>
      </view>
      <!-- 持卡人姓名 -->
      <view class="card-title" v-show="['2', '5', '6'].includes(state.accountInfo.type)">
        收款真名
      </view>
      <view
        class="input-box ss-flex ss-col-center border-bottom"
        v-show="['2', '5', '6'].includes(state.accountInfo.type)"
      >
        <view class="unit" />
        <uni-easyinput
          :inputBorder="false"
          class="ss-flex-1 ss-p-l-10"
          v-model="state.accountInfo.userName"
          placeholder="请输入收款真名"
        />
      </view>
      <!-- 提现银行 -->
      <view class="card-title" v-show="state.accountInfo.type === '2'">提现银行</view>
      <view
        class="input-box ss-flex ss-col-center border-bottom"
        v-show="state.accountInfo.type === '2'"
      >
        <view class="unit" />
        <!--银行改为下拉选择-->
        <picker
          @change="bankChange"
          :value="state.bankListSelectedIndex"
          :range="state.bankList"
          range-key="label"
          style="width: 100%"
        >
          <uni-easyinput
            :inputBorder="false"
            :value="state.accountInfo.bankName"
            placeholder="请选择银行"
            suffixIcon="right"
            disabled
            :styles="{ disableColor: '#fff', borderColor: '#fff', color: '#333!important' }"
          />
        </picker>
      </view>
      <!-- 开户地址 -->
      <view class="card-title" v-show="state.accountInfo.type === '2'">开户地址</view>
      <view
        class="input-box ss-flex ss-col-center border-bottom"
        v-show="state.accountInfo.type === '2'"
      >
        <view class="unit" />
        <uni-easyinput
          :inputBorder="false"
          class="ss-flex-1 ss-p-l-10"
          v-model="state.accountInfo.bankAddress"
          placeholder="请输入开户地址"
        />
      </view>
      <button class="ss-reset-button save-btn ui-BG-Main-Gradient ui-Shadow-Main" @tap="onConfirm">
        确认提现
      </button>
    </view>

    <!-- 提现说明 -->
    <view class="draw-notice">
      <view class="title ss-m-b-30">提现说明</view>
      <view class="draw-list"> 最低提现金额 {{ fen2yuan(state.minPrice) }} 元 </view>
      <view class="draw-list">
        冻结佣金：<text>￥{{ fen2yuan(state.brokerageInfo.frozenPrice) }}</text>
        （每笔佣金的冻结期为 {{ state.frozenDays }} 天，到期后可提现）
      </view>
    </view>

    <!-- 选择提现账户 -->
    <account-type-select
      :show="state.accountSelect"
      @close="onAccountSelect(false)"
      round="10"
      v-model="state.accountInfo"
      :methods="state.withdrawTypes"
    />
  </s-layout>
</template>

<script setup>
  import { onBeforeMount, reactive } from 'vue';
  import sheep from '@/sheep';
  import accountTypeSelect from './components/account-type-select.vue';
  import { fen2yuan } from '@/sheep/hooks/useGoods';
  import TradeConfigApi from '@/sheep/api/trade/config';
  import BrokerageApi from '@/sheep/api/trade/brokerage';
  import DictApi from '@/sheep/api/system/dict';
  import SLayout from '@/sheep/components/s-layout/s-layout.vue';
  import { getWeixinPayChannelCode, goBindWeixin } from '@/sheep/platform/pay';

  const headerBg = sheep.$url.css('/static/img/shop/user/withdraw_bg.png');
  const statusBarHeight = sheep.$platform.device.statusBarHeight * 2;

  const state = reactive({
    accountInfo: {
      // 提现表单
      type: undefined,
      userAccount: undefined,
      userName: undefined,
      qrCodeUrl: undefined,
      bankName: undefined,
      bankAddress: undefined,
    },

    accountSelect: false,

    brokerageInfo: {}, // 分销信息

    frozenDays: 0, // 冻结天数
    minPrice: 0, // 最低提现金额
    withdrawTypes: [], // 提现方式
    bankList: [], // 银行字典数据
    bankListSelectedIndex: '', // 选中银行 bankList 的 index
  });

  // 打开提现方式的弹窗
  const onAccountSelect = (e) => {
    state.accountSelect = e;
  };

  // 提交提现
  const onConfirm = async () => {
    // 参数校验
    if (
      !state.accountInfo.price ||
      state.accountInfo.price > state.brokerageInfo.price ||
      state.accountInfo.price <= 0
    ) {
      sheep.$helper.toast('请输入正确的提现金额');
      return;
    }
    if (!state.accountInfo.type) {
      sheep.$helper.toast('请选择提现方式');
      return;
    }
    let openid;
    if (state.accountInfo.type === '5') {
      openid = await sheep.$platform.useProvider('wechat').getOpenid();
      // 如果获取不到 openid，微信无法发起支付，此时需要引导
      if (!openid) {
        goBindWeixin();
        return;
      }
    }

    // 提交请求
    const data = {
      ...state.accountInfo,
      price: state.accountInfo.price * 100,
    };
    if (state.accountInfo.type === '5') {
      data.userAccount = openid;
      data.transferChannelCode = getWeixinPayChannelCode();
    } else if (state.accountInfo.type === '6' || state.accountInfo.type === '2') {
      delete data.transferChannelCode;
    } else {
      delete data.userAccount;
      delete data.transferChannelCode;
    }
    let { code } = await BrokerageApi.createBrokerageWithdraw(data);
    if (code !== 0) {
      return;
    }
    // 提示
    uni.showModal({
      title: '操作成功',
      content: '您的提现申请已成功提交',
      cancelText: '继续提现',
      confirmText: '查看记录',
      success: (res) => {
        if (res.confirm) {
          sheep.$router.go('/pages/commission/wallet', { type: 2 });
          return;
        }
        getBrokerageUser();
        state.accountInfo = {};
      },
    });
  };

  // 获得分销配置
  async function getWithdrawRules() {
    let { code, data } = await TradeConfigApi.getTradeConfig();
    if (code !== 0) {
      return;
    }
    if (data) {
      state.minPrice = data.brokerageWithdrawMinPrice || 0;
      state.frozenDays = data.brokerageFrozenDays || 0;
      state.withdrawTypes = data.brokerageWithdrawTypes;
    }
  }

  // 获得分销信息
  async function getBrokerageUser() {
    const { data, code } = await BrokerageApi.getBrokerageUser();
    if (code === 0) {
      state.brokerageInfo = data;
    }
  }

  // 获取提现银行配置字典
  async function getDictDataListByType() {
    let { code, data } = await DictApi.getDictDataListByType('brokerage_bank_name');
    if (code !== 0) {
      return;
    }
    if (data && data.length > 0) {
      state.bankList = data;
    }
  }

  // 银行选择
  function bankChange(e) {
    const value = e.detail.value;
    state.bankListSelectedIndex = value;
    state.accountInfo.bankName = state.bankList[value].label;
  }

  onBeforeMount(() => {
    getWithdrawRules();
    getBrokerageUser();
    getDictDataListByType(); //获取银行字典数据
  });
</script>

<style lang="scss" scoped>
  :deep() {
    .uni-input-input {
      font-family: OPPOSANS !important;
    }
  }

  .wallet-num-box {
    padding: 0 40rpx 80rpx;
    background: var(--ui-BG-Main) v-bind(headerBg) center/750rpx 100% no-repeat;
    border-radius: 0 0 5% 5%;

    .num-title {
      font-size: 26rpx;
      font-weight: 500;
      color: $white;
      margin-bottom: 20rpx;
    }

    .wallet-num {
      font-size: 60rpx;
      font-weight: 500;
      color: $white;
      font-family: OPPOSANS;
    }

    .log-btn {
      width: 170rpx;
      height: 60rpx;
      line-height: 60rpx;
      border: 1rpx solid $white;
      border-radius: 30rpx;
      padding: 0;
      font-size: 26rpx;
      font-weight: 500;
      color: $white;
    }
  }

  // 提现输入卡片
  .draw-card {
    background-color: $white;
    border-radius: 20rpx;
    width: 690rpx;
    min-height: 560rpx;
    margin: -60rpx 30rpx 30rpx 30rpx;
    padding: 30rpx;
    position: relative;
    z-index: 3;
    box-sizing: border-box;

    .card-title {
      font-size: 30rpx;
      font-weight: 500;
      margin-bottom: 30rpx;
    }

    .bank-box {
      .name {
        font-size: 28rpx;
        font-weight: 500;
      }

      .bank-list {
        .empty-text {
          font-size: 28rpx;
          font-weight: 400;
          color: $dark-9;
        }

        .cicon-forward {
          color: $dark-9;
        }
      }

      .input-box {
        width: 624rpx;
        height: 100rpx;
        margin-bottom: 40rpx;

        .unit {
          font-size: 48rpx;
          color: #333;
          font-weight: 500;
        }

        .uni-easyinput__placeholder-class {
          font-size: 30rpx;
          height: 36rpx;
        }

        :deep(.uni-easyinput__content-input) {
          font-size: 48rpx;
        }
      }

      .save-btn {
        width: 616rpx;
        height: 86rpx;
        line-height: 86rpx;
        border-radius: 40rpx;
        margin-top: 80rpx;
      }
    }

    .bind-box {
      .placeholder-text {
        font-size: 26rpx;
        color: $dark-9;
      }

      .add-btn {
        width: 100rpx;
        height: 50rpx;
        border-radius: 25rpx;
        line-height: 50rpx;
        font-size: 22rpx;
        color: var(--ui-BG-Main);
        background-color: var(--ui-BG-Main-light);
      }
    }

    .input-box {
      width: 624rpx;
      height: 100rpx;
      margin-bottom: 40rpx;

      .unit {
        font-size: 48rpx;
        color: #333;
        font-weight: 500;
      }

      .uni-easyinput__placeholder-class {
        font-size: 30rpx;
      }

      :deep(.uni-easyinput__content-input) {
        font-size: 48rpx;
      }
    }

    .save-btn {
      width: 616rpx;
      height: 86rpx;
      line-height: 86rpx;
      border-radius: 40rpx;
      margin-top: 80rpx;
    }
  }

  // 提现说明
  .draw-notice {
    width: 684rpx;
    background: #ffffff;
    border: 2rpx solid #fffaee;
    border-radius: 20rpx;
    margin: 20rpx 32rpx 0 32rpx;
    padding: 30rpx;
    box-sizing: border-box;

    .title {
      font-weight: 500;
      color: #333333;
      font-size: 30rpx;
    }

    .draw-list {
      font-size: 24rpx;
      color: #999999;
      line-height: 46rpx;
    }
  }
</style>
