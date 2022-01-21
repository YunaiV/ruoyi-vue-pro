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

    TASK_ASSIGNED("bpm_task_assigned"); // 任务被分配时，发送给审批人

    /**
     * 短信模板的标识
     *
     * 关联 {@link SysSmsTemplateDO#getCode()}
     */
    private final String smsCode;


}
