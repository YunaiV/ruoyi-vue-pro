package cn.iocoder.yudao.module.system.service.tenant;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import cn.iocoder.yudao.framework.tenant.config.TenantProperties;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.system.controller.admin.permission.vo.role.RoleCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.tenant.vo.tenant.TenantCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.tenant.vo.tenant.TenantExportReqVO;
import cn.iocoder.yudao.module.system.controller.admin.tenant.vo.tenant.TenantPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.tenant.vo.tenant.TenantUpdateReqVO;
import cn.iocoder.yudao.module.system.convert.tenant.TenantConvert;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.MenuDO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.RoleDO;
import cn.iocoder.yudao.module.system.dal.dataobject.tenant.TenantDO;
import cn.iocoder.yudao.module.system.dal.dataobject.tenant.TenantPackageDO;
import cn.iocoder.yudao.module.system.dal.mysql.tenant.TenantMapper;
import cn.iocoder.yudao.module.system.enums.permission.RoleCodeEnum;
import cn.iocoder.yudao.module.system.enums.permission.RoleTypeEnum;
import cn.iocoder.yudao.module.system.mq.producer.tenant.TenantProducer;
import cn.iocoder.yudao.module.system.service.permission.MenuService;
import cn.iocoder.yudao.module.system.service.permission.PermissionService;
import cn.iocoder.yudao.module.system.service.permission.RoleService;
import cn.iocoder.yudao.module.system.service.tenant.handler.TenantInfoHandler;
import cn.iocoder.yudao.module.system.service.tenant.handler.TenantMenuHandler;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertImmutableMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.getMaxValue;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.*;
import static java.util.Collections.singleton;

