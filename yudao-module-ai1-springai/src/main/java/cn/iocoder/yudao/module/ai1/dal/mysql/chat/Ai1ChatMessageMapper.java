package cn.iocoder.yudao.module.ai1.dal.mysql.chat;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.ai1.dal.dataobject.chat.Ai1ChatMessageDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * AI1 对话消息 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface Ai1ChatMessageMapper extends BaseMapperX<Ai1ChatMessageDO> {

    default List<Ai1ChatMessageDO> selectListByConversationId(Long conversationId) {
        return selectList(new LambdaQueryWrapperX<Ai1ChatMessageDO>()
                .eq(Ai1ChatMessageDO::getConversationId, conversationId)
                .orderByAsc(Ai1ChatMessageDO::getId));
    }

    /**
     * 按编号倒序，取指定消息之前最近的 limit 条
     */
    default List<Ai1ChatMessageDO> selectListByConversationIdAndIdLessThan(Long conversationId, Long id, Integer limit) {
        return selectList(new QueryWrapperX<Ai1ChatMessageDO>()
                .limitN(limit)
                .eq("conversation_id", conversationId)
                .lt("id", id)
                .orderByDesc("id"));
    }

    default Long selectCountByConversationId(Long conversationId) {
        return selectCount(Ai1ChatMessageDO::getConversationId, conversationId);
    }

    default int deleteByConversationIds(Collection<Long> conversationIds) {
        return delete(new LambdaQueryWrapperX<Ai1ChatMessageDO>().in(Ai1ChatMessageDO::getConversationId, conversationIds));
    }

}
