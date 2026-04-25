package cn.iocoder.yudao.module.ai.service.image;

import cn.iocoder.yudao.module.ai.controller.admin.image.vo.fashion.AiFashionSmartChatReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.fashion.AiFashionSmartChatRespVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiFashionSessionDO;

/**
 * AI 服装设计智能对话服务接口
 *
 * <p>提供多轮对话式设计交互能力，通过 NLP 解析用户意图并调度任务。</p>
 *
 * @author deepay
 */
public interface AiFashionConversationService {

    /**
     * 处理用户智能对话消息
     *
     * @param userId 当前登录用户ID
     * @param reqVO  请求参数
     * @return 响应结果（含会话令牌、意图解析、任务ID等）
     */
    AiFashionSmartChatRespVO chat(Long userId, AiFashionSmartChatReqVO reqVO);

    /**
     * 获取或创建会话
     *
     * @param userId       用户ID
     * @param sessionToken 前端传入的会话令牌（首次为 null 或空）
     * @return 会话 DO（已持久化）
     */
    AiFashionSessionDO getOrCreateSession(Long userId, String sessionToken);

}
