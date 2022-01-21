package cn.iocoder.yudao.adminserver.modules.bpm.enums.message;

import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.sms.SysSmsTemplateDO;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Bpm 消息的枚举
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum BpmMessageEnum {

    PROCESS_INSTANCE_APPROVE("bpm_process_instance_approve"), // 流程任务被审批通过时，发送给申请人
    PROCESS_INSTANCE_REJECT("bpm_process_instance_reject"), // 流程任务被审批不通过时，发送给申请人
    TASK_ASSIGNED("bpm_task_assigned"); // 任务被分配时，发送给审批人

    /**
     * 短信模板的标识
     *
     * 关联 {@link SysSmsTemplateDO#getCode()}
     */
    private final String smsCode;


}
