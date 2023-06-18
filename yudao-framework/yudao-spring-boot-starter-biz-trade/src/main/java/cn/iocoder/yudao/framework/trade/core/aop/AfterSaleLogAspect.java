package cn.iocoder.yudao.framework.trade.core.aop;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.util.spel.SpelUtil;
import cn.iocoder.yudao.framework.trade.core.annotations.AfterSaleLog;
import cn.iocoder.yudao.framework.trade.core.dto.TradeAfterSaleLogCreateReqDTO;
import cn.iocoder.yudao.framework.trade.core.enums.AfterSaleStatusEnum;
import cn.iocoder.yudao.framework.trade.core.service.AfterSaleLogService;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.HashMap;
import java.util.Map;

/**
 * 记录售后日志的 AOP 切面
 *
 * @author 陈賝
 * @since 2023/6/13 13:54
 */
@Slf4j
@Aspect
public class AfterSaleLogAspect {

    @AfterReturning(pointcut = "@annotation(afterSaleLog)", returning = "info")
    public void doAfterReturning(JoinPoint joinPoint, AfterSaleLog afterSaleLog, Object info) {
        try {
            // 日志对象拼接
            Integer userType = WebFrameworkUtils.getLoginUserType();
            Long id = WebFrameworkUtils.getLoginUserId();
            Map<String, String> formatObj = spelFormat(joinPoint, info);
            TradeAfterSaleLogCreateReqDTO dto = new TradeAfterSaleLogCreateReqDTO()
                    .setUserId(id).setUserType(userType)
//            MapUtil.getLong() TODO @陈賝：试试这个方法
                    .setAfterSaleId(Long.valueOf(formatObj.get("id")))
                    .setContent(formatObj.get("content"))
                    .setOperateType(formatObj.get("operateType"));
            // 异步存入数据库 TODO 可以注入哈；
            SpringUtil.getBean(AfterSaleLogService.class).insert(dto);
        } catch (Exception exception) {
            // TODO @陈賝：日志要记录下参数哈，不然排查问题不好搞；
            log.error("日志记录错误", exception);
        }
    }

    /**
     * 获取描述信息
     */
    public static Map<String, String> spelFormat(JoinPoint joinPoint, Object info) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AfterSaleLog afterSaleLogPoint = signature.getMethod().getAnnotation(AfterSaleLog.class);
        Map<String, String> result = new HashMap<>(2); // TODO @陈賝：Maps.newExpectedXXX(3)
        // 售后ID
        String id = SpelUtil.compileParams(joinPoint, info, afterSaleLogPoint.id());
        result.put("id", id);
        // 操作类型
        String operateType = SpelUtil.compileParams(joinPoint, info, afterSaleLogPoint.operateType());
        result.put("operateType", operateType);
        // 日志内容
        String content = SpelUtil.compileParams(joinPoint, info, afterSaleLogPoint.content());
        if (StrUtil.isNotEmpty(afterSaleLogPoint.operateType())) {
            content += AfterSaleStatusEnum.valueOf(SpelUtil.compileParams(joinPoint, info, afterSaleLogPoint.operateType())).description();
        }
        result.put("content", content);
        return result;
    }

}
