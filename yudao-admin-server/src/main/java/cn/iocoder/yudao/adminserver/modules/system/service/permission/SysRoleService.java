package cn.iocoder.yudao.adminserver.modules.system.service.permission;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.adminserver.modules.system.controller.permission.vo.role.SysRoleCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.permission.vo.role.SysRoleExportReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.permission.vo.role.SysRolePageReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.permission.vo.role.SysRoleUpdateReqVO;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.permission.SysRoleDO;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 角色 Service 接口
 *
 * @author 芋道源码
 */
public interface SysRoleService {

    /**
     * 初始化角色的本地缓存
     */
    void initLocalCache();

    /**
     * 创建角色
     *
     * @param reqVO 创建角色信息
     * @return 角色编号
     */
    Long createRole(SysRoleCreateReqVO reqVO);

    /**
     * 更新角色
     *
     * @param reqVO 更新角色信息
     */
    void updateRole(SysRoleUpdateReqVO reqVO);

    /**
     * 删除角色
     *
     * @param id 角色编号
     */
    void deleteRole(Long id);

    /**
     * 更新角色状态
     *
     * @param id 角色编号
     * @param status 状态
     */
    void updateRoleStatus(Long id, Integer status);

    /**
     * 设置角色的数据权限
     *
     * @param id 角色编号
     * @param dataScope 数据范围
     * @param dataScopeDeptIds 部门编号数组
     */
    void updateRoleDataScope(Long id, Integer dataScope, Set<Long> dataScopeDeptIds);

    /**
     * 获得角色，从缓存中
     *
     * @param id 角色编号
     * @return 角色
     */
    SysRoleDO getRoleFromCache(Long id);

    /**
     * 获得角色列表
     *
     * @param statuses 筛选的状态。允许空，空时不筛选
     * @return 角色列表
     */
    List<SysRoleDO> getRoles(@Nullable Collection<Integer> statuses);

    /**
     * 获得角色数组，从缓存中
     *
     * @param ids 角色编号数组
     * @return 角色数组
     */
    List<SysRoleDO> getRolesFromCache(Collection<Long> ids);

    /**
     * 判断角色数组中，是否有管理员
     *
     * @param roleList 角色数组
     * @return 是否有管理员
     */
    boolean hasAnyAdmin(Collection<SysRoleDO> roleList);

    /**
     * 判断角色编号数组中，是否有管理员
     *
     * @param ids 角色编号数组
     * @return 是否有管理员
     */
    default boolean hasAnyAdmin(Set<Long> ids) {
        return hasAnyAdmin(getRolesFromCache(ids));
    }

    /**
     * 获得角色
     *
     * @param id 角色编号
     * @return 角色
     */
    SysRoleDO getRole(Long id);

    /**
     * 获得角色分页
     *
     * @param reqVO 角色分页查询
     * @return 角色分页结果
     */
    PageResult<SysRoleDO> getRolePage(SysRolePageReqVO reqVO);

    /**
     * 获得角色列表
     *
     * @param reqVO 列表查询
     * @return 角色列表
     */
    List<SysRoleDO> getRoleList(SysRoleExportReqVO reqVO);



}
