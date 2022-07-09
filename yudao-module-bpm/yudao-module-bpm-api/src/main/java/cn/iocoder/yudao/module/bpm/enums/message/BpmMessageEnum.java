package cn.iocoder.yudao.module.bpm.enums.message;

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
     * 关联 SmsTemplateDO 的 code 属性
     */
    private final String smsTemplateCode;

}
