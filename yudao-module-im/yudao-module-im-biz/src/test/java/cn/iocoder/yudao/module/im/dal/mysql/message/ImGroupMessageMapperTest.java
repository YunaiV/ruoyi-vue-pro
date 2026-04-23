package cn.iocoder.yudao.module.im.dal.mysql.message;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImGroupMessageDO;
import cn.iocoder.yudao.module.im.enums.message.ImGroupMessageReceiptStatusEnum;
import cn.iocoder.yudao.module.im.enums.message.ImMessageStatusEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IM 群聊消息 Mapper 单元测试
 *
 * @author 芋道源码
 */
public class ImGroupMessageMapperTest extends BaseDbUnitTest {

    @Resource
    private ImGroupMessageMapper mapper;

    private static final LocalDateTime FAR_PAST = LocalDateTime.of(2000, 1, 1, 0, 0, 0);

    // ========== selectListByMinId ==========

    @Test
    public void testSelectListByMinId_onlyReturnsGroupsInIdList() {
        // 准备：群 10 的消息 + 群 20 的消息
        ImGroupMessageDO msg10 = buildMessage(10L, 1L, ImMessageStatusEnum.UNREAD);
        mapper.insert(msg10);
        ImGroupMessageDO msg20 = buildMessage(20L, 1L, ImMessageStatusEnum.UNREAD);
        mapper.insert(msg20);

        // 调用：只查群 10
        List<ImGroupMessageDO> result = mapper.selectListByMinId(List.of(10L), 0L, FAR_PAST, 100);

        // 断言：只有 msg10
        assertEquals(1, result.size());
        assertEquals(msg10.getId(), result.get(0).getId());
    }

    @Test
    public void testSelectListByMinId_excludeRecall() {
        // 准备：一条正常 + 一条撤回
        ImGroupMessageDO normal = buildMessage(10L, 1L, ImMessageStatusEnum.UNREAD);
        mapper.insert(normal);
        ImGroupMessageDO recalled = buildMessage(10L, 1L, ImMessageStatusEnum.RECALL);
        mapper.insert(recalled);

        List<ImGroupMessageDO> result = mapper.selectListByMinId(List.of(10L), 0L, FAR_PAST, 100);

        assertEquals(1, result.size());
        assertEquals(normal.getId(), result.get(0).getId());
    }

    @Test
    public void testSelectListByMinId_sendTimeWindow() {
        // 准备：一条在窗口内，一条在窗口外
        ImGroupMessageDO inWindow = buildMessage(10L, 1L, ImMessageStatusEnum.UNREAD);
        inWindow.setSendTime(LocalDateTime.now().minusDays(1));
        mapper.insert(inWindow);
        ImGroupMessageDO outWindow = buildMessage(10L, 1L, ImMessageStatusEnum.UNREAD);
        outWindow.setSendTime(LocalDateTime.now().minusDays(40));
        mapper.insert(outWindow);

        List<ImGroupMessageDO> result = mapper.selectListByMinId(List.of(10L), 0L,
                LocalDateTime.now().minusDays(30), 100);

        assertEquals(1, result.size());
        assertEquals(inWindow.getId(), result.get(0).getId());
    }

    @Test
    public void testSelectListByMinId_sortAscLimit() {
        // 准备：插入 3 条
        ImGroupMessageDO m1 = buildMessage(10L, 1L, ImMessageStatusEnum.UNREAD);
        mapper.insert(m1);
        ImGroupMessageDO m2 = buildMessage(10L, 1L, ImMessageStatusEnum.UNREAD);
        mapper.insert(m2);
        ImGroupMessageDO m3 = buildMessage(10L, 1L, ImMessageStatusEnum.UNREAD);
        mapper.insert(m3);

        // 调用：size=2，返回 id 最小的 2 条
        List<ImGroupMessageDO> result = mapper.selectListByMinId(List.of(10L), 0L, FAR_PAST, 2);

        assertEquals(2, result.size());
        assertTrue(result.get(0).getId() < result.get(1).getId());
        assertEquals(m1.getId(), result.get(0).getId());
        assertEquals(m2.getId(), result.get(1).getId());
    }

    // ========== selectListByGroupIdAndMinIdAndQuitTimeBefore ==========

    @Test
    public void testSelectListByGroupIdAndMinIdAndQuitTimeBefore_onlyReturnsBeforeQuit() {
        // 准备：退群时间 = now-1 day，插入一条"退群前"，一条"退群后"
        LocalDateTime quitTime = LocalDateTime.now().minusDays(1);
        ImGroupMessageDO before = buildMessage(10L, 1L, ImMessageStatusEnum.UNREAD);
        before.setSendTime(LocalDateTime.now().minusDays(2));
        mapper.insert(before);
        ImGroupMessageDO after = buildMessage(10L, 1L, ImMessageStatusEnum.UNREAD);
        after.setSendTime(LocalDateTime.now().minusHours(1));
        mapper.insert(after);

        List<ImGroupMessageDO> result = mapper.selectListByGroupIdAndMinIdAndQuitTimeBefore(
                10L, 0L, FAR_PAST, quitTime, 100);

        // 断言：只返回退群前消息
        assertEquals(1, result.size());
        assertEquals(before.getId(), result.get(0).getId());
    }

