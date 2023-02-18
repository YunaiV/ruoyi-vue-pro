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

    <!-- 支付选择框 -->
    <el-card style="margin-top: 10px" v-loading="submitLoading"  element-loading-text="提交支付中...">
      <!-- 支付宝 -->
      <el-descriptions title="选择支付宝支付">
      </el-descriptions>
      <div class="pay-channel-container">
        <div class="box" v-for="channel in aliPayChannels" :key="channel.code" @click="submit(channel.code)">
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

    <!-- 展示形式：二维码 -->
    <el-dialog :title="qrCode.title" :visible.sync="qrCode.visible" width="350px" append-to-body
               :close-on-press-escape="false">
      <qrcode-vue :value="qrCode.url" size="310" level="H" />
    </el-dialog>

    <!-- 展示形式：iframe -->
    <el-dialog :title="iframe.title" :visible.sync="iframe.visible" width="800px" height="800px" append-to-body
               :close-on-press-escape="false">
      <iframe :src="iframe.url" width="100%" />
    </el-dialog>

    <!-- 阿里支付 -->
    <div ref="alipayWap" v-html="alipayHtml.value" />

  </div>
</template>
<script>
import QrcodeVue from 'qrcode.vue'
import { DICT_TYPE, getDictDatas } from "@/utils/dict";
import { getOrder, submitOrder } from '@/api/pay/order';
import {PayChannelEnum, PayDisplayModeEnum, PayOrderStatusEnum} from "@/utils/constants";

export default {
  name: "PayOrderSubmit",
  components: {
    QrcodeVue,
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
      submitLoading: false, // 提交支付的 loading
      qrCode: { // 展示形式：二维码
        url: '',
        title: '',
        visible: false,
      },
      iframe: { // 展示形式：iframe
        url: '',
        title: '',
        visible: false
      },
      interval: undefined, // 定时任务，轮询是否完成支付
      alipayHtml: '' // 阿里支付的 HTML
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
    /** 获得支付信息 */
    getDetail() {
      // 1.1 未传递订单编号
      if (!this.id) {
        this.$message.error('未传递支付单号，无法查看对应的支付信息');
        this.goBackToList();
        return;
      }
      getOrder(this.id).then(response => {
        // 1.2 无法查询到支付信息
        if (!response.data) {
          this.$message.error('支付订单不存在，请检查！');
          this.goBackToList();
          return;
        }
        // 1.3 订单已支付
        if (response.data.status !== PayOrderStatusEnum.WAITING.status) {
          this.$message.error('支付订单不处于待支付状态，请检查！');
          this.goBackToList();
          return;
        }

        // 2. 可以展示
        this.payOrder = response.data;
      });
    },
    /** 提交支付 */
    submit(channelCode) {
      this.submitLoading = true
      submitOrder({
        id: this.id,
        channelCode: channelCode,
        ...this.buildSubmitParam(channelCode)
      }).then(response => {
        const data = response.data
        if (data.displayMode === 'iframe') {
          this.displayIFrame(channelCode, data)
        } else if (data.displayMode === 'url') {
          this.displayUrl(channelCode, data)
        }
        // 不同的支付，调用不同的策略
        // if (channelCode === PayChannelEnum.ALIPAY_QR.code) {
        //   this.submitAfterAlipayQr(invokeResponse)
        // } else if (channelCode === PayChannelEnum.ALIPAY_PC.code
        //   || channelCode === PayChannelEnum.ALIPAY_WAP.code) {
        //   this.submitAfterAlipayPc(invokeResponse)
        // }

        // 打开轮询任务
        this.createQueryInterval()
      })
    },
    /** 构建提交支付的额外参数 */
    buildSubmitParam(channelCode) {
      // 支付宝网页支付时，有多种展示形态
      if (channelCode === PayChannelEnum.ALIPAY_PC.code) {
        // 情况【前置模式】：将二维码前置到商户的订单确认页的模式。需要商户在自己的页面中以 iframe 方式请求支付宝页面。具体支持的枚举值有以下几种：
        // 0：订单码-简约前置模式，对应 iframe 宽度不能小于 600px，高度不能小于 300px
        // return {
        //   "channelExtras": {
        //     "qr_pay_mode": "0"
        //   }
        // }
        // 1：订单码-前置模式，对应iframe 宽度不能小于 300px，高度不能小于 600px
        // return {
        //   "channelExtras": {
        //     "qr_pay_mode": "1"
        //   }
        // }
        // 3：订单码-迷你前置模式，对应 iframe 宽度不能小于 75px，高度不能小于 75px
        // return {
        //   "channelExtras": {
        //     "qr_pay_mode": "3"
        //   }
        // }
        // 4：订单码-可定义宽度的嵌入式二维码，商户可根据需要设定二维码的大小
        // return {
        //   "channelExtras": {
        //     "qr_pay_mode": "4"
        //   }
        // }
        // 情况【跳转模式】：跳转模式下，用户的扫码界面是由支付宝生成的，不在商户的域名下。支持传入的枚举值有
        return {
          "channelExtras": {
            "qr_pay_mode": "2"
          }
        }
      }
      return {}
    },
    /** 提交支付后（支付宝扫码支付） */
    submitAfterAlipayQr(invokeResponse) {
      this.qrCode = {
        title: '请使用支付宝“扫一扫”扫码支付',
        url: invokeResponse.qrCode,
        visible: true
      }
      this.submitLoading = false
    },
    /** 提交支付后，IFrame 内置 URL 的展示形式 */
    displayIFrame(channelCode, data) {
      // TODO 芋艿：目前有点奇怪，支付宝总是会显示“支付环境存在风险”
      this.iframe = {
        title: '支付窗口',
        url: data.displayContent,
        visible: true
      }
      this.submitLoading = false
    },
    /** 提交支付后，URL 的展示形式 */
    displayUrl(channelCode, data) {
      window.open(data.displayContent)
      this.submitLoading = false
    },
    /** 提交支付后（支付宝 PC 网站支付） */
    submitAfterAlipayPc(invokeResponse) {
      // 渲染支付页面
      this.alipayHtml = {
        value: invokeResponse.body,
        visible: true
      }
      // 防抖避免重复支付
      this.$nextTick(() => {
        // 提交支付表单
        // this.$refs.alipayWap.children[0].submit();
        // setTimeout(() => {
        //   this.submitLoading = false
        // }, 1000);
      });
    },
    /** 轮询查询任务 */
    createQueryInterval() {
      if (!this.interval) {
        return
      }
      this.interval = setInterval(() => {
        getOrder(this.id).then(response => {
          // 已支付
          if (response.data.status === PayOrderStatusEnum.SUCCESS.status) {
            this.clearQueryInterval();
            this.$message.success('支付成功！');
            this.goBackToList();
          }
          // 已取消
          if (response.data.status === PayOrderStatusEnum.CLOSED.status) {
            this.clearQueryInterval();
            this.$message.error('支付已关闭！');
            this.goBackToList();
          }
        })
      }, 1000 * 2)
    },
    /** 清空查询任务 */
    clearQueryInterval() {
      // 清空各种弹窗
      this.qrCode = {
        title: '',
        url: '',
        visible: false
      }
      // 清空任务
      clearInterval(this.interval)
      this.interval = undefined
    },
    /** 回到列表 **/
    goBackToList() {
      this.$tab.closePage();
      this.$router.go(-1);
    }
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
