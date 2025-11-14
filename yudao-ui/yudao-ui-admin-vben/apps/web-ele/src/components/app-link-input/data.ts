// APP 链接分组
export interface AppLinkGroup {
  // 分组名称
  name: string;
  // 链接列表
  links: AppLink[];
}

// APP 链接
export interface AppLink {
  // 链接名称
  name: string;
  // 链接地址
  path: string;
  // 链接的类型
  type?: APP_LINK_TYPE_ENUM;
}

// APP 链接类型（需要特殊处理，例如商品详情）
export enum APP_LINK_TYPE_ENUM {
  // 拼团活动
  ACTIVITY_COMBINATION,
  // 积分商城活动
  ACTIVITY_POINT,
  // 秒杀活动
  ACTIVITY_SECKILL,
  // 文章详情
  ARTICLE_DETAIL,
  // 优惠券详情
  COUPON_DETAIL,
  // 自定义页面详情
  DIY_PAGE_DETAIL,
  // 品类列表
  PRODUCT_CATEGORY_LIST,
  // 拼团商品详情
  PRODUCT_DETAIL_COMBINATION,
  // 商品详情
  PRODUCT_DETAIL_NORMAL,
  // 秒杀商品详情
  PRODUCT_DETAIL_SECKILL,
  // 商品列表
  PRODUCT_LIST,
}

// APP 链接列表（做一下持久化？）
export const APP_LINK_GROUP_LIST = [
  {
    name: '商城',
    links: [
      {
        name: '首页',
        path: '/pages/index/index',
      },
      {
        name: '商品分类',
        path: '/pages/index/category',
        type: APP_LINK_TYPE_ENUM.PRODUCT_CATEGORY_LIST,
      },
      {
        name: '购物车',
        path: '/pages/index/cart',
      },
      {
        name: '个人中心',
        path: '/pages/index/user',
      },
      {
        name: '商品搜索',
        path: '/pages/index/search',
      },
      {
        name: '自定义页面',
        path: '/pages/index/page',
        type: APP_LINK_TYPE_ENUM.DIY_PAGE_DETAIL,
      },
      {
        name: '客服',
        path: '/pages/chat/index',
      },
      {
        name: '系统设置',
        path: '/pages/public/setting',
      },
      {
        name: '常见问题',
        path: '/pages/public/faq',
      },
    ],
  },
  {
    name: '商品',
    links: [
      {
        name: '商品列表',
        path: '/pages/goods/list',
        type: APP_LINK_TYPE_ENUM.PRODUCT_LIST,
      },
      {
        name: '商品详情',
        path: '/pages/goods/index',
        type: APP_LINK_TYPE_ENUM.PRODUCT_DETAIL_NORMAL,
      },
      {
        name: '拼团商品详情',
        path: '/pages/goods/groupon',
        type: APP_LINK_TYPE_ENUM.PRODUCT_DETAIL_COMBINATION,
      },
      {
        name: '秒杀商品详情',
        path: '/pages/goods/seckill',
        type: APP_LINK_TYPE_ENUM.PRODUCT_DETAIL_SECKILL,
      },
    ],
  },
  {
    name: '营销活动',
    links: [
      {
        name: '拼团订单',
        path: '/pages/activity/groupon/order',
      },
      {
        name: '营销商品',
        path: '/pages/activity/index',
      },
      {
        name: '拼团活动',
        path: '/pages/activity/groupon/list',
        type: APP_LINK_TYPE_ENUM.ACTIVITY_COMBINATION,
      },
      {
        name: '秒杀活动',
        path: '/pages/activity/seckill/list',
        type: APP_LINK_TYPE_ENUM.ACTIVITY_SECKILL,
      },
      {
        name: '积分商城活动',
        path: '/pages/activity/point/list',
        type: APP_LINK_TYPE_ENUM.ACTIVITY_POINT,
      },
      {
        name: '签到中心',
        path: '/pages/app/sign',
      },
      {
        name: '优惠券中心',
        path: '/pages/coupon/list',
      },
      {
        name: '优惠券详情',
        path: '/pages/coupon/detail',
        type: APP_LINK_TYPE_ENUM.COUPON_DETAIL,
      },
      {
        name: '文章详情',
        path: '/pages/public/richtext',
        type: APP_LINK_TYPE_ENUM.ARTICLE_DETAIL,
      },
    ],
  },
  {
    name: '分销商城',
    links: [
      {
        name: '分销中心',
        path: '/pages/commission/index',
      },
      {
        name: '推广商品',
        path: '/pages/commission/goods',
      },
      {
        name: '分销订单',
        path: '/pages/commission/order',
      },
      {
        name: '我的团队',
        path: '/pages/commission/team',
      },
    ],
  },
  {
    name: '支付',
    links: [
      {
        name: '充值余额',
        path: '/pages/pay/recharge',
      },
      {
        name: '充值记录',
        path: '/pages/pay/recharge-log',
      },
    ],
  },
  {
    name: '用户中心',
    links: [
      {
        name: '用户信息',
        path: '/pages/user/info',
      },
      {
        name: '用户订单',
        path: '/pages/order/list',
      },
      {
        name: '售后订单',
        path: '/pages/order/aftersale/list',
      },
      {
        name: '商品收藏',
        path: '/pages/user/goods-collect',
      },
      {
        name: '浏览记录',
        path: '/pages/user/goods-log',
      },
      {
        name: '地址管理',
        path: '/pages/user/address/list',
      },
      {
        name: '用户佣金',
        path: '/pages/user/wallet/commission',
      },
      {
        name: '用户余额',
        path: '/pages/user/wallet/money',
      },
      {
        name: '用户积分',
        path: '/pages/user/wallet/score',
      },
    ],
  },
] as AppLinkGroup[];
