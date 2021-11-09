/**
 * Created by 芋道源码
 *
 * 枚举类
 */

/**
 * 全局通用状态枚举
 */
export const SysCommonStatusEnum = {
  ENABLE: 0, // 开启
  DISABLE: 1 // 禁用
}

/**
 * 菜单的类型枚举
 */
export const SysMenuTypeEnum = {
  DIR: 1, // 目录
  MENU: 2, // 菜单
  BUTTON: 3 // 按钮
}

/**
 * 角色的类型枚举
 */
export const SysRoleTypeEnum = {
  SYSTEM: 1, // 内置角色
  CUSTOM: 2 // 自定义角色
}

/**
 * 数据权限的范围枚举
 */
export const SysDataScopeEnum = {
  ALL: 1, // 全部数据权限
  DEPT_CUSTOM: 2, // 指定部门数据权限
  DEPT_ONLY: 3, // 部门数据权限
  DEPT_AND_CHILD: 4, // 部门及以下数据权限
  DEPT_SELF: 5 // 仅本人数据权限
}

/**
 * 代码生成模板类型
 */
export const ToolCodegenTemplateTypeEnum = {
  CRUD: 1, // 基础 CRUD
  TREE: 2, // 树形 CRUD
  SUB: 3, // 主子表 CRUD
}

/**
 * 任务状态的枚举
 */
export const InfJobStatusEnum = {
  INIT: 0, // 初始化中
  NORMAL: 1, // 运行中
  STOP: 2, // 暂停运行
}

/**
 * API 异常数据的处理状态
 */
export const InfApiErrorLogProcessStatusEnum = {
  INIT: 0, // 未处理
  DONE: 1, // 已处理
  IGNORE: 2, // 已忽略
}

/**
 * 用户的社交平台的类型枚举
 */
export const SysUserSocialTypeEnum = {
  // GITEE: {
  //   title: "码云",
  //   type: 10,
  //   source: "gitee",
  //   img: "https://cdn.jsdelivr.net/gh/justauth/justauth-oauth-logo@1.11/gitee.png",
  // },
  DINGTALK: {
    title: "钉钉",
    type: 20,
    source: "dingtalk",
    img: "https://cdn.jsdelivr.net/gh/justauth/justauth-oauth-logo@1.11/dingtalk.png",
  },
  WECHAT_ENTERPRISE: {
    title: "企业微信",
    type: 30,
    source: "wechat_enterprise",
    img: "https://cdn.jsdelivr.net/gh/justauth/justauth-oauth-logo@1.11/wechat_enterprise.png",
  }
}

export const PayChannelEnum = {
  WX_PUB: {
    "code": "wx_pub",
    "name": "微信 JSAPI 支付",
  },
  WX_LITE: {
    "code": "wx_lite",
    "name": "微信小程序支付"
  },
  WX_APP: {
    "code": "wx_app",
    "name": "微信 APP 支付"
  },
  ALIPAY_PC: {
    "code": "alipay_pc",
    "name": "支付宝 PC 网站支付"
  },
  ALIPAY_WAP: {
    "code": "alipay_wap",
    "name": "支付宝 WAP 网站支付"
  },
  ALIPAY_APP: {
    "code": "alipay_app",
    "name": "支付宝 APP 支付"
  },
  ALIPAY_QR: {
    "code": "alipay_qr",
    "name": "支付宝扫码支付"
  },
}
