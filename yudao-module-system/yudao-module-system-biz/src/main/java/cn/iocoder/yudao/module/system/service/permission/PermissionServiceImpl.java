package cn.iocoder.yudao.module.system.service.permission;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.datapermission.core.dept.service.dto.DeptDataPermissionRespDTO;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.MenuDO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.RoleDO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.RoleMenuDO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.UserRoleDO;
import cn.iocoder.yudao.module.system.dal.mysql.permission.RoleMenuBatchInsertMapper;
import cn.iocoder.yudao.module.system.dal.mysql.permission.RoleMenuMapper;
import cn.iocoder.yudao.module.system.dal.mysql.permission.UserRoleBatchInsertMapper;
import cn.iocoder.yudao.module.system.dal.mysql.permission.UserRoleMapper;
import cn.iocoder.yudao.module.system.enums.permission.DataScopeEnum;
import cn.iocoder.yudao.module.system.mq.producer.permission.PermissionProducer;
import cn.iocoder.yudao.module.system.service.dept.DeptService;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;

/**
 * 权限 Service 实现类
 *
 * @author 芋道源码
 */
@Service("ss") // 使用 Spring Security 的缩写，方便食用
@Slf4j
public class PermissionServiceImpl implements PermissionService {

    /**
     * LoginUser 的 Context 缓存 Key
     */
    public static final String CONTEXT_KEY = PermissionServiceImpl.class.getSimpleName();

    /**
     * 定时执行 {@link #schedulePeriodicRefresh()} 的周期
     * 因为已经通过 Redis Pub/Sub 机制，所以频率不需要高
     */
    private static final long SCHEDULER_PERIOD = 5 * 60 * 1000L;

    /**
     * 角色编号与菜单编号的缓存映射
     * key：角色编号
     * value：菜单编号的数组
     *
     * 这里声明 volatile 修饰的原因是，每次刷新时，直接修改指向
     */
    private volatile Multimap<Long, Long> roleMenuCache;
    /**
     * 菜单编号与角色编号的缓存映射
     * key：菜单编号
     * value：角色编号的数组
     *
     * 这里声明 volatile 修饰的原因是，每次刷新时，直接修改指向
     */
    private volatile Multimap<Long, Long> menuRoleCache;
    /**
     * 缓存菜单的最大更新时间，用于后续的增量轮询，判断是否有更新
     */
    private volatile Date maxUpdateTime;

    @Resource
    private RoleMenuMapper roleMenuMapper;
    @Resource
    private RoleMenuBatchInsertMapper roleMenuBatchInsertMapper;
    @Resource
    private UserRoleMapper userRoleMapper;
    @Resource
    private UserRoleBatchInsertMapper userRoleBatchInsertMapper;

    @Resource
    private RoleService roleService;
    @Resource
    private MenuService menuService;
    @Resource
    private DeptService deptService;

    @Resource
    private PermissionProducer permissionProducer;

    @Resource
    @Lazy // 注入自己，所以延迟加载
    private PermissionService self;

    /**
     * 初始化 {@link #roleMenuCache} 和 {@link #menuRoleCache} 缓存
     */
    @Override
    @PostConstruct
    @TenantIgnore // 初始化缓存，无需租户过滤
    public void initLocalCache() {
        // 获取角色与菜单的关联列表，如果有更新
        List<RoleMenuDO> roleMenuList = loadRoleMenuIfUpdate(maxUpdateTime);
        if (CollUtil.isEmpty(roleMenuList)) {
            return;
        }

        // 初始化 roleMenuCache 和 menuRoleCache 缓存
        ImmutableMultimap.Builder<Long, Long> roleMenuCacheBuilder = ImmutableMultimap.builder();
        ImmutableMultimap.Builder<Long, Long> menuRoleCacheBuilder = ImmutableMultimap.builder();
        roleMenuList.forEach(roleMenuDO -> {
            roleMenuCacheBuilder.put(roleMenuDO.getRoleId(), roleMenuDO.getMenuId());
            menuRoleCacheBuilder.put(roleMenuDO.getMenuId(), roleMenuDO.getRoleId());
        });
        roleMenuCache = roleMenuCacheBuilder.build();
        menuRoleCache = menuRoleCacheBuilder.build();
        maxUpdateTime = CollectionUtils.getMaxValue(roleMenuList, RoleMenuDO::getUpdateTime);
        log.info("[initLocalCache][初始化角色与菜单的关联数量为 {}]", roleMenuList.size());
    }

