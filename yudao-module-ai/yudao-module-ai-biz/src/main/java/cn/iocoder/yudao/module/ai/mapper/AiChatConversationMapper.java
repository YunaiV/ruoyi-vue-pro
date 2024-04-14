package cn.iocoder.yudao.module.ai.mapper;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.ai.dataobject.AiChatConversationDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

/**
 * message mapper
 *
 * @fansili
 * @since v1.0
 */
@Repository
@Mapper
public interface AiChatConversationMapper extends BaseMapperX<AiChatConversationDO> {


    @Update("update ai_chat_conversation set chat_count = chat_count + 1 where id = #{id}")
    void updateIncrChatCount(@Param("id") Long id);

}
