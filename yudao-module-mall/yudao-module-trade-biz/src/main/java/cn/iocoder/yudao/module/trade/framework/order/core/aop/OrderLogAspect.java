package cn.iocoder.yudao.module.trade.framework.order.core.aop;


import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.util.spring.SpringExpressionUtils;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import cn.iocoder.yudao.module.trade.enums.aftersale.OrderOperateTypeEnum;
import cn.iocoder.yudao.module.trade.framework.order.core.annotations.OrderLog;
import cn.iocoder.yudao.module.trade.framework.order.core.dto.TradeOrderLogCreateReqDTO;
import cn.iocoder.yudao.module.trade.framework.order.core.service.OrderLogService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import javax.annotation.Resource;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.toJsonString;
import static java.util.Arrays.asList;

/**
 * 记录订单操作日志的 AOP 切面
 *
 * @author 陈賝
 * @since 2023/6/13 13:54
 */
@Slf4j
@Aspect
public class OrderLogAspect {

    @Resource
    private OrderLogService orderLogService;

    @AfterReturning(pointcut = "@annotation(orderLog)", returning = "info")
    public void doAfterReturning(JoinPoint joinPoint, OrderLog orderLog, Object info) {
        try {
            // 日志对象拼接
            Integer userType = WebFrameworkUtils.getLoginUserType();
            Long id = WebFrameworkUtils.getLoginUserId();
            TradeOrderLogCreateReqDTO dto = new TradeOrderLogCreateReqDTO()
                    .setUserId(id)
                    .setUserType(userType)
                    .setOrderId(getAfterSaleId(joinPoint, info, orderLog.id()))
                    .setOperateType(orderLog.operateType().getType())
                    .setContent(getContent(joinPoint, info, orderLog));
            // 异步存入数据库
            orderLogService.createLog(dto);
        } catch (Exception exception) {
            log.error("[doAfterReturning][orderLog({}) 订单日志错误]", toJsonString(orderLog), exception);
        }
    }

    /**
     * 获取描述信息
     */
    private static Map<String, Object> spelFormat(JoinPoint joinPoint, Object info) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        OrderLog afterSaleLogPoint = signature.getMethod().getAnnotation(OrderLog.class);
        return SpringExpressionUtils.parseExpression(joinPoint, info,
                asList(afterSaleLogPoint.id(), afterSaleLogPoint.content()));
    }


    /**
     * 获取订单ID
     */
    private static Long getAfterSaleId(JoinPoint joinPoint, Object info, String spel) {
        Map<String, Object> spelMap = spelFormat(joinPoint, info);
        return MapUtil.getLong(spelMap, spel);
    }

    /**
     * 获取解析后的日志内容
     */
    private static String getContent(JoinPoint joinPoint, Object info, OrderLog afterSaleLog) {
        Map<String, Object> spelMap = spelFormat(joinPoint, info);
        StringBuilder content = new StringBuilder().append(MapUtil.getStr(spelMap, afterSaleLog.content()));
        OrderOperateTypeEnum operateTypeEnum = afterSaleLog.operateType();
        return ObjectUtil.isNotNull(operateTypeEnum) ?
                content.append(operateTypeEnum.getDescription()).toString() : content.toString();
    }

}
