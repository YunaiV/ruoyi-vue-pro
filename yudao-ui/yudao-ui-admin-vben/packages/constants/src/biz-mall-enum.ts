// ========== MALL - 商品模块 ==========
/**
 * 商品 首页 日期类型
 */
export enum TimeRangeTypeEnum {
  DAY30 = 1,
  MONTH = 30,
  WEEK = 7,
  YEAR = 365,
}

/**
 * 商品 SPU 状态
 */
export const ProductSpuStatusEnum = {
  RECYCLE: {
    status: -1,
    name: '回收站',
  },
  DISABLE: {
    status: 0,
    name: '下架',
  },
  ENABLE: {
    status: 1,
    name: '上架',
  },
};

// ========== MALL - 营销模块 ==========
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

/**
 * 优惠劵模板的领取方式的枚举
 */
export const CouponTemplateTakeTypeEnum = {
  USER: {
    type: 1,
    name: '直接领取',
  },
  ADMIN: {
    type: 2,
    name: '指定发放',
  },
  REGISTER: {
    type: 3,
    name: '新人券',
  },
};

/**
 * 营销的商品范围枚举
 */
export const PromotionProductScopeEnum = {
  ALL: {
    scope: 1,
    name: '通用劵',
  },
  SPU: {
    scope: 2,
    name: '商品劵',
  },
  CATEGORY: {
    scope: 3,
    name: '品类劵',
  },
};

/**
 * 营销的条件类型枚举
 */
export const PromotionConditionTypeEnum = {
  PRICE: {
    type: 10,
    name: '满 N 元',
  },
  COUNT: {
    type: 20,
    name: '满 N 件',
  },
};

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

// ========== MALL - 交易模块 ==========
/**
 * 分销关系绑定模式枚举
 */
export const BrokerageBindModeEnum = {
  ANYTIME: {
    mode: 1,
    name: '首次绑定',
  },
  REGISTER: {
    mode: 2,
    name: '注册绑定',
  },
  OVERRIDE: {
    mode: 3,
    name: '覆盖绑定',
  },
};
/**
 * 分佣模式枚举
 */
export const BrokerageEnabledConditionEnum = {
  ALL: {
    condition: 1,
    name: '人人分销',
  },
  ADMIN: {
    condition: 2,
    name: '指定分销',
  },
};
/**
 * 佣金记录业务类型枚举
 */
export const BrokerageRecordBizTypeEnum = {
  ORDER: {
    type: 1,
    name: '获得推广佣金',
  },
  WITHDRAW: {
    type: 2,
    name: '提现申请',
  },
};
/**
 * 佣金提现状态枚举
 */
export const BrokerageWithdrawStatusEnum = {
  AUDITING: {
    status: 0,
    name: '审核中',
  },
  AUDIT_SUCCESS: {
    status: 10,
    name: '审核通过',
  },
  AUDIT_FAIL: {
    status: 20,
    name: '审核不通过',
  },
  WITHDRAW_SUCCESS: {
    status: 11,
    name: '提现成功',
  },
  WITHDRAW_FAIL: {
    status: 21,
    name: '提现失败',
  },
};
/**
 * 佣金提现类型枚举
 */
export const BrokerageWithdrawTypeEnum = {
  WALLET: {
    type: 1,
    name: '钱包',
  },
  BANK: {
    type: 2,
    name: '银行卡',
  },
  WECHAT: {
    type: 3,
    name: '微信',
  },
  ALIPAY: {
    type: 4,
    name: '支付宝',
  },
};

/**
 * 配送方式枚举
 */
export const DeliveryTypeEnum = {
  EXPRESS: {
    type: 1,
    name: '快递发货',
  },
  PICK_UP: {
    type: 2,
    name: '到店自提',
  },
};
/**
 * 交易订单 - 状态
 */
export const TradeOrderStatusEnum = {
  UNPAID: {
    status: 0,
    name: '待支付',
  },
  UNDELIVERED: {
    status: 10,
    name: '待发货',
  },
  DELIVERED: {
    status: 20,
    name: '已发货',
  },
  COMPLETED: {
    status: 30,
    name: '已完成',
  },
  CANCELED: {
    status: 40,
    name: '已取消',
  },
};

// 预设颜色
export const PREDEFINE_COLORS = [
  '#ff4500',
  '#ff8c00',
  '#ffd700',
  '#90ee90',
  '#00ced1',
  '#1e90ff',
  '#c71585',
  '#409EFF',
  '#909399',
  '#C0C4CC',
  '#b7390b',
  '#ff7800',
  '#fad400',
  '#5b8c5f',
  '#00babd',
  '#1f73c3',
  '#711f57',
];
