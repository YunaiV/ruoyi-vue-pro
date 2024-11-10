package cn.iocoder.yudao.module.erp.aop;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.erp.dal.mysql.product.ErpProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.AOP_ENHANCED_EXCEPTION;

/**
 * @className: SynExternalDataAspect
 * @author: Wqh
 * @date: 2024/11/5 8:50
 * @Version: 1.0
 * @description:
 */

@Aspect
@Slf4j
@Component
@RequiredArgsConstructor
public class SynExternalDataAspect {
    public static final String ERP_PRODUCT = "erp_product";
    public static final String ERP_CUSTOM_RULE = "erp_custom_rule";
    public static final String ERP_SUPPLIER_PRODUCT = "erp_supplier_product";
    private final ErpProductMapper erpProductMapper;
    @Autowired
    private MessageChannel productChannel;

    @Around(value = "@annotation(synExternalData)")
    @SneakyThrows
    public Object around(ProceedingJoinPoint joinPoint, SynExternalData synExternalData) {
        //获取注解中的表名
        String table = synExternalData.table();
        //获取类型
        Class<?> inClazz = synExternalData.inClazz();
        Long idValue = null;
        if (!inClazz.equals(Object.class)){
            //获取请求入参
            Object[] args = joinPoint.getArgs();
            for (Object arg : args){
                if (arg.getClass().equals(inClazz)){
                    //通过反射来获取arg中的id属性值
                    Field id = arg.getClass().getDeclaredField("id");
                    //获取入参中的id值
                    id.setAccessible(true);
                    idValue = (Long)id.get(arg);
                }
            }
        }
        //执行目标方法
        Object result = joinPoint.proceed();
        //当idValue不为null，则说明该请求为新增请求，反之则为修改请求
        if (idValue == null){
            //获取返参中的id
            if (ObjUtil.isNotEmpty(result)){
                idValue = (Long) result;
            }
        }
        //判断id是否为null，为null则存在异常
        ThrowUtil.ifThrow(idValue == null,AOP_ENHANCED_EXCEPTION);
        List<ErpProductDTO> products = switch (table) {
            case ERP_PRODUCT -> erpProductMapper.selectProductAllInfoListById(idValue);
            case ERP_CUSTOM_RULE -> erpProductMapper.selectProductAllInfoListByCustomRuleId(idValue);
            case ERP_SUPPLIER_PRODUCT -> erpProductMapper.selectProductAllInfoListBySupplierId(idValue);
            default -> new ArrayList<>();
        };
        //进行数据同步
        //过滤掉没有海关规则的数据（过滤掉ruleId为空的数据）
        products = products.stream().filter(product -> ObjUtil.isNotEmpty(product.getRuleId())).toList();
        //数据同步
        if (CollUtil.isNotEmpty(products)){
            log.info("send begin");
            productChannel.send(MessageBuilder.withPayload(products).build());
            log.info("send end");
        }
        return result;
    }
}
