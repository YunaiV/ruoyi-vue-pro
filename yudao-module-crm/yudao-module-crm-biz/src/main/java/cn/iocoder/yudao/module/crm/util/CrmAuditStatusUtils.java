package cn.iocoder.yudao.module.crm.util;

import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
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
        Assert.isTrue(isEndResult(bpmResult), "BPM 审批结果({}) 转换失败, 流程状态不是最终结果", bpmResult);
        Integer auditStatus = BpmProcessInstanceResultEnum.APPROVE.getResult().equals(bpmResult) ? CrmAuditStatusEnum.APPROVE.getStatus()
                : BpmProcessInstanceResultEnum.REJECT.getResult().equals(bpmResult) ? CrmAuditStatusEnum.REJECT.getStatus()
                : BpmProcessInstanceResultEnum.CANCEL.getResult();
        Assert.notNull(auditStatus, "BPM 审批结果({}) 转换失败", bpmResult);
        return auditStatus;
    }

    /**
     * 判断该结果是否处于 End 最终结果
     *
     * @param bpmResult BPM 审批结果
     * @return 是否
     */
    public static boolean isEndResult(Integer bpmResult) {
        return ObjectUtils.equalsAny(bpmResult, BpmProcessInstanceResultEnum.APPROVE.getResult(),
                BpmProcessInstanceResultEnum.REJECT.getResult(), BpmProcessInstanceResultEnum.CANCEL.getResult());
    }

}
