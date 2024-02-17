package cn.iocoder.yudao.module.crm.service.followup;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.crm.controller.admin.followup.vo.CrmFollowUpRecordPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.followup.vo.CrmFollowUpRecordSaveReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.followup.CrmFollowUpRecordDO;
import cn.iocoder.yudao.module.crm.dal.mysql.followup.CrmFollowUpRecordMapper;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.enums.permission.CrmPermissionLevelEnum;
import cn.iocoder.yudao.module.crm.framework.permission.core.annotations.CrmPermission;
import cn.iocoder.yudao.module.crm.service.business.CrmBusinessService;
import cn.iocoder.yudao.module.crm.service.clue.CrmClueService;
import cn.iocoder.yudao.module.crm.service.contact.CrmContactService;
import cn.iocoder.yudao.module.crm.service.contract.CrmContractService;
import cn.iocoder.yudao.module.crm.service.customer.CrmCustomerService;
import cn.iocoder.yudao.module.crm.service.followup.bo.CrmFollowUpCreateReqBO;
import cn.iocoder.yudao.module.crm.service.followup.bo.CrmUpdateFollowUpReqBO;
import cn.iocoder.yudao.module.crm.service.permission.CrmPermissionService;
import javax.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.FOLLOW_UP_RECORD_DELETE_DENIED;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.FOLLOW_UP_RECORD_NOT_EXISTS;

/**
 * 跟进记录 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class CrmFollowUpRecordServiceImpl implements CrmFollowUpRecordService {

    @Resource
    private CrmFollowUpRecordMapper crmFollowUpRecordMapper;

    @Resource
    @Lazy
    private CrmPermissionService permissionService;
    @Resource
    @Lazy
    private CrmBusinessService businessService;
    @Resource
    @Lazy
    private CrmClueService clueService;
    @Resource
    @Lazy
    private CrmContactService contactService;
    @Resource
    @Lazy
    private CrmContractService contractService;
    @Resource
    @Lazy
    private CrmCustomerService customerService;

    @Override
    @CrmPermission(bizTypeValue = "#createReqVO.bizType", bizId = "#createReqVO.bizId", level = CrmPermissionLevelEnum.WRITE)
    public Long createFollowUpRecord(CrmFollowUpRecordSaveReqVO createReqVO) {
        // 创建更进记录
        CrmFollowUpRecordDO followUpRecord = BeanUtils.toBean(createReqVO, CrmFollowUpRecordDO.class);
        crmFollowUpRecordMapper.insert(followUpRecord);

        LocalDateTime now = LocalDateTime.now();
        CrmUpdateFollowUpReqBO updateFollowUpReqBO = new CrmUpdateFollowUpReqBO().setBizId(followUpRecord.getBizId())
                .setContactLastTime(now).setContactNextTime(followUpRecord.getNextTime()).setContactLastContent(followUpRecord.getContent());
        // 2. 更新 bizId 对应的记录；
        if (ObjUtil.notEqual(CrmBizTypeEnum.CRM_BUSINESS.getType(), followUpRecord.getBizType())) { // 更新商机跟进信息
            businessService.updateBusinessFollowUpBatch(Collections.singletonList(updateFollowUpReqBO));
        }
        if (ObjUtil.notEqual(CrmBizTypeEnum.CRM_LEADS.getType(), followUpRecord.getBizType())) { // 更新线索跟进信息
            clueService.updateClueFollowUp(updateFollowUpReqBO);
        }
        if (ObjUtil.notEqual(CrmBizTypeEnum.CRM_CONTACT.getType(), followUpRecord.getBizType())) { // 更新联系人跟进信息
            contactService.updateContactFollowUpBatch(Collections.singletonList(updateFollowUpReqBO));
        }
        if (ObjUtil.notEqual(CrmBizTypeEnum.CRM_CONTRACT.getType(), followUpRecord.getBizType())) { // 更新合同跟进信息
            contractService.updateContractFollowUp(updateFollowUpReqBO);
        }
        if (ObjUtil.notEqual(CrmBizTypeEnum.CRM_CUSTOMER.getType(), followUpRecord.getBizType())) { // 更新客户跟进信息
            customerService.updateCustomerFollowUp(updateFollowUpReqBO);
        }

        // 3.1 更新 contactIds 对应的记录，不更新 lastTime 和 lastContent
        if (CollUtil.isNotEmpty(createReqVO.getContactIds())) {
            contactService.updateContactFollowUpBatch(convertList(createReqVO.getContactIds(),
                    contactId -> updateFollowUpReqBO.setBizId(contactId).setContactLastTime(null).setContactLastContent(null)));
        }
        // 3.2 需要更新 businessIds 对应的记录，不更新 lastTime 和 lastContent
        if (CollUtil.isNotEmpty(createReqVO.getBusinessIds())) {
            businessService.updateBusinessFollowUpBatch(convertList(createReqVO.getBusinessIds(),
                    businessId -> updateFollowUpReqBO.setBizId(businessId).setContactLastTime(null).setContactLastContent(null)));
        }
        return followUpRecord.getId();
    }

    @Override
    public void createFollowUpRecordBatch(List<CrmFollowUpCreateReqBO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        crmFollowUpRecordMapper.insertBatch(BeanUtils.toBean(list, CrmFollowUpRecordDO.class));
    }

    @Override
    public void deleteFollowUpRecord(Long id, Long userId) {
        // 校验存在
        CrmFollowUpRecordDO followUpRecord = validateFollowUpRecordExists(id);
        // 校验权限
        if (!permissionService.hasPermission(followUpRecord.getBizType(), followUpRecord.getBizId(), userId, CrmPermissionLevelEnum.OWNER)) {
            throw exception(FOLLOW_UP_RECORD_DELETE_DENIED);
        }

        // 删除
        crmFollowUpRecordMapper.deleteById(id);
    }

    @Override
    public void deleteFollowUpRecordByBiz(Integer bizType, Long bizId) {
        crmFollowUpRecordMapper.deleteByBiz(bizType, bizId);
    }

    private CrmFollowUpRecordDO validateFollowUpRecordExists(Long id) {
        CrmFollowUpRecordDO followUpRecord = crmFollowUpRecordMapper.selectById(id);
        if (followUpRecord == null) {
            throw exception(FOLLOW_UP_RECORD_NOT_EXISTS);
        }
        return followUpRecord;
    }

    @Override
    public CrmFollowUpRecordDO getFollowUpRecord(Long id) {
        return crmFollowUpRecordMapper.selectById(id);
    }

    @Override
    @CrmPermission(bizTypeValue = "#pageReqVO.bizType", bizId = "#pageReqVO.bizId", level = CrmPermissionLevelEnum.READ)
    public PageResult<CrmFollowUpRecordDO> getFollowUpRecordPage(CrmFollowUpRecordPageReqVO pageReqVO) {
        return crmFollowUpRecordMapper.selectPage(pageReqVO);
    }

    @Override
    public List<CrmFollowUpRecordDO> getFollowUpRecordByBiz(Integer bizType, Collection<Long> bizIds) {
        return crmFollowUpRecordMapper.selectListByBiz(bizType, bizIds);
    }

}