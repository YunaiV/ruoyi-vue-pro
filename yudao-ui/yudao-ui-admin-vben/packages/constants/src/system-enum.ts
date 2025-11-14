/**
 * Created by 芋道源码
 *
 * 枚举类
 */

/**
 * AI 平台的枚举
 */
export const AiPlatformEnum = {
  TONG_YI: 'TongYi', // 阿里
  YI_YAN: 'YiYan', // 百度
  DEEP_SEEK: 'DeepSeek', // DeepSeek
  ZHI_PU: 'ZhiPu', // 智谱 AI
  XING_HUO: 'XingHuo', // 讯飞
  SiliconFlow: 'SiliconFlow', // 硅基流动
  OPENAI: 'OpenAI',
  Ollama: 'Ollama',
  STABLE_DIFFUSION: 'StableDiffusion', // Stability AI
  MIDJOURNEY: 'Midjourney', // Midjourney
  SUNO: 'Suno', // Suno AI
};

export const AiModelTypeEnum = {
  CHAT: 1, // 聊天
  IMAGE: 2, // 图像
  VOICE: 3, // 音频
  VIDEO: 4, // 视频
  EMBEDDING: 5, // 向量
  RERANK: 6, // 重排
};
export interface ImageModel {
  image?: string;
  key: string;
  name: string;
}
export const OtherPlatformEnum: ImageModel[] = [
  {
    key: AiPlatformEnum.TONG_YI,
    name: '通义万相',
  },
  {
    key: AiPlatformEnum.YI_YAN,
    name: '百度千帆',
  },
  {
    key: AiPlatformEnum.ZHI_PU,
    name: '智谱 AI',
  },
  {
    key: AiPlatformEnum.SiliconFlow,
    name: '硅基流动',
  },
];
/**
 * AI 图像生成状态的枚举
 */
export const AiImageStatusEnum = {
  IN_PROGRESS: 10, // 进行中
  SUCCESS: 20, // 已完成
  FAIL: 30, // 已失败
};
/**
 * AI 音乐生成状态的枚举
 */
export const AiMusicStatusEnum = {
  IN_PROGRESS: 10, // 进行中
  SUCCESS: 20, // 已完成
  FAIL: 30, // 已失败
};

/**
 * AI 写作类型的枚举
 */
export enum AiWriteTypeEnum {
  WRITING = 1, // 撰写
  REPLY, // 回复
}

// ========== 【图片 UI】相关的枚举 ==========

export const ImageHotWords = [
  '中国旗袍',
  '古装美女',
  '卡通头像',
  '机甲战士',
  '童话小屋',
  '中国长城',
]; // 图片热词

export const ImageHotEnglishWords = [
  'Chinese Cheongsam',
  'Ancient Beauty',
  'Cartoon Avatar',
  'Mech Warrior',
  'Fairy Tale Cottage',
  'The Great Wall of China',
]; // 图片热词（英文）

export const StableDiffusionSamplers: ImageModel[] = [
  {
    key: 'DDIM',
    name: 'DDIM',
  },
  {
    key: 'DDPM',
    name: 'DDPM',
  },
  {
    key: 'K_DPMPP_2M',
    name: 'K_DPMPP_2M',
  },
  {
    key: 'K_DPMPP_2S_ANCESTRAL',
    name: 'K_DPMPP_2S_ANCESTRAL',
  },
  {
    key: 'K_DPM_2',
    name: 'K_DPM_2',
  },
  {
    key: 'K_DPM_2_ANCESTRAL',
    name: 'K_DPM_2_ANCESTRAL',
  },
  {
    key: 'K_EULER',
    name: 'K_EULER',
  },
  {
    key: 'K_EULER_ANCESTRAL',
    name: 'K_EULER_ANCESTRAL',
  },
  {
    key: 'K_HEUN',
    name: 'K_HEUN',
  },
  {
    key: 'K_LMS',
    name: 'K_LMS',
  },
];

