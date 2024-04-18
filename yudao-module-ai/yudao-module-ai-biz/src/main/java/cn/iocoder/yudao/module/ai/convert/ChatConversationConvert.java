package cn.iocoder.yudao.module.ai.convert;

import cn.iocoder.yudao.module.ai.dataobject.AiChatConversationDO;
import cn.iocoder.yudao.module.ai.vo.ChatConversationRes;
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
public interface ChatConversationConvert {

    ChatConversationConvert INSTANCE = Mappers.getMapper(ChatConversationConvert.class);

    /**
     * 转换 - 多个 ChatConversationRes
     *
     * @param top100Conversation
     * @return
     */
    List<ChatConversationRes> covnertChatConversationResList(List<AiChatConversationDO> top100Conversation);

    /**
     * 转换 - 单个 ChatConversationRes
     *
     * @param aiChatConversationDO
     * @return
     */
    ChatConversationRes covnertChatConversationRes(AiChatConversationDO aiChatConversationDO);
}
