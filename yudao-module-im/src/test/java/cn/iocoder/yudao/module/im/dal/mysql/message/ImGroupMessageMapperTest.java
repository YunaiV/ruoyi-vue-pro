package cn.iocoder.yudao.module.im.dal.mysql.message;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImGroupMessageDO;
import cn.iocoder.yudao.module.im.enums.message.ImMessageReceiptStatusEnum;
import cn.iocoder.yudao.module.im.enums.message.ImMessageStatusEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link ImGroupMessageMapper} 的单元测试
 *
 * @author 芋道源码
 */
public class ImGroupMessageMapperTest extends BaseDbUnitTest {

    @Resource
    private ImGroupMessageMapper mapper;

    private static final LocalDateTime FAR_PAST = LocalDateTime.of(2000, 1, 1, 0, 0, 0);

    // ========== selectListByMinId ==========

    @Test
    public void testSelectListByMinId_filterByReceiverSnapshot() {
        // 准备：群 10 三条消息，快照分别命中 / 不命中用户 1
        ImGroupMessageDO visible = buildMessage(10L, 2L, ImMessageStatusEnum.NORMAL, List.of(1L, 2L));
        mapper.insert(visible);
        ImGroupMessageDO invisible = buildMessage(10L, 2L, ImMessageStatusEnum.NORMAL, List.of(2L, 3L));
        mapper.insert(invisible);
        ImGroupMessageDO selfSend = buildMessage(10L, 1L, ImMessageStatusEnum.NORMAL, List.of(1L, 2L));
        mapper.insert(selfSend);

        // 调用：用户 1 拉取群 10
        List<ImGroupMessageDO> result = mapper.selectListByMinId(1L, List.of(10L), 0L, FAR_PAST, 100);

        // 断言：仅返回快照包含用户 1 的消息
        assertEquals(2, result.size());
        assertEquals(visible.getId(), result.get(0).getId());
        assertEquals(selfSend.getId(), result.get(1).getId());
    }

    @Test
    public void testSelectListByMinId_excludeNullAndEmptySnapshot() {
        // 准备：快照为 null / 空字符串的老数据，不应被任何用户拉到
        ImGroupMessageDO nullSnapshot = buildMessage(10L, 2L, ImMessageStatusEnum.NORMAL, null);
        mapper.insert(nullSnapshot);
        ImGroupMessageDO emptySnapshot = buildMessage(10L, 2L, ImMessageStatusEnum.NORMAL, List.of());
        mapper.insert(emptySnapshot);

        List<ImGroupMessageDO> result = mapper.selectListByMinId(1L, List.of(10L), 0L, FAR_PAST, 100);

        assertTrue(result.isEmpty());
    }

    @Test
    public void testSelectListByMinId_onlyReturnsGroupsInIdList() {
        // 准备：群 10 + 群 20 各一条，用户 1 都在快照内
        ImGroupMessageDO msg10 = buildMessage(10L, 2L, ImMessageStatusEnum.NORMAL, List.of(1L, 2L));
        mapper.insert(msg10);
        ImGroupMessageDO msg20 = buildMessage(20L, 2L, ImMessageStatusEnum.NORMAL, List.of(1L, 2L));
        mapper.insert(msg20);

        // 调用：候选群只含群 10
        List<ImGroupMessageDO> result = mapper.selectListByMinId(1L, List.of(10L), 0L, FAR_PAST, 100);

        assertEquals(1, result.size());
        assertEquals(msg10.getId(), result.get(0).getId());
    }

    @Test
    public void testSelectListByMinId_includesRecall() {
        // 准备：撤回消息也要返回，由客户端按 status 渲染「此消息已撤回」占位
        ImGroupMessageDO normal = buildMessage(10L, 2L, ImMessageStatusEnum.NORMAL, List.of(1L));
        mapper.insert(normal);
        ImGroupMessageDO recalled = buildMessage(10L, 2L, ImMessageStatusEnum.RECALL, List.of(1L));
        mapper.insert(recalled);

        List<ImGroupMessageDO> result = mapper.selectListByMinId(1L, List.of(10L), 0L, FAR_PAST, 100);

        assertEquals(2, result.size());
    }

    @Test
    public void testSelectListByMinId_sendTimeWindow() {
        // 准备：一条在窗口内，一条在窗口外
        ImGroupMessageDO inWindow = buildMessage(10L, 2L, ImMessageStatusEnum.NORMAL, List.of(1L));
        inWindow.setSendTime(LocalDateTime.now().minusDays(1));
        mapper.insert(inWindow);
        ImGroupMessageDO outWindow = buildMessage(10L, 2L, ImMessageStatusEnum.NORMAL, List.of(1L));
        outWindow.setSendTime(LocalDateTime.now().minusDays(40));
        mapper.insert(outWindow);

        List<ImGroupMessageDO> result = mapper.selectListByMinId(1L, List.of(10L), 0L,
                LocalDateTime.now().minusDays(30), 100);

        assertEquals(1, result.size());
        assertEquals(inWindow.getId(), result.get(0).getId());
    }

