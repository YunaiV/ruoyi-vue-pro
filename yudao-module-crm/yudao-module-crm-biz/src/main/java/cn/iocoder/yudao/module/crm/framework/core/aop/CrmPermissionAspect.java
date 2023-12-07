package cn.iocoder.yudao.module.crm.framework.core.aop;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.spring.SpringExpressionUtils;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import cn.iocoder.yudao.module.crm.dal.dataobject.permission.CrmPermissionDO;
import cn.iocoder.yudao.module.crm.framework.core.annotations.CrmPermission;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.enums.permission.CrmPermissionLevelEnum;
import cn.iocoder.yudao.module.crm.service.permission.CrmPermissionService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.toJsonString;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.CRM_PERMISSION_DENIED;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.CRM_PERMISSION_MODEL_NOT_EXISTS;

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
    private CrmPermissionService crmPermissionService;

    @Before("@annotation(crmPermission)")
    public void doBefore(JoinPoint joinPoint, CrmPermission crmPermission) {
        // TODO 芋艿：临时，方便大家调试
        //if (true) {
        //    return;
        //}
        // 获取相关属性值
        Map<String, Object> expressionValues = parseExpressions(joinPoint, crmPermission);
        Integer bizType = StrUtil.isEmpty(crmPermission.bizTypeValue()) ?
                crmPermission.bizType()[0].getType() : (Integer) expressionValues.get(crmPermission.bizTypeValue()); // 模块类型
        Long bizId = (Long) expressionValues.get(crmPermission.bizId()); // 模块数据编号
        Integer permissionLevel = crmPermission.level().getLevel(); // 需要的权限级别

        // TODO 如果是超级管理员则直接通过
        //if (superAdmin){
        //    return;
        //}

        // 1. 获取数据权限
        List<CrmPermissionDO> bizPermissions = crmPermissionService.getPermissionListByBiz(bizType, bizId);
        if (CollUtil.isEmpty(bizPermissions)) { // 数据权限不存存那么数据也不存在
            throw exception(CRM_PERMISSION_MODEL_NOT_EXISTS, CrmBizTypeEnum.getNameByType(bizType));
        }
        // 2.1 情况一：如果自己是负责人，则默认有所有权限
        CrmPermissionDO userPermission = CollUtil.findOne(bizPermissions, permission -> ObjUtil.equal(permission.getUserId(), getUserId()));
        if (userPermission != null) {
            if (CrmPermissionLevelEnum.isOwner(userPermission.getLevel())) {
                return;
            }
            // 2.2 情况二：校验自己是否有读权限
            if (CrmPermissionLevelEnum.isRead(permissionLevel)) {
                if (CrmPermissionLevelEnum.isRead(userPermission.getLevel()) // 校验当前用户是否有读权限
                        || CrmPermissionLevelEnum.isWrite(userPermission.getLevel())) { // 校验当前用户是否有写权限
                    return;
                }
            }
            // 2.3 情况三：校验自己是否有写权限
            if (CrmPermissionLevelEnum.isWrite(permissionLevel)) {
                if (CrmPermissionLevelEnum.isWrite(userPermission.getLevel())) { // 校验当前用户是否有写权限
                    return;
                }
            }
        }
        // 2.4 没有权限！
        // 打个 info 日志，方便后续排查问题、审计
        log.info("[doBefore][userId({}) 要求权限({}) 实际权限({}) 数据校验错误]",
                getUserId(), permissionLevel, toJsonString(userPermission));
        throw exception(CRM_PERMISSION_DENIED, CrmBizTypeEnum.getNameByType(bizType));
    }

    /**
     * 获得用户编号
     *
     * @return 用户编号
     */
    private static Long getUserId() {
        return WebFrameworkUtils.getLoginUserId();
    }

    private static Map<String, Object> parseExpressions(JoinPoint joinPoint, CrmPermission crmPermission) {
        // 1. 需要解析的表达式
        List<String> expressionStrings = new ArrayList<>(2);
        expressionStrings.add(crmPermission.bizId());
        if (StrUtil.isNotEmpty(crmPermission.bizTypeValue())) { // 为空则表示 bizType 有值
            expressionStrings.add(crmPermission.bizTypeValue());
        }
        // 2. 执行解析
        return SpringExpressionUtils.parseExpressions(joinPoint, expressionStrings);
    }

}
