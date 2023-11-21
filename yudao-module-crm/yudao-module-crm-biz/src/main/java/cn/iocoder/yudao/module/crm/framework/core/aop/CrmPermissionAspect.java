package cn.iocoder.yudao.module.crm.framework.core.aop;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import cn.iocoder.yudao.module.crm.dal.dataobject.permission.CrmPermissionDO;
import cn.iocoder.yudao.module.crm.framework.core.annotations.CrmPermission;
import cn.iocoder.yudao.module.crm.framework.enums.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.framework.enums.CrmPermissionLevelEnum;
import cn.iocoder.yudao.module.crm.service.permission.CrmPermissionService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.toJsonString;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.CRM_PERMISSION_DENIED;

/**
 * Crm 数据权限校验 AOP 切面
 *
 * @author HUIHUI
 */
@Component
@Aspect
@Slf4j
public class CrmPermissionAspect {

    @Resource
    private LocalVariableTableParameterNameDiscoverer discoverer;
    @Resource
    private SpelExpressionParser parser;

    @Resource
    private CrmPermissionService crmPermissionService;

    /**
     * 获得用户编号
     *
     * @return 用户编号
     */
    private static Long getUserId() {
        return WebFrameworkUtils.getLoginUserId();
    }

    @Before("@annotation(crmPermission)")
    public void doBefore(JoinPoint joinPoint, CrmPermission crmPermission) throws NoSuchMethodException {
        // TODO 芋艿：临时，方便大家调试
        if (true) {
            return;
        }
        KeyValue<Long, Integer> bizIdAndBizType = getBizIdAndBizType(joinPoint, crmPermission);
        Integer bizType = bizIdAndBizType.getValue(); // 模块类型
        Long bizId = bizIdAndBizType.getKey(); // 模块数据编号
        Integer permissionLevel = crmPermission.level().getLevel(); // 需要的权限级别

        // TODO 如果是超级管理员则直接通过
        //if (superAdmin){
        //    return;
        //}

        // 1. 获取数据权限
        List<CrmPermissionDO> bizPermissions = crmPermissionService.getPermissionByBizTypeAndBizId(bizType, bizId);
        // 2.1 情况一：如果自己是负责人，则默认有所有权限
        // TODO @puhui999：会不会存在空指针的问题？
        CrmPermissionDO userPermission = CollUtil.findOne(bizPermissions, item -> ObjUtil.equal(item.getUserId(), getUserId()));
        if (CrmPermissionLevelEnum.isOwner(userPermission.getLevel())) {
            return;
        }
        // 2.2 情况二：校验自己是否有读权限
        if (CrmPermissionLevelEnum.isRead(permissionLevel)) {
            // 如果没有数据权限或没有负责人则表示此记录为公海数据所有人都有只读权限可以领取成为负责人（团队成员领取的）
            // TODO @puhui999：89 到 92 这块的逻辑，感觉可以不用 @CrmPermission，公海那自己 check 即可；
            if (CollUtil.isEmpty(bizPermissions) || CollUtil.anyMatch(bizPermissions,
                    item -> ObjUtil.equal(item.getUserId(), CrmPermissionDO.POOL_USER_ID))) { // 详见 CrmPermissionDO.POOL_USER_ID 注释
                return;
            }
            if (CrmPermissionLevelEnum.isRead(userPermission.getLevel())) { // 校验当前用户是否有读权限
                return;
            }
            // 如果查询数据的话拥有写权限的也能查询
            if (CrmPermissionLevelEnum.isWrite(userPermission.getLevel())) { // 校验当前用户是否有写权限
                return;
            }
        }
        // 2.3 情况三：校验自己是否有写权限
        if (CrmPermissionLevelEnum.isWrite(permissionLevel)) {
            if (CrmPermissionLevelEnum.isWrite(userPermission.getLevel())) { // 校验当前用户是否有写权限
                return;
            }
        }

        // 打个 info 日志，方便后续排查问题、审计；
        log.info("[doBefore][crmPermission({}) 数据校验错误]", toJsonString(userPermission));
        throw exception(CRM_PERMISSION_DENIED, crmPermission.bizType().getName());
    }


    // TODO @puhui999：这块看看能不能用 SpringExpressionUtils 工具类；
    private KeyValue<Long, Integer> getBizIdAndBizType(JoinPoint joinPoint, CrmPermission crmPermission) throws NoSuchMethodException {
        Method method = getMethod(joinPoint);
        // 1. 获取方法的参数值
        Object[] args = joinPoint.getArgs();
        EvaluationContext context = bindParam(method, args);

        // 2. 根据spel表达式获取值
        KeyValue<Long, Integer> keyValue = new KeyValue<>();
        // 2.1 获取模块数据编号
        Expression expression = parser.parseExpression(crmPermission.bizId());
        keyValue.setKey(expression.getValue(context, Long.class));
        // 2.2 获取模块类型
        if (ObjUtil.equal(crmPermission.bizType().getType(), CrmBizTypeEnum.CRM_PERMISSION.getType())) {
            // 情况一：用于 CrmPermissionController 中数据权限校验
            Expression expression2 = parser.parseExpression(crmPermission.bizTypeValue());
            keyValue.setValue(expression2.getValue(context, Integer.class));
            return keyValue;
        }
        // 情况二：正常数据权限校验
        keyValue.setValue(crmPermission.bizType().getType());
        return keyValue;
    }

    /**
     * 获取当前执行的方法
     */
    private Method getMethod(JoinPoint joinPoint) throws NoSuchMethodException {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        return joinPoint.getTarget().getClass().getMethod(method.getName(), method.getParameterTypes());
    }

    /**
     * 将方法的参数名和参数值绑定
     *
     * @param method 方法，根据方法获取参数名
     * @param args   方法的参数值
     * @return 求值上下文
     */
    private EvaluationContext bindParam(Method method, Object[] args) {
        //获取方法的参数名
        String[] params = discoverer.getParameterNames(method);

        //将参数名与参数值对应起来
        EvaluationContext context = new StandardEvaluationContext();
        if (params != null) {
            for (int len = 0; len < params.length; len++) {
                context.setVariable(params[len], args[len]);
            }
        }
        return context;
    }

}