    @Test
    public void testSelectListByMinId_sortAscLimit() {
        // 准备：插入 3 条可见消息
        ImGroupMessageDO m1 = buildMessage(10L, 2L, ImMessageStatusEnum.NORMAL, List.of(1L));
        mapper.insert(m1);
        ImGroupMessageDO m2 = buildMessage(10L, 2L, ImMessageStatusEnum.NORMAL, List.of(1L));
        mapper.insert(m2);
        ImGroupMessageDO m3 = buildMessage(10L, 2L, ImMessageStatusEnum.NORMAL, List.of(1L));
        mapper.insert(m3);

        // 调用：size=2，返回 id 最小的 2 条
        List<ImGroupMessageDO> result = mapper.selectListByMinId(1L, List.of(10L), 0L, FAR_PAST, 2);

        assertEquals(2, result.size());
        assertTrue(result.get(0).getId() < result.get(1).getId());
        assertEquals(m1.getId(), result.get(0).getId());
        assertEquals(m2.getId(), result.get(1).getId());
    }

    @Test
    public void testSelectListByMinId_minIdExclusive() {
        // 准备：游标之前的消息不返回
        ImGroupMessageDO m1 = buildMessage(10L, 2L, ImMessageStatusEnum.NORMAL, List.of(1L));
        mapper.insert(m1);
        ImGroupMessageDO m2 = buildMessage(10L, 2L, ImMessageStatusEnum.NORMAL, List.of(1L));
        mapper.insert(m2);

        List<ImGroupMessageDO> result = mapper.selectListByMinId(1L, List.of(10L), m1.getId(), FAR_PAST, 100);

        assertEquals(1, result.size());
        assertEquals(m2.getId(), result.get(0).getId());
    }

    // ========== selectHistoryListByUser ==========

    @Test
    public void testSelectHistoryListByUser_filterByReceiverSnapshot() {
        // 准备：群 10 三条，用户 1 在前两条快照内、第三条定向给别人
        ImGroupMessageDO m1 = buildMessage(10L, 2L, ImMessageStatusEnum.NORMAL, List.of(1L, 2L));
        mapper.insert(m1);
        ImGroupMessageDO m2 = buildMessage(10L, 2L, ImMessageStatusEnum.NORMAL, List.of(1L, 2L));
        mapper.insert(m2);
        ImGroupMessageDO directedOther = buildMessage(10L, 2L, ImMessageStatusEnum.NORMAL, List.of(2L, 3L));
        mapper.insert(directedOther);
        // 别的群
        ImGroupMessageDO other = buildMessage(20L, 2L, ImMessageStatusEnum.NORMAL, List.of(1L));
        mapper.insert(other);

        List<ImGroupMessageDO> result = mapper.selectHistoryListByUser(1L, 10L, null, 100);

        // 断言：只返回群 10 中用户 1 可见的，按 id 倒序
        assertEquals(2, result.size());
        assertEquals(m2.getId(), result.get(0).getId());
        assertEquals(m1.getId(), result.get(1).getId());
    }

    @Test
    public void testSelectHistoryListByUser_maxIdCursor() {
        ImGroupMessageDO m1 = buildMessage(10L, 2L, ImMessageStatusEnum.NORMAL, List.of(1L));
        mapper.insert(m1);
        ImGroupMessageDO m2 = buildMessage(10L, 2L, ImMessageStatusEnum.NORMAL, List.of(1L));
        mapper.insert(m2);
        ImGroupMessageDO m3 = buildMessage(10L, 2L, ImMessageStatusEnum.NORMAL, List.of(1L));
        mapper.insert(m3);

        List<ImGroupMessageDO> result = mapper.selectHistoryListByUser(1L, 10L, m3.getId(), 100);

        // 断言：只返回 id < m3.id 的 m1、m2
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(m -> m.getId() < m3.getId()));
    }

    // ========== 工具方法 ==========

    private ImGroupMessageDO buildMessage(Long groupId, Long senderId, ImMessageStatusEnum status,
                                          List<Long> receiverUserIds) {
        return ImGroupMessageDO.builder()
                .clientMessageId("uuid-" + System.nanoTime())
                .senderId(senderId)
                .groupId(groupId)
                .type(0)
                .content("{\"content\":\"test\"}")
                .status(status.getStatus())
                .sendTime(LocalDateTime.now())
                .receiverUserIds(receiverUserIds)
                .receiptStatus(ImMessageReceiptStatusEnum.NO_RECEIPT.getStatus())
                .build();
    }

}