    @Test
    public void testSelectListByGroupIdAndMinIdAndQuitTimeBefore_boundaryEqualsQuitTime() {
        // 准备：消息发送时间恰好等于退群时间（le 语义应包含该条）
        LocalDateTime quitTime = LocalDateTime.now().minusDays(1).withNano(0);
        ImGroupMessageDO boundary = buildMessage(10L, 1L, ImMessageStatusEnum.UNREAD);
        boundary.setSendTime(quitTime);
        mapper.insert(boundary);

        List<ImGroupMessageDO> result = mapper.selectListByGroupIdAndMinIdAndQuitTimeBefore(
                10L, 0L, FAR_PAST, quitTime, 100);

        assertEquals(1, result.size());
    }

    @Test
    public void testSelectListByGroupIdAndMinIdAndQuitTimeBefore_otherGroupExcluded() {
        // 准备：群 10 和群 20 各一条消息
        LocalDateTime quitTime = LocalDateTime.now();
        ImGroupMessageDO in10 = buildMessage(10L, 1L, ImMessageStatusEnum.UNREAD);
        in10.setSendTime(LocalDateTime.now().minusHours(2));
        mapper.insert(in10);
        ImGroupMessageDO in20 = buildMessage(20L, 1L, ImMessageStatusEnum.UNREAD);
        in20.setSendTime(LocalDateTime.now().minusHours(2));
        mapper.insert(in20);

        List<ImGroupMessageDO> result = mapper.selectListByGroupIdAndMinIdAndQuitTimeBefore(
                10L, 0L, FAR_PAST, quitTime, 100);

        assertEquals(1, result.size());
        assertEquals(in10.getId(), result.get(0).getId());
    }

    // ========== selectHistoryList ==========

    @Test
    public void testSelectHistoryList_basic() {
        ImGroupMessageDO m1 = buildMessage(10L, 1L, ImMessageStatusEnum.UNREAD);
        mapper.insert(m1);
        ImGroupMessageDO m2 = buildMessage(10L, 2L, ImMessageStatusEnum.UNREAD);
        mapper.insert(m2);
        // 别的群
        ImGroupMessageDO other = buildMessage(20L, 1L, ImMessageStatusEnum.UNREAD);
        mapper.insert(other);

        List<ImGroupMessageDO> result = mapper.selectHistoryList(10L, null, 100, null);

        // 断言：只返回 10 群的，按 id 倒序
        assertEquals(2, result.size());
        assertTrue(result.get(0).getId() > result.get(1).getId());
    }

    @Test
    public void testSelectHistoryList_excludeRecall() {
        ImGroupMessageDO normal = buildMessage(10L, 1L, ImMessageStatusEnum.UNREAD);
        mapper.insert(normal);
        ImGroupMessageDO recalled = buildMessage(10L, 1L, ImMessageStatusEnum.RECALL);
        mapper.insert(recalled);

        List<ImGroupMessageDO> result = mapper.selectHistoryList(10L, null, 100, null);

        assertEquals(1, result.size());
        assertEquals(normal.getId(), result.get(0).getId());
    }

