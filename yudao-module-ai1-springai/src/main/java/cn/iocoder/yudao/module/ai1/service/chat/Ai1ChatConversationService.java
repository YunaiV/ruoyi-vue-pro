package cn.iocoder.yudao.module.ai1.service.chat;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai1.controller.admin.chat.vo.conversation.Ai1ChatConversationCreateMyReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.chat.vo.conversation.Ai1ChatConversationPageReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.chat.vo.conversation.Ai1ChatConversationUpdateMyReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.chat.Ai1ChatConversationDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * AI1 对话 Service 接口
 *
 * 「我的」系列方法按登录用户校验归属，避免越权访问他人对话
 *
 * @author 芋道源码
 */
public interface Ai1ChatConversationService {

    /**
     * 创建我的对话；后台对话不要求 Agent 已发布
     *
     * @param userId      用户编号
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createChatConversationMy(Long userId, @Valid Ai1ChatConversationCreateMyReqVO createReqVO);

    /**
     * 修改我的对话标题
     *
     * @param userId      用户编号
     * @param updateReqVO 修改信息
     */
    void updateChatConversationMy(Long userId, @Valid Ai1ChatConversationUpdateMyReqVO updateReqVO);

    /**
     * 删除我的对话，连带删除消息
     *
     * @param userId 用户编号
     * @param id     编号
     */
    void deleteChatConversationMy(Long userId, Long id);

    /**
     * 校验对话存在，且归属于指定用户
     *
     * @param userId 用户编号
     * @param id     编号
     * @return 对话
     */
    Ai1ChatConversationDO validateChatConversationMy(Long userId, Long id);

    /**
     * 获得指定用户在指定 Agent 下的对话列表，按编号倒序
     *
     * @param agentId Agent 编号
     * @param userId  用户编号
     * @return 对话列表
     */
    List<Ai1ChatConversationDO> getChatConversationListByAgentIdAndUserId(Long agentId, Long userId);

    /**
     * 获得对话分页（管理端查看 Agent 的全部对话）
     *
     * @param pageReqVO 分页查询
     * @return 对话分页
     */
    PageResult<Ai1ChatConversationDO> getChatConversationPage(Ai1ChatConversationPageReqVO pageReqVO);

    /**
     * 修改对话标题（首条消息发送时自动命名）
     *
     * @param id    编号
     * @param title 标题
     */
    void updateChatConversationTitle(Long id, String title);

    /**
     * 刷新对话的更新时间，即最近活跃时间
     *
     * @param id 编号
     */
    void touchChatConversation(Long id);

    /**
     * 删除 Agent 下的全部对话与消息（Agent 删除时调用）
     *
     * @param agentIds Agent 编号集合
     */
    void deleteChatConversationListByAgentIds(Collection<Long> agentIds);

}
