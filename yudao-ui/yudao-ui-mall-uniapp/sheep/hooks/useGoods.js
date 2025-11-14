import { ref } from 'vue';
import dayjs from 'dayjs';
import $url from '@/sheep/url';
import { formatDate } from '@/sheep/helper/utils';

/**
 * 格式化销量
 * @param {'exact' | string} type 格式类型：exact=精确值，其它=大致数量
 * @param {number} num 销量
 * @return {string} 格式化后的销量字符串
 */
export function formatSales(type, num) {
  let prefix = type !== 'exact' && num < 10 ? '销量' : '已售';
  return formatNum(prefix, type, num);
}

/**
 * 格式化兑换量
 * @param {'exact' | string} type 格式类型：exact=精确值，其它=大致数量
 * @param {number} num 销量
 * @return {string} 格式化后的销量字符串
 */
export function formatExchange(type, num) {
  return formatNum('已兑换', type, num);
}

/**
 * 格式化库存
 * @param {'exact' | any} type 格式类型：exact=精确值，其它=大致数量
 * @param {number} num 销量
 * @return {string} 格式化后的销量字符串
 */
export function formatStock(type, num) {
  return formatNum('库存', type, num);
}

/**
 * 格式化数字
 * @param {string} prefix 前缀
 * @param {'exact' | string} type 格式类型：exact=精确值，其它=大致数量
 * @param {number} num 销量
 * @return {string} 格式化后的销量字符串
 */
export function formatNum(prefix, type, num) {
  num = num || 0;
  // 情况一：精确数值
  if (type === 'exact') {
    return prefix + num;
  }
  // 情况二：小于等于 10
  if (num < 10) {
    return `${prefix}≤10`;
  }
  // 情况三：大于 10，除第一位外，其它位都显示为0
  // 例如：100  - 199  显示为 100+
  //      9000 - 9999 显示为 9000+
  const numStr = num.toString();
  const first = numStr[0];
  const other = '0'.repeat(numStr.length - 1);
  return `${prefix}${first}${other}+`;
}

// 格式化价格
export function formatPrice(e) {
  return e.length === 1 ? e[0] : e.join('~');
}

// 视频格式后缀列表
const VIDEO_SUFFIX_LIST = ['.avi', '.mp4'];

/**
 * 转换商品轮播的链接列表：根据链接的后缀，判断是视频链接还是图片链接
 *
 * @param {string[]} urlList 链接列表
 * @return {{src: string, type: 'video' | 'image' }[]}  转换后的链接列表
 */
export function formatGoodsSwiper(urlList) {
  return (
    urlList
      ?.filter((url) => url)
      .map((url, key) => {
        const isVideo = VIDEO_SUFFIX_LIST.some((suffix) => url.includes(suffix));
        const type = isVideo ? 'video' : 'image';
        const src = $url.cdn(url);
        return {
          type,
          src,
        };
      }) || []
  );
}

/**
 * 格式化订单状态的颜色
 *
 * @param order 订单
 * @return {string} 颜色的 class 名称
 */
export function formatOrderColor(order) {
  if (order.status === 0) {
    return 'info-color';
  }
  if (order.status === 10 || order.status === 20 || (order.status === 30 && !order.commentStatus)) {
    return 'warning-color';
  }
  if (order.status === 30 && order.commentStatus) {
    return 'success-color';
  }
  return 'danger-color';
}

/**
 * 格式化订单状态
 *
 * @param order 订单
 */
export function formatOrderStatus(order) {
  if (order.status === 0) {
    return '待付款';
  }
  if (order.status === 10 && order.deliveryType === 1) {
    return '待发货';
  }
  if (order.status === 10 && order.deliveryType === 2) {
    return '待核销';
  }
  if (order.status === 20) {
    return '待收货';
  }
  if (order.status === 30 && !order.commentStatus) {
    return '待评价';
  }
  if (order.status === 30 && order.commentStatus) {
    return '已完成';
  }
  return '已关闭';
}

/**
 * 格式化订单状态的描述
 *
 * @param order 订单
 */
export function formatOrderStatusDescription(order) {
  if (order.status === 0) {
    return `请在 ${formatDate(order.payExpireTime)} 前完成支付`;
  }
  if (order.status === 10) {
    return '商家未发货，请耐心等待';
  }
  if (order.status === 20) {
    return '商家已发货，请耐心等待';
  }
  if (order.status === 30 && !order.commentStatus) {
    return '已收货，快去评价一下吧';
  }
  if (order.status === 30 && order.commentStatus) {
    return '交易完成，感谢您的支持';
  }
  return '交易关闭';
}

/**
 * 处理订单的 button 操作按钮数组
 *
 * @param order 订单
 */
