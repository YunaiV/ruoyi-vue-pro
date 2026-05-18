package cn.iocoder.yudao.module.im.dal.mysql.rtc;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.im.dal.dataobject.rtc.ImRtcParticipantDO;
import cn.iocoder.yudao.module.im.enums.rtc.ImRtcParticipantRoleEnum;
import cn.iocoder.yudao.module.im.enums.rtc.ImRtcParticipantStatusEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ImRtcParticipantMapperTest extends BaseDbUnitTest {

    @Resource
    private ImRtcParticipantMapper mapper;

    // ========== selectByRoomAndUserId ==========

    @Test
    public void testSelectByRoomAndUserId_match() {
        // 准备：room=r1 / userId=100 命中；同 room 其他 user 不应误命中
        ImRtcParticipantDO target = insert("r1", 100L, ImRtcParticipantStatusEnum.INVITING);
        insert("r1", 101L, ImRtcParticipantStatusEnum.INVITING);
        insert("r2", 100L, ImRtcParticipantStatusEnum.INVITING);

        // 调用 + 断言
        ImRtcParticipantDO got = mapper.selectByRoomAndUserId("r1", 100L);
        assertNotNull(got);
        assertEquals(target.getId(), got.getId());
    }

    @Test
    public void testSelectByRoomAndUserId_miss() {
        // 准备：room 不存在的查询返 null
        insert("r1", 100L, ImRtcParticipantStatusEnum.INVITING);

        // 调用 + 断言
        assertNull(mapper.selectByRoomAndUserId("r2", 100L));
        assertNull(mapper.selectByRoomAndUserId("r1", 999L));
    }

    // ========== selectListByRoom ==========

    @Test
    public void testSelectListByRoom_filterByRoom() {
        // 准备：r1 两条、r2 一条
        insert("r1", 100L, ImRtcParticipantStatusEnum.INVITING);
        insert("r1", 101L, ImRtcParticipantStatusEnum.JOINED);
        insert("r2", 200L, ImRtcParticipantStatusEnum.INVITING);

        // 调用 + 断言：仅 r1 命中两条
        List<ImRtcParticipantDO> result = mapper.selectListByRoom("r1");
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(p -> "r1".equals(p.getRoom())));
    }

    // ========== selectListByStatusAndInviteTimeBefore ==========

    @Test
    public void testSelectListByStatusAndInviteTimeBefore_filterStatusAndTime() {
        // 准备：3 条 INVITING（一旧两新）+ 1 条 JOINED（旧，状态不匹配）
        LocalDateTime now = LocalDateTime.now();
        ImRtcParticipantDO oldInviting = insert("r1", 100L, ImRtcParticipantStatusEnum.INVITING, now.minusMinutes(5));
        insert("r1", 101L, ImRtcParticipantStatusEnum.INVITING, now.minusSeconds(10));
        insert("r2", 102L, ImRtcParticipantStatusEnum.INVITING, now.minusSeconds(10));
        insert("r3", 103L, ImRtcParticipantStatusEnum.JOINED, now.minusMinutes(5));

        // 调用：cutoff = now - 1 分钟，只命中早于该时间的 INVITING
        List<ImRtcParticipantDO> result = mapper.selectListByStatusAndInviteTimeBefore(
                ImRtcParticipantStatusEnum.INVITING.getStatus(), now.minusMinutes(1));

        // 断言：仅 oldInviting 命中（status + time 双重过滤）
        assertEquals(1, result.size());
        assertEquals(oldInviting.getId(), result.get(0).getId());
    }

    @Test
    public void testSelectListByStatusAndInviteTimeBefore_emptyWhenNoMatch() {
        // 准备：所有 INVITING 都是新鲜的
        LocalDateTime now = LocalDateTime.now();
        insert("r1", 100L, ImRtcParticipantStatusEnum.INVITING, now.minusSeconds(10));

        // 调用 + 断言：cutoff 在更早的时间，无命中
        List<ImRtcParticipantDO> result = mapper.selectListByStatusAndInviteTimeBefore(
                ImRtcParticipantStatusEnum.INVITING.getStatus(), now.minusMinutes(5));
        assertTrue(result.isEmpty());
    }

    // ========== selectListByRoomAndStatusAndInviteTimeBefore ==========

    @Test
    public void testSelectListByRoomAndStatusAndInviteTimeBefore_limitToRoom() {
        // 准备：r1 一旧 INVITING；r2 一旧 INVITING；r1 一新 INVITING
        LocalDateTime now = LocalDateTime.now();
        ImRtcParticipantDO r1Old = insert("r1", 100L, ImRtcParticipantStatusEnum.INVITING, now.minusMinutes(5));
        insert("r2", 101L, ImRtcParticipantStatusEnum.INVITING, now.minusMinutes(5));
        insert("r1", 102L, ImRtcParticipantStatusEnum.INVITING, now.minusSeconds(10));

        // 调用：限定 room=r1，cutoff = now - 1 分钟
        List<ImRtcParticipantDO> result = mapper.selectListByRoomAndStatusAndInviteTimeBefore(
                "r1", ImRtcParticipantStatusEnum.INVITING.getStatus(), now.minusMinutes(1));

        // 断言：r2 的旧 INVITING 被 room 过滤掉；r1 的新 INVITING 被 time 过滤掉
        assertEquals(1, result.size());
        assertEquals(r1Old.getId(), result.get(0).getId());
    }

    @Test
    public void testSelectListByRoomAndStatusAndInviteTimeBefore_ignoresOtherStatus() {
        // 准备：r1 一旧 JOINED（虽然 time 满足，status 不匹配）+ 一旧 REJECTED
        LocalDateTime now = LocalDateTime.now();
        insert("r1", 100L, ImRtcParticipantStatusEnum.JOINED, now.minusMinutes(5));
        insert("r1", 101L, ImRtcParticipantStatusEnum.REJECTED, now.minusMinutes(5));

        // 调用 + 断言：仅扫 INVITING 状态，无命中
        List<ImRtcParticipantDO> result = mapper.selectListByRoomAndStatusAndInviteTimeBefore(
                "r1", ImRtcParticipantStatusEnum.INVITING.getStatus(), now.minusMinutes(1));
        assertTrue(result.isEmpty());
    }

    // ========== selectLastOneByUserIdAndStatus ==========

    @Test
    public void testSelectLastOneByUserIdAndStatus_returnsLatestActive() {
        // 准备：user 100 两条 INVITING（不同 room）+ 一条 LEFT
        insert("r1", 100L, ImRtcParticipantStatusEnum.LEFT);
        insert("r2", 100L, ImRtcParticipantStatusEnum.INVITING);
        ImRtcParticipantDO latest = insert("r3", 100L, ImRtcParticipantStatusEnum.JOINED);

        // 调用：忙线检测取 ACTIVE_STATUSES，应取 id 最大那条
        ImRtcParticipantDO got = mapper.selectLastOneByUserIdAndStatus(100L,
                ImRtcParticipantStatusEnum.ACTIVE_STATUSES);

        // 断言：返回最新的 JOINED 记录（id 最大）
        assertNotNull(got);
        assertEquals(latest.getId(), got.getId());
    }

    @Test
    public void testSelectLastOneByUserIdAndStatus_missWhenAllTerminal() {
        // 准备：user 100 只有终态记录
        insert("r1", 100L, ImRtcParticipantStatusEnum.LEFT);
        insert("r2", 100L, ImRtcParticipantStatusEnum.REJECTED);

        // 调用 + 断言：忙线检测无命中
        assertNull(mapper.selectLastOneByUserIdAndStatus(100L,
                ImRtcParticipantStatusEnum.ACTIVE_STATUSES));
    }

    // ========== updateByIdAndStatus（振铃超时 CAS 路径关键） ==========

    @Test
    public void testUpdateByIdAndStatus_inviting2NoAnswer_success() {
        // 准备：一条 INVITING 候选
        ImRtcParticipantDO p = insert("r1", 100L, ImRtcParticipantStatusEnum.INVITING, LocalDateTime.now().minusMinutes(5));

        // 调用：CAS INVITING → NO_ANSWER；模拟 Job 单参与者抢占
        int updated = mapper.updateByIdAndStatus(p.getId(),
                ImRtcParticipantStatusEnum.INVITING.getStatus(),
                new ImRtcParticipantDO().setId(p.getId())
                        .setStatus(ImRtcParticipantStatusEnum.NO_ANSWER.getStatus())
                        .setLeaveTime(LocalDateTime.now()));

        // 断言：CAS 成功 + 状态真的落地
        assertEquals(1, updated);
        assertEquals(ImRtcParticipantStatusEnum.NO_ANSWER.getStatus(),
                mapper.selectById(p.getId()).getStatus());
    }

    @Test
    public void testUpdateByIdAndStatus_concurrentReject_casFails() {
        // 准备：一条记录已经被用户主动 reject（状态变成 REJECTED）
        ImRtcParticipantDO p = insert("r1", 100L, ImRtcParticipantStatusEnum.REJECTED, LocalDateTime.now().minusMinutes(5));

        // 调用：Job 仍按 INVITING 抢占；并发已变更应失败
        int updated = mapper.updateByIdAndStatus(p.getId(),
                ImRtcParticipantStatusEnum.INVITING.getStatus(),
                new ImRtcParticipantDO().setId(p.getId())
                        .setStatus(ImRtcParticipantStatusEnum.NO_ANSWER.getStatus())
                        .setLeaveTime(LocalDateTime.now()));

        // 断言：CAS 失败 + 原状态保留
        assertEquals(0, updated);
        assertEquals(ImRtcParticipantStatusEnum.REJECTED.getStatus(),
                mapper.selectById(p.getId()).getStatus());
    }

    // ========== updateByRoomAndStatus（endSession 批量改 INVITING → NO_ANSWER 路径关键） ==========

    @Test
    public void testUpdateByRoomAndStatus_inviting2NoAnswer_batchUpdate() {
        // 准备：r1 两条 INVITING + 一条 JOINED（状态不匹配，不应被改）；r2 一条 INVITING（room 不匹配，不应被改）
        ImRtcParticipantDO inv1 = insert("r1", 100L, ImRtcParticipantStatusEnum.INVITING);
        ImRtcParticipantDO inv2 = insert("r1", 101L, ImRtcParticipantStatusEnum.INVITING);
        ImRtcParticipantDO joined = insert("r1", 102L, ImRtcParticipantStatusEnum.JOINED);
        ImRtcParticipantDO otherRoom = insert("r2", 103L, ImRtcParticipantStatusEnum.INVITING);

        // 调用：模拟 endSession 把残留 INVITING 批量改 NO_ANSWER
        int updated = mapper.updateByRoomAndStatus("r1",
                ImRtcParticipantStatusEnum.INVITING.getStatus(),
                new ImRtcParticipantDO().setStatus(ImRtcParticipantStatusEnum.NO_ANSWER.getStatus()));

        // 断言：仅 r1 的 2 条 INVITING 被改，其它保持
        assertEquals(2, updated);
        assertEquals(ImRtcParticipantStatusEnum.NO_ANSWER.getStatus(), mapper.selectById(inv1.getId()).getStatus());
        assertEquals(ImRtcParticipantStatusEnum.NO_ANSWER.getStatus(), mapper.selectById(inv2.getId()).getStatus());
        assertEquals(ImRtcParticipantStatusEnum.JOINED.getStatus(), mapper.selectById(joined.getId()).getStatus());
        assertEquals(ImRtcParticipantStatusEnum.INVITING.getStatus(), mapper.selectById(otherRoom.getId()).getStatus());
    }

    @Test
    public void testUpdateByRoomAndStatus_noMatch_returnsZero() {
        // 准备：r1 只有 JOINED 状态
        insert("r1", 100L, ImRtcParticipantStatusEnum.JOINED);

        // 调用：找 INVITING 应无命中
        int updated = mapper.updateByRoomAndStatus("r1",
                ImRtcParticipantStatusEnum.INVITING.getStatus(),
                new ImRtcParticipantDO().setStatus(ImRtcParticipantStatusEnum.NO_ANSWER.getStatus()));

        // 断言：0 行受影响
        assertEquals(0, updated);
    }

    private ImRtcParticipantDO insert(String room, Long userId, ImRtcParticipantStatusEnum status) {
        return insert(room, userId, status, LocalDateTime.now());
    }

    private ImRtcParticipantDO insert(String room, Long userId, ImRtcParticipantStatusEnum status, LocalDateTime inviteTime) {
        ImRtcParticipantDO p = new ImRtcParticipantDO()
                .setRoom(room)
                .setUserId(userId)
                .setRole(ImRtcParticipantRoleEnum.INVITEE.getRole())
                .setStatus(status.getStatus())
                .setInviteTime(inviteTime);
        mapper.insert(p);
        return p;
    }

}
