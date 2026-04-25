package cn.iocoder.yudao.module.trade.service.collaboration.bo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 协作会话 BO
 *
 * @author deepay
 */
@Data
public class CollabSessionBO {

    private String id;

    /** 关联的订单编号 */
    private String orderId;

    private List<CollabParticipantBO> participants;

    /** 会话内文档列表（类型 + 链接） */
    private List<Map<String, Object>> documents;

    /** 任务列表 */
    private List<CollabTaskBO> tasks;

    /**
     * 设置：requireSignature / auditTrail / blockchainBackup
     */
    private Map<String, Object> settings;

    /** 创建时间（ISO） */
    private String createdAt;

    /**
     * 状态：active / closed
     */
    private String status;

    /** 区块链存证 ID */
    private String blockchainTaskId;

}
