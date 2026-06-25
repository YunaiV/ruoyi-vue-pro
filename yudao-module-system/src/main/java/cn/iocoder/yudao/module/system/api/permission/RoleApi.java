package cn.iocoder.yudao.module.system.api.permission;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.system.api.permission.dto.RoleRespDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 角色 API 接口
 *
 * @author 芋道源码
 */
public interface RoleApi {

    /**
     * 校验角色们是否有效。如下情况，视为无效：
     * 1. 角色编号不存在
     * 2. 角色被禁用
     *
     * @param ids 角色编号数组
     */
    void validRoleList(Collection<Long> ids);

    /**
     * 获得角色信息
     *
     * @param id 角色编号
     * @return 角色信息
     */
    RoleRespDTO getRole(Long id);

    /**
     * 获得角色信息数组
     *
     * @param ids 角色编号数组
     * @return 角色信息数组
     */
    List<RoleRespDTO> getRoleList(Collection<Long> ids);

    /**
     * 获得指定编号的角色 Map
     *
     * @param ids 角色编号数组
     * @return 角色 Map
     */
    default Map<Long, RoleRespDTO> getRoleMap(Collection<Long> ids) {
        List<RoleRespDTO> list = getRoleList(ids);
        return CollectionUtils.convertMap(list, RoleRespDTO::getId);
    }

}
