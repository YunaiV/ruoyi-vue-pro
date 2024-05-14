package cn.iocoder.yudao.module.ai.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.dal.dataobject.chat.AiChatConversationDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.chat.AiChatMessageDO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

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
     * 查询 - 根据 对话id查询
     *
     * @param conversationId
     */
    default List<AiChatMessageDO> selectByConversationId(Long conversationId) {
        return this.selectList(
                new LambdaQueryWrapperX<AiChatMessageDO>()
                        .eq(AiChatMessageDO::getConversationId, conversationId)
                        .orderByAsc(AiChatMessageDO::getId)
        );
    }

}
