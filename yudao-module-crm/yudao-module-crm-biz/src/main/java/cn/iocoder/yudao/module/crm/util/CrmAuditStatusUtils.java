package cn.iocoder.yudao.module.crm.util;

import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.module.bpm.enums.task.BpmTaskStatusEnum;
import cn.iocoder.yudao.module.crm.enums.common.CrmAuditStatusEnum;

/**
 * CRM 流程工具类
 *
 * @author HUIHUI
 */
public class CrmAuditStatusUtils {

    /**
     * BPM 审批结果转换
     *
     * @param bpmResult BPM 审批结果
     */
    public static Integer convertBpmResultToAuditStatus(Integer bpmResult) {
        Integer auditStatus = BpmTaskStatusEnum.APPROVE.getStatus().equals(bpmResult) ? CrmAuditStatusEnum.APPROVE.getStatus()
                : BpmTaskStatusEnum.REJECT.getStatus().equals(bpmResult) ? CrmAuditStatusEnum.REJECT.getStatus()
                : BpmTaskStatusEnum.CANCEL.getStatus().equals(bpmResult) ? BpmTaskStatusEnum.CANCEL.getStatus() : null;
        Assert.notNull(auditStatus, "BPM 审批结果({}) 转换失败", bpmResult);
        return auditStatus;
    }

}
