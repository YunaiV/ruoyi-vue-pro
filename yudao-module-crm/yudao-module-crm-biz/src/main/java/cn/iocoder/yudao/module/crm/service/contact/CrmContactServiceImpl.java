package cn.iocoder.yudao.module.crm.service.contact;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.CrmContactBusinessReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.CrmContactPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.CrmContactSaveReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.CrmContactTransferReqVO;
import cn.iocoder.yudao.module.crm.convert.contact.CrmContactConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.contact.CrmContactDO;
import cn.iocoder.yudao.module.crm.dal.mysql.contact.CrmContactMapper;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.enums.permission.CrmPermissionLevelEnum;
import cn.iocoder.yudao.module.crm.framework.permission.core.annotations.CrmPermission;
import cn.iocoder.yudao.module.crm.service.business.CrmBusinessService;
import cn.iocoder.yudao.module.crm.service.contract.CrmContractService;
import cn.iocoder.yudao.module.crm.service.customer.CrmCustomerService;
import cn.iocoder.yudao.module.crm.service.followup.bo.CrmUpdateFollowUpReqBO;
import cn.iocoder.yudao.module.crm.service.permission.CrmPermissionService;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionCreateReqBO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.impl.DiffParseFunction;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.PageParam.PAGE_SIZE_NONE;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.crm.enums.LogRecordConstants.*;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.USER_NOT_EXISTS;
import static java.util.Collections.singletonList;

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
    private CrmPermissionService permissionService;
    @Resource
    @Lazy
    private CrmContractService contractService;
    @Resource
    private CrmContactBusinessService contactBusinessService;
    @Resource
    private CrmBusinessService businessService;

    @Resource
    private AdminUserApi adminUserApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_CONTACT_TYPE, subType = CRM_CONTACT_CREATE_SUB_TYPE, bizNo = "{{#contact.id}}",
            success = CRM_CONTACT_CREATE_SUCCESS)
    public Long createContact(CrmContactSaveReqVO createReqVO, Long userId) {
        createReqVO.setId(null);
        // 1. 校验
        validateRelationDataExists(createReqVO);

        // 2. 插入联系人
        CrmContactDO contact = BeanUtils.toBean(createReqVO, CrmContactDO.class);
        contactMapper.insert(contact);

        // 3. 创建数据权限
        permissionService.createPermission(new CrmPermissionCreateReqBO().setUserId(userId)
                .setBizType(CrmBizTypeEnum.CRM_CONTACT.getType()).setBizId(contact.getId())
                .setLevel(CrmPermissionLevelEnum.OWNER.getLevel()));

        // 4. 如果有关联商机，则需要创建关联
        if (createReqVO.getBusinessId() != null) {
            contactBusinessService.createContactBusinessList(new CrmContactBusinessReqVO()
                    .setContactId(contact.getId()).setBusinessIds(singletonList(createReqVO.getBusinessId())));
        }

        // 5. 记录操作日志
        LogRecordContext.putVariable("contact", contact);
        return contact.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_CONTACT_TYPE, subType = CRM_CONTACT_UPDATE_SUB_TYPE, bizNo = "{{#updateReqVO.id}}",
            success = CRM_CONTACT_UPDATE_SUCCESS)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CONTACT, bizId = "#updateReqVO.id", level = CrmPermissionLevelEnum.WRITE)
    public void updateContact(CrmContactSaveReqVO updateReqVO) {
        // 1. 校验存在
        CrmContactDO oldContact = validateContactExists(updateReqVO.getId());
        validateRelationDataExists(updateReqVO);

        // 2. 更新联系人
        CrmContactDO updateObj = BeanUtils.toBean(updateReqVO, CrmContactDO.class);
        contactMapper.updateById(updateObj);

        // 3. 记录操作日志
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT, BeanUtils.toBean(oldContact, CrmContactSaveReqVO.class));
        LogRecordContext.putVariable("contactName", oldContact.getName());
    }

    /**
     * 校验关联的数据都存在
     *
     * @param saveReqVO 新增/修改请求 VO
     */
    private void validateRelationDataExists(CrmContactSaveReqVO saveReqVO) {
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
        // 4. 如果有关联商机，则需要校验存在
        if (saveReqVO.getBusinessId() != null && businessService.getBusiness(saveReqVO.getBusinessId()) == null) {
            throw exception(BUSINESS_NOT_EXISTS);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_CONTACT_TYPE, subType = CRM_CONTACT_DELETE_SUB_TYPE, bizNo = "{{#id}}",
            success = CRM_CONTACT_DELETE_SUCCESS)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CONTACT, bizId = "#id", level = CrmPermissionLevelEnum.OWNER)
    public void deleteContact(Long id) {
        // 1.1 校验存在
        CrmContactDO contact = validateContactExists(id);
        // 1.2 校验是否关联合同
        if (contractService.getContractCountByContactId(id) > 0) {
            throw exception(CONTACT_DELETE_FAIL_CONTRACT_LINK_EXISTS);
        }

        // 2. 删除联系人
        contactMapper.deleteById(id);

        // 4.1 删除数据权限
        permissionService.deletePermission(CrmBizTypeEnum.CRM_CONTACT.getType(), id);
        // 4.2 删除商机关联
        contactBusinessService.deleteContactBusinessByContactId(id);

        // 记录操作日志上下文
        LogRecordContext.putVariable("contactName", contact.getName());
    }

    private CrmContactDO validateContactExists(Long id) {
        CrmContactDO contactDO = contactMapper.selectById(id);
        if (contactDO == null) {
            throw exception(CONTACT_NOT_EXISTS);
        }
        return contactDO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_CONTACT_TYPE, subType = CRM_CONTACT_TRANSFER_SUB_TYPE, bizNo = "{{#reqVO.id}}",
            success = CRM_CONTACT_TRANSFER_SUCCESS)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CONTACT, bizId = "#reqVO.id", level = CrmPermissionLevelEnum.OWNER)
    public void transferContact(CrmContactTransferReqVO reqVO, Long userId) {
        // 1 校验联系人是否存在
        CrmContactDO contact = validateContactExists(reqVO.getId());

        // 2.1 数据权限转移
        permissionService.transferPermission(
                CrmContactConvert.INSTANCE.convert(reqVO, userId).setBizType(CrmBizTypeEnum.CRM_CONTACT.getType()));
        // 2.2 设置新的负责人
        contactMapper.updateOwnerUserIdById(reqVO.getId(), reqVO.getNewOwnerUserId());

        // 3. 记录转移日志
        LogRecordContext.putVariable("contact", contact);
    }

    @Override
    public void updateOwnerUserIdByCustomerId(Long customerId, Long ownerUserId) {
        contactMapper.updateOwnerUserIdByCustomerId(customerId, ownerUserId);
    }

    @Override
    public void updateContactFollowUpBatch(List<CrmUpdateFollowUpReqBO> updateFollowUpReqBOList) {
        contactMapper.updateBatch(CrmContactConvert.INSTANCE.convertList(updateFollowUpReqBOList));
    }

    //======================= 查询相关 =======================

    @Override
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CONTACT, bizId = "#id", level = CrmPermissionLevelEnum.READ)
    public CrmContactDO getContact(Long id) {
        return contactMapper.selectById(id);
    }

    @Override
    public List<CrmContactDO> getContactListByIds(Collection<Long> ids, Long userId) {
        if (CollUtil.isEmpty(ids)) {
            return ListUtil.empty();
        }
        return contactMapper.selectBatchIds(ids, userId);
    }

    @Override
    public List<CrmContactDO> getContactListByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return ListUtil.empty();
        }
        return contactMapper.selectBatchIds(ids);
    }

    @Override
    public List<CrmContactDO> getContactList() {
        return contactMapper.selectList();
    }

    @Override
    public List<CrmContactDO> getSimpleContactList(Long userId) {
        CrmContactPageReqVO reqVO = new CrmContactPageReqVO();
        reqVO.setPageSize(PAGE_SIZE_NONE); // 不分页
        return contactMapper.selectPage(reqVO, userId).getList();
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
    public Long getContactCountByCustomerId(Long customerId) {
        return contactMapper.selectCount(CrmContactDO::getCustomerId, customerId);
    }

}