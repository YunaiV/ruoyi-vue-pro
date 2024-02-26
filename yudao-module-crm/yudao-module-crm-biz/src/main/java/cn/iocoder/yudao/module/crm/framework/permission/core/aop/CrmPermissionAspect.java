package cn.iocoder.yudao.module.crm.framework.permission.core.aop;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.spring.SpringExpressionUtils;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import cn.iocoder.yudao.module.crm.dal.dataobject.permission.CrmPermissionDO;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.enums.permission.CrmPermissionLevelEnum;
import cn.iocoder.yudao.module.crm.framework.permission.core.annotations.CrmPermission;
import cn.iocoder.yudao.module.crm.util.CrmPermissionUtils;
import cn.iocoder.yudao.module.crm.service.permission.CrmPermissionService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
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
    private CrmPermissionService crmPermissionService;

    @Before("@annotation(crmPermission)")
    public void doBefore(JoinPoint joinPoint, CrmPermission crmPermission) {
        // 1.1 获取相关属性值
        Map<String, Object> expressionValues = parseExpressions(joinPoint, crmPermission);
        Integer bizType = StrUtil.isEmpty(crmPermission.bizTypeValue()) ?
                crmPermission.bizType()[0].getType() : (Integer) expressionValues.get(crmPermission.bizTypeValue()); // 模块类型
        // 1.2 处理兼容多个 bizId 的情况
        Object object = expressionValues.get(crmPermission.bizId()); // 模块数据编号
        Set<Long> bizIds = new HashSet<>();
        if (object instanceof Collection<?>) {
            bizIds.addAll(convertSet((Collection<?>) object, item -> Long.parseLong(item.toString())));
        } else {
            bizIds.add(Long.parseLong(object.toString()));
        }
        Integer permissionLevel = crmPermission.level().getLevel(); // 需要的权限级别

        // 2. 逐个校验权限
        List<CrmPermissionDO> permissionList = crmPermissionService.getPermissionListByBiz(bizType, bizIds);
        Map<Long, List<CrmPermissionDO>> multiMap = convertMultiMap(permissionList, CrmPermissionDO::getBizId);
        bizIds.forEach(bizId -> validatePermission(bizType, multiMap.get(bizId), permissionLevel));
    }

    private void validatePermission(Integer bizType, List<CrmPermissionDO> bizPermissions, Integer permissionLevel) {
        // 1. 如果是超级管理员则直接通过
        if (CrmPermissionUtils.isCrmAdmin()) {
            return;
        }
        // 1.1 没有数据权限的情况
        if (CollUtil.isEmpty(bizPermissions)) {
            // 公海数据如果没有团队成员大家也因该有读权限才对
            if (CrmPermissionLevelEnum.isRead(permissionLevel)) {
                return;
            }
            // 没有数据权限的情况下超出了读权限直接报错，避免后面校验空指针
            throw exception(CRM_PERMISSION_DENIED, CrmBizTypeEnum.getNameByType(bizType));
        } else { // 1.2 有数据权限但是没有负责人的情况
            if (!anyMatch(bizPermissions, item -> CrmPermissionLevelEnum.isOwner(item.getLevel()))) {
                if (CrmPermissionLevelEnum.isRead(permissionLevel)) {
                    return;
                }
            }
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
        // 2.4 没有权限，抛出异常
        log.info("[doBefore][userId({}) 要求权限({}) 实际权限({}) 数据校验错误]", // 打个 info 日志，方便后续排查问题、审计
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
