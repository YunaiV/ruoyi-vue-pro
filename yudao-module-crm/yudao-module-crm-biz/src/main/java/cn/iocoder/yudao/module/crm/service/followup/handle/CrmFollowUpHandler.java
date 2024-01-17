package cn.iocoder.yudao.module.crm.service.followup.handle;

import cn.iocoder.yudao.module.crm.dal.dataobject.followup.CrmFollowUpRecordDO;

import java.time.LocalDateTime;

/**
 * CRM 跟进信息处理器 handler 接口
 *
 * @author HUIHUI
 */
public interface CrmFollowUpHandler {

    /**
     * 执行更新
     *
     * @param followUpRecord 跟进记录
     * @param now            跟进时间
     */
    void execute(CrmFollowUpRecordDO followUpRecord, LocalDateTime now);

}