export const StableDiffusionStylePresets: ImageModel[] = [
  {
    key: '3d-model',
    name: '3d-model',
  },
  {
    key: 'analog-film',
    name: 'analog-film',
  },
  {
    key: 'anime',
    name: 'anime',
  },
  {
    key: 'cinematic',
    name: 'cinematic',
  },
  {
    key: 'comic-book',
    name: 'comic-book',
  },
  {
    key: 'digital-art',
    name: 'digital-art',
  },
  {
    key: 'enhance',
    name: 'enhance',
  },
  {
    key: 'fantasy-art',
    name: 'fantasy-art',
  },
  {
    key: 'isometric',
    name: 'isometric',
  },
  {
    key: 'line-art',
    name: 'line-art',
  },
  {
    key: 'low-poly',
    name: 'low-poly',
  },
  {
    key: 'modeling-compound',
    name: 'modeling-compound',
  },
  // neon-punk origami photographic pixel-art tile-texture
  {
    key: 'neon-punk',
    name: 'neon-punk',
  },
  {
    key: 'origami',
    name: 'origami',
  },
  {
    key: 'photographic',
    name: 'photographic',
  },
  {
    key: 'pixel-art',
    name: 'pixel-art',
  },
  {
    key: 'tile-texture',
    name: 'tile-texture',
  },
];

export const StableDiffusionClipGuidancePresets: ImageModel[] = [
  {
    key: 'NONE',
    name: 'NONE',
  },
  {
    key: 'FAST_BLUE',
    name: 'FAST_BLUE',
  },
  {
    key: 'FAST_GREEN',
    name: 'FAST_GREEN',
  },
  {
    key: 'SIMPLE',
    name: 'SIMPLE',
  },
  {
    key: 'SLOW',
    name: 'SLOW',
  },
  {
    key: 'SLOWER',
    name: 'SLOWER',
  },
  {
    key: 'SLOWEST',
    name: 'SLOWEST',
  },
];
// ========== COMMON 模块 ==========
// 全局通用状态枚举
export const CommonStatusEnum = {
  ENABLE: 0, // 开启
  DISABLE: 1, // 禁用
};

// 全局用户类型枚举
export const UserTypeEnum = {
  MEMBER: 1, // 会员
  ADMIN: 2, // 管理员
};

// ========== SYSTEM 模块 ==========
/**
 * 菜单的类型枚举
 */
export const SystemMenuTypeEnum = {
  DIR: 1, // 目录
  MENU: 2, // 菜单
  BUTTON: 3, // 按钮
};

/**
 * 角色的类型枚举
 */
export const SystemRoleTypeEnum = {
  SYSTEM: 1, // 内置角色
  CUSTOM: 2, // 自定义角色
};

/**
 * 数据权限的范围枚举
 */
export const SystemDataScopeEnum = {
  ALL: 1, // 全部数据权限
  DEPT_CUSTOM: 2, // 指定部门数据权限
  DEPT_ONLY: 3, // 部门数据权限
  DEPT_AND_CHILD: 4, // 部门及以下数据权限
  DEPT_SELF: 5, // 仅本人数据权限
};

/**
 * 用户的社交平台的类型枚举
 */
export const SystemUserSocialTypeEnum = {
  DINGTALK: {
    title: '钉钉',
    type: 20,
    source: 'dingtalk',
    img: 'https://s1.ax1x.com/2022/05/22/OzMDRs.png',
  },
  WECHAT_ENTERPRISE: {
    title: '企业微信',
    type: 30,
    source: 'wechat_enterprise',
    img: 'https://s1.ax1x.com/2022/05/22/OzMrzn.png',
  },
};

// ========== INFRA 模块 ==========
/**
 * 代码生成模板类型
 */
export const InfraCodegenTemplateTypeEnum = {
  CRUD: 1, // 基础 CRUD
  TREE: 2, // 树形 CRUD
  SUB: 15, // 主子表 CRUD
};

/**
 * 任务状态的枚举
 */
export const InfraJobStatusEnum = {
  INIT: 0, // 初始化中
  NORMAL: 1, // 运行中
  STOP: 2, // 暂停运行
};

/**
 * API 异常数据的处理状态
 */
export const InfraApiErrorLogProcessStatusEnum = {
  INIT: 0, // 未处理
  DONE: 1, // 已处理
  IGNORE: 2, // 已忽略
};
export interface ImageSize {
  height: string;
  key: string;
  name?: string;
  style: string;
  width: string;
}
export const Dall3SizeList: ImageSize[] = [
  {
    key: '1024x1024',
    name: '1:1',
    width: '1024',
    height: '1024',
    style: 'width: 30px; height: 30px;background-color: #dcdcdc;',
  },
  {
    key: '1024x1792',
    name: '3:5',
    width: '1024',
    height: '1792',
    style: 'width: 30px; height: 50px;background-color: #dcdcdc;',
  },
  {
    key: '1792x1024',
    name: '5:3',
    width: '1792',
    height: '1024',
    style: 'width: 50px; height: 30px;background-color: #dcdcdc;',
  },
];