    @Scheduled(fixedDelay = SCHEDULER_PERIOD, initialDelay = SCHEDULER_PERIOD)
    public void schedulePeriodicRefresh() {
        self.initLocalCache();
    }

    /**
     * 如果角色与菜单的关联发生变化，从数据库中获取最新的全量角色与菜单的关联。
     * 如果未发生变化，则返回空
     *
     * @param maxUpdateTime 当前角色与菜单的关联的最大更新时间
     * @return 角色与菜单的关联列表
     */
    protected List<RoleMenuDO> loadRoleMenuIfUpdate(Date maxUpdateTime) {
        // 第一步，判断是否要更新。
        if (maxUpdateTime == null) { // 如果更新时间为空，说明 DB 一定有新数据
            log.info("[loadRoleMenuIfUpdate][首次加载全量角色与菜单的关联]");
        } else { // 判断数据库中是否有更新的角色与菜单的关联
            if (Objects.isNull(roleMenuMapper.selectExistsByUpdateTimeAfter(maxUpdateTime))) {
                return null;
            }
            log.info("[loadRoleMenuIfUpdate][增量加载全量角色与菜单的关联]");
        }
        // 第二步，如果有更新，则从数据库加载所有角色与菜单的关联
        return roleMenuMapper.selectList();
    }

    @Override
    public List<MenuDO> getRoleMenuListFromCache(Collection<Long> roleIds, Collection<Integer> menuTypes,
                                                 Collection<Integer> menusStatuses) {
        // 任一一个参数为空时，不返回任何菜单
        if (CollectionUtils.isAnyEmpty(roleIds, menuTypes, menusStatuses)) {
            return Collections.emptyList();
        }

        // 判断角色是否包含超级管理员。如果是超级管理员，获取到全部
        List<RoleDO> roleList = roleService.getRolesFromCache(roleIds);
        if (roleService.hasAnySuperAdmin(roleList)) {
            return menuService.getMenuListFromCache(menuTypes, menusStatuses);
        }

        // 获得角色拥有的菜单关联
        List<Long> menuIds = MapUtils.getList(roleMenuCache, roleIds);
        return menuService.getMenuListFromCache(menuIds, menuTypes, menusStatuses);
    }

    @Override
    public Set<Long> getUserRoleIds(Long userId, Collection<Integer> roleStatuses) {
        List<UserRoleDO> userRoleList = userRoleMapper.selectListByUserId(userId);
        // 过滤角色状态
        if (CollectionUtil.isNotEmpty(roleStatuses)) {
            userRoleList.removeIf(userRoleDO -> {
                RoleDO role = roleService.getRoleFromCache(userRoleDO.getRoleId());
                return role == null || !roleStatuses.contains(role.getStatus());
            });
        }
        return CollectionUtils.convertSet(userRoleList, UserRoleDO::getRoleId);
    }

