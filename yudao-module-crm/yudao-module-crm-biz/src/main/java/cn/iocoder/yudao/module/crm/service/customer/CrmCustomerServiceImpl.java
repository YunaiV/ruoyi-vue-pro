package cn.iocoder.yudao.module.crm.service.customer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.*;
import cn.iocoder.yudao.module.crm.convert.customer.CrmCustomerConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.permission.CrmPermissionDO;
import cn.iocoder.yudao.module.crm.dal.mysql.customer.CrmCustomerMapper;
import cn.iocoder.yudao.module.crm.enums.customer.CrmCustomerSceneEnum;
import cn.iocoder.yudao.module.crm.framework.core.annotations.CrmPermission;
import cn.iocoder.yudao.module.crm.framework.enums.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.framework.enums.CrmPermissionLevelEnum;
import cn.iocoder.yudao.module.crm.service.permission.CrmPermissionService;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionCreateReqBO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.*;

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

    @Override
    public Long createCustomer(CrmCustomerCreateReqVO createReqVO, Long userId) {
        // 插入
        CrmCustomerDO customer = CrmCustomerConvert.INSTANCE.convert(createReqVO);
        customerMapper.insert(customer);

        // 创建数据权限
        crmPermissionService.createPermission(new CrmPermissionCreateReqBO().setBizType(CrmBizTypeEnum.CRM_CUSTOMER.getType())
                .setBizId(customer.getId()).setUserId(userId).setLevel(CrmPermissionLevelEnum.OWNER.getLevel())); // 设置当前操作的人为负责人

        // 返回
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
    public PageResult<CrmCustomerDO> getCustomerPage(CrmCustomerPageReqVO pageReqVO, Long userId) {
        // 1.1 TODO 如果是超级管理员
        boolean admin = false;
        if (admin && ObjUtil.notEqual(userId, CrmPermissionDO.POOL_USER_ID)) {
            return customerMapper.selectPage(pageReqVO, Collections.emptyList());
        }
        // 1.2 获取当前用户能看的分页数据
        // TODO @puhui999：如果业务的数据量比较大，in 太多可能有性能问题噢；看看是不是搞成 join 连表了；可以微信讨论下；
        List<CrmPermissionDO> permissions = crmPermissionService.getPermissionListByBizTypeAndUserId(
                CrmBizTypeEnum.CRM_CUSTOMER.getType(), userId);
        // 1.3 TODO 场景数据过滤
        if (CrmCustomerSceneEnum.isOwner(pageReqVO.getSceneType())) { // 场景一：我负责的数据
            permissions = CollectionUtils.filterList(permissions, item -> CrmPermissionLevelEnum.isOwner(item.getLevel()));
        }
        Set<Long> ids = convertSet(permissions, CrmPermissionDO::getBizId);
        if (CollUtil.isEmpty(ids)) { // 没得说明没有什么给他看的
            return PageResult.empty();
        }

        // 2. 获取客户分页数据
        return customerMapper.selectPage(pageReqVO, ids);
    }

    @Override
    public List<CrmCustomerDO> getCustomerList(CrmCustomerExportReqVO exportReqVO) {
        //return customerMapper.selectList(exportReqVO);
        return Collections.emptyList();
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
    public void transferCustomer(CrmCustomerTransferReqVO reqVO, Long userId) {
        // 1. 校验合同是否存在
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
    public void receiveCustomer(List <Long> ids,Long ownerUserId) {
        transferCustomerOwner(ids, ownerUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void distributeCustomer(List <Long> ids, Long ownerUserId) {
        transferCustomerOwner(ids, ownerUserId);
    }

    /**
     * 转移客户负责人
     *
     * @param ids 客户编号数组
     * @param ownerUserId 负责人编号
     */
    private void transferCustomerOwner(List <Long> ids, Long ownerUserId) {
        // 先一次性加载所有数据，校验客户是否可用
        List <CrmCustomerDO> customers = customerMapper.selectBatchIds(ids);
        for (CrmCustomerDO customer : customers) {
            // 校验是否已有负责人
            validateCustomerOwnerExists(customer);
            // 校验是否锁定
            validateCustomerIsLocked(customer);
            // 校验成交状态
            validateCustomerDeal(customer);
        }

        // TODO @QingX：这里是不是改成一次性更新；不然，如果有 20 个客户，就要执行 20 次 SQL 了；
        // 统一修改状态
        CrmCustomerDO updateDo = new CrmCustomerDO();
        updateDo.setOwnerUserId(ownerUserId);
        // TODO @QingX：如果更新的数量不对，则应该抛出异常，回滚，并错误提示；
        for (Long id : ids) {
            customerMapper.updateCustomerByOwnerUserIdIsNull(id,updateDo);
        }
    }

    // TODO @QingX：错误提示里面，可以把客户的名字带上哈；不然不知道是谁；
    private void validateCustomerOwnerExists(CrmCustomerDO customer) {
        if (customer.getOwnerUserId() != null) {
            throw exception(CUSTOMER_OWNER_EXISTS);
        }
    }

    private void validateCustomerIsLocked(CrmCustomerDO customer) {
        if (customer.getLockStatus()) {
            throw exception(CUSTOMER_LOCKED);
        }
    }

    private void validateCustomerDeal(CrmCustomerDO customer) {
        if (customer.getDealStatus()) {
            throw exception(CUSTOMER_ALREADY_DEAL);
        }
    }

}