export const Dall3Models: ImageModel[] = [
  {
    key: 'dall-e-3',
    name: 'DALL·E 3',
    image: `/static/imgs/ai/dall2.jpg`,
  },
  {
    key: 'dall-e-2',
    name: 'DALL·E 2',
    image: `/static/imgs/ai/dall3.jpg`,
  },
];

export const Dall3StyleList: ImageModel[] = [
  {
    key: 'vivid',
    name: '清晰',
    image: `/static/imgs/ai/qingxi.jpg`,
  },
  {
    key: 'natural',
    name: '自然',
    image: `/static/imgs/ai/ziran.jpg`,
  },
];
export const MidjourneyModels: ImageModel[] = [
  {
    key: 'midjourney',
    name: 'MJ',
    image: 'https://bigpt8.com/pc/_nuxt/mj.34a61377.png',
  },
  {
    key: 'niji',
    name: 'NIJI',
    image: 'https://bigpt8.com/pc/_nuxt/nj.ca79b143.png',
  },
];
export const MidjourneyVersions = [
  {
    value: '6.0',
    label: 'v6.0',
  },
  {
    value: '5.2',
    label: 'v5.2',
  },
  {
    value: '5.1',
    label: 'v5.1',
  },
  {
    value: '5.0',
    label: 'v5.0',
  },
  {
    value: '4.0',
    label: 'v4.0',
  },
];

export const NijiVersionList = [
  {
    value: '5',
    label: 'v5',
  },
];

export const MidjourneySizeList: ImageSize[] = [
  {
    key: '1:1',
    width: '1',
    height: '1',
    style: 'width: 30px; height: 30px;background-color: #dcdcdc;',
  },
  {
    key: '3:4',
    width: '3',
    height: '4',
    style: 'width: 30px; height: 40px;background-color: #dcdcdc;',
  },
  {
    key: '4:3',
    width: '4',
    height: '3',
    style: 'width: 40px; height: 30px;background-color: #dcdcdc;',
  },
  {
    key: '9:16',
    width: '9',
    height: '16',
    style: 'width: 30px; height: 50px;background-color: #dcdcdc;',
  },
  {
    key: '16:9',
    width: '16',
    height: '9',
    style: 'width: 50px; height: 30px;background-color: #dcdcdc;',
  },
];
// ========== PAY 模块 ==========
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

// ========== ERP - 企业资源计划 ==========

export const ErpBizType = {
  PURCHASE_ORDER: 10,
  PURCHASE_IN: 11,
  PURCHASE_RETURN: 12,
  SALE_ORDER: 20,
  SALE_OUT: 21,
  SALE_RETURN: 22,
};

// ========== BPM 模块 ==========

// 候选人策略枚举 （ 用于审批节点。抄送节点 )
export enum BpmCandidateStrategyEnum {
  /**
   * 审批人自选
   */
  APPROVE_USER_SELECT = 34,
  /**
   * 部门的负责人
   */
  DEPT_LEADER = 21,
  /**
   * 部门成员
   */
  DEPT_MEMBER = 20,
  /**
   * 流程表达式
   */
  EXPRESSION = 60,
  /**
   * 表单内部门负责人
   */
  FORM_DEPT_LEADER = 51,
  /**
   * 表单内用户字段
   */
  FORM_USER = 50,
  /**
   * 连续多级部门的负责人
   */
  MULTI_LEVEL_DEPT_LEADER = 23,
  /**
   * 指定岗位
   */
  POST = 22,
  /**
   * 指定角色
   */
  ROLE = 10,
  /**
   * 发起人自己
   */
  START_USER = 36,
  /**
   * 发起人部门负责人
   */
  START_USER_DEPT_LEADER = 37,
  /**
   * 发起人连续多级部门的负责人
   */
  START_USER_MULTI_LEVEL_DEPT_LEADER = 38,
  /**
   * 发起人自选
   */
  START_USER_SELECT = 35,
  /**
   * 指定用户
   */
  USER = 30,
  /**
   * 指定用户组
   */
  USER_GROUP = 40,
}

/**
 * 节点类型
 */
export enum BpmNodeTypeEnum {
  /**
   * 子流程节点
   */
  CHILD_PROCESS_NODE = 20,
  /**
   * 条件分支节点 (对应排他网关)
   */
  CONDITION_BRANCH_NODE = 51,
  /**
   * 条件节点
   */
  CONDITION_NODE = 50,

