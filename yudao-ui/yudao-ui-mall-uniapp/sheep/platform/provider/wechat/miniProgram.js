import AuthUtil from '@/sheep/api/member/auth';
import SocialApi from '@/sheep/api/member/social';
import UserApi from '@/sheep/api/member/user';
import sheep from '@/sheep';

const socialType = 34; // 社交类型 - 微信小程序

let subscribeEventList = [];

// 加载微信小程序
function load() {
  checkUpdate();
  getSubscribeTemplate();
}

// 微信小程序静默授权登陆
const login = async () => {
  return new Promise(async (resolve, reject) => {
    // 1. 获得微信 code
    const codeResult = await uni.login();
    if (codeResult.errMsg !== 'login:ok') {
      return resolve(false);
    }

    // 2. 社交登录
    const loginResult = await AuthUtil.socialLogin(socialType, codeResult.code, 'default');
    if (loginResult.code === 0) {
      setOpenid(loginResult.data.openid);
      return resolve(true);
    } else {
      return resolve(false);
    }
  });
};

// 微信小程序手机号授权登陆
const mobileLogin = async (e) => {
  return new Promise(async (resolve, reject) => {
    if (e.errMsg !== 'getPhoneNumber:ok') {
      return resolve(false);
    }

    // 1. 获得微信 code
    const codeResult = await uni.login();
    if (codeResult.errMsg !== 'login:ok') {
      return resolve(false);
    }

    // 2. 一键登录
    const loginResult = await AuthUtil.weixinMiniAppLogin(e.code, codeResult.code, 'default');
    if (loginResult.code === 0) {
      setOpenid(loginResult.data.openid);
      return resolve(true);
    } else {
      return resolve(false);
    }
  });
};

// 微信小程序绑定
const bind = () => {
  return new Promise(async (resolve, reject) => {
    // 1. 获得微信 code
    const codeResult = await uni.login();
    if (codeResult.errMsg !== 'login:ok') {
      return resolve(false);
    }

    // 2. 绑定账号
    const bindResult = await SocialApi.socialBind(socialType, codeResult.code, 'default');
    if (bindResult.code === 0) {
      setOpenid(bindResult.data);
      return resolve(true);
    } else {
      return resolve(false);
    }
  });
};

// 微信小程序解除绑定
const unbind = async (openid) => {
  const { code } = await SocialApi.socialUnbind(socialType, openid);
  return code === 0;
};

// 绑定用户手机号
const bindUserPhoneNumber = (e) => {
  return new Promise(async (resolve, reject) => {
    const { code } = await UserApi.updateUserMobileByWeixin(e.code);
    if (code === 0) {
      resolve(true);
    }
    resolve(false);
  });
};

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

// ========== 非登录相关的逻辑 ==========

// 小程序更新
const checkUpdate = async (silence = true) => {
  if (uni.canIUse('getUpdateManager')) {
    const updateManager = uni.getUpdateManager();
    updateManager.onCheckForUpdate(function (res) {
      // 请求完新版本信息的回调
      if (res.hasUpdate) {
        updateManager.onUpdateReady(function () {
          uni.showModal({
            title: '更新提示',
            content: '新版本已经准备好，是否重启应用？',
            success: function (res) {
              if (res.confirm) {
                // 新的版本已经下载好，调用 applyUpdate 应用新版本并重启
                updateManager.applyUpdate();
              }
            },
          });
        });
        updateManager.onUpdateFailed(function () {
          // 新的版本下载失败
          // uni.showModal({
          //   title: '已经有新版本了哟~',
          //   content: '新版本已经上线啦，请您删除当前小程序，重新搜索打开~',
          // });
        });
      } else {
        if (!silence) {
          uni.showModal({
            title: '当前为最新版本',
            showCancel: false,
          });
        }
      }
    });
  }
};

// 获取订阅消息模板
async function getSubscribeTemplate() {
  const { code, data } = await SocialApi.getSubscribeTemplateList();
  if (code === 0) {
    subscribeEventList = data;
  }
}

// 订阅消息
function subscribeMessage(event, callback = undefined) {
  let tmplIds = [];
  if (typeof event === 'string') {
    const temp = subscribeEventList.find((item) => item.title.includes(event));
    if (temp) {
      tmplIds.push(temp.id);
    }
  }
  if (typeof event === 'object') {
    event.forEach((e) => {
      const temp = subscribeEventList.find((item) => item.title.includes(e));
      if (temp) {
        tmplIds.push(temp.id);
      }
    });
  }
  if (tmplIds.length === 0) return;

  uni.requestSubscribeMessage({
    tmplIds,
    success: () => {
      // 不管是拒绝还是同意都触发
      callback && callback();
    },
    fail: (err) => {
      console.log(err);
    },
  });
}

// 商家转账用户确认模式下，拉起页面请求用户确认收款 Transfer
function requestMerchantTransfer(mchId, packageInfo, successCallback, failCallback) {
  if (!wx.canIUse('requestMerchantTransfer')) {
    wx.showModal({
      content: '你的微信版本过低，请更新至最新版本。',
      showCancel: false,
    });
    return;
  }
  wx.requestMerchantTransfer({
    mchId: mchId,
    appId: wx.getAccountInfoSync().miniProgram.appId,
    package: packageInfo,
    success: (res) => {
      // res.err_msg 将在页面展示成功后返回应用时返回 ok，并不代表付款成功
      console.log('success:', res);
      successCallback && successCallback(res);
    },
    fail: (res) => {
      console.log('fail:', res);
      sheep.$helper.toast(res.errMsg);
      failCallback && failCallback(res);
    },
  });
}

export default {
  load,
  login,
  bind,
  unbind,
  bindUserPhoneNumber,
  mobileLogin,
  getInfo,
  getOpenid,
  subscribeMessage,
  checkUpdate,
  requestMerchantTransfer,
};
