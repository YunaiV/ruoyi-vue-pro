package cn.iocoder.yudao.module.trade.service.collaboration.bo;

import lombok.Data;

/**
 * 协作会话参与者 BO
 *
 * @author deepay
 */
@Data
public class CollabParticipantBO {

    /** 参与者用户编号 */
    private Long userId;

    /** 显示名称 */
    private String name;

    /**
     * 角色：purchaser（采购方）/ finance（财务方）/ logistics（物流方）/ supplier（供应商）
     */
    private String role;

    /** 每人独立的访问令牌 */
    private String accessToken;

    /** 邀请链接 */
    private String inviteLink;

    /** 是否已接受邀请 */
    private boolean accepted;

}