  /**
   * 抄送人节点
   */
  COPY_TASK_NODE = 12,

  /**
   * 延迟器节点
   */
  DELAY_TIMER_NODE = 14,

  /**
   * 结束节点
   */
  END_EVENT_NODE = 1,

  /**
   * 包容分支节点 (对应包容网关)
   */
  INCLUSIVE_BRANCH_NODE = 53,

  /**
   * 并行分支节点 (对应并行网关)
   */
  PARALLEL_BRANCH_NODE = 52,

  /**
   * 路由分支节点
   */
  ROUTER_BRANCH_NODE = 54,
  /**
   * 发起人节点
   */
  START_USER_NODE = 10,
  /**
   * 办理人节点
   */
  TRANSACTOR_NODE = 13,

  /**
   * 触发器节点
   */
  TRIGGER_NODE = 15,
  /**
   * 审批人节点
   */
  USER_TASK_NODE = 11,
}

/**
 *  流程任务操作按钮
 */
export enum BpmTaskOperationButtonTypeEnum {
  /**
   * 加签
   */
  ADD_SIGN = 5,
  /**
   * 通过
   */
  APPROVE = 1,
  /**
   * 抄送
   */
  COPY = 7,
  /**
   * 委派
   */
  DELEGATE = 4,
  /**
   * 拒绝
   */
  REJECT = 2,
  /**
   * 退回
   */
  RETURN = 6,
  /**
   * 转办
   */
  TRANSFER = 3,
}

/**
 * 任务状态枚举
 */
export enum BpmTaskStatusEnum {
  /**
   * 审批通过
   */
  APPROVE = 2,
  /**
   * 审批通过中
   */
  APPROVING = 7,

  /**
   * 已取消
   */
  CANCEL = 4,
  /**
   * 未开始
   */
  NOT_START = -1,
  /**
   * 审批不通过
   */
  REJECT = 3,

  /**
   * 已退回
   */
  RETURN = 5,

  /**
   * 审批中
   */
  RUNNING = 1,
  /**
   * 跳过
   */
  SKIP = -2,
  /**
   * 待审批
   */
  WAIT = 0,
}

/**
 * 节点 Id 枚举
 */
export enum BpmNodeIdEnum {
  /**
   * 发起人节点 Id
   */
  END_EVENT_NODE_ID = 'EndEvent',

  /**
   * 发起人节点 Id
   */
  START_USER_NODE_ID = 'StartUserNode',
}

/**
 * 表单权限的枚举
 */
export enum BpmFieldPermissionType {
  /**
   * 隐藏
   */
  NONE = '3',
  /**
   * 只读
   */
  READ = '1',
  /**
   * 编辑
   */
  WRITE = '2',
}

/**
 * 流程模型类型
 */
export const BpmModelType = {
  BPMN: 10, // BPMN 设计器
  SIMPLE: 20, // 简易设计器
};

/**
 * 流程模型表单类型
 */
export const BpmModelFormType = {
  NORMAL: 10, // 流程表单
  CUSTOM: 20, // 业务表单
};

/**
 * 流程实例状态
 */
export const BpmProcessInstanceStatus = {
  NOT_START: -1, // 未开始
  RUNNING: 1, // 审批中
  APPROVE: 2, // 审批通过
  REJECT: 3, // 审批不通过
  CANCEL: 4, // 已取消
};

/**
 * 自动审批类型
 */
export const BpmAutoApproveType = {
  NONE: 0, // 不自动通过
  APPROVE_ALL: 1, // 仅审批一次，后续重复的审批节点均自动通过
  APPROVE_SEQUENT: 2, // 仅针对连续审批的节点自动通过
};

/**
 * 审批操作按钮名称
 */
export const OPERATION_BUTTON_NAME = new Map<number, string>();
OPERATION_BUTTON_NAME.set(BpmTaskOperationButtonTypeEnum.APPROVE, '通过');
OPERATION_BUTTON_NAME.set(BpmTaskOperationButtonTypeEnum.REJECT, '拒绝');
OPERATION_BUTTON_NAME.set(BpmTaskOperationButtonTypeEnum.TRANSFER, '转办');
OPERATION_BUTTON_NAME.set(BpmTaskOperationButtonTypeEnum.DELEGATE, '委派');
OPERATION_BUTTON_NAME.set(BpmTaskOperationButtonTypeEnum.ADD_SIGN, '加签');
OPERATION_BUTTON_NAME.set(BpmTaskOperationButtonTypeEnum.RETURN, '退回');
OPERATION_BUTTON_NAME.set(BpmTaskOperationButtonTypeEnum.COPY, '抄送');