    @Test
    public void testSelectHistoryList_maxIdCursor() {
        ImGroupMessageDO m1 = buildMessage(10L, 1L, ImMessageStatusEnum.UNREAD);
        mapper.insert(m1);
        ImGroupMessageDO m2 = buildMessage(10L, 1L, ImMessageStatusEnum.UNREAD);
        mapper.insert(m2);
        ImGroupMessageDO m3 = buildMessage(10L, 1L, ImMessageStatusEnum.UNREAD);
        mapper.insert(m3);

        List<ImGroupMessageDO> result = mapper.selectHistoryList(10L, m3.getId(), 100, null);

        // 断言：只返回 id < m3.id 的，即 m1、m2
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(m -> m.getId() < m3.getId()));
    }

    @Test
    public void testSelectHistoryList_filterByJoinTime() {
        // 准备：一条在入群前，一条在入群后
        LocalDateTime joinTime = LocalDateTime.now().minusHours(1);
        ImGroupMessageDO before = buildMessage(10L, 1L, ImMessageStatusEnum.UNREAD);
        before.setSendTime(LocalDateTime.now().minusHours(2));
        mapper.insert(before);
        ImGroupMessageDO after = buildMessage(10L, 1L, ImMessageStatusEnum.UNREAD);
        after.setSendTime(LocalDateTime.now().minusMinutes(30));
        mapper.insert(after);

        List<ImGroupMessageDO> result = mapper.selectHistoryList(10L, null, 100, joinTime);

        // 断言：入群前消息不可见
        assertEquals(1, result.size());
        assertEquals(after.getId(), result.get(0).getId());
    }

    // ========== selectBySenderIdAndClientMessageId ==========

    @Test
    public void testSelectBySenderIdAndClientMessageId() {
        ImGroupMessageDO msg = buildMessage(10L, 1L, ImMessageStatusEnum.UNREAD);
        msg.setClientMessageId("group-uuid-001");
        mapper.insert(msg);

        ImGroupMessageDO result = mapper.selectBySenderIdAndClientMessageId(1L, "group-uuid-001");
        assertNotNull(result);
        assertEquals(msg.getId(), result.getId());

        assertNull(mapper.selectBySenderIdAndClientMessageId(1L, "not-exist"));
    }

    // ========== selectListByGroupIdAndPendingReceipt ==========

    @Test
    public void testSelectListByGroupIdAndPendingReceipt_basic() {
        // 准备：待回执 + 已完成 + 无需回执，三种状态
        ImGroupMessageDO pending = buildMessage(10L, 1L, ImMessageStatusEnum.UNREAD);
        pending.setReceiptStatus(ImGroupMessageReceiptStatusEnum.PENDING.getStatus());
        mapper.insert(pending);
        ImGroupMessageDO done = buildMessage(10L, 1L, ImMessageStatusEnum.UNREAD);
        done.setReceiptStatus(ImGroupMessageReceiptStatusEnum.DONE.getStatus());
        mapper.insert(done);
        ImGroupMessageDO noReceipt = buildMessage(10L, 1L, ImMessageStatusEnum.UNREAD);
        noReceipt.setReceiptStatus(ImGroupMessageReceiptStatusEnum.NO_RECEIPT.getStatus());
        mapper.insert(noReceipt);

        List<ImGroupMessageDO> result = mapper.selectListByGroupIdAndPendingReceipt(10L, null, Long.MAX_VALUE);

        // 断言：只返回 pending 记录
        assertEquals(1, result.size());
        assertEquals(pending.getId(), result.get(0).getId());
    }

    @Test
    public void testSelectListByGroupIdAndPendingReceipt_windowAndRecall() {
        // 准备：窗口外 + 撤回 + 窗口内 pending
        ImGroupMessageDO belowRange = buildMessage(10L, 1L, ImMessageStatusEnum.UNREAD);
        belowRange.setReceiptStatus(ImGroupMessageReceiptStatusEnum.PENDING.getStatus());
        mapper.insert(belowRange);
        ImGroupMessageDO inRange = buildMessage(10L, 1L, ImMessageStatusEnum.UNREAD);
        inRange.setReceiptStatus(ImGroupMessageReceiptStatusEnum.PENDING.getStatus());
        mapper.insert(inRange);
        ImGroupMessageDO recalled = buildMessage(10L, 1L, ImMessageStatusEnum.RECALL);
        recalled.setReceiptStatus(ImGroupMessageReceiptStatusEnum.PENDING.getStatus());
        mapper.insert(recalled);

        // 调用：minId = belowRange.id（排除）, maxId = inRange.id（包含）
        List<ImGroupMessageDO> result = mapper.selectListByGroupIdAndPendingReceipt(
                10L, belowRange.getId(), inRange.getId());

        // 断言：只返回窗口内的非撤回 pending，即 inRange
        assertEquals(1, result.size());
        assertEquals(inRange.getId(), result.get(0).getId());
    }

    @Test
    public void testSelectListByGroupIdAndPendingReceipt_otherGroupExcluded() {
        ImGroupMessageDO g10 = buildMessage(10L, 1L, ImMessageStatusEnum.UNREAD);
        g10.setReceiptStatus(ImGroupMessageReceiptStatusEnum.PENDING.getStatus());
        mapper.insert(g10);
        ImGroupMessageDO g20 = buildMessage(20L, 1L, ImMessageStatusEnum.UNREAD);
        g20.setReceiptStatus(ImGroupMessageReceiptStatusEnum.PENDING.getStatus());
        mapper.insert(g20);

        List<ImGroupMessageDO> result = mapper.selectListByGroupIdAndPendingReceipt(10L, null, Long.MAX_VALUE);
        assertEquals(1, result.size());
        assertEquals(g10.getId(), result.get(0).getId());
    }

    // ========== 工具方法 ==========

    private ImGroupMessageDO buildMessage(Long groupId, Long senderId, ImMessageStatusEnum status) {
        return ImGroupMessageDO.builder()
                .clientMessageId("uuid-" + System.nanoTime())
                .senderId(senderId)
                .groupId(groupId)
                .type(0)
                .content("{\"content\":\"test\"}")
                .status(status.getStatus())
                .sendTime(LocalDateTime.now())
                .receiptStatus(ImGroupMessageReceiptStatusEnum.NO_RECEIPT.getStatus())
                .build();
    }

}