/**
 * 租户 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class TenantServiceImpl implements TenantService {

    /**
     * 定时执行 {@link #schedulePeriodicRefresh()} 的周期
     * 因为已经通过 Redis Pub/Sub 机制，所以频率不需要高
     */
    private static final long SCHEDULER_PERIOD = 5 * 60 * 1000L;

    /**
     * 角色缓存
     * key：角色编号 {@link RoleDO#getId()}
     *
     * 这里声明 volatile 修饰的原因是，每次刷新时，直接修改指向
     */
    @Getter
    private volatile Map<Long, TenantDO> tenantCache;
    /**
     * 缓存角色的最大更新时间，用于后续的增量轮询，判断是否有更新
     */
    @Getter
    private volatile Date maxUpdateTime;

    @Resource
    private TenantProperties tenantProperties;

    @Resource
    private TenantMapper tenantMapper;

    @Resource
    private TenantPackageService tenantPackageService;
    @Resource
    @Lazy // 延迟，避免循环依赖报错
    private AdminUserService userService;
    @Resource
    private RoleService roleService;
    @Resource
    private MenuService menuService;
    @Resource
    private PermissionService permissionService;

    @Resource
    private TenantProducer tenantProducer;

    /**
     * 初始化 {@link #tenantCache} 缓存
     */
    @Override
    @PostConstruct
    public void initLocalCache() {
        // 获取租户列表，如果有更新
        List<TenantDO> tenantList = loadTenantIfUpdate(maxUpdateTime);
        if (CollUtil.isEmpty(tenantList)) {
            return;
        }

        // 写入缓存
        tenantCache = convertImmutableMap(tenantList, TenantDO::getId);
        maxUpdateTime = getMaxValue(tenantList, TenantDO::getUpdateTime);
        log.info("[initLocalCache][初始化 Tenant 数量为 {}]", tenantList.size());
    }

    @Scheduled(fixedDelay = SCHEDULER_PERIOD, initialDelay = SCHEDULER_PERIOD)
    public void schedulePeriodicRefresh() {
        initLocalCache();
    }

    /**
     * 如果租户发生变化，从数据库中获取最新的全量租户。
     * 如果未发生变化，则返回空
     *
     * @param maxUpdateTime 当前租户的最大更新时间
     * @return 租户列表
     */
    private List<TenantDO> loadTenantIfUpdate(Date maxUpdateTime) {
        // 第一步，判断是否要更新。
        if (maxUpdateTime == null) { // 如果更新时间为空，说明 DB 一定有新数据
            log.info("[loadTenantIfUpdate][首次加载全量租户]");
        } else { // 判断数据库中是否有更新的租户
            if (tenantMapper.selectExistsByUpdateTimeAfter(maxUpdateTime) == null) {
                return null;
            }
            log.info("[loadTenantIfUpdate][增量加载全量租户]");
        }
        // 第二步，如果有更新，则从数据库加载所有租户
        return tenantMapper.selectList();
    }

    @Override
    public List<Long> getTenantIds() {
        return new ArrayList<>(tenantCache.keySet());
    }

    @Override
    public void validTenant(Long id) {
        TenantDO tenant = tenantCache.get(id);
        if (tenant == null) {
            throw exception(TENANT_NOT_EXISTS);
        }
        if (tenant.getStatus().equals(CommonStatusEnum.DISABLE.getStatus())) {
            throw exception(TENANT_DISABLE, tenant.getName());
        }
        if (DateUtils.isExpired(tenant.getExpireTime())) {
            throw exception(TENANT_EXPIRE, tenant.getName());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTenant(TenantCreateReqVO createReqVO) {
        // 校验套餐被禁用
        TenantPackageDO tenantPackage = tenantPackageService.validTenantPackage(createReqVO.getPackageId());

        // 创建租户
        TenantDO tenant = TenantConvert.INSTANCE.convert(createReqVO);
        tenantMapper.insert(tenant);

        TenantUtils.execute(tenant.getId(), () -> {
            // 创建角色
            Long roleId = createRole(tenantPackage);
            // 创建用户，并分配角色
            Long userId = createUser(roleId, createReqVO);
            // 修改租户的管理员
            tenantMapper.updateById(new TenantDO().setId(tenant.getId()).setContactUserId(userId));
        });
        // 发送刷新消息
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                tenantProducer.sendTenantRefreshMessage();
            }
        });
        return tenant.getId();
    }

    private Long createUser(Long roleId, TenantCreateReqVO createReqVO) {
        // 创建用户
        Long userId = userService.createUser(TenantConvert.INSTANCE.convert02(createReqVO));
        // 分配角色
        permissionService.assignUserRole(userId, singleton(roleId));
        return userId;
    }

    private Long createRole(TenantPackageDO tenantPackage) {
        // 创建角色
        RoleCreateReqVO reqVO = new RoleCreateReqVO();
        reqVO.setName(RoleCodeEnum.TENANT_ADMIN.getName()).setCode(RoleCodeEnum.TENANT_ADMIN.getCode())
                .setSort(0).setRemark("系统自动生成");
        Long roleId = roleService.createRole(reqVO, RoleTypeEnum.SYSTEM.getType());
        // 分配权限
        permissionService.assignRoleMenu(roleId, tenantPackage.getMenuIds());
        return roleId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTenant(TenantUpdateReqVO updateReqVO) {
        // 校验存在
        TenantDO tenant = checkUpdateTenant(updateReqVO.getId());
        // 校验套餐被禁用
        TenantPackageDO tenantPackage = tenantPackageService.validTenantPackage(updateReqVO.getPackageId());

        // 更新租户
        TenantDO updateObj = TenantConvert.INSTANCE.convert(updateReqVO);
        tenantMapper.updateById(updateObj);
        // 如果套餐发生变化，则修改其角色的权限
        if (ObjectUtil.notEqual(tenant.getPackageId(), updateReqVO.getPackageId())) {
            updateTenantRoleMenu(tenant.getId(), tenantPackage.getMenuIds());
        }
        // 发送刷新消息
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                tenantProducer.sendTenantRefreshMessage();
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTenantRoleMenu(Long tenantId, Set<Long> menuIds) {
        TenantUtils.execute(tenantId, () -> {
            // 获得所有角色
            List<RoleDO> roles = roleService.getRoles(null);
            roles.forEach(role -> Assert.isTrue(tenantId.equals(role.getTenantId()), "角色({}/{}) 租户不匹配",
                    role.getId(), role.getTenantId(), tenantId)); // 兜底校验
            // 重新分配每个角色的权限
            roles.forEach(role -> {
                // 如果是租户管理员，重新分配其权限为租户套餐的权限
                if (Objects.equals(role.getCode(), RoleCodeEnum.TENANT_ADMIN.getCode())) {
                    permissionService.assignRoleMenu(role.getId(), menuIds);
                    log.info("[updateTenantRoleMenu][租户管理员({}/{}) 的权限修改为({})]", role.getId(), role.getTenantId(), menuIds);
                    return;
                }
                // 如果是其他角色，则去掉超过套餐的权限
                Set<Long> roleMenuIds = permissionService.getRoleMenuIds(role.getId());
                roleMenuIds = CollUtil.intersectionDistinct(roleMenuIds, menuIds);
                permissionService.assignRoleMenu(role.getId(), roleMenuIds);
                log.info("[updateTenantRoleMenu][角色({}/{}) 的权限修改为({})]", role.getId(), role.getTenantId(), roleMenuIds);
            });
        });
    }

    @Override
    public void deleteTenant(Long id) {
        // 校验存在
        checkUpdateTenant(id);
        // 删除
        tenantMapper.deleteById(id);
    }

    private TenantDO checkUpdateTenant(Long id) {
        TenantDO tenant = tenantMapper.selectById(id);
        if (tenant == null) {
            throw exception(TENANT_NOT_EXISTS);
        }
        // 内置租户，不允许删除
        if (isSystemTenant(tenant)) {
            throw exception(TENANT_CAN_NOT_UPDATE_SYSTEM);
        }
        return tenant;
    }

    @Override
    public TenantDO getTenant(Long id) {
        return tenantMapper.selectById(id);
    }

    @Override
    public PageResult<TenantDO> getTenantPage(TenantPageReqVO pageReqVO) {
        return tenantMapper.selectPage(pageReqVO);
    }

    @Override
    public List<TenantDO> getTenantList(TenantExportReqVO exportReqVO) {
        return tenantMapper.selectList(exportReqVO);
    }

    @Override
    public TenantDO getTenantByName(String name) {
        return tenantMapper.selectByName(name);
    }

    @Override
    public Integer getTenantCountByPackageId(Long packageId) {
        return tenantMapper.selectCountByPackageId(packageId);
    }

    @Override
    public List<TenantDO> getTenantListByPackageId(Long packageId) {
        return tenantMapper.selectListByPackageId(packageId);
    }

    @Override
    public void handleTenantInfo(TenantInfoHandler handler) {
        // 如果禁用，则不执行逻辑
        if (Boolean.FALSE.equals(tenantProperties.getEnable())) {
            return;
        }
        // 获得租户
        TenantDO tenant = getTenant(TenantContextHolder.getRequiredTenantId());
        // 执行处理器
        handler.handle(tenant);
    }

    @Override
    public void handleTenantMenu(TenantMenuHandler handler) {
        // 如果禁用，则不执行逻辑
        if (Boolean.FALSE.equals(tenantProperties.getEnable())) {
            return;
        }
        // 获得租户，然后获得菜单
        TenantDO tenant = getTenant(TenantContextHolder.getRequiredTenantId());
        Set<Long> menuIds;
        if (isSystemTenant(tenant)) { // 系统租户，菜单是全量的
            menuIds = CollectionUtils.convertSet(menuService.getMenus(), MenuDO::getId);
        } else {
            menuIds = tenantPackageService.getTenantPackage(tenant.getPackageId()).getMenuIds();
        }
        // 执行处理器
        handler.handle(menuIds);
    }

    private static boolean isSystemTenant(TenantDO tenant) {
        return Objects.equals(tenant.getPackageId(), TenantDO.PACKAGE_ID_SYSTEM);
    }

}
