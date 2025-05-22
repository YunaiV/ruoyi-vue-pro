package cn.iocoder.yudao.module.trade.framework.order.core.aop;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderLogDO;
import cn.iocoder.yudao.module.trade.framework.order.core.annotations.TradeOrderLog;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderLogService;
import cn.iocoder.yudao.module.trade.service.order.bo.TradeOrderLogCreateReqBO;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.toJsonString;
import static java.util.Collections.emptyMap;

/**
 * 交易订单的操作日志的记录 AOP 切面
 *
 * @author 陈賝
 * @since 2023/6/13 13:54
 */
@Component
@Aspect
@Slf4j
public class TradeOrderLogAspect {

    /**
     * 用户编号
     *
     * 目前的使用场景：支付回调时，需要强制设置下用户编号
     */
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    /**
     * 用户类型
     */
    private static final ThreadLocal<Integer> USER_TYPE = new ThreadLocal<>();
    /**
     * 订单编号
     */
    private static final ThreadLocal<Long> ORDER_ID = new ThreadLocal<>();
    /**
     * 操作前的状态
     */
    private static final ThreadLocal<Integer> BEFORE_STATUS = new ThreadLocal<>();
    /**
     * 操作后的状态
     */
    private static final ThreadLocal<Integer> AFTER_STATUS = new ThreadLocal<>();
    /**
     * 拓展参数 Map，用于格式化操作内容
     */
    private static final ThreadLocal<Map<String, Object>> EXTS = new ThreadLocal<>();

    @Resource
    private TradeOrderLogService orderLogService;

    @AfterReturning("@annotation(orderLog)")
    public void doAfterReturning(JoinPoint joinPoint, TradeOrderLog orderLog) {
        try {
            // 1.1 操作用户
            Integer userType = getUserType();
            Long userId = getUserId();
            // 1.2 订单信息
            Long orderId = ORDER_ID.get();
            if (orderId == null) { // 如果未设置，只有注解，说明不需要记录日志
                return;
            }
            Integer beforeStatus = BEFORE_STATUS.get();
            Integer afterStatus = AFTER_STATUS.get();
            Map<String, Object> exts = ObjectUtil.defaultIfNull(EXTS.get(), emptyMap());
            String content = StrUtil.format(orderLog.operateType().getContent(), exts);

            // 2. 记录日志
            TradeOrderLogCreateReqBO createBO = new TradeOrderLogCreateReqBO()
                    .setUserId(userId).setUserType(userType)
                    .setOrderId(orderId).setBeforeStatus(beforeStatus).setAfterStatus(afterStatus)
                    .setOperateType(orderLog.operateType().getType()).setContent(content);
            orderLogService.createOrderLog(createBO);
        } catch (Exception ex) {
            log.error("[doAfterReturning][orderLog({}) 订单日志错误]", toJsonString(orderLog), ex);
        } finally {
            clear();
        }
    }

    /**
     * 获得用户类型
     *
     * 如果没有，则约定为 {@link TradeOrderLogDO#getUserType()} 系统
     *
     * @return 用户类型
     */
    private static Integer getUserType() {
        return ObjectUtil.defaultIfNull(WebFrameworkUtils.getLoginUserType(), TradeOrderLogDO.USER_TYPE_SYSTEM);
    }

    /**
     * 获得用户编号
     *
     * 如果没有，则约定为 {@link TradeOrderLogDO#getUserId()} 系统
     *
     * @return 用户类型
     */
    private static Long getUserId() {
        return ObjectUtil.defaultIfNull(WebFrameworkUtils.getLoginUserId(), TradeOrderLogDO.USER_ID_SYSTEM);
    }

    public static void setOrderInfo(Long id, Integer beforeStatus, Integer afterStatus, Map<String, Object> exts) {
        ORDER_ID.set(id);
        BEFORE_STATUS.set(beforeStatus);
        AFTER_STATUS.set(afterStatus);
        EXTS.set(exts);
    }

    public static void setUserInfo(Long userId, Integer userType) {
        USER_ID.set(userId);
        USER_TYPE.set(userType);
    }

    private static void clear() {
        USER_ID.remove();
        USER_TYPE.remove();
        ORDER_ID.remove();
        BEFORE_STATUS.remove();
        AFTER_STATUS.remove();
        EXTS.remove();
    }

}
