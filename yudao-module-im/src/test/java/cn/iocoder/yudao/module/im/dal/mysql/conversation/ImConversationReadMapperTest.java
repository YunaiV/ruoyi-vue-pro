package cn.iocoder.yudao.module.im.dal.mysql.conversation;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.im.dal.dataobject.conversation.ImConversationReadDO;
import cn.iocoder.yudao.module.im.enums.ImConversationTypeEnum;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link ImConversationReadMapper} 的单元测试
 *
 * @author 芋道源码
 */
public class ImConversationReadMapperTest extends BaseDbUnitTest {

    @Resource
    private ImConversationReadMapper mapper;

    @Test
    public void testUpdateReadMessageIdToLarger_onlyAdvancesWhenLarger() {
        // 准备：用户 1 在群 10 的读位置 = 50
        ImConversationReadDO read = buildRead(1L, ImConversationTypeEnum.GROUP.getType(), 10L, 50L);
        mapper.insert(read);

        // 1. 新位置更大 → 前进
        int advanced = mapper.updateReadMessageIdToLarger(read.getId(), 100L, LocalDateTime.now());
        assertEquals(1, advanced);
        assertEquals(100L, mapper.selectById(read.getId()).getMessageId());

        // 2. 新位置相等 → 不更新
        int equalUpdate = mapper.updateReadMessageIdToLarger(read.getId(), 100L, LocalDateTime.now());
        assertEquals(0, equalUpdate);

        // 3. 新位置更小（乱序上报）→ 不回退
        int smaller = mapper.updateReadMessageIdToLarger(read.getId(), 80L, LocalDateTime.now());
        assertEquals(0, smaller);
        assertEquals(100L, mapper.selectById(read.getId()).getMessageId());
    }

    @Test
    public void testSelectByUserIdAndConversation() {
        ImConversationReadDO read = buildRead(1L, ImConversationTypeEnum.GROUP.getType(), 10L, 50L);
        mapper.insert(read);
        // 同用户不同会话类型，不应命中
        mapper.insert(buildRead(1L, ImConversationTypeEnum.PRIVATE.getType(), 10L, 99L));

        ImConversationReadDO result = mapper.selectByUserIdAndConversation(
                1L, ImConversationTypeEnum.GROUP.getType(), 10L);

        assertNotNull(result);
        assertEquals(50L, result.getMessageId());
    }

    @Test
    public void testSelectListByConversation_aggregatesAllUsers() {
        // 群 10 内三个用户的读位置，用于群回执人数聚合
        mapper.insert(buildRead(1L, ImConversationTypeEnum.GROUP.getType(), 10L, 100L));
        mapper.insert(buildRead(2L, ImConversationTypeEnum.GROUP.getType(), 10L, 80L));
        mapper.insert(buildRead(3L, ImConversationTypeEnum.GROUP.getType(), 10L, 50L));
        // 别的群，不应混入
        mapper.insert(buildRead(1L, ImConversationTypeEnum.GROUP.getType(), 20L, 999L));

        List<ImConversationReadDO> list = mapper.selectListByConversation(
                ImConversationTypeEnum.GROUP.getType(), 10L);
        Map<Long, Long> positions = convertMap(list,
                ImConversationReadDO::getUserId, ImConversationReadDO::getMessageId);

        assertEquals(3, positions.size());
        assertEquals(100L, positions.get(1L));
        assertEquals(80L, positions.get(2L));
        assertEquals(50L, positions.get(3L));
    }

    @Test
    public void testSelectListByUserIdAndConversations_batch() {
        // 用户 1 在频道 10 / 20 / 30 的读位置
        mapper.insert(buildRead(1L, ImConversationTypeEnum.CHANNEL.getType(), 10L, 5L));
        mapper.insert(buildRead(1L, ImConversationTypeEnum.CHANNEL.getType(), 20L, 8L));
        mapper.insert(buildRead(1L, ImConversationTypeEnum.CHANNEL.getType(), 30L, 3L));

        List<ImConversationReadDO> list = mapper.selectListByUserIdAndConversations(
                1L, ImConversationTypeEnum.CHANNEL.getType(), Arrays.asList(10L, 20L));

        assertEquals(2, list.size());
    }

    private ImConversationReadDO buildRead(Long userId, Integer conversationType, Long conversationId,
                                           Long readMessageId) {
        return ImConversationReadDO.builder()
                .userId(userId)
                .conversationType(conversationType)
                .targetId(conversationId)
                .messageId(readMessageId)
                .readTime(LocalDateTime.now())
                .build();
    }

}
