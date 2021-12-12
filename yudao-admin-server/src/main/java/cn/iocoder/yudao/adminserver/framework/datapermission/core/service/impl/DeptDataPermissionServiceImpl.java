package cn.iocoder.yudao.adminserver.framework.datapermission.core.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.adminserver.framework.datapermission.core.service.DeptDataPermissionService;
import cn.iocoder.yudao.adminserver.framework.datapermission.core.service.dto.DeptDataPermissionRespDTO;
import cn.iocoder.yudao.adminserver.modules.system.dal.dataobject.dept.SysDeptDO;
import cn.iocoder.yudao.adminserver.modules.system.dal.dataobject.permission.SysRoleDO;
import cn.iocoder.yudao.adminserver.modules.system.service.dept.SysDeptService;
import cn.iocoder.yudao.adminserver.modules.system.service.permission.SysRoleService;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.enums.DataScopeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;

/**
 * 基于部门的数据权限 Service 实现类
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Slf4j
public class DeptDataPermissionServiceImpl implements DeptDataPermissionService {

    /**
     * LoginUser 的 Context 缓存 Key
     */
    private static final String CONTEXT_KEY = DeptDataPermissionServiceImpl.class.getSimpleName();

    private final SysRoleService roleService;
    private final SysDeptService deptService;

    @Override
    public DeptDataPermissionRespDTO getDeptDataPermission(LoginUser loginUser) {
        // 判断是否 context 已经缓存
        DeptDataPermissionRespDTO result = loginUser.getContext(CONTEXT_KEY, DeptDataPermissionRespDTO.class);
        if (result != null) {
            return result;
        }

        // 创建 DeptDataPermissionRespDTO 对象
        result = new DeptDataPermissionRespDTO();
        List<SysRoleDO> roles = roleService.getRolesFromCache(loginUser.getRoleIds());
        for (SysRoleDO role : roles) {
            // 为空时，跳过
            if (role.getDataScope() == null) {
                continue;
            }
            // 情况一，ALL
            if (Objects.equals(role.getDataScope(), DataScopeEnum.ALL.getScope())) {
                result.setAll(true);
                continue;
            }
            // 情况二，DEPT_CUSTOM
            if (Objects.equals(role.getDataScope(), DataScopeEnum.DEPT_CUSTOM.getScope())) {
                CollUtil.addAll(result.getDeptIds(), role.getDataScopeDeptIds());
                continue;
            }
            // 情况三，DEPT_ONLY
            if (Objects.equals(role.getDataScope(), DataScopeEnum.DEPT_ONLY.getScope())) {
                CollectionUtils.addIfNotNull(result.getDeptIds(), loginUser.getDeptId());
                continue;
            }
            // 情况四，DEPT_DEPT_AND_CHILD
            if (Objects.equals(role.getDataScope(), DataScopeEnum.DEPT_AND_CHILD.getScope())) {
                List<SysDeptDO> depts = deptService.getDeptsByParentIdFromCache(loginUser.getDeptId(), true);
                CollUtil.addAll(result.getDeptIds(), CollectionUtils.convertList(depts, SysDeptDO::getId));
                continue;
            }
            // 情况五，SELF
            if (Objects.equals(role.getDataScope(), DataScopeEnum.SELF.getScope())) {
                result.setSelf(true);
                continue;
            }
            // 未知情况，error log 即可
            log.error("[getDeptDataPermission][LoginUser({}) role({}) 无法处理]", loginUser.getId(), JsonUtils.toJsonString(result));
        }

        // 添加到缓存，并返回
        loginUser.setContext(CONTEXT_KEY, result);
        return null;
    }

}
