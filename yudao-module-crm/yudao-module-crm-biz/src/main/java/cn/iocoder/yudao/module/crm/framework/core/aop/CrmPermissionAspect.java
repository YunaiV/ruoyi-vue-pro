package cn.iocoder.yudao.module.crm.framework.core.aop;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import cn.iocoder.yudao.module.crm.dal.dataobject.permission.CrmPermissionDO;
import cn.iocoder.yudao.module.crm.framework.core.annotations.CrmPermission;
import cn.iocoder.yudao.module.crm.framework.enums.OperationTypeEnum;
import cn.iocoder.yudao.module.crm.service.permission.CrmPermissionService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

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

    /**
     * 获得用户编号
     *
     * @return 用户编号
     */
    private static Long getUserId() {
        return WebFrameworkUtils.getLoginUserId();
    }

    @Before("@annotation(crmPermission)")
    public void doBefore(JoinPoint joinPoint, CrmPermission crmPermission) {
        try {
            Integer crmType = crmPermission.crmType().getType();
            Integer operationType = crmPermission.operationType().getType();
            // TODO @puhui999：不一定是 id 参数噢；例如说，ContactServiceImpl 的 updateContact
            Long id = (Long) joinPoint.getArgs()[0];// 获取操作数据的编号

            // 1.1 获取数据权限
            CrmPermissionDO permission = crmPermissionService.getCrmPermissionByCrmTypeAndCrmDataId(crmType, id);
            if (permission == null) {
                // 不存在说明数据也不存在
                throw exception(CRM_PERMISSION_MODEL_NOT_EXISTS, crmPermission.crmType().getName());
            }
            // 1.2 校验是否为公海数据
            // TODO @puhui999：这个判断去掉比较合适哈。这里更多是业务逻辑，不算权限判断。例如说，公海的客户，只要没负责人，就可以领取了；
            if (permission.getOwnerUserId() == null) {
                return;
            }
            // 1.3 校验当前负责人是不是自己
            if (ObjUtil.equal(permission.getOwnerUserId(), getUserId())) {
                return;
            }
            // 1.4 TODO 校验是否为超级管理员

            // 2. 校验是否有读权限
            if (OperationTypeEnum.isRead(operationType)) {
                // 校验该数据当前用户是否可读
                // TODO @puhui999：直接 CollUtil.contains 就好，因为就是有某个 userId 呀
                boolean isRead = CollUtil.contains(permission.getRoUserIds(), item -> ObjUtil.equal(item, getUserId()))
                        || CollUtil.contains(permission.getRwUserIds(), item -> ObjUtil.equal(item, getUserId()));
                if (isRead) {
                    return;
                }
            }

            // 3. 校验是否有编辑权限
            if (OperationTypeEnum.isEdit(operationType)) {
                // 校验该数据当前用户是否可读写
                if (CollUtil.contains(permission.getRwUserIds(), item -> ObjUtil.equal(item, getUserId()))) {
                    return;
                }
            }

            // 4. 没通过结束，报错 {}操作失败，原因：没有权限
            throw exception(CRM_PERMISSION_DENIED, crmPermission.crmType().getName());
        } catch (Exception ex) {
            log.error("[doBefore][crmPermission({}) 数据校验错误]", toJsonString(crmPermission), ex);
        }
    }

}
