import $store from '@/sheep/store';
import $platform from '@/sheep/platform';
import $router from '@/sheep/router';
import $url from '@/sheep/url';
import BrokerageApi from '@/sheep/api/trade/brokerage';
import { SharePageEnum } from '@/sheep/helper/const';

// #ifdef H5
import $wxsdk from '@/sheep/libs/sdk-h5-weixin';
// #endif

// 设置分享的平台渠道: 1=H5,2=微信公众号网页,3=微信小程序,4=App,...按需扩展
const platformMap = ['H5', 'WechatOfficialAccount', 'WechatMiniProgram', 'App'];

// 设置分享方式: 1=直接转发,2=海报,3=复制链接,...按需扩展
const fromMap = ['forward', 'poster', 'link'];

// 设置分享信息参数
const getShareInfo = (
  scene = {
    title: '', // 自定义分享标题
    desc: '', // 自定义描述
    image: '', // 自定义分享图片
    params: {}, // 自定义分享参数
  },
  poster = {
    // 自定义海报数据
    type: 'user',
  },
) => {
  const shareInfo = {
    title: '', // 分享标题
    desc: '', // 描述
    image: '', // 分享图片
    path: '', // 分享页面+参数
    link: '', // 分享Url+参数
    query: '', // 分享参数
    poster, // 海报所需数据
    forward: {}, // 转发所需参数
  };
  shareInfo.title = scene.title;
  shareInfo.image = $url.cdn(scene.image);
  shareInfo.desc = scene.desc;

  const app = $store('app');
  const shareConfig = app.platform.share;

  // 自动拼接分享用户参数
  const query = buildSpmQuery(scene.params);
  shareInfo.query = query;

  // 配置分享链接地址
  shareInfo.link = buildSpmLink(query, shareConfig.linkAddress);
  // 配置页面地址带参数
  shareInfo.path = buildSpmPath();

  // 配置页面转发参数
  if (shareConfig.methods.includes('forward')) {
    shareInfo.forward.path = buildSpmPath(query);
  }

  return shareInfo;
};

/**
 * 构造 spm 分享参数
 *
 * @param params json 格式，其中包含：1）shareId 分享用户的编号；2）page 页面类型；3）query 页面 ID（参数）；4）platform 平台类型；5）from 分享来源类型。
 * @return 分享串 `spm=${shareId}.${page}.${query}.${platform}.${from}`
 */
const buildSpmQuery = (params) => {
  const user = $store('user');
  let shareId = '0'; // 设置分享者用户ID
  if (typeof params.shareId === 'undefined') {
    if (user.isLogin) {
      shareId = user.userInfo.id;
    }
  }
  let page = SharePageEnum.HOME.value; // 页面类型，默认首页
  if (typeof params.page !== 'undefined') {
    page = params.page;
  }
  let query = '0'; // 设置页面ID: 如商品ID、拼团ID等
  if (typeof params.query !== 'undefined') {
    query = params.query;
  }
  let platform = platformMap.indexOf($platform.name) + 1;
  let from = '1';
  if (typeof params.from !== 'undefined') {
    from = platformMap.indexOf(params.from) + 1;
  }
  // spmParams = ...  可按需扩展
  return `spm=${shareId}.${page}.${query}.${platform}.${from}`;
};

// 构造页面分享参数: 所有的分享都先到首页进行 spm 参数解析
const buildSpmPath = (query) => {
  // 默认是主页，页面 page，例如 pages/index/index，根路径前不要填加 /，
  // 不能携带参数（参数请放在scene字段里），如果不填写这个字段，默认跳主页面。scancode_time为系统保留参数，不允许配置
  // 页面分享时参数使用 ? 拼接
  return typeof query === 'undefined' ? `pages/index/index` : `pages/index/index?${query}`;
};

// 构造分享链接
const buildSpmLink = (query, linkAddress = '') => {
  return `${linkAddress}?${query}`;
};

// 解析Spm
const decryptSpm = (spm) => {
  const user = $store('user');
  let shareParamsArray = spm.split('.');
  let shareParams = {
    spm,
    shareId: 0,
    page: '',
    query: {},
    platform: '',
    from: '',
  };
  let query;
  shareParams.shareId = shareParamsArray[0];
  switch (shareParamsArray[1]) {
    case SharePageEnum.HOME.value:
      // 默认首页不跳转
      shareParams.page = SharePageEnum.HOME.page;
      break;
    case SharePageEnum.GOODS.value:
      // 普通商品
      shareParams.page = SharePageEnum.GOODS.page;
      shareParams.query = {
        id: shareParamsArray[2], // 设置活动编号
      };
      break;
    case SharePageEnum.GROUPON.value:
      // 拼团商品
      shareParams.page = SharePageEnum.GROUPON.page;
      shareParams.query = {
        id: shareParamsArray[2], // 设置活动编号
      };
      break;
    case SharePageEnum.SECKILL.value:
      // 秒杀商品
      shareParams.page = SharePageEnum.SECKILL.page;
      shareParams.query = {
        id: shareParamsArray[2], // 设置活动编号
      };
      break;
    case SharePageEnum.GROUPON_DETAIL.value:
      // 参与拼团
      shareParams.page = SharePageEnum.GROUPON_DETAIL.page;
      shareParams.query = {
        id: shareParamsArray[2], // 设置活动编号
      };
      break;
    case SharePageEnum.POINT.value:
      // 积分商品
      shareParams.page = SharePageEnum.POINT.page;
      shareParams.query = {
        id: shareParamsArray[2], // 设置活动编号
      };
      break;
  }
  shareParams.platform = platformMap[shareParamsArray[3] - 1];
  shareParams.from = fromMap[shareParamsArray[4] - 1];
  if (shareParams.shareId !== 0) {
    // 记录分享者编号
    uni.setStorageSync('shareId', shareParams.shareId);
    // 已登录 绑定推广员
    if (!!user.isLogin) {
      bindBrokerageUser(shareParams.shareId);
    }
  }

  if (shareParams.page !== SharePageEnum.HOME.page) {
    $router.go(shareParams.page, shareParams.query);
  }
  return shareParams;
};

// 绑定推广员
const bindBrokerageUser = async (val = undefined) => {
  try {
    const shareId = val || uni.getStorageSync('shareId');
    if (!shareId) {
      return;
    }
    // 绑定成功返回 true，失败返回 false
    const { data } = await BrokerageApi.bindBrokerageUser({ bindUserId: shareId });
    // 绑定成功后清除缓存
    if (data) {
      uni.removeStorageSync('shareId');
    }
  } catch (e) {
    console.error(e);
  }
};

// 更新公众号分享sdk
const updateShareInfo = (shareInfo) => {
  // #ifdef H5
  if ($platform.name === 'WechatOfficialAccount') {
    $wxsdk.updateShareInfo(shareInfo);
  }
  // #endif
};

export default {
  getShareInfo,
  updateShareInfo,
  decryptSpm,
  bindBrokerageUser,
};
