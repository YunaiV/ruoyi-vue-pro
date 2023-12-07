package cn.iocoder.yudao.module.crm.service.customer;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.CrmCustomerCreateReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.CrmCustomerPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.CrmCustomerTransferReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.CrmCustomerUpdateReqVO;
import cn.iocoder.yudao.module.crm.convert.customer.CrmCustomerConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.dal.mysql.customer.CrmCustomerMapper;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.enums.permission.CrmPermissionLevelEnum;
import cn.iocoder.yudao.module.crm.framework.core.annotations.CrmPermission;
import cn.iocoder.yudao.module.crm.service.permission.CrmPermissionService;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionCreateReqBO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.*;
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
    private CrmPermissionService crmPermissionService;

    @Resource
    private AdminUserApi adminUserApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCustomer(CrmCustomerCreateReqVO createReqVO, Long userId) {
        // 插入
        CrmCustomerDO customer = CrmCustomerConvert.INSTANCE.convert(createReqVO);
        customerMapper.insert(customer);

        // 创建数据权限
        crmPermissionService.createPermission(new CrmPermissionCreateReqBO().setBizType(CrmBizTypeEnum.CRM_CUSTOMER.getType())
                .setBizId(customer.getId()).setUserId(userId).setLevel(CrmPermissionLevelEnum.OWNER.getLevel())); // 设置当前操作的人为负责人
        return customer.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CUSTOMER, bizId = "#updateReqVO.id", level = CrmPermissionLevelEnum.WRITE)
    public void updateCustomer(CrmCustomerUpdateReqVO updateReqVO) {
        // 校验存在
        validateCustomerExists(updateReqVO.getId());

        // 更新
        CrmCustomerDO updateObj = CrmCustomerConvert.INSTANCE.convert(updateReqVO);
        customerMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CUSTOMER, bizId = "#id", level = CrmPermissionLevelEnum.OWNER)
    public void deleteCustomer(Long id) {
        // 校验存在
        validateCustomerExists(id);

        // 删除
        customerMapper.deleteById(id);
    }

    private void validateCustomerExists(Long id) {
        if (customerMapper.selectById(id) == null) {
            throw exception(CUSTOMER_NOT_EXISTS);
        }
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
        boolean admin = false; // TODO 如果是管理员
        return customerMapper.selectPage(pageReqVO, userId, adminUserApi.getSubordinateIds(userId), admin);
    }

    /**
     * 校验客户是否存在
     *
     * @param customerId 客户 id
     * @return 客户
     */
    @Override
    public CrmCustomerDO validateCustomer(Long customerId) {
        CrmCustomerDO customer = getCustomer(customerId);
        if (Objects.isNull(customer)) {
            throw exception(CUSTOMER_NOT_EXISTS);
        }
        return customer;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CUSTOMER, bizId = "#reqVO.id", level = CrmPermissionLevelEnum.OWNER)
    public void transferCustomer(CrmCustomerTransferReqVO reqVO, Long userId) {
        // 1. 校验客户是否存在
        validateCustomer(reqVO.getId());

        // 2. 数据权限转移
        crmPermissionService.transferPermission(
                CrmCustomerConvert.INSTANCE.convert(reqVO, userId).setBizType(CrmBizTypeEnum.CRM_CUSTOMER.getType()));

        // 3. TODO 记录转移日志
    }

    @Override
    public void lockCustomer(CrmCustomerUpdateReqVO updateReqVO) {
        // 校验存在
        validateCustomerExists(updateReqVO.getId());
        // TODO @Joey：可以校验下，如果已经对应的锁定状态，报个业务异常；原因是：后续这个业务会记录操作日志，会记录多了；
        // TODO @芋艿：业务完善，增加锁定上限；

        // 更新
        CrmCustomerDO updateObj = CrmCustomerConvert.INSTANCE.convert(updateReqVO);
        customerMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
        crmPermissionService.deletePermission(CrmBizTypeEnum.CRM_CUSTOMER.getType(), customer.getId(),
                CrmPermissionLevelEnum.OWNER.getLevel());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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

        // 2. 领取公海数据
        List<CrmCustomerDO> updateCustomers = new ArrayList<>();
        List<CrmPermissionCreateReqBO> createPermissions = new ArrayList<>();
        customers.forEach(customer -> {
            // 2.1. 设置负责人
            updateCustomers.add(new CrmCustomerDO().setId(customer.getId()).setOwnerUserId(ownerUserId));
            // 2.2. 创建负责人数据权限
            createPermissions.add(new CrmPermissionCreateReqBO().setBizType(CrmBizTypeEnum.CRM_CUSTOMER.getType())
                    .setBizId(customer.getId()).setUserId(ownerUserId).setLevel(CrmPermissionLevelEnum.OWNER.getLevel()));
        });

        // 3.1 更新客户负责人
        customerMapper.updateBatch(updateCustomers);
        // 3.2 创建负责人数据权限
        crmPermissionService.createPermissionBatch(createPermissions);
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
