package cn.iocoder.yudao.module.crm.service.customer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.CrmCustomerLockReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.CrmCustomerPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.CrmCustomerSaveReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.CrmCustomerTransferReqVO;
import cn.iocoder.yudao.module.crm.convert.customer.CrmCustomerConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerLimitConfigDO;
import cn.iocoder.yudao.module.crm.dal.mysql.customer.CrmCustomerMapper;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.enums.permission.CrmPermissionLevelEnum;
import cn.iocoder.yudao.module.crm.framework.permission.core.annotations.CrmPermission;
import cn.iocoder.yudao.module.crm.service.permission.CrmPermissionService;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionCreateReqBO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.impl.DiffParseFunction;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.crm.enums.LogRecordConstants.CRM_CUSTOMER;
import static cn.iocoder.yudao.module.crm.enums.LogRecordConstants.TRANSFER_CUSTOMER_LOG_SUCCESS;
import static cn.iocoder.yudao.module.crm.enums.customer.CrmCustomerLimitConfigTypeEnum.CUSTOMER_LOCK_LIMIT;
import static cn.iocoder.yudao.module.crm.enums.customer.CrmCustomerLimitConfigTypeEnum.CUSTOMER_OWNER_LIMIT;
import static java.util.Collections.singletonList;

/**
 * 客户 Service 实现类
 *
 * @author Wanwan
 */
@Service
@Validated
public class CrmCustomerServiceImpl implements CrmCustomerService {

    @Resource
    private CrmCustomerMapper customerMapper;

    @Resource
    private CrmPermissionService permissionService;
    @Resource
    private CrmCustomerLimitConfigService customerLimitConfigService;

