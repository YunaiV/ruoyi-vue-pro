import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MallOrderApi {
  /** 订单信息 */
  export interface Order {
    id?: number; // 订单编号
    no?: string; // 订单流水号
    createTime?: Date; // 下单时间
    type?: number; // 订单类型
    terminal?: number; // 订单来源
    userId?: number; // 用户编号
    userIp?: string; // 用户 IP
    userRemark?: string; // 用户备注
    status?: number; // 订单状态
    productCount?: number; // 购买的商品数量
    finishTime?: Date; // 订单完成时间
    cancelTime?: Date; // 订单取消时间
    cancelType?: number; // 取消类型
    remark?: string; // 商家备注
    payOrderId?: number; // 支付订单编号
    payStatus?: boolean; // 是否已支付
    payTime?: Date; // 付款时间
    payChannelCode?: string; // 支付渠道
    totalPrice?: number; // 商品原价（总）
    discountPrice?: number; // 订单优惠（总）
    deliveryPrice?: number; // 运费金额
    adjustPrice?: number; // 订单调价（总）
    payPrice?: number; // 应付金额（总）
    deliveryType?: number; // 发货方式
    pickUpStoreId?: number; // 自提门店编号
    pickUpVerifyCode?: string; // 自提核销码
    deliveryTemplateId?: number; // 配送模板编号
    logisticsId?: number; // 发货物流公司编号
    logisticsNo?: string; // 发货物流单号
    deliveryTime?: Date; // 发货时间
    receiveTime?: Date; // 收货时间
    receiverName?: string; // 收件人名称
    receiverMobile?: string; // 收件人手机
    receiverPostCode?: number; // 收件人邮编
    receiverAreaId?: number; // 收件人地区编号
    receiverAreaName?: string; // 收件人地区名字
    receiverDetailAddress?: string; // 收件人详细地址
    afterSaleStatus?: number; // 售后状态
    refundPrice?: number; // 退款金额
    couponId?: number; // 优惠劵编号
    couponPrice?: number; // 优惠劵减免金额
    pointPrice?: number; // 积分抵扣的金额
    vipPrice?: number; // VIP 减免金额
    items?: OrderItem[]; // 订单项列表
    user?: {
      // 下单用户信息
      avatar?: string; // 用户头像
      id?: number; // 用户编号
      nickname?: string; // 用户昵称
    };
    brokerageUser?: {
      // 推广用户信息
      avatar?: string; // 用户头像
      id?: number; // 用户编号
      nickname?: string; // 用户昵称
    }; // 推广用户信息
    logs?: OrderLog[]; // 订单操作日志
  }

  /** 订单项 */
  export interface OrderItem {
    id?: number; // 编号
    userId?: number; // 用户编号
    orderId?: number; // 订单编号
    spuId?: number; // 商品 SPU 编号
    spuName?: string; // 商品 SPU 名称
    skuId?: number; // 商品 SKU 编号
    picUrl?: string; // 商品图片
    count?: number; // 购买数量
    originalPrice?: number; // 商品原价（总）
    originalUnitPrice?: number; // 商品原价（单）
    discountPrice?: number; // 商品优惠（总）
    payPrice?: number; // 商品实付金额（总）
    orderPartPrice?: number; // 子订单分摊金额（总）
    orderDividePrice?: number; // 分摊后子订单实付金额（总）
    afterSaleStatus?: number; // 售后状态
    properties?: {
      propertyId?: number; // 属性的编号
      propertyName?: string; // 属性的名称
      valueId?: number; // 属性值的编号
      valueName?: string; // 属性值的名称
    }[]; // 属性数组
    price?: number;
  }

  /** 订单日志 */
  export interface OrderLog {
    content?: string; // 日志内容
    createTime?: Date; // 创建时间
    userType?: number; // 用户类型
    userId?: number; // 用户编号
  }

  /** 交易订单统计响应 */
  export interface OrderSummaryRespVO {
    orderCount: number; // 订单数量
    orderPayPrice: number; // 订单金额
    afterSaleCount: number; // 退款单数
    afterSalePrice: number; // 退款金额
  }

  /** 订单发货请求 */
  export interface OrderUpdateDeliveryReqVO {
    id?: number; // 订单编号
    expressType: string; // 发货方式
    logisticsId: number; // 物流公司编号
    logisticsNo: string; // 物流编号
  }

  /** 订单备注请求 */
  export interface OrderUpdateRemarkReqVO {
    id: number; // 订单编号
    remark: string; // 备注
  }

  /** 订单调价请求 */
  export interface OrderUpdatePriceReqVO {
    id: number; // 订单编号
    adjustPrice: number; // 调整金额，单位：分
  }

  /** 订单地址请求 */
  export interface OrderUpdateAddressReqVO {
    id: number; // 订单编号
    receiverName: string; // 收件人名称
    receiverMobile: string; // 收件人手机
    receiverAreaId: number; // 收件人地区编号
    receiverDetailAddress: string; // 收件人详细地址
  }
}

/** 查询交易订单列表 */
export function getOrderPage(params: PageParam) {
  return requestClient.get<PageResult<MallOrderApi.Order>>(
    '/trade/order/page',
    {
      params,
    },
  );
}

/** 查询交易订单统计 */
export function getOrderSummary(params: any) {
  return requestClient.get<MallOrderApi.OrderSummaryRespVO>(
    '/trade/order/summary',
    {
      params,
    },
  );
}

/** 查询交易订单详情 */
export function getOrder(id: number) {
  return requestClient.get<MallOrderApi.Order>(
    `/trade/order/get-detail?id=${id}`,
  );
}

/** 查询交易订单物流详情 */
export function getExpressTrackList(id: number) {
  return requestClient.get(`/trade/order/get-express-track-list?id=${id}`);
}

/** 订单发货 */
export function deliveryOrder(data: MallOrderApi.OrderUpdateDeliveryReqVO) {
  return requestClient.put('/trade/order/delivery', data);
}

/** 订单备注 */
export function updateOrderRemark(data: MallOrderApi.OrderUpdateRemarkReqVO) {
  return requestClient.put('/trade/order/update-remark', data);
}

/** 订单调价 */
export function updateOrderPrice(data: MallOrderApi.OrderUpdatePriceReqVO) {
  return requestClient.put('/trade/order/update-price', data);
}

/** 修改订单地址 */
export function updateOrderAddress(data: MallOrderApi.OrderUpdateAddressReqVO) {
  return requestClient.put('/trade/order/update-address', data);
}

/** 订单核销 */
export function pickUpOrder(id: number) {
  return requestClient.put(`/trade/order/pick-up-by-id?id=${id}`);
}

/** 订单核销 */
export function pickUpOrderByVerifyCode(pickUpVerifyCode: string) {
  return requestClient.put('/trade/order/pick-up-by-verify-code', undefined, {
    params: { pickUpVerifyCode },
  });
}

/** 查询核销码对应的订单 */
export function getOrderByPickUpVerifyCode(pickUpVerifyCode: string) {
  return requestClient.get<MallOrderApi.Order>(
    '/trade/order/get-by-pick-up-verify-code',
    { params: { pickUpVerifyCode } },
  );
}