export function handleOrderButtons(order) {
  order.buttons = [];
  if (order.type === 3) {
    // 查看拼团
    order.buttons.push('combination');
  }
  if (order.status === 20) {
    // 确认收货
    order.buttons.push('confirm');
  }
  if (order.logisticsId > 0) {
    // 查看物流
    order.buttons.push('express');
  }
  if (order.status === 0) {
    // 取消订单 / 发起支付
    order.buttons.push('cancel');
    order.buttons.push('pay');
  }
  if (order.status === 30 && !order.commentStatus) {
    // 发起评价
    order.buttons.push('comment');
  }
  if (order.status === 40) {
    // 删除订单
    order.buttons.push('delete');
  }
}

/**
 * 格式化售后状态
 *
 * @param afterSale 售后
 */
export function formatAfterSaleStatus(afterSale) {
  if (afterSale.status === 10) {
    return '申请售后';
  }
  if (afterSale.status === 20) {
    return '商品待退货';
  }
  if (afterSale.status === 30) {
    return '商家待收货';
  }
  if (afterSale.status === 40) {
    return '等待退款';
  }
  if (afterSale.status === 50) {
    return '退款成功';
  }
  if (afterSale.status === 61) {
    return '买家取消';
  }
  if (afterSale.status === 62) {
    return '商家拒绝';
  }
  if (afterSale.status === 63) {
    return '商家拒收货';
  }
  return '未知状态';
}

/**
 * 格式化售后状态的描述
 *
 * @param afterSale 售后
 */
export function formatAfterSaleStatusDescription(afterSale) {
  if (afterSale.status === 10) {
    return '退款申请待商家处理';
  }
  if (afterSale.status === 20) {
    return '请退货并填写物流信息';
  }
  if (afterSale.status === 30) {
    return '退货退款申请待商家处理';
  }
  if (afterSale.status === 40) {
    return '等待退款';
  }
  if (afterSale.status === 50) {
    return '退款成功';
  }
  if (afterSale.status === 61) {
    return '退款关闭';
  }
  if (afterSale.status === 62) {
    return `商家不同意退款申请，拒绝原因：${afterSale.auditReason}`;
  }
  if (afterSale.status === 63) {
    return `商家拒绝收货，不同意退款，拒绝原因：${afterSale.auditReason}`;
  }
  return '未知状态';
}

/**
 * 处理售后的 button 操作按钮数组
 *
 * @param afterSale 售后
 */
export function handleAfterSaleButtons(afterSale) {
  afterSale.buttons = [];
  if ([10, 20, 30].includes(afterSale.status)) {
    // 取消订单
    afterSale.buttons.push('cancel');
  }
  if (afterSale.status === 20) {
    // 退货信息
    afterSale.buttons.push('delivery');
  }
}

/**
 * 倒计时
 * @param toTime   截止时间
 * @param fromTime 起始时间，默认当前时间
 * @return {{s: string, ms: number, h: string, m: string}} 持续时间
 */
export function useDurationTime(toTime, fromTime = '') {
  toTime = getDayjsTime(toTime);
  if (fromTime === '') {
    fromTime = dayjs();
  }
  let duration = ref(toTime - fromTime);
  if (duration.value > 0) {
    setTimeout(() => {
      if (duration.value > 0) {
        duration.value -= 1000;
      }
    }, 1000);
  }

  let durationTime = dayjs.duration(duration.value);
  return {
    h: (durationTime.months() * 30 * 24 + durationTime.days() * 24 + durationTime.hours())
      .toString()
      .padStart(2, '0'),
    m: durationTime.minutes().toString().padStart(2, '0'),
    s: durationTime.seconds().toString().padStart(2, '0'),
    ms: durationTime.$ms,
  };
}

/**
 * 转换为 Dayjs
 * @param {any} time 时间
 * @return {dayjs.Dayjs}
 */
function getDayjsTime(time) {
  time = time.toString();
  if (time.indexOf('-') > 0) {
    // 'date'
    return dayjs(time);
  }
  if (time.length > 10) {
    // 'timestamp'
    return dayjs(parseInt(time));
  }
  if (time.length === 10) {
    // 'unixTime'
    return dayjs.unix(parseInt(time));
  }
}

/**
 * 将分转成元
 *
 * @param price 分，例如说 100 分
 * @returns {string} 元，例如说 1.00 元
 */
export function fen2yuan(price) {
  return (price / 100.0).toFixed(2);
}

/**
 * 将分转成元
 *
 * 如果没有小数点，则不展示小数点部分
 *
 * @param price 分，例如说 100 分
 * @returns {string} 元，例如说 1 元
 */
export function fen2yuanSimple(price) {
  return fen2yuan(price).replace(/\.?0+$/, '');
}

/**
 * 将折扣百分比转化为“打x者”的 x 部分
 *
 * @param discountPercent
 */
