package cn.iocoder.yudao.module.crm.service.followup;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.crm.controller.admin.followup.vo.CrmFollowUpRecordPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.followup.vo.CrmFollowUpRecordSaveReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.followup.CrmFollowUpRecordDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.permission.CrmPermissionDO;
import cn.iocoder.yudao.module.crm.dal.mysql.followup.CrmFollowUpRecordMapper;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.enums.permission.CrmPermissionLevelEnum;
import cn.iocoder.yudao.module.crm.framework.permission.core.annotations.CrmPermission;
import cn.iocoder.yudao.module.crm.service.business.CrmBusinessService;
import cn.iocoder.yudao.module.crm.service.clue.CrmClueService;
import cn.iocoder.yudao.module.crm.service.contact.CrmContactService;
import cn.iocoder.yudao.module.crm.service.contract.CrmContractService;
import cn.iocoder.yudao.module.crm.service.customer.CrmCustomerService;
import cn.iocoder.yudao.module.crm.service.followup.bo.CrmUpdateFollowUpReqBO;
import cn.iocoder.yudao.module.crm.service.permission.CrmPermissionService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.anyMatch;
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
    private CrmPermissionService permissionService;
    @Resource
    private CrmBusinessService businessService;
    @Resource
    private CrmClueService clueService;
    @Resource
    private CrmContactService contactService;
    @Resource
    private CrmContractService contractService;
    @Resource
    private CrmCustomerService customerService;

    @Override
    @CrmPermission(bizTypeValue = "#createReqVO.bizType", bizId = "#createReqVO.bizId", level = CrmPermissionLevelEnum.WRITE)
    public Long createFollowUpRecord(CrmFollowUpRecordSaveReqVO createReqVO) {
        // 创建更进记录
        CrmFollowUpRecordDO followUpRecord = BeanUtils.toBean(createReqVO, CrmFollowUpRecordDO.class);
        crmFollowUpRecordMapper.insert(followUpRecord);

        LocalDateTime now = LocalDateTime.now();
        // 2. 更新 bizId 对应的记录；
        updateBizTypeFollowUp(followUpRecord, now);
        // 3.1 更新 contactIds 对应的记录
        if (CollUtil.isNotEmpty(createReqVO.getContactIds())) {
            contactService.updateContactFollowUpBatch(convertList(createReqVO.getContactIds(), contactId ->
                    new CrmUpdateFollowUpReqBO().setBizId(contactId).setContactNextTime(followUpRecord.getNextTime())
                            .setContactLastTime(now).setContactLastContent(followUpRecord.getContent())));
        }
        // 3.2 需要更新 businessIds、contactIds 对应的记录
        if (CollUtil.isNotEmpty(createReqVO.getBusinessIds())) {
            businessService.updateBusinessFollowUpBatch(convertList(createReqVO.getBusinessIds(), businessId ->
                    new CrmUpdateFollowUpReqBO().setBizId(businessId).setContactNextTime(followUpRecord.getNextTime())
                            .setContactLastTime(now).setContactLastContent(followUpRecord.getContent())));
        }
        return followUpRecord.getId();
    }

    /**
     * 执行更新
     *
     * @param followUpRecord 跟进记录
     * @param now            跟进时间
     */
    private void updateBizTypeFollowUp(CrmFollowUpRecordDO followUpRecord, LocalDateTime now) {
        updateBusinessFollowUp(followUpRecord, now);
        updateClueFollowUp(followUpRecord, now);
        updateContactFollowUp(followUpRecord, now);
        updateContractFollowUp(followUpRecord, now);
        updateCustomerFollowUp(followUpRecord, now);
    }

    private void updateBusinessFollowUp(CrmFollowUpRecordDO followUpRecord, LocalDateTime now) {
        if (ObjUtil.notEqual(CrmBizTypeEnum.CRM_BUSINESS.getType(), followUpRecord.getBizType())) {
            return;
        }

        // 更新商机跟进信息
        businessService.updateBusinessFollowUpBatch(Collections.singletonList(new CrmUpdateFollowUpReqBO()
                .setBizId(followUpRecord.getBizId()).setContactNextTime(followUpRecord.getNextTime()).setContactLastTime(now)
                .setContactLastContent(followUpRecord.getContent())));
    }

    private void updateClueFollowUp(CrmFollowUpRecordDO followUpRecord, LocalDateTime now) {
        if (ObjUtil.notEqual(CrmBizTypeEnum.CRM_LEADS.getType(), followUpRecord.getBizType())) {
            return;
        }

        // 更新线索跟进信息
        clueService.updateClueFollowUp(new CrmUpdateFollowUpReqBO().setBizId(followUpRecord.getBizId()).setContactLastTime(now)
                .setContactNextTime(followUpRecord.getNextTime()).setContactLastContent(followUpRecord.getContent()));
    }

    private void updateContactFollowUp(CrmFollowUpRecordDO followUpRecord, LocalDateTime now) {
        if (ObjUtil.notEqual(CrmBizTypeEnum.CRM_CONTACT.getType(), followUpRecord.getBizType())) {
            return;
        }

        // 更新联系人跟进信息
        contactService.updateContactFollowUpBatch(Collections.singletonList(new CrmUpdateFollowUpReqBO()
                .setBizId(followUpRecord.getBizId()).setContactNextTime(followUpRecord.getNextTime()).setContactLastTime(now)
                .setContactLastContent(followUpRecord.getContent())));
    }

    private void updateContractFollowUp(CrmFollowUpRecordDO followUpRecord, LocalDateTime now) {
        if (ObjUtil.notEqual(CrmBizTypeEnum.CRM_CONTRACT.getType(), followUpRecord.getBizType())) {
            return;
        }

        // 更新合同跟进信息
        contractService.updateContractFollowUp(new CrmUpdateFollowUpReqBO().setBizId(followUpRecord.getBizId()).setContactLastTime(now)
                .setContactNextTime(followUpRecord.getNextTime()).setContactLastContent(followUpRecord.getContent()));
    }

    private void updateCustomerFollowUp(CrmFollowUpRecordDO followUpRecord, LocalDateTime now) {
        if (ObjUtil.notEqual(CrmBizTypeEnum.CRM_CUSTOMER.getType(), followUpRecord.getBizType())) {
            return;
        }

        // 更新客户跟进信息
        customerService.updateCustomerFollowUp(new CrmUpdateFollowUpReqBO().setBizId(followUpRecord.getBizId()).setContactLastTime(now)
                .setContactNextTime(followUpRecord.getNextTime()).setContactLastContent(followUpRecord.getContent()));
    }

    @Override
    public void deleteFollowUpRecord(Long id, Long userId) {
        // 校验存在
        CrmFollowUpRecordDO followUpRecord = validateFollowUpRecordExists(id);
        // 校验权限
        List<CrmPermissionDO> permissionList = permissionService.getPermissionListByBiz(
                followUpRecord.getBizType(), followUpRecord.getBizId());
        boolean condition = anyMatch(permissionList, permission ->
                ObjUtil.equal(permission.getUserId(), userId) && ObjUtil.equal(permission.getLevel(), CrmPermissionLevelEnum.OWNER.getLevel()));
        if (!condition) {
            throw exception(FOLLOW_UP_RECORD_DELETE_DENIED);
        }

        // 删除
        crmFollowUpRecordMapper.deleteById(id);
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

}