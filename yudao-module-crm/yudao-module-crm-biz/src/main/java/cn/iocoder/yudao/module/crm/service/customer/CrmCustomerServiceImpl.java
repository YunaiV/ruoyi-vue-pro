package cn.iocoder.yudao.module.crm.service.customer;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.*;
import cn.iocoder.yudao.module.crm.convert.customer.CrmCustomerConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.dal.mysql.customer.CrmCustomerMapper;
import cn.iocoder.yudao.module.crm.framework.core.annotations.CrmPermission;
import cn.iocoder.yudao.module.crm.framework.enums.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.framework.enums.CrmPermissionLevelEnum;
import cn.iocoder.yudao.module.crm.service.permission.CrmPermissionService;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionCreateReqBO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
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
        //// 1.1 TODO 如果是超级管理员
        //boolean admin = false;
        //if (admin && ObjUtil.notEqual(userId, CrmPermissionDO.POOL_USER_ID)) {
        //    return customerMapper.selectPage(pageReqVO, Collections.emptyList());
        //}
        //// 1.2 获取当前用户能看的分页数据
        //// TODO @puhui999：如果业务的数据量比较大，in 太多可能有性能问题噢；看看是不是搞成 join 连表了；可以微信讨论下；
        //List<CrmPermissionDO> permissions = crmPermissionService.getPermissionListByBizTypeAndUserId(
        //        CrmBizTypeEnum.CRM_CUSTOMER.getType(), userId);
        //// 1.3 TODO 场景数据过滤
        //if (CrmCustomerSceneEnum.isOwner(pageReqVO.getSceneType())) { // 场景一：我负责的数据
        //    permissions = CollectionUtils.filterList(permissions, item -> CrmPermissionLevelEnum.isOwner(item.getLevel()));
        //}
        //Set<Long> ids = convertSet(permissions, CrmPermissionDO::getBizId);
        //if (CollUtil.isEmpty(ids)) { // 没得说明没有什么给他看的
        //    return PageResult.empty();
        //}
        //
        //// 2. 获取客户分页数据
        //return customerMapper.selectPage(pageReqVO, ids);
        return customerMapper.selectPage(pageReqVO, userId);
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
    public void receive(List <Long> ids) {
        transferCustomerOwner(ids,SecurityFrameworkUtils.getLoginUserId());
    }

    @Override
    public void distributeByIds(List <Long> cIds, Long ownerId) {
        transferCustomerOwner(cIds,ownerId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receive(Long id, Long userId) {
        // 1. 校验存在
        CrmCustomerDO customer = customerMapper.selectById(id);
        if (customer == null) {
            throw exception(CUSTOMER_NOT_EXISTS);
        }
        // 1.2. 校验是否为公海数据
        if (customer.getOwnerUserId() != null) {
            throw exception(CUSTOMER_NOT_IN_POOL);
        }

        // 2. 领取公海数据-设置负责人
        customerMapper.updateById(new CrmCustomerDO().setId(customer.getId()).setOwnerUserId(userId));
        // 3. 创建负责人数据权限
        crmPermissionService.createPermission(new CrmPermissionCreateReqBO().setBizType(CrmBizTypeEnum.CRM_CUSTOMER.getType())
                .setBizId(customer.getId()).setUserId(userId).setLevel(CrmPermissionLevelEnum.OWNER.getLevel())); // 设置当前操作的人为负责人
    }

    @Override
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CUSTOMER, bizId = "#id", level = CrmPermissionLevelEnum.OWNER)
    public void putPool(Long id) {
        // 1. 校验存在
        CrmCustomerDO customer = customerMapper.selectById(id);
        if (customer == null) {
            throw exception(CUSTOMER_NOT_EXISTS);
        }
        // 1.2. 校验是否为公海数据
        if (customer.getOwnerUserId() == null) {
            throw exception(CUSTOMER_IN_POOL);
        }
        // 1.3. 校验客户是否锁定、
        if (customer.getLockStatus()) {
            throw exception(CUSTOMER_LOCKED_PUT_POOL_FAIL);
        }

        // 2. 公海数据-设置负责人 NULL
        customerMapper.updateById(new CrmCustomerDO().setId(customer.getId()).setOwnerUserId(null));
        // 3. 删除负责人数据权限
        crmPermissionService.deletePermission(CrmBizTypeEnum.CRM_CUSTOMER.getType(), customer.getId(),
                CrmPermissionLevelEnum.OWNER.getLevel());
    }

    private void transferCustomerOwner(List <Long> cIds, Long ownerId){
        // 先一次性校验完成客户是否可用
        // TODO @xiaqing：批量一次性加载客户列表，然后去逐个校验；
        for (Long cId : cIds) {
            //校验是否存在
            validateCustomerExists(cId);
            //todo 校验是否已有负责人
            validCustomerOwnerExist(cId);
            //todo 校验是否锁定
            validCustomerIsLocked(cId);
            //todo 校验成交状态
            validCustomerDeal(cId);
        }
        // TODO @xiaqing：每个客户更新的时候，where 条件，加上 owner_user_id is null，防止并发问题；
        List<CrmCustomerDO> updateDos = new ArrayList <>();
        for (Long cId : cIds){
            CrmCustomerDO customerDO = new CrmCustomerDO();
            customerDO.setId(cId);
            customerDO.setOwnerUserId(SecurityFrameworkUtils.getLoginUserId());
        }
        // 统一修改状态
        customerMapper.updateBatch(updateDos);
    }

    private void validCustomerOwnerExist(Long id) {
        if (customerMapper.selectById(id).getOwnerUserId()!=null) {
            throw exception(CUSTOMER_OWNER_EXISTS);
        }
    }

    private void validCustomerIsLocked(Long id) {
        if (customerMapper.selectById(id).getLockStatus() ==true) {
            throw exception(CUSTOMER_LOCKED);
        }
    }

    private void validCustomerDeal(Long id) {
        if (customerMapper.selectById(id).getDealStatus() ==true) {
            throw exception(CUSTOMER_ALREADY_DEAL);
        }
    }

}
