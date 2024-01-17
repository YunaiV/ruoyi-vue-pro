package cn.iocoder.yudao.module.crm.service.followup.handle;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.module.crm.dal.dataobject.followup.CrmFollowUpRecordDO;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.service.business.CrmBusinessService;
import cn.iocoder.yudao.module.crm.service.business.bo.CrmBusinessUpdateFollowUpReqBO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * CRM 商机的 {@link CrmFollowUpHandler} 实现类
 *
 * @author HUIHUI
 */
@Component
public class CrmBusinessFollowUpHandler implements CrmFollowUpHandler {

    @Resource
    private CrmBusinessService businessService;

    @Override
    public void execute(CrmFollowUpRecordDO followUpRecord, LocalDateTime now) {
        if (ObjUtil.notEqual(CrmBizTypeEnum.CRM_BUSINESS.getType(), followUpRecord.getBizType())) {
            return;
        }

        // 更新商机跟进信息
        CrmBusinessUpdateFollowUpReqBO businessUpdateFollowUpReqBO = new CrmBusinessUpdateFollowUpReqBO();
        businessUpdateFollowUpReqBO.setId(followUpRecord.getBizId()).setContactNextTime(followUpRecord.getNextTime())
                .setContactLastTime(now).setContactLastContent(followUpRecord.getContent());
        businessService.updateContactFollowUpBatch(Collections.singletonList(businessUpdateFollowUpReqBO));
    }

}