    @Override
    public Set<Long> getRoleMenuIds(Long roleId) {
        // 如果是管理员的情况下，获取全部菜单编号
        RoleDO role = roleService.getRole(roleId);
        if (roleService.hasAnySuperAdmin(Collections.singletonList(role))) {
            return CollectionUtils.convertSet(menuService.getMenus(), MenuDO::getId);
        }
        // 如果是非管理员的情况下，获得拥有的菜单编号
        return CollectionUtils.convertSet(roleMenuMapper.selectListByRoleId(roleId),
                RoleMenuDO::getMenuId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoleMenu(Long roleId, Set<Long> menuIds) {
        // 获得角色拥有菜单编号
        Set<Long> dbMenuIds = CollectionUtils.convertSet(roleMenuMapper.selectListByRoleId(roleId),
                RoleMenuDO::getMenuId);
        // 计算新增和删除的菜单编号
        Collection<Long> createMenuIds = CollUtil.subtract(menuIds, dbMenuIds);
        Collection<Long> deleteMenuIds = CollUtil.subtract(dbMenuIds, menuIds);
        // 执行新增和删除。对于已经授权的菜单，不用做任何处理
        if (!CollectionUtil.isEmpty(createMenuIds)) {
            roleMenuBatchInsertMapper.saveBatch(CollectionUtils.convertList(createMenuIds, menuId -> {
                RoleMenuDO entity = new RoleMenuDO();
                entity.setRoleId(roleId);
                entity.setMenuId(menuId);
                return entity;
            }));
        }
        if (!CollectionUtil.isEmpty(deleteMenuIds)) {
            roleMenuMapper.deleteListByRoleIdAndMenuIds(roleId, deleteMenuIds);
        }
        // 发送刷新消息. 注意，需要事务提交后，在进行发送刷新消息。不然 db 还未提交，结果缓存先刷新了
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

            @Override
            public void afterCommit() {
                permissionProducer.sendRoleMenuRefreshMessage();
            }

        });
    }

    @Override
    public Set<Long> getUserRoleIdListByUserId(Long userId) {
        return CollectionUtils.convertSet(userRoleMapper.selectListByUserId(userId),
                UserRoleDO::getRoleId);
    }

    @Override
    public Set<Long> getUserRoleIdListByRoleId(Long roleId) {
        return CollectionUtils.convertSet(userRoleMapper.selectListByRoleId(roleId),
                UserRoleDO::getRoleId);
    }

    @Override
    public void assignUserRole(Long userId, Set<Long> roleIds) {
        // 获得角色拥有角色编号
        Set<Long> dbRoleIds = CollectionUtils.convertSet(userRoleMapper.selectListByUserId(userId),
                UserRoleDO::getRoleId);
        // 计算新增和删除的角色编号
        Collection<Long> createRoleIds = CollUtil.subtract(roleIds, dbRoleIds);
        Collection<Long> deleteMenuIds = CollUtil.subtract(dbRoleIds, roleIds);
        // 执行新增和删除。对于已经授权的角色，不用做任何处理
        if (!CollectionUtil.isEmpty(createRoleIds)) {
            userRoleBatchInsertMapper.saveBatch(CollectionUtils.convertList(createRoleIds, roleId -> {
                UserRoleDO entity = new UserRoleDO();
                entity.setUserId(userId);
                entity.setRoleId(roleId);
                return entity;
            }));
        }
        if (!CollectionUtil.isEmpty(deleteMenuIds)) {
            userRoleMapper.deleteListByUserIdAndRoleIdIds(userId, deleteMenuIds);
        }
    }

