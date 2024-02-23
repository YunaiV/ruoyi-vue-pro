package cn.iocoder.yudao.module.crm.util;

import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceResultEnum;
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
        Integer auditStatus = BpmProcessInstanceResultEnum.APPROVE.getResult().equals(bpmResult) ? CrmAuditStatusEnum.APPROVE.getStatus()
                : BpmProcessInstanceResultEnum.REJECT.getResult().equals(bpmResult) ? CrmAuditStatusEnum.REJECT.getStatus()
                : BpmProcessInstanceResultEnum.CANCEL.getResult().equals(bpmResult) ? BpmProcessInstanceResultEnum.CANCEL.getResult() : null;
        Assert.notNull(auditStatus, "BPM 审批结果({}) 转换失败", bpmResult);
        return auditStatus;
    }

}
