package cn.iocoder.yudao.module.crm.service.customer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.*;
import cn.iocoder.yudao.module.crm.convert.customer.CrmCustomerConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.permission.CrmPermissionDO;
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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.CUSTOMER_NOT_EXISTS;

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
        List<CrmPermissionDO> permissions = crmPermissionService.getPermissionListByBizTypeAndUserId(
                CrmBizTypeEnum.CRM_CUSTOMER.getType(), userId);
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

}
