package cn.iocoder.yudao.module.im.dal.mysql.conversation;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.im.dal.dataobject.conversation.ImConversationReadDO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * IM 会话读位置 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ImConversationReadMapper extends BaseMapperX<ImConversationReadDO> {

    default ImConversationReadDO selectByUserIdAndConversation(Long userId, Integer conversationType, Long conversationId) {
        return selectOne(new LambdaQueryWrapperX<ImConversationReadDO>()
                .eq(ImConversationReadDO::getUserId, userId)
                .eq(ImConversationReadDO::getConversationType, conversationType)
                .eq(ImConversationReadDO::getTargetId, conversationId));
    }

    default List<ImConversationReadDO> selectListByConversation(Integer conversationType, Long conversationId) {
        return selectList(new LambdaQueryWrapperX<ImConversationReadDO>()
                .eq(ImConversationReadDO::getConversationType, conversationType)
                .eq(ImConversationReadDO::getTargetId, conversationId));
    }

    default List<ImConversationReadDO> selectListByUserIdAndConversations(Long userId, Integer conversationType,
                                                                          Collection<Long> conversationIds) {
        return selectList(new LambdaQueryWrapperX<ImConversationReadDO>()
                .eq(ImConversationReadDO::getUserId, userId)
                .eq(ImConversationReadDO::getConversationType, conversationType)
                .in(ImConversationReadDO::getTargetId, conversationIds));
    }

    /**
     * 单调递增更新读位置：仅当新位置更大时才更新
     * <p>
     * 通过 {@code read_message_id < messageId} 的 CAS 条件，保证乱序 / 并发上报时读位置不会回退。
     *
     * @param id                记录编号
     * @param messageId  新的最大已读消息编号
     * @param readTime          已读时间
     * @return 影响行数；0 表示新位置不大于已有位置，未更新
     */
    default int updateReadMessageIdToLarger(Long id, Long messageId, LocalDateTime readTime) {
        return update(null, Wrappers.<ImConversationReadDO>lambdaUpdate()
                .set(ImConversationReadDO::getMessageId, messageId)
                .set(ImConversationReadDO::getReadTime, readTime)
                .eq(ImConversationReadDO::getId, id)
                .lt(ImConversationReadDO::getMessageId, messageId));
    }

}
