package cn.iocoder.yudao.module.crm.service.contact;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.*;
import cn.iocoder.yudao.module.crm.convert.contact.CrmContactConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contact.CrmContactDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contract.CrmContractDO;
import cn.iocoder.yudao.module.crm.dal.mysql.contact.CrmContactMapper;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.enums.permission.CrmPermissionLevelEnum;
import cn.iocoder.yudao.module.crm.framework.permission.core.annotations.CrmPermission;
import cn.iocoder.yudao.module.crm.service.business.CrmBusinessService;
import cn.iocoder.yudao.module.crm.service.contract.CrmContractService;
import cn.iocoder.yudao.module.crm.service.customer.CrmCustomerService;
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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.PageParam.PAGE_SIZE_NONE;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.crm.enums.LogRecordConstants.CRM_CONTACT;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.ERROR_CODE_DUPLICATE;
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
    private CrmContractService crmContractService;
    @Resource
    private CrmContactBusinessService contactBusinessService;
    @Resource
    private CrmBusinessService businessService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_CONTACT, subType = "创建联系人[{{#contactName}}]", bizNo = "{{#contactId}}", success = "创建了联系人")
    public Long createContact(CrmContactSaveReqVO createReqVO, Long userId) {
        // 1. 校验
        validateRelationDataExists(createReqVO);

        // 2. 插入联系人
        CrmContactDO contact = CrmContactConvert.INSTANCE.convert(createReqVO);
        int contactId = contactMapper.insert(contact);

        // 3. 创建数据权限
        crmPermissionService.createPermission(new CrmPermissionCreateReqBO().setUserId(userId)
                .setBizType(CrmBizTypeEnum.CRM_CONTACT.getType()).setBizId(contact.getId())
                .setLevel(CrmPermissionLevelEnum.OWNER.getLevel()));

        //4.若传businessId自动关联商机
        Optional.ofNullable(createReqVO.getBusinessId()).ifPresent(businessId -> {
            CrmBusinessDO crmBusinessDO = businessService.getBusiness(createReqVO.getBusinessId());
            if(crmBusinessDO == null){
                throw exception(BUSINESS_NOT_EXISTS);
            }
            CrmContactBusinessReqVO crmContactBusinessReqVO = new CrmContactBusinessReqVO();
            crmContactBusinessReqVO.setContactId(contact.getId());
            crmContactBusinessReqVO.setBusinessIds(List.of(businessId));
            contactBusinessService.createContactBusinessList(crmContactBusinessReqVO);
        });

        // 5. 记录操作日志
        LogRecordContext.putVariable("contactId", contact.getId());
        LogRecordContext.putVariable("contactName", contact.getName());
        return contact.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_CONTACT, subType = "更新联系人", bizNo = "{{#updateReqVO.id}}", success = "更新了联系人{_DIFF{#updateReqVO}}")
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CONTACT, bizId = "#updateReqVO.id", level = CrmPermissionLevelEnum.WRITE)
    public void updateContact(CrmContactSaveReqVO updateReqVO) {
        // 1. 校验存在
        CrmContactDO contactDO = validateContactExists(updateReqVO.getId());
        validateRelationDataExists(updateReqVO);

        // 2. 更新联系人
        CrmContactDO updateObj = CrmContactConvert.INSTANCE.convert(updateReqVO);
        contactMapper.updateById(updateObj);

        // 3. 记录操作日志
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT, BeanUtils.toBean(contactDO, CrmContactSaveReqVO.class));
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
    }

    @Override
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CONTACT, bizId = "#id", level = CrmPermissionLevelEnum.OWNER)
    @Transactional(rollbackFor = Exception.class)
    public void deleteContact(Long id) {
        //1. 校验存在
        validateContactExists(id);
        //2.校验是否关联合同
        CrmContractDO crmContractDO = crmContractService.getContractByContactId(id);
        if(crmContractDO != null){
            throw exception(CONTACT_CONTRACT_LINK_EXISTS);
        }
        //3.删除联系人
        contactMapper.deleteById(id);
        //4.删除数据权限
        crmPermissionService.deletePermission(CrmBizTypeEnum.CRM_CONTACT.getType(), id);
        //5.删除商机关联
        contactBusinessService.deleteContactBusinessByContactId(id);
        // TODO @puhui999：删除跟进记录
    }

    private CrmContactDO validateContactExists(Long id) {
        CrmContactDO contactDO = contactMapper.selectById(id);
        if (contactDO == null) {
            throw exception(CONTACT_NOT_EXISTS);
        }
        return contactDO;
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
    // TODO @puhui999：权限校验
    // TODO @puhui999：记录操作日志；将联系人【名字】转移给【新负责人】
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

    @Override
    public List<CrmContactSimpleRespVO> simpleContactList() {
        CrmContactPageReqVO pageReqVO = new CrmContactPageReqVO();
        pageReqVO.setPageSize(PAGE_SIZE_NONE);
        List<CrmContactDO> list =contactMapper.selectPage(pageReqVO, getLoginUserId()).getList();
        return BeanUtils.toBean(list, CrmContactSimpleRespVO.class);
    }

}