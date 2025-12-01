/**
 * 支付渠道枚举
 */
export const PayChannelEnum = {
  WX_PUB: {
    code: 'wx_pub',
    name: '微信 JSAPI 支付',
  },
  WX_LITE: {
    code: 'wx_lite',
    name: '微信小程序支付',
  },
  WX_APP: {
    code: 'wx_app',
    name: '微信 APP 支付',
  },
  WX_NATIVE: {
    code: 'wx_native',
    name: '微信 Native 支付',
  },
  WX_WAP: {
    code: 'wx_wap',
    name: '微信 WAP 网站支付',
  },
  WX_BAR: {
    code: 'wx_bar',
    name: '微信条码支付',
  },
  ALIPAY_PC: {
    code: 'alipay_pc',
    name: '支付宝 PC 网站支付',
  },
  ALIPAY_WAP: {
    code: 'alipay_wap',
    name: '支付宝 WAP 网站支付',
  },
  ALIPAY_APP: {
    code: 'alipay_app',
    name: '支付宝 APP 支付',
  },
  ALIPAY_QR: {
    code: 'alipay_qr',
    name: '支付宝扫码支付',
  },
  ALIPAY_BAR: {
    code: 'alipay_bar',
    name: '支付宝条码支付',
  },
  WALLET: {
    code: 'wallet',
    name: '钱包支付',
  },
  MOCK: {
    code: 'mock',
    name: '模拟支付',
  },
};

/**
 * 支付的展示模式每局
 */
export const PayDisplayModeEnum = {
  URL: {
    mode: 'url',
  },
  IFRAME: {
    mode: 'iframe',
  },
  FORM: {
    mode: 'form',
  },
  QR_CODE: {
    mode: 'qr_code',
  },
  APP: {
    mode: 'app',
  },
};

/**
 * 支付类型枚举
 */
export const PayType = {
  WECHAT: 'WECHAT',
  ALIPAY: 'ALIPAY',
  MOCK: 'MOCK',
};

/**
 * 支付订单状态枚举
 */
export const PayOrderStatusEnum = {
  WAITING: {
    status: 0,
    name: '未支付',
  },
  SUCCESS: {
    status: 10,
    name: '已支付',
  },
  CLOSED: {
    status: 20,
    name: '未支付',
  },
};
