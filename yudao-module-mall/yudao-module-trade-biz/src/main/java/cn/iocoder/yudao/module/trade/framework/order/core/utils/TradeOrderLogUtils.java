package cn.iocoder.yudao.module.trade.framework.order.core.utils;

import cn.iocoder.yudao.module.trade.framework.order.core.aop.TradeOrderLogAspect;

import java.util.Map;

/**
 * 交易订单的操作日志 Utils
 *
 * @author 芋道源码
 */
public class TradeOrderLogUtils {

    public static void setOrderInfo(Long id, Integer beforeStatus, Integer afterStatus) {
        TradeOrderLogAspect.setOrderInfo(id, beforeStatus, afterStatus, null);
    }

    public static void setOrderInfo(Long id, Integer beforeStatus, Integer afterStatus,
                                    Map<String, Object> exts) {
        TradeOrderLogAspect.setOrderInfo(id, beforeStatus, afterStatus, exts);
    }

    public static void setUserInfo(Long userId, Integer userType) {
        TradeOrderLogAspect.setUserInfo(userId, userType);
    }

}
