package cn.iocoder.yudao.module.im.service.conversation;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.im.dal.dataobject.conversation.ImConversationReadDO;
import cn.iocoder.yudao.module.im.dal.mysql.conversation.ImConversationReadMapper;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * IM 会话读位置 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class ImConversationReadServiceImpl implements ImConversationReadService {

    @Resource
    private ImConversationReadMapper conversationReadMapper;

    @Override
    public boolean updateConversationReadPosition(Long userId, Integer conversationType, Long conversationId, Long readMessageId) {
        LocalDateTime now = LocalDateTime.now();
        // 1. 不存在则插入；并发下唯一键冲突，降级为回查 + CAS 更新
        ImConversationReadDO existing = conversationReadMapper.selectByUserIdAndConversation(
                userId, conversationType, conversationId);
        if (existing == null) {
            try {
                conversationReadMapper.insert(new ImConversationReadDO().setUserId(userId)
                        .setConversationType(conversationType).setTargetId(conversationId)
                        .setMessageId(readMessageId).setReadTime(now));
                return true;
            } catch (DuplicateKeyException e) {
                log.warn("[updateConversationReadPosition][userId({}) type({}) conversationId({}) 并发插入冲突，回查更新]",
                        userId, conversationType, conversationId);
                existing = conversationReadMapper.selectByUserIdAndConversation(userId, conversationType, conversationId);
            }
        }
        if (existing == null) {
            return false;
        }

        // 2. CAS 单调更新：mapper 内 WHERE message_id < ? 保证乱序 / 并发不回退；影响行数 > 0 即读位置前进
        return conversationReadMapper.updateReadMessageIdToLarger(existing.getId(), readMessageId, now) > 0;
    }

    @Override
    public Long getConversationReadMessageId(Long userId, Integer conversationType, Long conversationId) {
        ImConversationReadDO read = conversationReadMapper.selectByUserIdAndConversation(
                userId, conversationType, conversationId);
        return read != null ? read.getMessageId() : null;
    }

    @Override
    public Map<Long, Long> getUserReadMessageIdMap(Integer conversationType, Long conversationId) {
        return convertMap(conversationReadMapper.selectListByConversation(conversationType, conversationId),
                ImConversationReadDO::getUserId, ImConversationReadDO::getMessageId);
    }

    @Override
    public Map<Long, Long> getConversationReadMessageIdMap(Long userId, Integer conversationType,
                                                           Collection<Long> conversationIds) {
        if (CollUtil.isEmpty(conversationIds)) {
            return Collections.emptyMap();
        }
        List<ImConversationReadDO> list =  conversationReadMapper.selectListByUserIdAndConversations(
                userId, conversationType, conversationIds);
        return convertMap(list, ImConversationReadDO::getTargetId, ImConversationReadDO::getMessageId);
    }

    @Override
    public List<ImConversationReadDO> pullConversationReadList(Long userId, Long lastUpdateTime, Long lastId, Integer limit) {
        return conversationReadMapper.selectPullListByUserId(userId, lastUpdateTime, lastId, limit);
    }

}
