package cn.iocoder.yudao.module.crm.util;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.module.bpm.api.listener.dto.BpmResultListenerRespDTO;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceResultEnum;
import cn.iocoder.yudao.module.crm.enums.common.CrmAuditStatusEnum;

/**
 * CRM 流程工具类
 *
 * @author HUIHUI
 */
public class CrmAuditStatusUtils {

    /**
     * 流程审批状态转换
     *
     * @param event 业务流程实例的结果
     */
    public static void convertAuditStatus(BpmResultListenerRespDTO event) {
        // 状态转换
        if (ObjUtil.equal(event.getResult(), BpmProcessInstanceResultEnum.APPROVE.getResult())) {
            event.setResult(CrmAuditStatusEnum.APPROVE.getStatus());
        }
        if (ObjUtil.equal(event.getResult(), BpmProcessInstanceResultEnum.REJECT.getResult())) {
            event.setResult(CrmAuditStatusEnum.REJECT.getStatus());
        }
        if (ObjUtil.equal(event.getResult(), BpmProcessInstanceResultEnum.CANCEL.getResult())) {
            event.setResult(CrmAuditStatusEnum.CANCEL.getStatus());
        }
    }

    /**
     * 判断该结果是否处于 End 最终结果
     *
     * @param result 结果
     * @return 是否
     */
    public static boolean isEndResult(Integer result) {
        return ObjectUtils.equalsAny(result, BpmProcessInstanceResultEnum.APPROVE.getResult(),
                BpmProcessInstanceResultEnum.REJECT.getResult(), BpmProcessInstanceResultEnum.CANCEL.getResult());
    }

}
