import sheep from '@/sheep';
// #ifdef H5
import $wxsdk from '@/sheep/libs/sdk-h5-weixin';
// #endif
import { getRootUrl } from '@/sheep/helper';
import PayOrderApi from '@/sheep/api/pay/order';

/**
 * 支付
 *
 * @param {String} payment = ['wechat','alipay','wallet','mock']  	- 支付方式
 * @param {String} orderType = ['goods','recharge','groupon']  	- 订单类型
 * @param {String} id					- 订单号
 */

export default class SheepPay {
  constructor(payment, orderType, id) {
    this.payment = payment;
    this.id = id;
    this.orderType = orderType;
    this.payAction();
  }

  payAction() {
    const payAction = {
      WechatOfficialAccount: {
        wechat: () => {
          this.wechatOfficialAccountPay();
        },
        alipay: () => {
          this.redirectPay(); // 现在公众号可以直接跳转支付宝页面
        },
        wallet: () => {
          this.walletPay();
        },
        mock: () => {
          this.mockPay();
        },
      },
      WechatMiniProgram: {
        wechat: () => {
          this.wechatMiniProgramPay();
        },
        alipay: () => {
          this.copyPayLink();
        },
        wallet: () => {
          this.walletPay();
        },
        mock: () => {
          this.mockPay();
        },
      },
      App: {
        wechat: () => {
          this.wechatAppPay();
        },
        alipay: () => {
          this.alipay();
        },
        wallet: () => {
          this.walletPay();
        },
        mock: () => {
          this.mockPay();
        },
      },
      H5: {
        wechat: () => {
          this.wechatWapPay();
        },
        alipay: () => {
          this.redirectPay();
        },
        wallet: () => {
          this.walletPay();
        },
        mock: () => {
          this.mockPay();
        },
      },
    };
    return payAction[sheep.$platform.name][this.payment]();
  }

  // 预支付
  prepay(channel) {
    return new Promise(async (resolve, reject) => {
      let data = {
        id: this.id,
        channelCode: channel,
        channelExtras: {},
      };
      // 特殊逻辑：微信公众号、小程序支付时，必须传入 openid
      if (['wx_pub', 'wx_lite'].includes(channel)) {
        const openid = await sheep.$platform.useProvider('wechat').getOpenid(true);
        // 如果获取不到 openid，微信无法发起支付，此时需要引导
        if (!openid) {
          this.bindWeixin();
          return;
        }
        data.channelExtras.openid = openid;
      }
      // 发起预支付 API 调用
      PayOrderApi.submitOrder(data).then((res) => {
        // 成功时
        res.code === 0 && resolve(res);
        // 失败时
        if (res.code !== 0 && res.msg.indexOf('无效的openid') >= 0) {
          // 特殊逻辑：微信公众号、小程序支付时，必须传入 openid 不正确的情况
          if (
            res.msg.indexOf('无效的openid') >= 0 || // 获取的 openid 不正确时，或者随便输入了个 openid
            res.msg.indexOf('下单账号与支付账号不一致') >= 0
          ) {
            // https://developers.weixin.qq.com/community/develop/doc/00008c53c347804beec82aed051c00
            this.bindWeixin();
          }
        }
      });
    });
  }
  // #ifdef H5
  // 微信公众号 JSSDK 支付
  async wechatOfficialAccountPay() {
    let { code, data } = await this.prepay('wx_pub');
    if (code !== 0) {
      return;
    }
    const payConfig = JSON.parse(data.displayContent);
    $wxsdk.wxpay(payConfig, {
      success: () => {
        this.payResult('success');
      },
      cancel: () => {
        sheep.$helper.toast('支付已手动取消');
      },
      fail: (error) => {
        if (error.errMsg.indexOf('chooseWXPay:没有此SDK或暂不支持此SDK模拟') >= 0) {
          sheep.$helper.toast(
            '发起微信支付失败，原因：可能是微信开发者工具不支持，建议使用微信打开网页后支付',
          );
          return;
        }
        this.payResult('fail');
      },
    });
  }