    @Override
    public void assignRoleDataScope(Long roleId, Integer dataScope, Set<Long> dataScopeDeptIds) {
        roleService.updateRoleDataScope(roleId, dataScope, dataScopeDeptIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processRoleDeleted(Long roleId) {
        // 标记删除 UserRole
        userRoleMapper.deleteListByRoleId(roleId);
        // 标记删除 RoleMenu
        roleMenuMapper.deleteListByRoleId(roleId);
        // 发送刷新消息. 注意，需要事务提交后，在进行发送刷新消息。不然 db 还未提交，结果缓存先刷新了
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

            @Override
            public void afterCommit() {
                permissionProducer.sendRoleMenuRefreshMessage();
            }

        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processMenuDeleted(Long menuId) {
        roleMenuMapper.deleteListByMenuId(menuId);
        // 发送刷新消息. 注意，需要事务提交后，在进行发送刷新消息。不然 db 还未提交，结果缓存先刷新了
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

            @Override
            public void afterCommit() {
                permissionProducer.sendRoleMenuRefreshMessage();
            }

        });
    }

    @Override
    public void processUserDeleted(Long userId) {
        userRoleMapper.deleteListByUserId(userId);
    }

    @Override
    public boolean hasPermission(String permission) {
        return hasAnyPermissions(permission);
    }

    @Override
    public boolean hasAnyPermissions(String... permissions) {
        // 如果为空，说明已经有权限
        if (ArrayUtil.isEmpty(permissions)) {
            return true;
        }

        // 获得当前登录的角色。如果为空，说明没有权限
        Set<Long> roleIds = SecurityFrameworkUtils.getLoginUserRoleIds();
        if (CollUtil.isEmpty(roleIds)) {
            return false;
        }
        // 判断是否是超管。如果是，当然符合条件
        if (roleService.hasAnySuperAdmin(roleIds)) {
            return true;
        }

        // 遍历权限，判断是否有一个满足
        return Arrays.stream(permissions).anyMatch(permission -> {
            List<MenuDO> menuList = menuService.getMenuListByPermissionFromCache(permission);
            // 采用严格模式，如果权限找不到对应的 Menu 的话，认为
            if (CollUtil.isEmpty(menuList)) {
                return false;
            }
            // 获得是否拥有该权限，任一一个
            return menuList.stream().anyMatch(menu -> CollUtil.containsAny(roleIds,
                    menuRoleCache.get(menu.getId())));
        });
    }

    @Override
    public boolean hasRole(String role) {
        return hasAnyRoles(role);
    }

    @Override
    public boolean hasAnyRoles(String... roles) {
        // 如果为空，说明已经有权限
        if (ArrayUtil.isEmpty(roles)) {
            return true;
        }

        // 获得当前登录的角色。如果为空，说明没有权限
        Set<Long> roleIds = SecurityFrameworkUtils.getLoginUserRoleIds();
        if (CollUtil.isEmpty(roleIds)) {
            return false;
        }
        // 判断是否是超管。如果是，当然符合条件
        if (roleService.hasAnySuperAdmin(roleIds)) {
            return true;
        }
        Set<String> userRoles = CollectionUtils.convertSet(roleService.getRolesFromCache(roleIds),
                RoleDO::getCode);
        return CollUtil.containsAny(userRoles, Sets.newHashSet(roles));
    }

    @Override
    public DeptDataPermissionRespDTO getDeptDataPermission(LoginUser loginUser) {
        // 判断是否 context 已经缓存
        DeptDataPermissionRespDTO result = loginUser.getContext(CONTEXT_KEY, DeptDataPermissionRespDTO.class);
        if (result != null) {
            return result;
        }

        // 创建 DeptDataPermissionRespDTO 对象
        result = new DeptDataPermissionRespDTO();
        List<RoleDO> roles = roleService.getRolesFromCache(loginUser.getRoleIds());
        for (RoleDO role : roles) {
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
                // 自定义可见部门时，保证可以看到自己所在的部门。否则，一些场景下可能会有问题。
                // 例如说，登录时，基于 t_user 的 username 查询会可能被 dept_id 过滤掉
                CollUtil.addAll(result.getDeptIds(), loginUser.getDeptId());
                continue;
            }
            // 情况三，DEPT_ONLY
            if (Objects.equals(role.getDataScope(), DataScopeEnum.DEPT_ONLY.getScope())) {
                CollectionUtils.addIfNotNull(result.getDeptIds(), loginUser.getDeptId());
                continue;
            }
            // 情况四，DEPT_DEPT_AND_CHILD
            if (Objects.equals(role.getDataScope(), DataScopeEnum.DEPT_AND_CHILD.getScope())) {
                List<DeptDO> depts = deptService.getDeptsByParentIdFromCache(loginUser.getDeptId(), true);
                CollUtil.addAll(result.getDeptIds(), CollectionUtils.convertList(depts, DeptDO::getId));
                //添加本身部门id
                CollUtil.addAll(result.getDeptIds(), loginUser.getDeptId());
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
        return result;
    }

    @Override
    public Set<Long> getUserRoleIdListByRoleIds(Collection<Long> roleIds) {
        return CollectionUtils.convertSet(userRoleMapper.selectListByRoleIds(roleIds),
                UserRoleDO::getUserId);
    }

}
