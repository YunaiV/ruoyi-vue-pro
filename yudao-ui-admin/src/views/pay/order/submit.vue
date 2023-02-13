<template>
  <div class="app-container">
    <!-- 支付信息 -->
    <el-card v-loading="loading">
      <el-descriptions title="支付信息" :column="3" border>
        <el-descriptions-item label="支付单号">{{ payOrder.id }}</el-descriptions-item>
        <el-descriptions-item label="商品标题">{{ payOrder.subject }}</el-descriptions-item>
        <el-descriptions-item label="商品内容">{{ payOrder.body }}</el-descriptions-item>
        <el-descriptions-item label="支付金额">￥{{ (payOrder.amount / 100.0).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ parseTime(payOrder.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="过期时间">{{ parseTime(payOrder.expireTime) }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 微信支付 -->
    <el-card style="margin-top: 10px">
      <!-- 支付宝 -->
      <el-descriptions title="选择支付宝支付">
      </el-descriptions>
      <div class="pay-channel-container">
        <div class="box" v-for="channel in aliPayChannels" :key="channel.code">
          <img :src="icons[channel.code]">
          <div class="title">{{ channel.name }}</div>
        </div>
      </div>
      <!-- 微信支付 -->
      <el-descriptions title="选择微信支付" style="margin-top: 20px;" />
      <div class="pay-channel-container">
        <div class="box" v-for="channel in wxPayChannels" :key="channel.code">
          <img :src="icons[channel.code]">
          <div class="title">{{ channel.name }}</div>
        </div>
      </div>
      <!-- 其它支付 -->
      <el-descriptions title="选择其它支付" style="margin-top: 20px;" />
      <div class="pay-channel-container">
        <div class="box" v-for="channel in otherPayChannels" :key="channel.code">
          <img :src="icons[channel.code]">
          <div class="title">{{ channel.name }}</div>
        </div>
      </div>
    </el-card>
  </div>
</template>
<script>
import { DICT_TYPE, getDictDatas } from "@/utils/dict";
import { getOrder } from '@/api/pay/order';
import { PayOrderStatusEnum } from "@/utils/constants";

export default {
  name: "PayOrderSubmit",
  components: {
  },
  data() {
    return {
      id: undefined, // 请假编号
      loading: false, // 支付信息的 loading
      payOrder: {}, // 支付信息
      aliPayChannels: [], // 阿里支付的渠道
      wxPayChannels: [], // 微信支付的渠道
      otherPayChannels: [], // 其它的支付渠道
      icons: {
        alipay_qr: require("@/assets/images/pay/icon/alipay_qr.svg"),
        alipay_app: require("@/assets/images/pay/icon/alipay_app.svg"),
        alipay_wap: require("@/assets/images/pay/icon/alipay_wap.svg"),
        alipay_pc: require("@/assets/images/pay/icon/alipay_pc.svg"),
        wx_app: require("@/assets/images/pay/icon/wx_app.svg"),
        wx_lite: require("@/assets/images/pay/icon/wx_lite.svg"),
        wx_pub: require("@/assets/images/pay/icon/wx_pub.svg"),
        mock: require("@/assets/images/pay/icon/mock.svg"),
      },
    };
  },
  created() {
    this.id = this.$route.query.id;
    this.getDetail();
    this.initPayChannels();
  },
  methods: {
    /** 初始化支付渠道 */
    initPayChannels() {
      // 微信支付
      for (const dict of getDictDatas(DICT_TYPE.PAY_CHANNEL_CODE_TYPE)) {
        const payChannel = {
          name: dict.label,
          code: dict.value
        }
        if (dict.value.indexOf('wx_') === 0) {
          this.wxPayChannels.push(payChannel);
        } else if (dict.value.indexOf('alipay_') === 0) {
          this.aliPayChannels.push(payChannel);
        } else {
          this.otherPayChannels.push(payChannel);
        }
      }
    },
    /** 获得请假信息 */
    getDetail() {
      // 1.1 未传递订单编号
      if (!this.id) {
        this.$message.error('未传递支付单号，无法查看对应的支付信息');
        this.$router.go(-1);
        return;
      }
      getOrder(this.id).then(response => {
        // 1.2 无法查询到支付信息
        if (!response.data) {
          this.$message.error('支付订单不存在，请检查！');
          this.$router.go(-1);
          return;
        }
        // 1.3 订单已支付
        if (response.data.status !== PayOrderStatusEnum.WAITING.status) {
          this.$message.error('支付订单不处于待支付状态，请检查！');
          this.$router.go(-1);
          return;
        }

        // 2. 可以展示
        this.payOrder = response.data;
      });
    },
  }
};
</script>
<style lang="scss" scoped>
.pay-channel-container {
  display: flex;
  margin-top: -10px;
  .box {
    width: 130px;
    border: 1px solid #e6ebf5;
    cursor: pointer;
    text-align: center;
    padding-top: 10px;
    padding-bottom: 5px;
    margin-right: 10px;
    img {
      width: 40px;
      height: 40px;
    }
    .title {
      padding-top: 5px
    }
  }
}
</style>
