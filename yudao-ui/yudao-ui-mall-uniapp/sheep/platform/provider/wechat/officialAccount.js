import $wxsdk from '@/sheep/libs/sdk-h5-weixin';
import { getRootUrl } from '@/sheep/helper';
import AuthUtil from '@/sheep/api/member/auth';
import SocialApi from '@/sheep/api/member/social';

const socialType = 31; // 社交类型 - 微信公众号

// 加载微信公众号JSSDK
async function load() {
  $wxsdk.init();
}

// 微信公众号登陆
async function login(code = '', state = '') {
  // 情况一：没有 code 时，去获取 code
  if (!code) {
    const loginUrl = await getLoginUrl();
    if (loginUrl) {
      uni.setStorageSync('returnUrl', location.href);
      window.location = loginUrl;
    }
    // 情况二：有 code 时，使用 code 去自动登录
  } else {
    // 解密 code 发起登陆
    const loginResult = await AuthUtil.socialLogin(socialType, code, state);
    if (loginResult.code === 0) {
      setOpenid(loginResult.data.openid);
      return loginResult;
    }
  }
  return false;
}

// 微信公众号绑定
async function bind(code = '', state = '') {
  // 情况一：没有 code 时，去获取 code
  if (code === '') {
    const loginUrl = await getLoginUrl('bind');
    if (loginUrl) {
      uni.setStorageSync('returnUrl', location.href);
      window.location = loginUrl;
    }
  } else {
    // 情况二：有 code 时，使用 code 去自动绑定
    const loginResult = await SocialApi.socialBind(socialType, code, state);
    if (loginResult.code === 0) {
      setOpenid(loginResult.data);
      return loginResult;
    }
  }
  return false;
}

// 微信公众号解除绑定
const unbind = async (openid) => {
  const { code } = await SocialApi.socialUnbind(socialType, openid);
  return code === 0;
};

// 获取公众号登陆地址
async function getLoginUrl(event = 'login') {
  const page = getRootUrl() + 'pages/index/login' + '?event=' + event; // event 目的，区分是 login 还是 bind
  const { code, data } = await AuthUtil.socialAuthRedirect(socialType, page);
  if (code !== 0) {
    return undefined;
  }
  return data;
}

// 设置 openid 到本地存储，目前只有 pay 支付时会使用
function setOpenid(openid) {
  uni.setStorageSync('openid', openid);
}

// 获得 openid
async function getOpenid(force = false) {
  let openid = uni.getStorageSync('openid');
  if (!openid && force) {
    const info = await getInfo();
    if (info && info.openid) {
      openid = info.openid;
      setOpenid(openid);
    }
  }
  return openid;
}

// 获得社交信息
async function getInfo() {
  const { code, data } = await SocialApi.getSocialUser(socialType);
  if (code !== 0) {
    return undefined;
  }
  return data;
}

export default {
  load,
  login,
  bind,
  unbind,
  getInfo,
  getOpenid,
  jsWxSdk: $wxsdk,
};
