package cn.iocoder.yudao.module.trade.service.collaboration;

import cn.iocoder.yudao.module.trade.service.collaboration.bo.CollabParticipantBO;
import cn.iocoder.yudao.module.trade.service.collaboration.bo.CollabSessionBO;

import java.util.List;

/**
 * 多方协作服务
 * <p>
 * 对应 Python CollaborationWorkflow
 *
 * @author deepay
 */
public interface CollaborationService {

    /**
     * 创建协作会话（采购方 / 财务方 / 物流方等多方参与）
     *
     * @param orderId      关联的订单编号
     * @param participants 参与者列表
     * @return 协作会话
     */
    CollabSessionBO createSession(String orderId, List<CollabParticipantBO> participants);

    /**
     * 获取协作会话
     *
     * @param sessionId 会话 ID
     * @return 会话；不存在返回 null
     */
    CollabSessionBO getSession(String sessionId);

}
