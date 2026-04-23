package cn.iocoder.yudao.module.im.dal.mysql.group;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupMemberDO;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IM 群成员 Mapper 单元测试
 *
 * @author 芋道源码
 */
public class ImGroupMemberMapperTest extends BaseDbUnitTest {

    @Resource
    private ImGroupMemberMapper mapper;

    // ========== selectQuitListByUserId ==========

    @Test
    public void testSelectQuitListByUserId_onlyDisabled() {
        // 准备：一条 ENABLE + 一条 DISABLE
        mapper.insert(buildMember(10L, 1L, CommonStatusEnum.ENABLE));
        ImGroupMemberDO quit = buildMember(20L, 1L, CommonStatusEnum.DISABLE);
        quit.setQuitTime(LocalDateTime.now().minusDays(1));
        mapper.insert(quit);

        // 调用：minQuitTime 为 null，不按时间过滤
        List<ImGroupMemberDO> result = mapper.selectQuitListByUserId(1L, null);

        // 断言：只返回退群记录
        assertEquals(1, result.size());
        assertEquals(quit.getId(), result.get(0).getId());
    }

    @Test
    public void testSelectQuitListByUserId_filterByMinQuitTime() {
        // 准备：两条退群记录，时间一早一晚
        LocalDateTime old = LocalDateTime.now().minusDays(10);
        LocalDateTime recent = LocalDateTime.now().minusDays(1);
        ImGroupMemberDO oldQuit = buildMember(10L, 1L, CommonStatusEnum.DISABLE);
        oldQuit.setQuitTime(old);
        mapper.insert(oldQuit);
        ImGroupMemberDO recentQuit = buildMember(20L, 1L, CommonStatusEnum.DISABLE);
        recentQuit.setQuitTime(recent);
        mapper.insert(recentQuit);

        // 调用：minQuitTime = now - 5 天，只返回近期退群
        List<ImGroupMemberDO> result = mapper.selectQuitListByUserId(1L, LocalDateTime.now().minusDays(5));
        assertEquals(1, result.size());
        assertEquals(recentQuit.getId(), result.get(0).getId());

        // 调用：minQuitTime 为 null，不过滤
        List<ImGroupMemberDO> all = mapper.selectQuitListByUserId(1L, null);
        assertEquals(2, all.size());
    }

    // ========== updateByGroupIdAndUserIdsAndStatus ==========

    @Test
    public void testUpdateByGroupIdAndUserIdsAndStatus_onlyUpdatesOldStatusMatches() {
        // 准备：u1 ENABLE，u2 DISABLE（已退群，不应更新）
        ImGroupMemberDO u1 = buildMember(10L, 1L, CommonStatusEnum.ENABLE);
        mapper.insert(u1);
        ImGroupMemberDO u2 = buildMember(10L, 2L, CommonStatusEnum.DISABLE);
        u2.setQuitTime(LocalDateTime.now().minusDays(5));
        mapper.insert(u2);

        // 调用：把 ENABLE 状态的成员移除
        LocalDateTime now = LocalDateTime.now();
        mapper.updateByGroupIdAndUserIdsAndStatus(10L, List.of(1L, 2L),
                CommonStatusEnum.ENABLE.getStatus(),
                new ImGroupMemberDO().setStatus(CommonStatusEnum.DISABLE.getStatus()).setQuitTime(now));

        // 断言：u1 被更新
        ImGroupMemberDO after1 = mapper.selectById(u1.getId());
        assertEquals(CommonStatusEnum.DISABLE.getStatus(), after1.getStatus());
        assertNotNull(after1.getQuitTime());
        // 断言：u2 的 quitTime 不变（仍为 5 天前）
        ImGroupMemberDO after2 = mapper.selectById(u2.getId());
        assertEquals(CommonStatusEnum.DISABLE.getStatus(), after2.getStatus());
        assertTrue(after2.getQuitTime().isBefore(now.minusDays(1)));
    }

    // ========== updateByGroupIdAndStatus ==========

    @Test
    public void testUpdateByGroupIdAndStatus_allActiveInGroup() {
        // 准备：群 10 有 2 个活跃 + 1 个已退；群 20 有 1 个活跃
        ImGroupMemberDO a1 = buildMember(10L, 1L, CommonStatusEnum.ENABLE);
        mapper.insert(a1);
        ImGroupMemberDO a2 = buildMember(10L, 2L, CommonStatusEnum.ENABLE);
        mapper.insert(a2);
        ImGroupMemberDO a3 = buildMember(10L, 3L, CommonStatusEnum.DISABLE);
        a3.setQuitTime(LocalDateTime.now().minusDays(5));
        mapper.insert(a3);
        ImGroupMemberDO b1 = buildMember(20L, 1L, CommonStatusEnum.ENABLE);
        mapper.insert(b1);

        // 调用：解散群 10
        mapper.updateByGroupIdAndStatus(10L, CommonStatusEnum.ENABLE.getStatus(),
                new ImGroupMemberDO().setStatus(CommonStatusEnum.DISABLE.getStatus())
                        .setQuitTime(LocalDateTime.now()));

        // 断言：群 10 的活跃成员全部变为 DISABLE，群 20 的不受影响
        assertEquals(CommonStatusEnum.DISABLE.getStatus(), mapper.selectById(a1.getId()).getStatus());
        assertEquals(CommonStatusEnum.DISABLE.getStatus(), mapper.selectById(a2.getId()).getStatus());
        assertEquals(CommonStatusEnum.ENABLE.getStatus(), mapper.selectById(b1.getId()).getStatus());
    }

    // ========== 工具方法 ==========

    private ImGroupMemberDO buildMember(Long groupId, Long userId, CommonStatusEnum status) {
        return ImGroupMemberDO.builder()
                .groupId(groupId).userId(userId)
                .status(status.getStatus())
                .joinTime(LocalDateTime.now().minusDays(30))
                .build();
    }

}
