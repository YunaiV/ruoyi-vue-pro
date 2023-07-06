package cn.iocoder.yudao.module.trade.framework.aftersalelog.core.aop;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.util.spring.SpringExpressionUtils;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import cn.iocoder.yudao.module.trade.enums.aftersale.AfterSaleOperateTypeEnum;
import cn.iocoder.yudao.module.trade.framework.aftersalelog.core.annotations.AfterSaleLog;
import cn.iocoder.yudao.module.trade.framework.aftersalelog.core.dto.TradeAfterSaleLogCreateReqDTO;
import cn.iocoder.yudao.module.trade.framework.aftersalelog.core.service.AfterSaleLogService;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.toJsonString;
import static java.util.Arrays.asList;

/**
 * 记录售后日志的 AOP 切面
 *
 * @author 陈賝
 * @since 2023/6/13 13:54
 */
@Slf4j
@Aspect
public class AfterSaleLogAspect {

    @Resource
    private AfterSaleLogService afterSaleLogService;

    /**
     * 切面存入日志
     */
    @AfterReturning(pointcut = "@annotation(afterSaleLog)", returning = "info")
    public void doAfterReturning(JoinPoint joinPoint, AfterSaleLog afterSaleLog, Object info) {
        try {
            // 日志对象拼接
            Integer userType = WebFrameworkUtils.getLoginUserType();
            Long id = WebFrameworkUtils.getLoginUserId();
            TradeAfterSaleLogCreateReqDTO dto = new TradeAfterSaleLogCreateReqDTO()
                    .setUserId(id)
                    .setUserType(userType)
                    .setAfterSaleId(getAfterSaleId(joinPoint, info, afterSaleLog.id()))
                    .setOperateType(afterSaleLog.operateType().getType())
                    .setContent(getContent(joinPoint, info, afterSaleLog));
            // 异步存入数据库
            afterSaleLogService.createLog(dto);
        } catch (Exception exception) {
            log.error("[doAfterReturning][afterSaleLog({}) 日志记录错误]", toJsonString(afterSaleLog), exception);
        }
    }

    /**
     * 获取描述信息
     */
    private static Map<String, Object> spelFormat(JoinPoint joinPoint, Object info) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AfterSaleLog afterSaleLogPoint = signature.getMethod().getAnnotation(AfterSaleLog.class);
        return SpringExpressionUtils.parseExpression(joinPoint, info,
                asList(afterSaleLogPoint.id(), afterSaleLogPoint.content()));
    }

    /**
     * 获取售后ID
     */
    private static Long getAfterSaleId(JoinPoint joinPoint, Object info, String spel) {
        Map<String, Object> spelMap = spelFormat(joinPoint, info);
        return MapUtil.getLong(spelMap, spel);
    }

    /**
     * 获取解析后的日志内容
     */
    private static String getContent(JoinPoint joinPoint, Object info, AfterSaleLog afterSaleLog) {
        Map<String, Object> spelMap = spelFormat(joinPoint, info);
        StringBuilder content = new StringBuilder().append(MapUtil.getStr(spelMap, afterSaleLog.content()));
        AfterSaleOperateTypeEnum afterSaleOperateTypeEnum = afterSaleLog.operateType();
        return ObjectUtil.isNotNull(afterSaleOperateTypeEnum) ?
                content.append(afterSaleOperateTypeEnum.getDescription()).toString() : content.toString();
    }

}
