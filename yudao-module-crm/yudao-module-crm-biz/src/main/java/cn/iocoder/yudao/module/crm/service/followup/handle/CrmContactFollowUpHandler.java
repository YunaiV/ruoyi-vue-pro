package cn.iocoder.yudao.module.crm.service.followup.handle;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.module.crm.dal.dataobject.followup.CrmFollowUpRecordDO;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.service.contact.CrmContactService;
import cn.iocoder.yudao.module.crm.service.contact.bo.CrmContactUpdateFollowUpReqBO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * CRM 联系人的 {@link CrmFollowUpHandler} 实现类
 *
 * @author HUIHUI
 */
@Component
public class CrmContactFollowUpHandler implements CrmFollowUpHandler {

    @Resource
    private CrmContactService contactService;

    @Override
    public void execute(CrmFollowUpRecordDO followUpRecord, LocalDateTime now) {
        if (ObjUtil.notEqual(CrmBizTypeEnum.CRM_CONTACT.getType(), followUpRecord.getBizType())) {
            return;
        }

        // 更新联系人跟进信息
        CrmContactUpdateFollowUpReqBO contactUpdateFollowUpReqBO = new CrmContactUpdateFollowUpReqBO();
        contactUpdateFollowUpReqBO.setId(followUpRecord.getBizId()).setContactNextTime(followUpRecord.getNextTime())
                .setContactLastTime(now).setContactLastContent(followUpRecord.getContent());
        contactService.updateContactFollowUpBatch(Collections.singletonList(contactUpdateFollowUpReqBO));
    }

}
