package cn.iocoder.yudao.module.im.dal.mysql.message;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImPrivateMessageDO;
import cn.iocoder.yudao.module.im.enums.message.ImMessageStatusEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IM 私聊消息 Mapper 单元测试
 *
 * @author 芋道源码
 */
public class ImPrivateMessageMapperTest extends BaseDbUnitTest {

    @Resource
    private ImPrivateMessageMapper mapper;

    // ========== selectListByMinId ==========

    @Test
    public void testSelectListByMinId() {
        // 准备：用户 1 发给用户 2
        ImPrivateMessageDO msg1 = buildMessage(1L, 2L, ImMessageStatusEnum.UNREAD);
        mapper.insert(msg1);
        // 用户 2 发给用户 1
        ImPrivateMessageDO msg2 = buildMessage(2L, 1L, ImMessageStatusEnum.UNREAD);
        mapper.insert(msg2);
        // 用户 3 发给用户 4（不相关）
        ImPrivateMessageDO msg3 = buildMessage(3L, 4L, ImMessageStatusEnum.UNREAD);
        mapper.insert(msg3);

        // 调用：用户 1 从 id=0 拉取
        List<ImPrivateMessageDO> result = mapper.selectListByMinId(1L, 0L, 100);

        // 断言：只包含与用户 1 相关的 2 条
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(m ->
                m.getSenderId().equals(1L) || m.getReceiverId().equals(1L)));
    }

    @Test
    public void testSelectListByMinId_withMinId() {
        // 准备
        ImPrivateMessageDO msg1 = buildMessage(1L, 2L, ImMessageStatusEnum.UNREAD);
        mapper.insert(msg1);
        ImPrivateMessageDO msg2 = buildMessage(2L, 1L, ImMessageStatusEnum.UNREAD);
        mapper.insert(msg2);

        // 调用：从 msg1.id 之后拉取
        List<ImPrivateMessageDO> result = mapper.selectListByMinId(1L, msg1.getId(), 100);

        // 断言：只有 msg2
        assertEquals(1, result.size());
        assertEquals(msg2.getId(), result.get(0).getId());
    }

    @Test
    public void testSelectListByMinId_limitSize() {
        // 准备：插入 3 条
        mapper.insert(buildMessage(1L, 2L, ImMessageStatusEnum.UNREAD));
        mapper.insert(buildMessage(2L, 1L, ImMessageStatusEnum.UNREAD));
        mapper.insert(buildMessage(1L, 2L, ImMessageStatusEnum.UNREAD));

        // 调用：limit 2
        List<ImPrivateMessageDO> result = mapper.selectListByMinId(1L, 0L, 2);

        // 断言
        assertEquals(2, result.size());
    }

    // ========== selectHistoryList ==========

    @Test
    public void testSelectHistoryList_basic() {
        // 准备：用户 1 <-> 用户 2 的消息
        ImPrivateMessageDO msg1 = buildMessage(1L, 2L, ImMessageStatusEnum.UNREAD);
        mapper.insert(msg1);
        ImPrivateMessageDO msg2 = buildMessage(2L, 1L, ImMessageStatusEnum.UNREAD);
        mapper.insert(msg2);
        // 用户 1 <-> 用户 3 的消息（不相关）
        ImPrivateMessageDO msg3 = buildMessage(1L, 3L, ImMessageStatusEnum.UNREAD);
        mapper.insert(msg3);

        // 调用
        List<ImPrivateMessageDO> result = mapper.selectHistoryList(1L, 2L, null, 100);

        // 断言：只有与用户 2 对话的 2 条，按 id 倒序
        assertEquals(2, result.size());
        assertTrue(result.get(0).getId() > result.get(1).getId());
    }

    @Test
    public void testSelectHistoryList_excludeRecall() {
        // 准备
        ImPrivateMessageDO msg1 = buildMessage(1L, 2L, ImMessageStatusEnum.UNREAD);
        mapper.insert(msg1);
        ImPrivateMessageDO msg2 = buildMessage(1L, 2L, ImMessageStatusEnum.RECALL);
        mapper.insert(msg2);

        // 调用
        List<ImPrivateMessageDO> result = mapper.selectHistoryList(1L, 2L, null, 100);

        // 断言：撤回的不返回
        assertEquals(1, result.size());
        assertEquals(msg1.getId(), result.get(0).getId());
    }

    @Test
    public void testSelectHistoryList_withMaxId() {
        // 准备
        ImPrivateMessageDO msg1 = buildMessage(1L, 2L, ImMessageStatusEnum.UNREAD);
        mapper.insert(msg1);
        ImPrivateMessageDO msg2 = buildMessage(1L, 2L, ImMessageStatusEnum.UNREAD);
        mapper.insert(msg2);
        ImPrivateMessageDO msg3 = buildMessage(1L, 2L, ImMessageStatusEnum.UNREAD);
        mapper.insert(msg3);

        // 调用：从 msg3 往前拉
        List<ImPrivateMessageDO> result = mapper.selectHistoryList(1L, 2L, msg3.getId(), 100);

        // 断言：只有 msg1 和 msg2
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(m -> m.getId() < msg3.getId()));
    }

    @Test
    public void testSelectHistoryList_limit() {
        // 准备
        mapper.insert(buildMessage(1L, 2L, ImMessageStatusEnum.UNREAD));
        mapper.insert(buildMessage(2L, 1L, ImMessageStatusEnum.UNREAD));
        mapper.insert(buildMessage(1L, 2L, ImMessageStatusEnum.UNREAD));

        // 调用：limit 2
        List<ImPrivateMessageDO> result = mapper.selectHistoryList(1L, 2L, null, 2);

        // 断言
        assertEquals(2, result.size());
    }

    @Test
    public void testSelectHistoryList_bidirectional() {
        // 准备：验证双向查询
        ImPrivateMessageDO msg1 = buildMessage(2L, 1L, ImMessageStatusEnum.UNREAD); // 对方发的
        mapper.insert(msg1);

        // 调用：以用户 1 的视角查
        List<ImPrivateMessageDO> result = mapper.selectHistoryList(1L, 2L, null, 100);

        // 断言：能查到对方发来的消息
        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getSenderId());
    }

    // ========== selectBySenderIdAndClientMessageId ==========

    @Test
    public void testSelectBySenderIdAndClientMessageId() {
        // 准备
        ImPrivateMessageDO msg = buildMessage(1L, 2L, ImMessageStatusEnum.UNREAD);
        msg.setClientMessageId("uuid-001");
        mapper.insert(msg);

        // 调用
        ImPrivateMessageDO result = mapper.selectBySenderIdAndClientMessageId(1L, "uuid-001");

        // 断言
        assertNotNull(result);
        assertEquals(msg.getId(), result.getId());
    }

    @Test
    public void testSelectBySenderIdAndClientMessageId_notFound() {
        ImPrivateMessageDO result = mapper.selectBySenderIdAndClientMessageId(1L, "not-exist");
        assertNull(result);
    }

    // ========== 工具方法 ==========

    private ImPrivateMessageDO buildMessage(Long senderId, Long receiverId, ImMessageStatusEnum status) {
        return ImPrivateMessageDO.builder()
                .clientMessageId("uuid-" + System.nanoTime())
                .senderId(senderId)
                .receiverId(receiverId)
                .type(0)
                .content("{\"content\":\"test\"}")
                .status(status.getStatus())
                .sendTime(LocalDateTime.now())
                .build();
    }

}
