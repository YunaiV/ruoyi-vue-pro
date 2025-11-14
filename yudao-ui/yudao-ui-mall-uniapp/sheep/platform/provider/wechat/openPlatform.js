// 登录
import third from '@/sheep/api/migration/third';
import SocialApi from '@/sheep/api/member/social';
import $share from '@/sheep/platform/share';

// TODO 芋艿：等后面搞 App 再弄
const socialType = 32; // 社交类型 - 微信开放平台

const load = async () => {};

// 微信开放平台移动应用授权登陆
const login = () => {
  return new Promise(async (resolve, reject) => {
    const loginRes = await uni.login({
      provider: 'weixin',
      onlyAuthorize: true,
    });
    debugger
    if (loginRes.errMsg == 'login:ok') {
      // TODO third.wechat.login 函数未实现
      const res = await third.wechat.login({
        platform: 'openPlatform',
        shareInfo: uni.getStorageSync('shareLog') || {},
        payload: encodeURIComponent(
          JSON.stringify({
            code: loginRes.code,
          }),
        ),
      });

      if (res.error === 0) {
        $share.bindBrokerageUser()
        resolve(true);
      }
    } else {
      uni.showToast({
        icon: 'none',
        title: loginRes.errMsg,
      });
    }
    resolve(false);
  });
};

// 微信 App 解除绑定
const unbind = async (openid) => {
  const { code } = await SocialApi.socialUnbind(socialType, openid);
  return code === 0;
};

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
  getInfo
};