    @Resource
    private AdminUserApi adminUserApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_CUSTOMER, subType = "创建客户", bizNo = "{{#customerId}}", success = "创建了客户")
    // TODO @puhui999：创建了客户【客户名】，要记录进去；不然在展示操作日志的全列表，看不清楚是哪个客户哈；
    public Long createCustomer(CrmCustomerSaveReqVO createReqVO, Long userId) {
        createReqVO.setId(null);
        // 1. 校验拥有客户是否到达上限
        validateCustomerExceedOwnerLimit(createReqVO.getOwnerUserId(), 1);

        // 2. 插入客户
        CrmCustomerDO customer = CrmCustomerConvert.INSTANCE.convert(createReqVO)
                .setLockStatus(false).setDealStatus(false)
                .setContactLastTime(LocalDateTime.now());
        // TODO @puhui999：可能要加个 receiveTime 字段，记录最后接收时间
        customerMapper.insert(customer);

        // 3. 创建数据权限
        permissionService.createPermission(new CrmPermissionCreateReqBO().setBizType(CrmBizTypeEnum.CRM_CUSTOMER.getType())
                .setBizId(customer.getId()).setUserId(userId).setLevel(CrmPermissionLevelEnum.OWNER.getLevel())); // 设置当前操作的人为负责人

        // 4. 记录操作日志
        LogRecordContext.putVariable("customerId", customer.getId());
        return customer.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_CUSTOMER, subType = "更新客户", bizNo = "{{#updateReqVO.id}}", success = "更新了客户{_DIFF{#updateReqVO}}", extra = "{{#extra}}")
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CUSTOMER, bizId = "#updateReqVO.id", level = CrmPermissionLevelEnum.WRITE)
    public void updateCustomer(CrmCustomerSaveReqVO updateReqVO) {
        Assert.notNull(updateReqVO.getId(), "客户编号不能为空");
        // 更新的时候，要把 updateReqVO 负责人设置为空，避免修改。
        updateReqVO.setOwnerUserId(null);
        // 1. 校验存在
        CrmCustomerDO oldCustomer = validateCustomerExists(updateReqVO.getId());

        // 2. 更新客户
        CrmCustomerDO updateObj = CrmCustomerConvert.INSTANCE.convert(updateReqVO);
        customerMapper.updateById(updateObj);

        // 3. 记录操作日志
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT, BeanUtils.toBean(oldCustomer, CrmCustomerSaveReqVO.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_CUSTOMER, subType = "删除客户", bizNo = "{{#id}}", success = "删除了客户")
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CUSTOMER, bizId = "#id", level = CrmPermissionLevelEnum.OWNER)
    public void deleteCustomer(Long id) {
        // 校验存在
        validateCustomerExists(id);
        // TODO @puhui999：如果有联系人、商机，则不允许删除；

        // 删除
        customerMapper.deleteById(id);
        // 删除数据权限
        permissionService.deletePermission(CrmBizTypeEnum.CRM_CUSTOMER.getType(), id);
        // TODO @puhui999：删除跟进记录
    }

    private CrmCustomerDO validateCustomerExists(Long id) {
        CrmCustomerDO customerDO = customerMapper.selectById(id);
        if (customerDO == null) {
            throw exception(CUSTOMER_NOT_EXISTS);
        }
        return customerDO;
    }

    @Override
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CUSTOMER, bizId = "#id", level = CrmPermissionLevelEnum.READ)
    public CrmCustomerDO getCustomer(Long id) {
        return customerMapper.selectById(id);
    }

    @Override
    public List<CrmCustomerDO> getCustomerList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return customerMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<CrmCustomerDO> getCustomerPage(CrmCustomerPageReqVO pageReqVO, Long userId) {
        return customerMapper.selectPage(pageReqVO, userId);
    }

    /**
     * 校验客户是否存在
     *
     * @param customerId 客户 id
     */
    @Override
    public void validateCustomer(Long customerId) {
        validateCustomerExists(customerId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_CUSTOMER, subType = "转移客户", bizNo = "{{#reqVO.id}}", success = TRANSFER_CUSTOMER_LOG_SUCCESS)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CUSTOMER, bizId = "#reqVO.id", level = CrmPermissionLevelEnum.OWNER)
    public void transferCustomer(CrmCustomerTransferReqVO reqVO, Long userId) {
        // 1.1 校验客户是否存在
        CrmCustomerDO customer = validateCustomerExists(reqVO.getId());
        // 1.2 校验拥有客户是否到达上限
        validateCustomerExceedOwnerLimit(reqVO.getNewOwnerUserId(), 1);

        // 2.1 数据权限转移
        permissionService.transferPermission(
                CrmCustomerConvert.INSTANCE.convert(reqVO, userId).setBizType(CrmBizTypeEnum.CRM_CUSTOMER.getType()));
        // 2.2 转移后重新设置负责人
        customerMapper.updateOwnerUserIdById(reqVO.getId(), reqVO.getNewOwnerUserId());

        // 3. TODO 记录转移日志
        LogRecordContext.putVariable("crmCustomer", customer);
    }

    @Override
    // TODO @puhui999：看看这个能不能根据条件，写操作日志；
    // TODO 如果是 锁定，则 subType 为 锁定客户；success 为 将客户【】锁定
    // TODO 如果是 解锁，则 subType 为 解锁客户；success 为 将客户【】解锁
    @LogRecord(type = CRM_CUSTOMER, subType = "锁定/解锁客户", bizNo = "{{#updateReqVO.id}}", success = "锁定了客户")
    // TODO @puhui999：数据权限
    public void lockCustomer(CrmCustomerLockReqVO lockReqVO, Long userId) {
        // 1.1 校验当前客户是否存在
        validateCustomerExists(lockReqVO.getId());
        // 1.2 校验当前是否重复操作锁定/解锁状态
        CrmCustomerDO customer = customerMapper.selectById(lockReqVO.getId());
        if (customer.getLockStatus().equals(lockReqVO.getLockStatus())) {
            throw exception(customer.getLockStatus() ? CUSTOMER_LOCK_FAIL_IS_LOCK : CUSTOMER_UNLOCK_FAIL_IS_UNLOCK);
        }
        // 1.3 校验锁定上限。
        if (lockReqVO.getLockStatus()) {
            validateCustomerExceedLockLimit(userId);
        }

        // 2. 更新锁定状态
        customerMapper.updateById(BeanUtils.toBean(lockReqVO, CrmCustomerDO.class));
    }

    /**
     * 校验用户拥有的客户数量，是否到达上限
     *
     * @param userId   用户编号
     * @param newCount 附加数量
     */
    private void validateCustomerExceedOwnerLimit(Long userId, int newCount) {
        List<CrmCustomerLimitConfigDO> limitConfigs = customerLimitConfigService.getCustomerLimitConfigListByUserId(
                CUSTOMER_OWNER_LIMIT.getType(), userId);
        if (CollUtil.isEmpty(limitConfigs)) {
            return;
        }
        Long ownerCount = customerMapper.selectCountByDealStatusAndOwnerUserId(null, userId);
        Long dealOwnerCount = customerMapper.selectCountByDealStatusAndOwnerUserId(true, userId);
        limitConfigs.forEach(limitConfig -> {
            long nowCount = limitConfig.getDealCountEnabled() ? ownerCount : ownerCount - dealOwnerCount;
            if (nowCount + newCount > limitConfig.getMaxCount()) {
                throw exception(CUSTOMER_OWNER_EXCEED_LIMIT);
            }
        });
    }

    /**
     * 校验用户锁定的客户数量，是否到达上限
     *
     * @param userId 用户编号
     */
    private void validateCustomerExceedLockLimit(Long userId) {
        List<CrmCustomerLimitConfigDO> limitConfigs = customerLimitConfigService.getCustomerLimitConfigListByUserId(
                CUSTOMER_LOCK_LIMIT.getType(), userId);
        if (CollUtil.isEmpty(limitConfigs)) {
            return;
        }
        Long lockCount = customerMapper.selectCountByLockStatusAndOwnerUserId(true, userId);
        Integer maxCount = CollectionUtils.getMaxValue(limitConfigs, CrmCustomerLimitConfigDO::getMaxCount);
        assert maxCount != null;
        if (lockCount >= maxCount) {
            throw exception(CUSTOMER_LOCK_EXCEED_LIMIT);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_CUSTOMER, subType = "客户放入公海", bizNo = "{{#id}}", success = "将客户放入了公海")
    // TODO @puhui999：将客户【】放入了公海
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CUSTOMER, bizId = "#id", level = CrmPermissionLevelEnum.OWNER)
    public void putCustomerPool(Long id) {
        // 1. 校验存在
        CrmCustomerDO customer = customerMapper.selectById(id);
        if (customer == null) {
            throw exception(CUSTOMER_NOT_EXISTS);
        }
        // 1.2. 校验是否为公海数据
        validateCustomerOwnerExists(customer, true);
        // 1.3. 校验客户是否锁定
        validateCustomerIsLocked(customer, true);

        // 2. 设置负责人为 NULL
        int updateOwnerUserIncr = customerMapper.updateOwnerUserIdById(customer.getId(), null);
        if (updateOwnerUserIncr == 0) {
            throw exception(CUSTOMER_UPDATE_OWNER_USER_FAIL);
        }
        // 3. 删除负责人数据权限
        permissionService.deletePermission(CrmBizTypeEnum.CRM_CUSTOMER.getType(), customer.getId(),
                CrmPermissionLevelEnum.OWNER.getLevel());
        // TODO @puhui999：联系人的负责人，也要设置为 null；这块和领取是对应的；因为领取后，负责人也要关联过来；
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    // TODO @puhui999：权限校验

    // TODO @puhui999：如果是分配，操作日志是 “将客户【】分配给【】”
    // TODO @puhui999：如果是领取，操作日志是“领取客户【】”；
    // TODO @puhui999：如果是多条，则需要记录多条操作日志；不然 bizId 不好关联
    public void receiveCustomer(List<Long> ids, Long ownerUserId) {
        // 1.1 校验存在
        List<CrmCustomerDO> customers = customerMapper.selectBatchIds(ids);
        if (customers.size() != ids.size()) {
            throw exception(CUSTOMER_NOT_EXISTS);
        }
        // 1.2. 校验负责人是否存在
        adminUserApi.validateUserList(singletonList(ownerUserId));
        // 1.3. 校验状态
        customers.forEach(customer -> {
            // 校验是否已有负责人
            validateCustomerOwnerExists(customer, false);
            // 校验是否锁定
            validateCustomerIsLocked(customer, false);
            // 校验成交状态
            validateCustomerDeal(customer);
        });
        // 1.4  校验负责人是否到达上限
        validateCustomerExceedOwnerLimit(ownerUserId, customers.size());

        // 2.1 领取公海数据
        List<CrmCustomerDO> updateCustomers = new ArrayList<>();
        List<CrmPermissionCreateReqBO> createPermissions = new ArrayList<>();
        customers.forEach(customer -> {
            // 2.1. 设置负责人
            updateCustomers.add(new CrmCustomerDO().setId(customer.getId()).setOwnerUserId(ownerUserId));
            // 2.2. 创建负责人数据权限
            createPermissions.add(new CrmPermissionCreateReqBO().setBizType(CrmBizTypeEnum.CRM_CUSTOMER.getType())
                    .setBizId(customer.getId()).setUserId(ownerUserId).setLevel(CrmPermissionLevelEnum.OWNER.getLevel()));
        });
        // 2.2 更新客户负责人
        customerMapper.updateBatch(updateCustomers);
        // 2.3 创建负责人数据权限
        permissionService.createPermissionBatch(createPermissions);
        // TODO @芋艿：要不要处理关联的联系人？？？
    }

    private void validateCustomerOwnerExists(CrmCustomerDO customer, Boolean pool) {
        if (customer == null) { // 防御一下
            throw exception(CUSTOMER_NOT_EXISTS);
        }
        // 校验是否为公海数据
        if (pool && customer.getOwnerUserId() == null) {
            throw exception(CUSTOMER_IN_POOL, customer.getName());
        }
        // 负责人已存在
        if (customer.getOwnerUserId() != null) {
            throw exception(CUSTOMER_OWNER_EXISTS, customer.getName());
        }
    }

    private void validateCustomerIsLocked(CrmCustomerDO customer, Boolean pool) {
        if (customer.getLockStatus()) {
            throw exception(pool ? CUSTOMER_LOCKED_PUT_POOL_FAIL : CUSTOMER_LOCKED, customer.getName());
        }
    }

    private void validateCustomerDeal(CrmCustomerDO customer) {
        if (customer.getDealStatus()) {
            throw exception(CUSTOMER_ALREADY_DEAL);
        }
    }

    @Override
    public List<CrmCustomerDO> getCustomerList() {
        return customerMapper.selectList();
    }

}
