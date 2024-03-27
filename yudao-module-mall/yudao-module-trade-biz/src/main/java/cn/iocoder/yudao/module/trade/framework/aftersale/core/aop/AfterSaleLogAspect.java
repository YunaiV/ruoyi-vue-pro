package cn.iocoder.yudao.module.trade.framework.aftersale.core.aop;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderLogDO;
import cn.iocoder.yudao.module.trade.framework.aftersale.core.annotations.AfterSaleLog;
import cn.iocoder.yudao.module.trade.service.aftersale.AfterSaleLogService;
import cn.iocoder.yudao.module.trade.service.aftersale.bo.AfterSaleLogCreateReqBO;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;

import javax.annotation.Resource;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.toJsonString;
import static java.util.Collections.emptyMap;

/**
 * 售后订单的操作记录的 AOP 切面
 *
 * @author 陈賝
 * @since 2023/6/13 13:54
 */
@Slf4j
@Aspect
public class AfterSaleLogAspect {

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
    private static final ThreadLocal<Long> AFTER_SALE_ID = new ThreadLocal<>();
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
    private AfterSaleLogService afterSaleLogService;

    @AfterReturning(pointcut = "@annotation(afterSaleLog)")
    public void doAfterReturning(JoinPoint joinPoint, AfterSaleLog afterSaleLog) {
        try {
            // 1.1 操作用户
            Integer userType = getUserType();
            Long userId = getUserId();
            // 1.2 售后信息
            Long afterSaleId = AFTER_SALE_ID.get();
            if (afterSaleId == null) { // 如果未设置，只有注解，说明不需要记录日志
                return;
            }
            Integer beforeStatus = BEFORE_STATUS.get();
            Integer afterStatus = AFTER_STATUS.get();
            Map<String, Object> exts = ObjectUtil.defaultIfNull(EXTS.get(), emptyMap());
            String content = StrUtil.format(afterSaleLog.operateType().getContent(), exts);

            // 2. 记录日志
            AfterSaleLogCreateReqBO createBO = new AfterSaleLogCreateReqBO()
                    .setUserId(userId).setUserType(userType)
                    .setAfterSaleId(afterSaleId).setBeforeStatus(beforeStatus).setAfterStatus(afterStatus)
                    .setOperateType(afterSaleLog.operateType().getType()).setContent(content);
            afterSaleLogService.createAfterSaleLog(createBO);
        } catch (Exception exception) {
            log.error("[doAfterReturning][afterSaleLog({}) 日志记录错误]", toJsonString(afterSaleLog), exception);
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

    public static void setAfterSale(Long id, Integer beforeStatus, Integer afterStatus, Map<String, Object> exts) {
        AFTER_SALE_ID.set(id);
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
        AFTER_SALE_ID.remove();
        BEFORE_STATUS.remove();
        AFTER_STATUS.remove();
        EXTS.remove();
    }

}