export function formatDiscountPercent(discountPercent) {
  return (discountPercent / 10.0).toFixed(1).replace(/\.?0+$/, '');
}

/**
 * 从商品 SKU 数组中，转换出商品属性的数组
 *
 * 类似结构：[{
 *    id: // 属性的编号
 *    name: // 属性的名字
 *    values: [{
 *      id: // 属性值的编号
 *      name: // 属性值的名字
 *    }]
 * }]
 *
 * @param skus 商品 SKU 数组
 */
export function convertProductPropertyList(skus) {
  let result = [];
  for (const sku of skus) {
    if (!sku.properties) {
      continue;
    }
    for (const property of sku.properties) {
      // ① 先处理属性
      let resultProperty = result.find((item) => item.id === property.propertyId);
      if (!resultProperty) {
        resultProperty = {
          id: property.propertyId,
          name: property.propertyName,
          values: [],
        };
        result.push(resultProperty);
      }
      // ② 再处理属性值
      let resultValue = resultProperty.values.find((item) => item.id === property.valueId);
      if (!resultValue) {
        resultProperty.values.push({
          id: property.valueId,
          name: property.valueName,
        });
      }
    }
  }
  return result;
}

export function appendSettlementProduct(spus, settlementInfos) {
  if (!settlementInfos || settlementInfos.length === 0) {
    return;
  }
  for (const spu of spus) {
    const settlementInfo = settlementInfos.find((info) => info.spuId === spu.id);
    if (!settlementInfo) {
      return;
    }
    // 选择价格最小的 SKU 设置到 SPU 上
    const settlementSku = settlementInfo.skus
      .filter((sku) => sku.promotionPrice > 0)
      .reduce((prev, curr) => (prev.promotionPrice < curr.promotionPrice ? prev : curr), []);
    if (settlementSku) {
      spu.promotionType = settlementSku.promotionType;
      spu.promotionPrice = settlementSku.promotionPrice;
    }
    // 设置【满减送】活动
    if (settlementInfo.rewardActivity) {
      spu.rewardActivity = settlementInfo.rewardActivity;
    }
  }
}

// 获得满减送活动的规则描述（group）
export function getRewardActivityRuleGroupDescriptions(activity) {
  if (!activity || !activity.rules || activity.rules.length === 0) {
    return [];
  }
  const result = [
    { name: '满减', values: [] },
    { name: '赠品', values: [] },
    { name: '包邮', values: [] },
  ];
  activity.rules.forEach((rule) => {
    const conditionTypeStr =
      activity.conditionType === 10 ? `满 ${fen2yuanSimple(rule.limit)} 元` : `满 ${rule.limit} 件`;
    // 满减
    if (rule.limit) {
      result[0].values.push(`${conditionTypeStr} 减 ${fen2yuanSimple(rule.discountPrice)} 元`);
    }
    // 赠品
    if (rule.point || (rule.giveCouponTemplateCounts && rule.giveCouponTemplateCounts.length > 0)) {
      let tips = [];
      if (rule.point) {
        tips.push(`送 ${rule.point} 积分`);
      }
      if (rule.giveCouponTemplateCounts && rule.giveCouponTemplateCounts.length > 0) {
        tips.push(`送 ${rule.giveCouponTemplateCounts.length} 张优惠券`);
      }
      result[1].values.push(`${conditionTypeStr} ${tips.join('、')}`);
    }
    // 包邮
    if (rule.freeDelivery) {
      result[2].values.push(`${conditionTypeStr} 包邮`);
    }
  });
  // 移除 values 为空的元素
  result.forEach((item) => {
    if (item.values.length === 0) {
      result.splice(result.indexOf(item), 1);
    }
  });
  return result;
}

// 获得满减送活动的规则描述（item）
export function getRewardActivityRuleItemDescriptions(activity) {
  if (!activity || !activity.rules || activity.rules.length === 0) {
    return [];
  }
  const result = [];
  activity.rules.forEach((rule) => {
    const conditionTypeStr =
      activity.conditionType === 10 ? `满${fen2yuanSimple(rule.limit)}元` : `满${rule.limit}件`;
    // 满减
    if (rule.limit) {
      result.push(`${conditionTypeStr}减${fen2yuanSimple(rule.discountPrice)}元`);
    }
    // 赠品
    if (rule.point) {
      result.push(`${conditionTypeStr}送${rule.point}积分`);
    }
    if (rule.giveCouponTemplateCounts && rule.giveCouponTemplateCounts.length > 0) {
      result.push(`${conditionTypeStr}送${rule.giveCouponTemplateCounts.length}张优惠券`);
    }
    // 包邮
    if (rule.freeDelivery) {
      result.push(`${conditionTypeStr}包邮`);
    }
  });
  return result;
}
