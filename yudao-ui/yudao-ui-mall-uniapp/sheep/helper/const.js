// ========== COMMON - 公共模块 ==========

/**
 * 与后端Terminal枚举一一对应
 */
export const TerminalEnum = {
  UNKNOWN: 0, // 未知, 目的：在无法解析到 terminal 时，使用它
  WECHAT_MINI_PROGRAM: 10, //微信小程序
  WECHAT_WAP: 11, // 微信公众号
  H5: 20, // H5 网页
  APP: 31, // 手机 App
};

/**
 * 将 uni-app 提供的平台转换为后端所需的 terminal值
 *
 * @return 终端
 */
export const getTerminal = () => {
  const platformType = uni.getAppBaseInfo().uniPlatform;
  // 与后端terminal枚举一一对应
  switch (platformType) {
    case 'app':
      return TerminalEnum.APP;
    case 'web':
      return TerminalEnum.H5;
    case 'mp-weixin':
      return TerminalEnum.WECHAT_MINI_PROGRAM;
    default:
      return TerminalEnum.UNKNOWN;
  }
};

// ========== MALL - 营销模块 ==========

import dayjs from 'dayjs';

/**
 * 优惠类型枚举
 */
export const PromotionDiscountTypeEnum = {
  PRICE: {
    type: 1,
    name: '满减',
  },
  PERCENT: {
    type: 2,
    name: '折扣',
  },
};

/**
 * 优惠劵模板的有限期类型的枚举
 */
export const CouponTemplateValidityTypeEnum = {
  DATE: {
    type: 1,
    name: '固定日期可用',
  },
  TERM: {
    type: 2,
    name: '领取之后可用',
  },
};

// 时间段的状态枚举
export const TimeStatusEnum = {
  WAIT_START: '即将开始',
  STARTED: '进行中',
  END: '已结束',
};

/**
 * 微信小程序的订阅模版
 */
export const WxaSubscribeTemplate = {
  TRADE_ORDER_DELIVERY: '订单发货通知',
  PROMOTION_COMBINATION_SUCCESS: '拼团结果通知',
  PAY_WALLET_RECHARGER_SUCCESS: '充值成功通知',
};
export const PromotionActivityTypeEnum = {
  NORMAL: {
    type: 0,
    name: '普通',
  },
  SECKILL: {
    type: 1,
    name: '秒杀',
  },
  BARGAIN: {
    type: 2,
    name: '砍价',
  },
  COMBINATION: {
    type: 3,
    name: '拼团',
  },
  POINT: {
    type: 4,
    name: '积分商城',
  },
};
/** 配送方式枚举 */
export const DeliveryTypeEnum = {
  EXPRESS: { type: 1, name: '快递发货' },
  PICK_UP: { type: 2, name: '用户自提' },
};
export const getTimeStatusEnum = (startTime, endTime) => {
  const now = dayjs();
  if (now.isBefore(startTime)) {
    return TimeStatusEnum.WAIT_START;
  } else if (now.isAfter(endTime)) {
    return TimeStatusEnum.END;
  } else {
    return TimeStatusEnum.STARTED;
  }
};
/**
 * 分享页枚举
 * 按需扩展
 * */
export const SharePageEnum = {
  HOME: {
    name: '首页',
    page: '/pages/index/index',
    value: '1',
  },
  GOODS: {
    name: '普通商品页',
    page: '/pages/goods/index',
    value: '2',
  },
  GROUPON: {
    name: '拼团商品页',
    page: '/pages/goods/groupon',
    value: '3',
  },
  SECKILL: {
    name: '秒杀商品页',
    page: '/pages/goods/seckill',
    value: '4',
  },
  GROUPON_DETAIL: {
    name: '参与拼团页',
    page: '/pages/activity/groupon/detail',
    value: '5',
  },
  POINT: {
    name: '积分商品页',
    page: '/pages/goods/point',
    value: '6',
  },
};
