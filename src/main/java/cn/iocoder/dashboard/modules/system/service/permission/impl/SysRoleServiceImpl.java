package cn.iocoder.dashboard.modules.system.service.permission.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.iocoder.dashboard.common.enums.CommonStatusEnum;
import cn.iocoder.dashboard.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.system.controller.permission.vo.role.SysRoleCreateReqVO;
import cn.iocoder.dashboard.modules.system.controller.permission.vo.role.SysRolePageReqVO;
import cn.iocoder.dashboard.modules.system.controller.permission.vo.role.SysRoleUpdateReqVO;
import cn.iocoder.dashboard.modules.system.convert.permission.SysRoleConvert;
import cn.iocoder.dashboard.modules.system.dal.mysql.dao.permission.SysRoleMapper;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.permission.SysRoleDO;
import cn.iocoder.dashboard.modules.system.enums.permission.RoleCodeEnum;
import cn.iocoder.dashboard.modules.system.enums.permission.RoleTypeEnum;
import cn.iocoder.dashboard.modules.system.service.permission.SysPermissionService;
import cn.iocoder.dashboard.modules.system.service.permission.SysRoleService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.dashboard.modules.system.enums.SysErrorCodeConstants.*;

/**
 * 角色 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class SysRoleServiceImpl implements SysRoleService {

    @Resource
    private SysPermissionService permissionService;

    @Resource
    private SysRoleMapper roleMapper;

    /**
     * 角色缓存
     * key：角色编号 {@link SysRoleDO#getId()}
     *
     * 这里声明 volatile 修饰的原因是，每次刷新时，直接修改指向
     */
    private volatile Map<Long, SysRoleDO> roleCache;

    /**
     * 初始化 {@link #roleCache} 缓存
     */
    @Override
    @PostConstruct
    public void init() {
        // 从数据库中读取
        List<SysRoleDO> roleDOList = roleMapper.selectList(null);
        // 写入缓存
        ImmutableMap.Builder<Long, SysRoleDO> builder = ImmutableMap.builder();
        roleDOList.forEach(sysRoleDO -> builder.put(sysRoleDO.getId(), sysRoleDO));
        roleCache = builder.build();
        log.info("[init][初始化 Role 数量为 {}]", roleDOList.size());
    }

    @Override
    public SysRoleDO getRoleFromCache(Long id) {
        return roleCache.get(id);
    }

    @Override
    public List<SysRoleDO> listRolesFromCache(Collection<Long> ids) {
        if (CollectionUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return roleCache.values().stream().filter(roleDO -> ids.contains(roleDO.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasAnyAdmin(Collection<SysRoleDO> roleList) {
        if (CollectionUtil.isEmpty(roleList)) {
            return false;
        }
        return roleList.stream().anyMatch(roleDO -> RoleCodeEnum.ADMIN.getKey().equals(roleDO.getCode()));
    }

    @Override
    public Long createRole(SysRoleCreateReqVO reqVO) {
        // 校验角色
        checkDuplicateRole(reqVO.getName(), reqVO.getCode(), null);
        // 插入到数据库
        SysRoleDO role = SysRoleConvert.INSTANCE.convert(reqVO);
        role.setType(RoleTypeEnum.CUSTOM.getType());
        role.setStatus(CommonStatusEnum.ENABLE.getStatus());
        roleMapper.insert(role);
        // 返回
        return role.getId();
    }

    @Override
    public void updateRole(SysRoleUpdateReqVO reqVO) {
        // 校验是否可以更新
        this.checkUpdateRole(reqVO.getId());
        // 校验角色的唯一字段是否重复
        checkDuplicateRole(reqVO.getName(), reqVO.getCode(), reqVO.getId());
        // 更新到数据库
        SysRoleDO updateObject = SysRoleConvert.INSTANCE.convert(reqVO);
        roleMapper.updateById(updateObject);
    }

    @Override
    public void deleteRole(Long id) {
        // 校验是否可以更新
        this.checkUpdateRole(id);
        // 标记删除
        roleMapper.deleteById(id);
        // 删除相关数据
        permissionService.processRoleDeleted(id);
    }

    @Override
    public SysRoleDO getRole(Long id) {
        return roleMapper.selectById(id);
    }


    @Override
    public PageResult<SysRoleDO> pageRole(SysRolePageReqVO reqVO) {
        IPage<SysRoleDO> roleDOPage = roleMapper.selectPage(reqVO);
        return SysRoleConvert.INSTANCE.convertPage(roleDOPage);
    }

    @Override
    public void updateRoleStatus(Long id, Integer status) {
        // 校验是否可以更新
        this.checkUpdateRole(id);
        // 更新状态
        SysRoleDO updateObject = new SysRoleDO();
        updateObject.setId(id);
        updateObject.setStatus(status);
        roleMapper.updateById(updateObject);
    }

    /**
     * 校验角色的唯一字段是否重复
     *
     * 1. 是否存在相同名字的角色
     * 2. 是否存在相同编码的角色
     *
     * @param name 角色名字
     * @param code 角色额编码
     * @param id 角色编号
     */
    private void checkDuplicateRole(String name, String code, Long id) {
        // 1. 该 name 名字被其它角色所使用
        SysRoleDO role = roleMapper.selectByName(name);
        if (role != null && !role.getId().equals(id)) {
            throw ServiceExceptionUtil.exception(ROLE_NAME_DUPLICATE, name);
        }
        // 2. 是否存在相同编码的角色
        if (!StringUtils.hasText(code)) {
            return;
        }
        // 该 code 编码被其它角色所使用
        role = roleMapper.selectByCode(code);
        if (role != null && !role.getId().equals(id)) {
            throw ServiceExceptionUtil.exception(ROLE_CODE_DUPLICATE, name);
        }
    }

    /**
     * 校验角色是否可以被更新
     *
     * @param id 角色编号
     */
    private void checkUpdateRole(Long id) {
        SysRoleDO roleDO = roleMapper.selectById(id);
        if (roleDO == null) {
            throw ServiceExceptionUtil.exception(ROLE_NOT_EXISTS);
        }
        // 内置角色，不允许删除
        if (RoleTypeEnum.SYSTEM.getType().equals(roleDO.getType())) {
            throw ServiceExceptionUtil.exception(ROLE_CAN_NOT_DELETE_SYSTEM_TYPE_ROLE);
        }
    }

}
