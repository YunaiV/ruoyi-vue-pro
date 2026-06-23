package cn.iocoder.yudao.module.im.dal.mysql.statistics;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupDO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupMemberDO;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImGroupMessageDO;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImPrivateMessageDO;
import cn.iocoder.yudao.module.im.dal.mysql.group.ImGroupMapper;
import cn.iocoder.yudao.module.im.dal.mysql.group.ImGroupMemberMapper;
import cn.iocoder.yudao.module.im.dal.mysql.message.ImGroupMessageMapper;
import cn.iocoder.yudao.module.im.dal.mysql.message.ImPrivateMessageMapper;
import cn.iocoder.yudao.module.im.enums.message.ImMessageStatusEnum;
import cn.iocoder.yudao.module.im.enums.ImContentTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link ImStatisticsManagerMapper} 的单元测试
 *
 * @author 芋道源码
 */
public class ImStatisticsManagerMapperTest extends BaseDbUnitTest {

    private static final LocalDateTime WINDOW_BEGIN = LocalDateTime.of(2026, 1, 1, 0, 0);
    private static final LocalDateTime WINDOW_END = LocalDateTime.of(2026, 2, 1, 0, 0);

    @Resource
    private ImStatisticsManagerMapper mapper;

    @Resource
    private ImPrivateMessageMapper privateMessageMapper;
    @Resource
    private ImGroupMessageMapper groupMessageMapper;
    @Resource
    private ImGroupMapper groupMapper;
    @Resource
    private ImGroupMemberMapper groupMemberMapper;
    @Resource
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void initJdbcTemplate() {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    // ========== 用户 ==========

    @Test
    public void testSelectTotalUserCount() {
        // 准备：3 个未删除用户
        insertUser(1L, LocalDateTime.now());
        insertUser(2L, LocalDateTime.now());
        insertUser(3L, LocalDateTime.now());

        // 调用 + 断言
        assertEquals(3L, mapper.selectTotalUserCount());
    }

    @Test
    public void testSelectNewUserCount_windowFilter() {
        // 准备：1 个在窗口内 + 2 个在窗口外
        insertUser(1L, WINDOW_BEGIN.plusDays(3));
        insertUser(2L, WINDOW_BEGIN.minusDays(1));
        insertUser(3L, WINDOW_END.plusDays(1));

        // 调用 + 断言：只命中窗口内
        assertEquals(1L, mapper.selectNewUserCount(WINDOW_BEGIN, WINDOW_END));
    }

    @Test
    public void testSelectActiveUserCount_distinctAcrossPrivateAndGroup() {
        // 准备：1 私聊；2 群聊；3 在窗口外；1 既发私聊又发群聊（去重）
        privateMessageMapper.insert(buildPrivate(1L, 2L, WINDOW_BEGIN.plusDays(1)));
        privateMessageMapper.insert(buildPrivate(1L, 2L, WINDOW_BEGIN.plusDays(2)));
        groupMessageMapper.insert(buildGroupMessage(2L, 100L, WINDOW_BEGIN.plusDays(3)));
        groupMessageMapper.insert(buildGroupMessage(1L, 100L, WINDOW_BEGIN.plusDays(4))); // 用户 1 已计一次
        privateMessageMapper.insert(buildPrivate(3L, 4L, WINDOW_BEGIN.minusDays(1)));     // 窗口外

        // 调用 + 断言：去重后 = 2
        assertEquals(2L, mapper.selectActiveUserCount(WINDOW_BEGIN, WINDOW_END));
    }

    @Test
    public void testSelectNewUserDailyCount_groupByDay() {
        // 准备：第 1 天 2 个；第 2 天 1 个；窗口外 1 个
        insertUser(1L, WINDOW_BEGIN.plusDays(1));
        insertUser(2L, WINDOW_BEGIN.plusDays(1));
        insertUser(3L, WINDOW_BEGIN.plusDays(2));
        insertUser(4L, WINDOW_BEGIN.minusDays(1));

        // 调用
        List<Map<String, Object>> list = mapper.selectNewUserDailyCount(WINDOW_BEGIN, WINDOW_END);

        // 断言：返回两个分组、总数 = 3
        assertEquals(2, list.size());
        long sum = list.stream().mapToLong(m -> ((Number) m.get("count")).longValue()).sum();
        assertEquals(3L, sum);
    }

    @Test
    public void testSelectActiveUserDailyCount_distinctPerDay() {
        // 准备：同一天里用户 1 在私聊与群聊都发了；第二天用户 2 发了
        privateMessageMapper.insert(buildPrivate(1L, 9L, WINDOW_BEGIN.plusDays(1)));
        groupMessageMapper.insert(buildGroupMessage(1L, 100L, WINDOW_BEGIN.plusDays(1)));
        groupMessageMapper.insert(buildGroupMessage(2L, 100L, WINDOW_BEGIN.plusDays(2)));

        // 调用
        List<Map<String, Object>> list = mapper.selectActiveUserDailyCount(WINDOW_BEGIN, WINDOW_END);

        // 断言：两天合计 2 名活跃用户（同一天 user 1 去重 + 第二天 user 2）
        assertEquals(2, list.size());
        long sum = list.stream().mapToLong(m -> ((Number) m.get("count")).longValue()).sum();
        assertEquals(2L, sum);
    }

    // ========== 群 ==========

    @Test
    public void testSelectTotalGroupCount_onlyEnabled() {
        // 准备：2 个正常 + 1 个已解散
        groupMapper.insert(buildGroup(CommonStatusEnum.ENABLE));
        groupMapper.insert(buildGroup(CommonStatusEnum.ENABLE));
        groupMapper.insert(buildGroup(CommonStatusEnum.DISABLE));

        // 调用 + 断言
        assertEquals(2L, mapper.selectTotalGroupCount());
    }

    @Test
    public void testSelectNewGroupCount_windowFilter() {
        // 准备：1 个窗口内 + 1 个窗口外
        groupMapper.insert(buildGroup(CommonStatusEnum.ENABLE));
        groupMapper.insert(buildGroup(CommonStatusEnum.ENABLE));
        // 窗口窄到只覆盖 1 秒
        LocalDateTime narrowBegin = LocalDateTime.now().plusYears(10);
        LocalDateTime narrowEnd = narrowBegin.plusSeconds(1);

        // 调用 + 断言：极窄窗口不命中任何群
        assertEquals(0L, mapper.selectNewGroupCount(narrowBegin, narrowEnd));
    }

    @Test
    public void testSelectGroupSizeDistribution_bucketing() {
        // 准备：一个 5 人群、一个 15 人群
        Long groupSmall = insertGroupAndMembers(5);
        Long groupMid = insertGroupAndMembers(15);
        assertNotNull(groupSmall);
        assertNotNull(groupMid);

        // 调用
        List<Map<String, Object>> dist = mapper.selectGroupSizeDistribution();

        // 断言：分桶包含「1-9 人」和「10-49 人」
        Map<Object, Long> byRange = dist.stream().collect(java.util.stream.Collectors.toMap(
                m -> m.get("range"), m -> ((Number) m.get("count")).longValue()));
        assertEquals(1L, byRange.get("1-9 人"));
        assertEquals(1L, byRange.get("10-49 人"));
    }

    // ========== 消息 ==========

    @Test
    public void testSelectPrivateMessageCount_windowFilter() {
        // 准备：1 在窗口内 + 1 在窗口外
        privateMessageMapper.insert(buildPrivate(1L, 2L, WINDOW_BEGIN.plusDays(1)));
        privateMessageMapper.insert(buildPrivate(1L, 2L, WINDOW_BEGIN.minusDays(1)));

        // 调用 + 断言
        assertEquals(1L, mapper.selectPrivateMessageCount(WINDOW_BEGIN, WINDOW_END));
    }

    @Test
    public void testSelectGroupMessageCount_windowFilter() {
        // 准备：1 在窗口内 + 1 在窗口外
        groupMessageMapper.insert(buildGroupMessage(1L, 100L, WINDOW_BEGIN.plusDays(1)));
        groupMessageMapper.insert(buildGroupMessage(1L, 100L, WINDOW_BEGIN.minusDays(1)));

        // 调用 + 断言
        assertEquals(1L, mapper.selectGroupMessageCount(WINDOW_BEGIN, WINDOW_END));
    }

    @Test
    public void testSelectMessageTypeDistribution_mergePrivateAndGroup() {
        // 准备：私聊 type=0 ×1，群聊 type=0 ×2、type=1 ×1
        privateMessageMapper.insert(buildPrivate(1L, 2L, WINDOW_BEGIN.plusDays(1)).setType(ImContentTypeEnum.TEXT.getType()));
        groupMessageMapper.insert(buildGroupMessage(1L, 100L, WINDOW_BEGIN.plusDays(1)).setType(ImContentTypeEnum.TEXT.getType()));
        groupMessageMapper.insert(buildGroupMessage(1L, 100L, WINDOW_BEGIN.plusDays(1)).setType(ImContentTypeEnum.TEXT.getType()));
        groupMessageMapper.insert(buildGroupMessage(1L, 100L, WINDOW_BEGIN.plusDays(1)).setType(ImContentTypeEnum.IMAGE.getType()));

        // 调用
        List<Map<String, Object>> dist = mapper.selectMessageTypeDistribution(WINDOW_BEGIN, WINDOW_END);

        // 断言：type=TEXT(0) → 3；type=IMAGE(1) → 1
        Map<Object, Long> byType = dist.stream().collect(java.util.stream.Collectors.toMap(
                m -> ((Number) m.get("type")).intValue(), m -> ((Number) m.get("count")).longValue()));
        assertEquals(3L, byType.get(ImContentTypeEnum.TEXT.getType()));
        assertEquals(1L, byType.get(ImContentTypeEnum.IMAGE.getType()));
    }

    @Test
    public void testSelectTopSenders_orderByCountDescAndLimit() {
        // 准备：user 1 共 3 条；user 2 共 2 条；user 3 共 1 条
        privateMessageMapper.insert(buildPrivate(1L, 9L, WINDOW_BEGIN.plusDays(1)));
        groupMessageMapper.insert(buildGroupMessage(1L, 100L, WINDOW_BEGIN.plusDays(1)));
        groupMessageMapper.insert(buildGroupMessage(1L, 100L, WINDOW_BEGIN.plusDays(1)));
        privateMessageMapper.insert(buildPrivate(2L, 9L, WINDOW_BEGIN.plusDays(1)));
        groupMessageMapper.insert(buildGroupMessage(2L, 100L, WINDOW_BEGIN.plusDays(1)));
        privateMessageMapper.insert(buildPrivate(3L, 9L, WINDOW_BEGIN.plusDays(1)));

        // 调用：取 TOP 2
        List<Map<String, Object>> tops = mapper.selectTopSenders(WINDOW_BEGIN, WINDOW_END, 2);

        // 断言：返回 2 条，按消息数倒序，user1 > user2
        assertEquals(2, tops.size());
        assertEquals(1L, ((Number) tops.get(0).get("userId")).longValue());
        assertEquals(3L, ((Number) tops.get(0).get("messageCount")).longValue());
        assertEquals(2L, ((Number) tops.get(1).get("userId")).longValue());
        assertEquals(2L, ((Number) tops.get(1).get("messageCount")).longValue());
    }

    // ========== 工具方法 ==========

    private void insertUser(Long id, LocalDateTime createTime) {
        jdbcTemplate.update(
                "INSERT INTO system_users (id, username, password, nickname, status, create_time, update_time, deleted, tenant_id) " +
                        "VALUES (?, ?, '', ?, 0, ?, ?, FALSE, 0)",
                id, "u" + id, "n" + id, createTime, createTime);
    }

    private static ImPrivateMessageDO buildPrivate(Long senderId, Long receiverId, LocalDateTime sendTime) {
        return ImPrivateMessageDO.builder()
                .clientMessageId("uuid-" + System.nanoTime())
                .senderId(senderId).receiverId(receiverId)
                .type(ImContentTypeEnum.TEXT.getType())
                .content("{}")
                .status(ImMessageStatusEnum.NORMAL.getStatus())
                .sendTime(sendTime).build();
    }

    private static ImGroupMessageDO buildGroupMessage(Long senderId, Long groupId, LocalDateTime sendTime) {
        return new ImGroupMessageDO()
                .setClientMessageId("uuid-" + System.nanoTime())
                .setSenderId(senderId).setGroupId(groupId)
                .setType(ImContentTypeEnum.TEXT.getType())
                .setContent("{}")
                .setStatus(ImMessageStatusEnum.NORMAL.getStatus())
                .setSendTime(sendTime);
    }

    private static ImGroupDO buildGroup(CommonStatusEnum status) {
        return ImGroupDO.builder().name("g" + System.nanoTime()).ownerUserId(1L)
                .status(status.getStatus()).build();
    }

    private Long insertGroupAndMembers(int memberCount) {
        ImGroupDO group = buildGroup(CommonStatusEnum.ENABLE);
        groupMapper.insert(group);
        for (int i = 0; i < memberCount; i++) {
            groupMemberMapper.insert(new ImGroupMemberDO()
                    .setGroupId(group.getId()).setUserId((long) (1000 + i))
                    .setStatus(CommonStatusEnum.ENABLE.getStatus()).setRole(3));
        }
        return group.getId();
    }

}
