package cn.iocoder.yudao.module.ai.convert;

import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.conversation.AiChatConversationUpdateReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.chat.AiChatConversationDO;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.conversation.AiChatConversationRespVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 聊天 对话 convert
 *
 * @author fansili
 * @time 2024/4/18 16:39
 * @since 1.0
 */
@Mapper
public interface AiChatConversationConvert {

    AiChatConversationConvert INSTANCE = Mappers.getMapper(AiChatConversationConvert.class);

    /**
     * 转换 - 多个 ChatConversationRes
     *
     * @param top100Conversation
     * @return
     */
    List<AiChatConversationRespVO> covnertChatConversationResList(List<AiChatConversationDO> top100Conversation);

    /**
     * 转换 - 单个 ChatConversationRes
     *
     * @param aiChatConversationDO
     * @return
     */
    AiChatConversationRespVO covnertChatConversationRes(AiChatConversationDO aiChatConversationDO);

    /**
     * 转换 - AiChatConversationDO
     *
     * @param updateReqVO
     */
    AiChatConversationDO convertAiChatConversationDO(AiChatConversationUpdateReqVO updateReqVO);
}
