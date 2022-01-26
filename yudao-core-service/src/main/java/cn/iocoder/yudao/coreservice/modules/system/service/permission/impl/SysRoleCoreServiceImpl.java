package cn.iocoder.yudao.coreservice.modules.system.service.permission.impl;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.permission.SysRoleDO;
import cn.iocoder.yudao.coreservice.modules.system.dal.mysql.permission.SysRoleCoreMapper;
import cn.iocoder.yudao.coreservice.modules.system.service.permission.SysRoleCoreService;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.coreservice.modules.system.enums.SysErrorCodeConstants.ROLE_IS_DISABLE;
import static cn.iocoder.yudao.coreservice.modules.system.enums.SysErrorCodeConstants.ROLE_NOT_EXISTS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 角色 Core Service 实现类
 *
 * @author 芋道源码
 */
@Service
public class SysRoleCoreServiceImpl implements SysRoleCoreService {

    @Resource
    private SysRoleCoreMapper sysRoleCoreMapper;

    @Override
    public void validRoles(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 获得角色信息
        List<SysRoleDO> roles = sysRoleCoreMapper.selectBatchIds(ids);
        Map<Long, SysRoleDO> roleMap = CollectionUtils.convertMap(roles, SysRoleDO::getId);
        // 校验
        ids.forEach(id -> {
            SysRoleDO role = roleMap.get(id);
            if (role == null) {
                throw exception(ROLE_NOT_EXISTS);
            }
            if (!CommonStatusEnum.ENABLE.getStatus().equals(role.getStatus())) {
                throw exception(ROLE_IS_DISABLE, role.getName());
            }
        });
    }
}
