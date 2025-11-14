import {
  SvgAlipayAppIcon,
  SvgAlipayBarIcon,
  SvgAlipayPcIcon,
  SvgAlipayQrIcon,
  SvgAlipayWapIcon,
  SvgMockIcon,
  SvgWalletIcon,
  SvgWxAppIcon,
  SvgWxBarIcon,
  SvgWxLiteIcon,
  SvgWxNativeIcon,
  SvgWxPubIcon,
} from '@vben/icons';

export const channelsAlipay = [
  {
    name: '支付宝 PC 网站支付',
    icon: SvgAlipayPcIcon,
    code: 'alipay_pc',
  },
  {
    name: '支付宝 Wap 网站支付',
    icon: SvgAlipayWapIcon,
    code: 'alipay_wap',
  },
  {
    name: '支付宝 App 网站支付',
    icon: SvgAlipayAppIcon,
    code: 'alipay_app',
  },
  {
    name: '支付宝扫码支付',
    icon: SvgAlipayQrIcon,
    code: 'alipay_qr',
  },
  {
    name: '支付宝条码支付',
    icon: SvgAlipayBarIcon,
    code: 'alipay_bar',
  },
];

export const channelsWechat = [
  {
    name: '微信公众号支付',
    icon: SvgWxPubIcon,
    code: 'wx_pub',
  },
  {
    name: '微信小程序支付',
    icon: SvgWxLiteIcon,
    code: 'wx_lite',
  },
  {
    name: '微信 App 支付',
    icon: SvgWxAppIcon,
    code: 'wx_app',
  },
  {
    name: '微信扫码支付',
    icon: SvgWxNativeIcon,
    code: 'wx_native',
  },
  {
    name: '微信条码支付',
    icon: SvgWxBarIcon,
    code: 'wx_bar',
  },
];

export const channelsMock = [
  {
    name: '钱包支付',
    icon: SvgWalletIcon,
    code: 'wallet',
  },
  {
    name: '模拟支付',
    icon: SvgMockIcon,
    code: 'mock',
  },
];
