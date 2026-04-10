package cn.iocoder.yudao.module.crm.service.contact;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.CrmContactBusiness2ReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.CrmContactBusinessReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contact.CrmContactBusinessDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contact.CrmContactDO;
import cn.iocoder.yudao.module.crm.dal.mysql.contact.CrmContactBusinessMapper;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.enums.permission.CrmPermissionLevelEnum;
import cn.iocoder.yudao.module.crm.framework.permission.core.annotations.CrmPermission;
import cn.iocoder.yudao.module.crm.service.business.CrmBusinessService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.BUSINESS_NOT_EXISTS;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.CONTACT_NOT_EXISTS;

/**
 * 联系人与商机的关联 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class CrmContactBusinessServiceImpl implements CrmContactBusinessService {

    @Resource
    private CrmContactBusinessMapper contactBusinessMapper;

    @Resource
    @Lazy // 延迟加载，为了解决延迟加载
    private CrmBusinessService businessService;
    @Resource
    @Lazy // 延迟加载，为了解决延迟加载
    private CrmContactService contactService;

    @Override
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CONTACT, bizId = "#createReqVO.contactId", level = CrmPermissionLevelEnum.WRITE)
    public void createContactBusinessList(CrmContactBusinessReqVO createReqVO) {
        CrmContactDO contact = contactService.getContact(createReqVO.getContactId());
        if (contact == null) {
            throw exception(CONTACT_NOT_EXISTS);
        }
        // 遍历处理，考虑到一般数量不会太多，代码处理简单
        List<CrmContactBusinessDO> saveDOList = new ArrayList<>();
        createReqVO.getBusinessIds().forEach(businessId -> {
            CrmBusinessDO business = businessService.getBusiness(businessId);
            if (business == null) {
                throw exception(BUSINESS_NOT_EXISTS);
            }
            // 关联判重
            if (contactBusinessMapper.selectByContactIdAndBusinessId(createReqVO.getContactId(), businessId) != null) {
                return;
            }
            saveDOList.add(new CrmContactBusinessDO(null, createReqVO.getContactId(), businessId));
        });
        // 批量插入
        if (CollUtil.isNotEmpty(saveDOList)) {
            contactBusinessMapper.insertBatch(saveDOList);
        }
    }

    @Override
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_BUSINESS, bizId = "#createReqVO.businessId", level = CrmPermissionLevelEnum.WRITE)
    public void createContactBusinessList2(CrmContactBusiness2ReqVO createReqVO) {
        CrmBusinessDO business = businessService.getBusiness(createReqVO.getBusinessId());
        if (business == null) {
            throw exception(BUSINESS_NOT_EXISTS);
        }
        // 遍历处理，考虑到一般数量不会太多，代码处理简单
        List<CrmContactBusinessDO> saveDOList = new ArrayList<>();
        createReqVO.getContactIds().forEach(contactId -> {
            CrmContactDO contact = contactService.getContact(contactId);
            if (contact == null) {
                throw exception(CONTACT_NOT_EXISTS);
            }
            // 关联判重
            if (contactBusinessMapper.selectByContactIdAndBusinessId(contactId, createReqVO.getBusinessId()) != null) {
                return;
            }
            saveDOList.add(new CrmContactBusinessDO(null, contactId, createReqVO.getBusinessId()));
        });
        // 批量插入
        if (CollUtil.isNotEmpty(saveDOList)) {
            contactBusinessMapper.insertBatch(saveDOList);
        }
    }

    @Override
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CONTACT, bizId = "#deleteReqVO.contactId", level = CrmPermissionLevelEnum.WRITE)
    public void deleteContactBusinessList(CrmContactBusinessReqVO deleteReqVO) {
        CrmContactDO contact = contactService.getContact(deleteReqVO.getContactId());
        if (contact == null) {
            throw exception(CONTACT_NOT_EXISTS);
        }
        // 直接删除
        contactBusinessMapper.deleteByContactIdAndBusinessId(
                deleteReqVO.getContactId(), deleteReqVO.getBusinessIds());
    }

    @Override
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_BUSINESS, bizId = "#deleteReqVO.businessId", level = CrmPermissionLevelEnum.WRITE)
    public void deleteContactBusinessList2(CrmContactBusiness2ReqVO deleteReqVO) {
        CrmBusinessDO business = businessService.getBusiness(deleteReqVO.getBusinessId());
        if (business == null) {
            throw exception(BUSINESS_NOT_EXISTS);
        }
        // 直接删除
        contactBusinessMapper.deleteByBusinessIdAndContactId(
                deleteReqVO.getBusinessId(), deleteReqVO.getContactIds());
    }

    @Override
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CONTACT, bizId = "#contactId", level = CrmPermissionLevelEnum.WRITE)
    public void deleteContactBusinessByContactId(Long contactId) {
        contactBusinessMapper.delete(CrmContactBusinessDO::getContactId, contactId);
    }

    @Override
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CONTACT, bizId = "#contactId", level = CrmPermissionLevelEnum.READ)
    public List<CrmContactBusinessDO> getContactBusinessListByContactId(Long contactId) {
        return contactBusinessMapper.selectListByContactId(contactId);
    }

    @Override
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_BUSINESS, bizId = "#businessId", level = CrmPermissionLevelEnum.READ)
    public List<CrmContactBusinessDO> getContactBusinessListByBusinessId(Long businessId) {
        return contactBusinessMapper.selectListByBusinessId(businessId);
    }

}