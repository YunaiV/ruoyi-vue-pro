package cn.iocoder.yudao.module.ai.service;

import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.conversation.AiChatConversationCreateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.conversation.AiChatConversationListReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.conversation.AiChatConversationRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.conversation.AiChatConversationUpdateReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.chat.AiChatConversationDO;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * chat 对话
 *
 * @fansili
 * @since v1.0
 */
public interface AiChatConversationService {

    /**
     * 对话 - 创建对话
     *
     * @param req
     * @return
     */
    Long createConversation(AiChatConversationCreateReqVO req);

    /**
     * 对话 - 更新对话
     *
     * @param updateReqVO
     * @return
     */
    Boolean updateConversation(AiChatConversationUpdateReqVO updateReqVO);

    /**
     * 获取 - 对话列表
     *
     * @return
     */
    List<AiChatConversationRespVO> listConversation();

    /**
     * 获取 - 对话
     *
     * @param id
     * @return
     */
    AiChatConversationRespVO getConversationOfValidate(Long id);

    /**
     * 删除 - 根据id
     *
     * @param id
     */
    Boolean deleteConversation(Long id);

    /**
     * 校验 - 是否存在
     *
     * @param id
     * @return
     */
    AiChatConversationDO validateExists(Long id);
}
