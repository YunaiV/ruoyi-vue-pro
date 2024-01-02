package cn.iocoder.yudao.module.crm.service.contact;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.*;
import cn.iocoder.yudao.module.crm.convert.contact.CrmContactConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.contact.CrmContactDO;
import cn.iocoder.yudao.module.crm.dal.mysql.contact.CrmContactMapper;
import cn.iocoder.yudao.module.crm.dal.mysql.contactbusinesslink.CrmContactBusinessMapper;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.enums.permission.CrmPermissionLevelEnum;
import cn.iocoder.yudao.module.crm.framework.permission.core.annotations.CrmPermission;
import cn.iocoder.yudao.module.crm.service.customer.CrmCustomerService;
import cn.iocoder.yudao.module.crm.service.permission.CrmPermissionService;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionCreateReqBO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.CONTACT_NOT_EXISTS;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.CUSTOMER_NOT_EXISTS;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.USER_NOT_EXISTS;

/**
 * CRM 联系人 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class CrmContactServiceImpl implements CrmContactService {

    @Resource
    private CrmContactMapper contactMapper;

    @Resource
    private CrmCustomerService customerService;
    @Resource
    private CrmPermissionService crmPermissionService;
    @Resource
    private AdminUserApi adminUserApi;

    @Resource
    private CrmContactBusinessMapper contactBusinessLinkMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createContact(CrmContactCreateReqVO createReqVO, Long userId) {
        // 1.1 校验
        validateRelationDataExists(createReqVO);
        // 1.2 插入
        CrmContactDO contact = CrmContactConvert.INSTANCE.convert(createReqVO);
        contactMapper.insert(contact);

        // 2. 创建数据权限
        crmPermissionService.createPermission(new CrmPermissionCreateReqBO().setUserId(userId)
                .setBizType(CrmBizTypeEnum.CRM_CONTACT.getType()).setBizId(contact.getId())
                .setLevel(CrmPermissionLevelEnum.OWNER.getLevel()));
        return contact.getId();
    }

    @Override
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CONTACT, bizId = "#updateReqVO.id", level = CrmPermissionLevelEnum.WRITE)
    public void updateContact(CrmContactUpdateReqVO updateReqVO) {
        // 1. 校验存在
        validateContactExists(updateReqVO.getId());
        validateRelationDataExists(updateReqVO);
        // 2. 更新
        CrmContactDO updateObj = CrmContactConvert.INSTANCE.convert(updateReqVO);
        contactMapper.updateById(updateObj);
    }

    /**
     * 校验关联的数据都存在
     *
     * @param saveReqVO 新增/修改请求 VO
     */
    private void validateRelationDataExists(CrmContactBaseVO saveReqVO) {
        // 1. 校验客户
        if (saveReqVO.getCustomerId() != null && customerService.getCustomer(saveReqVO.getCustomerId()) == null) {
            throw exception(CUSTOMER_NOT_EXISTS);
        }
        // 2. 校验负责人
        if (saveReqVO.getOwnerUserId() != null && adminUserApi.getUser(saveReqVO.getOwnerUserId()) == null) {
            throw exception(USER_NOT_EXISTS);
        }
        // 3. 直属上级
        if (saveReqVO.getParentId() != null && contactMapper.selectById(saveReqVO.getParentId()) == null) {
            throw exception(CONTACT_NOT_EXISTS);
        }
    }

    @Override
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CONTACT, bizId = "#id", level = CrmPermissionLevelEnum.OWNER)
    public void deleteContact(Long id) {
        // 校验存在
        validateContactExists(id);
        // 删除
        contactMapper.deleteById(id);
        // 删除数据权限
        crmPermissionService.deletePermission(CrmBizTypeEnum.CRM_CONTACT.getType(), id);
    }

    private void validateContactExists(Long id) {
        if (contactMapper.selectById(id) == null) {
            throw exception(CONTACT_NOT_EXISTS);
        }
    }

    @Override
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CONTACT, bizId = "#id", level = CrmPermissionLevelEnum.READ)
    public CrmContactDO getContact(Long id) {
        return contactMapper.selectById(id);
    }

    @Override
    public List<CrmContactDO> getContactList(Collection<Long> ids, Long userId) {
        if (CollUtil.isEmpty(ids)) {
            return ListUtil.empty();
        }
        return contactMapper.selectBatchIds(ids, userId);
    }

    @Override
    public PageResult<CrmContactDO> getContactPage(CrmContactPageReqVO pageReqVO, Long userId) {
        return contactMapper.selectPage(pageReqVO, userId);
    }

    @Override
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CUSTOMER, bizId = "#pageVO.customerId", level = CrmPermissionLevelEnum.READ)
    public PageResult<CrmContactDO> getContactPageByCustomerId(CrmContactPageReqVO pageVO) {
        return contactMapper.selectPageByCustomerId(pageVO);
    }

    @Override
    public void transferContact(CrmContactTransferReqVO reqVO, Long userId) {
        // 1 校验联系人是否存在
        validateContactExists(reqVO.getId());

        // 2.1 数据权限转移
        crmPermissionService.transferPermission(
                CrmContactConvert.INSTANCE.convert(reqVO, userId).setBizType(CrmBizTypeEnum.CRM_CONTACT.getType()));
        // 2.2 设置新的负责人
        contactMapper.updateOwnerUserIdById(reqVO.getId(), reqVO.getNewOwnerUserId());

        // 3. TODO 记录转移日志
    }

}