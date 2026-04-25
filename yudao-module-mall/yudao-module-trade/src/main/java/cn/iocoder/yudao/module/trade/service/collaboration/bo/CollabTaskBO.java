package cn.iocoder.yudao.module.trade.service.collaboration.bo;

import lombok.Data;

/**
 * 协作任务 BO
 *
 * @author deepay
 */
@Data
public class CollabTaskBO {

    private String id;

    private String title;

    /** 负责角色（对应 CollabParticipantBO.role） */
    private String assigneeRole;

    /** 截止时间（ISO） */
    private String deadline;

    /**
     * 状态：pending / in_progress / done
     */
    private String status;

    /** 是否需要审批 */
    private boolean requiresApproval;

    /** 是否需要签字 */
    private boolean requiresSignature;

    /** 是否需要附件 */
    private boolean attachmentsRequired;

}
