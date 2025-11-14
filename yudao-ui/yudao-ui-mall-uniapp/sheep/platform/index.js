/**
 * Shopro 第三方平台功能聚合
 * @version 1.0.3
 * @author lidongtony
 * @param {String} name - 厂商+平台名称
 * @param {String} provider - 厂商
 * @param {String} platform - 平台名称
 * @param {String} os - 系统型号
 * @param {Object} device - 设备信息
 */

import { isEmpty } from 'lodash-es';
// #ifdef H5
import { isWxBrowser } from '@/sheep/helper/utils';
// #endif
import wechat from './provider/wechat/index.js';
import apple from './provider/apple';
import share from './share';
import Pay from './pay';

const device = uni.getWindowInfo();

const os = uni.getDeviceInfo().platform;

let name = '';
let provider = '';
let platform = '';
let isWechatInstalled = true;

// #ifdef H5
if (isWxBrowser()) {
  name = 'WechatOfficialAccount';
  provider = 'wechat';
  platform = 'officialAccount';
} else {
  name = 'H5';
  platform = 'h5';
}
// #endif

// #ifdef APP-PLUS
name = 'App';
platform = 'openPlatform';
// 检查微信客户端是否安装，否则AppleStore会因此拒绝上架
if (os === 'ios') {
  isWechatInstalled = plus.ios.import('WXApi').isWXAppInstalled();
}
// #endif

// #ifdef MP-WEIXIN
name = 'WechatMiniProgram';
platform = 'miniProgram';
provider = 'wechat';
// #endif

if (isEmpty(name)) {
  uni.showToast({
    title: '暂不支持该平台',
    icon: 'none',
  });
}

// 加载当前平台前置行为
const load = () => {
  if (provider === 'wechat') {
    wechat.load();
  }
};

// 使用厂商独占sdk name = 'wechat' | 'alipay' | 'apple'
const useProvider = (_provider = '') => {
  if (_provider === '') _provider = provider;
  if (_provider === 'wechat') return wechat;
  if (_provider === 'apple') return apple;
};

// 支付服务转发
const pay = (payment, orderType, orderSN) => {
  return new Pay(payment, orderType, orderSN);
};

/**
 * 检查更新 (只检查小程序和App)
 * @param {Boolean} silence - 静默检查
 */
const checkUpdate = (silence = false) => {
  let canUpdate;
  // #ifdef MP-WEIXIN
  useProvider().checkUpdate(silence);
  // #endif

  // #ifdef APP-PLUS
  // TODO: 热更新
  // #endif
};

/**
 * 检查网络
 * @param {Boolean} silence - 静默检查
 */
async function checkNetwork() {
  const networkStatus = await uni.getNetworkType();
  if (networkStatus.networkType == 'none') {
    return Promise.resolve(false);
  }
  return Promise.resolve(true);
}

// 获取小程序胶囊信息
const getCapsule = () => {
  // #ifdef MP
  let capsule = uni.getMenuButtonBoundingClientRect();
  if (!capsule) {
    capsule = {
      bottom: 56,
      height: 32,
      left: 278,
      right: 365,
      top: 24,
      width: 87,
    };
  }
  return capsule;
  // #endif

  // #ifndef MP
  return {
    bottom: 56,
    height: 32,
    left: 278,
    right: 365,
    top: 24,
    width: 87,
  };
  // #endif
};

const capsule = getCapsule();

// 标题栏高度
const getNavBar = () => {
  return device.statusBarHeight + 44;
};
const navbar = getNavBar();

function getLandingPage() {
  let page = '';
  // #ifdef H5
  page = location.href.split('?')[0];
  // #endif
  return page;
}

// 设置ios+公众号网页落地页 解决微信sdk签名问题
const landingPage = getLandingPage();

const _platform = {
  name,
  device,
  os,
  provider,
  platform,
  useProvider,
  checkUpdate,
  checkNetwork,
  pay,
  share,
  load,
  capsule,
  navbar,
  landingPage,
  isWechatInstalled,
};

export default _platform;