/**
 * 流程实例的变量枚举
 */
export enum ProcessVariableEnum {
  /**
   * 流程定义名称
   */
  PROCESS_DEFINITION_NAME = 'PROCESS_DEFINITION_NAME',
  /**
   * 发起时间
   */
  START_TIME = 'PROCESS_START_TIME',
  /**
   * 发起用户 ID
   */
  START_USER_ID = 'PROCESS_START_USER_ID',
}
// ========== 【写作 UI】相关的枚举 ==========

/** 写作点击示例时的数据 */
export const WriteExample = {
  write: {
    prompt: 'vue',
    data: 'Vue.js 是一种用于构建用户界面的渐进式 JavaScript 框架。它的核心库只关注视图层，易于上手，同时也便于与其他库或已有项目整合。\n\nVue.js 的特点包括：\n- 响应式的数据绑定：Vue.js 会自动将数据与 DOM 同步，使得状态管理变得更加简单。\n- 组件化：Vue.js 允许开发者通过小型、独立和通常可复用的组件构建大型应用。\n- 虚拟 DOM：Vue.js 使用虚拟 DOM 实现快速渲染，提高了性能。\n\n在 Vue.js 中，一个典型的应用结构可能包括：\n1. 根实例：每个 Vue 应用都需要一个根实例作为入口点。\n2. 组件系统：可以创建自定义的可复用组件。\n3. 指令：特殊的带有前缀 v- 的属性，为 DOM 元素提供特殊的行为。\n4. 插值：用于文本内容，将数据动态地插入到 HTML。\n5. 计算属性和侦听器：用于处理数据的复杂逻辑和响应数据变化。\n6. 条件渲染：根据条件决定元素的渲染。\n7. 列表渲染：用于显示列表数据。\n8. 事件处理：响应用户交互。\n9. 表单输入绑定：处理表单输入和验证。\n10. 组件生命周期钩子：在组件的不同阶段执行特定的函数。\n\nVue.js 还提供了官方的路由器 Vue Router 和状态管理库 Vuex，以支持构建复杂的单页应用（SPA）。\n\n在开发过程中，开发者通常会使用 Vue CLI，这是一个强大的命令行工具，用于快速生成 Vue 项目脚手架，集成了诸如 Babel、Webpack 等现代前端工具，以及热重载、代码检测等开发体验优化功能。\n\nVue.js 的生态系统还包括大量的第三方库和插件，如 Vuetify（UI 组件库）、Vue Test Utils（测试工具）等，这些都极大地丰富了 Vue.js 的开发生态。\n\n总的来说，Vue.js 是一个灵活、高效的前端框架，适合从小型项目到大型企业级应用的开发。它的易用性、灵活性和强大的社区支持使其成为许多开发者的首选框架之一。',
  },
  reply: {
    originalContent: '领导，我想请假',
    prompt: '不批',
    data: '您的请假申请已收悉，经核实和考虑，暂时无法批准您的请假申请。\n\n如有特殊情况或紧急事务，请及时与我联系。\n\n祝工作顺利。\n\n谢谢。',
  },
};

// ========== 【思维导图 UI】相关的枚举 ==========

/** 思维导图已有内容生成示例 */
export const MindMapContentExample = `# Java 技术栈

## 核心技术
### Java SE
### Java EE

## 框架
### Spring
#### Spring Boot
#### Spring MVC
#### Spring Data
### Hibernate
### MyBatis

## 构建工具
### Maven
### Gradle

## 版本控制
### Git
### SVN

## 测试工具
### JUnit
### Mockito
### Selenium

## 应用服务器
### Tomcat
### Jetty
### WildFly

## 数据库
### MySQL
### PostgreSQL
### Oracle
### MongoDB

## 消息队列
### Kafka
### RabbitMQ
### ActiveMQ

## 微服务
### Spring Cloud
### Dubbo

## 容器化
### Docker
### Kubernetes

## 云服务
### AWS
### Azure
### Google Cloud

## 开发工具
### IntelliJ IDEA
### Eclipse
### Visual Studio Code`;

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