  // 浏览器微信 H5 支付 TODO 芋艿：待接入（注意：H5 支付是给普通浏览器，不是微信公众号的支付，绝大多数人用不到，可以忽略）
  async wechatWapPay() {
    const { error, data } = await this.prepay();
    if (error === 0) {
      const redirect_url = `${getRootUrl()}pages/pay/result?id=${this.id}&payment=${
        this.payment
      }&orderType=${this.orderType}`;
      location.href = `${data.pay_data.h5_url}&redirect_url=${encodeURIComponent(redirect_url)}`;
    }
  }

  // 支付链接(支付宝 wap 支付)
  async redirectPay() {
    let { code, data } = await this.prepay('alipay_wap');
    if (code !== 0) {
      return;
    }
    location.href = data.displayContent;
  }

  // #endif

  // 微信小程序支付
  async wechatMiniProgramPay() {
    // let that = this;
    let { code, data } = await this.prepay('wx_lite');
    if (code !== 0) {
      return;
    }
    // 调用微信小程序支付
    const payConfig = JSON.parse(data.displayContent);
    uni.requestPayment({
      provider: 'wxpay',
      timeStamp: payConfig.timeStamp,
      nonceStr: payConfig.nonceStr,
      package: payConfig.packageValue,
      signType: payConfig.signType,
      paySign: payConfig.paySign,
      success: (res) => {
        this.payResult('success');
      },
      fail: (err) => {
        if (err.errMsg === 'requestPayment:fail cancel') {
          sheep.$helper.toast('支付已手动取消');
        } else {
          this.payResult('fail');
        }
      },
    });
  }

  // 余额支付
  async walletPay() {
    const { code } = await this.prepay('wallet');
    code === 0 && this.payResult('success');
  }

  // 模拟支付
  async mockPay() {
    const { code } = await this.prepay('mock');
    code === 0 && this.payResult('success');
  }

  // 支付宝复制链接支付（通过支付宝 wap 支付实现）
  async copyPayLink() {
    let { code, data } = await this.prepay('alipay_wap');
    if (code !== 0) {
      return;
    }
    // 引入 showModal 点击确认：复制链接；
    uni.showModal({
      title: '支付宝支付',
      content: '复制链接到外部浏览器',
      confirmText: '复制链接',
      success: (res) => {
        if (res.confirm) {
          sheep.$helper.copyText(data.displayContent);
        }
      },
    });
  }

  // 支付宝支付（App）
  async alipay() {
    let that = this;
    const { code, data } = await this.prepay('alipay_app');
    if (code !== 0) {
      return;
    }

    // TODO @芋艿：【可优化】如果是沙箱支付，EnvUtils.setEnv(EnvUtils.EnvEnum.SANDBOX)，相关 https://t.zsxq.com/yjyJQ
    uni.requestPayment({
      provider: 'alipay',
      orderInfo: data.displayContent, // 直接使用返回的支付参数
      success: (res) => {
        that.payResult('success');
      },
      fail: (err) => {
        if (err.errMsg === 'requestPayment:fail [paymentAlipay:62001]user cancel') {
          sheep.$helper.toast('支付已手动取消');
        } else {
          that.payResult('fail');
        }
      },
    });
  }

