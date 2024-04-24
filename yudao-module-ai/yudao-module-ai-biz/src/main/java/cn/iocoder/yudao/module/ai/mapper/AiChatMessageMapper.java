package cn.iocoder.yudao.module.ai.mapper;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.dal.dataobject.AiChatMessageDO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * message mapper
 *
 * @fansili
 * @since v1.0
 */
@Repository
@Mapper
public interface AiChatMessageMapper extends BaseMapperX<AiChatMessageDO> {

    /**
     * 删除 - 根据 Conversation 和 id
     *
     * @param chatConversationId
     * @param id
     */
    default int deleteByConversationAndId(Long chatConversationId, Long id) {
        return this.delete(new LambdaQueryWrapperX<AiChatMessageDO>()
                .eq(AiChatMessageDO::getChatConversationId, chatConversationId)
                .eq(AiChatMessageDO::getId, id)
        );
    }
}