  // 微信支付（App）
  async wechatAppPay() {
    let that = this;
    // 获取预支付信息
    let { code, data } = await this.prepay('wx_app');
    if (code !== 0) {
      sheep.$helper.toast('获取支付信息失败');
      return;
    }

    // 解析支付参数
    let payConfig = JSON.parse(data.displayContent);
    if(typeof payConfig.appId === 'undefined'){
      payConfig.appId = payConfig.appid;
    }
    if(typeof payConfig.nonceStr === 'undefined'){
      payConfig.nonceStr = payConfig.noncestr;
    }
    if(typeof payConfig.timeStamp === 'undefined'){
      payConfig.timeStamp = payConfig.timestamp;
    }
    // 调用微信支付
    uni.requestPayment({
      provider: 'wxpay',
      timeStamp: payConfig.timeStamp,
      nonceStr: payConfig.nonceStr,
      package: payConfig.packageValue,
      signType: payConfig.signType,
      paySign: payConfig.paySign,
      success: (res) => {
        that.payResult('success');
      },
      fail: (err) => {
        if (err.errMsg === 'requestPayment:fail cancel') {
          sheep.$helper.toast('支付已手动取消');
        } else {
          sheep.$helper.toast('支付失败：' + err.errMsg);
          that.payResult('fail');
        }
      },
    });
  }

  // 支付结果跳转,success:成功，fail:失败
  payResult(resultType) {
    goPayResult(this.id, this.orderType, resultType);
  }

  // 引导绑定微信
  bindWeixin() {
    goBindWeixin();
  }
}

export function getPayMethods(channels) {
  const payMethods = [
    {
      icon: '/static/img/shop/pay/wechat.png',
      title: '微信支付',
      value: 'wechat',
      disabled: true,
    },
    {
      icon: '/static/img/shop/pay/alipay.png',
      title: '支付宝支付',
      value: 'alipay',
      disabled: true,
    },
    {
      icon: '/static/img/shop/pay/wallet.png',
      title: '余额支付',
      value: 'wallet',
      disabled: true,
    },
    {
      icon: '/static/img/shop/pay/apple.png',
      title: 'Apple Pay',
      value: 'apple',
      disabled: true,
    },
    {
      icon: '/static/img/shop/pay/wallet.png',
      title: '模拟支付',
      value: 'mock',
      disabled: true,
    },
  ];
  const platform = sheep.$platform.name;

  // 1. 处理【微信支付】
  const wechatMethod = payMethods[0];
  if (
    (platform === 'WechatOfficialAccount' && channels.includes('wx_pub')) ||
    (platform === 'WechatMiniProgram' && channels.includes('wx_lite')) ||
    (platform === 'App' && channels.includes('wx_app'))
  ) {
    wechatMethod.disabled = false;
  }

  // 2. 处理【支付宝支付】
  const alipayMethod = payMethods[1];
  if (
    (platform === 'H5' && channels.includes('alipay_wap')) ||
    (platform === 'WechatOfficialAccount' && channels.includes('alipay_wap')) ||
    (platform === 'WechatMiniProgram' && channels.includes('alipay_wap')) ||
    (platform === 'App' && channels.includes('alipay_app'))
  ) {
    alipayMethod.disabled = false;
  }
  // 3. 处理【余额支付】
  const walletMethod = payMethods[2];
  if (channels.includes('wallet')) {
    walletMethod.disabled = false;
  }
  // 4. 处理【苹果支付】TODO 芋艿：未来接入
  // 5. 处理【模拟支付】
  const mockMethod = payMethods[4];
  if (channels.includes('mock')) {
    mockMethod.disabled = false;
  }
  return payMethods;
}

// 支付结果跳转,success:成功，fail:失败
export function goPayResult(id, orderType, resultType) {
  sheep.$router.redirect('/pages/pay/result', {
    id,
    orderType,
    payState: resultType,
  });
}

export function goBindWeixin() {
  uni.showModal({
    title: '微信支付',
    content: '请先绑定微信再使用微信支付',
    success: function (res) {
      if (res.confirm) {
        sheep.$platform.useProvider('wechat').bind();
      }
    },
  });
}

// 获取微信支付渠道码
export function getWeixinPayChannelCode() {
  const platform = sheep.$platform.name;
  switch (platform) {
    case 'WechatOfficialAccount':
      return 'wx_pub';
    case 'WechatMiniProgram':
      return 'wx_lite';
    case 'App':
      return 'wx_app';
    case 'H5':
      return 'wx_wap';
    default:
      return '';
  }
}
