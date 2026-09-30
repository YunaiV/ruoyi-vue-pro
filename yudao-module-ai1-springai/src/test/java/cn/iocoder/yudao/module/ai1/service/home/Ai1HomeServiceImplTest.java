package cn.iocoder.yudao.module.ai1.service.home;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.ai1.controller.admin.home.vo.Ai1HomeMessageSummaryByAgentRespVO;
import cn.iocoder.yudao.module.ai1.controller.admin.home.vo.Ai1HomeMessageSummaryByDateRespVO;
import cn.iocoder.yudao.module.ai1.controller.admin.home.vo.Ai1HomeSummaryRespVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.agent.Ai1AgentDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.session.Ai1SessionDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.session.Ai1SessionMessageDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.mcp.Ai1McpDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.model.Ai1ModelDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.skill.Ai1SkillDO;
import cn.iocoder.yudao.module.ai1.dal.mysql.agent.Ai1AgentMapper;
import cn.iocoder.yudao.module.ai1.dal.mysql.session.Ai1SessionMapper;
import cn.iocoder.yudao.module.ai1.dal.mysql.session.Ai1SessionMessageMapper;
import cn.iocoder.yudao.module.ai1.dal.mysql.home.Ai1HomeMapper;
import cn.iocoder.yudao.module.ai1.dal.mysql.mcp.Ai1McpMapper;
import cn.iocoder.yudao.module.ai1.dal.mysql.model.Ai1ModelMapper;
import cn.iocoder.yudao.module.ai1.dal.mysql.skill.Ai1SkillMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.getDayBeginTime;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link Ai1HomeServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
@Import(Ai1HomeServiceImpl.class)
public class Ai1HomeServiceImplTest extends BaseDbUnitTest {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Resource
    private Ai1HomeServiceImpl homeService;

    @Resource
    private Ai1HomeMapper homeMapper;
    @Resource
    private Ai1AgentMapper agentMapper;
    @Resource
    private Ai1SkillMapper skillMapper;
    @Resource
    private Ai1McpMapper mcpMapper;
    @Resource
    private Ai1ModelMapper modelMapper;
    @Resource
    private Ai1SessionMapper sessionMapper;
    @Resource
    private Ai1SessionMessageMapper sessionMessageMapper;

    @Test
    public void testGetSummary() {
        // mock 数据：2 个 Agent、1 个 SKILL、1 个 MCP、3 个模型，其中 1 个 Agent 已删除
        agentMapper.insert(randomAgent());
        Ai1AgentDO deletedAgent = randomAgent();
        agentMapper.insert(deletedAgent);
        agentMapper.deleteById(deletedAgent.getId());
        agentMapper.insert(randomAgent());
        skillMapper.insert(randomPojo(Ai1SkillDO.class, o -> o.setStatus(0)));
        mcpMapper.insert(randomPojo(Ai1McpDO.class, o -> o.setStatus(0)));
        for (int i = 0; i < 3; i++) {
            modelMapper.insert(randomPojo(Ai1ModelDO.class, o -> o.setType(0).setStatus(0)));
        }

        // 调用
        Ai1HomeSummaryRespVO summary = homeService.getSummary();

        // 断言：已删除的不计入
        assertEquals(2L, summary.getAgentCount());
        assertEquals(1L, summary.getSkillCount());
        assertEquals(1L, summary.getMcpCount());
        assertEquals(3L, summary.getModelCount());
    }

    @Test
    public void testGetMessageSummaryByDate() {
        // mock 数据：今天 2 条、前天 1 条、区间外（8 天前）1 条、今天已删除 1 条
        LocalDateTime now = LocalDateTime.now();
        Long sessionId = insertSession(1L);
        insertSessionMessage(sessionId, now);
        insertSessionMessage(sessionId, now);
        insertSessionMessage(sessionId, now.minusDays(2));
        insertSessionMessage(sessionId, now.minusDays(8));
        Long deletedMessageId = insertSessionMessage(sessionId, now);
        sessionMessageMapper.deleteById(deletedMessageId);

        // 调用：近 7 天
        List<Ai1HomeMessageSummaryByDateRespVO> list = homeService.getMessageSummaryByDate(7);

        // 断言：连续 7 天，无消息的日期补 0，区间外和已删除不计入
        assertEquals(7, list.size());
        assertEquals(getDayBeginTime(now.minusDays(6)).format(DATE_FORMATTER), list.get(0).getDate());
        assertEquals(now.format(DATE_FORMATTER), list.get(6).getDate());
        assertEquals(2L, list.get(6).getCount());
        assertEquals(0L, list.get(5).getCount());
        assertEquals(1L, list.get(4).getCount());
        assertEquals(3L, list.stream().mapToLong(Ai1HomeMessageSummaryByDateRespVO::getCount).sum());
    }

    @Test
    public void testGetMessageSummaryByDate_daysGreaterThan30() {
        // 调用：超过 30 天时按实际天数统计
        List<Ai1HomeMessageSummaryByDateRespVO> list = homeService.getMessageSummaryByDate(60);

        // 断言
        assertEquals(60, list.size());
    }

    @Test
    public void testGetMessageSummaryByAgent() {
        // mock 数据：Agent 1 有 3 条、Agent 2 有 1 条、区间外 1 条（Agent 2）、已删除对话下 5 条（Agent 3）
        LocalDateTime now = LocalDateTime.now();
        Long session1 = insertSession(1L);
        Long session2 = insertSession(2L);
        Long deletedSession = insertSession(3L);
        insertSessionMessage(session1, now);
        insertSessionMessage(session1, now.minusDays(1));
        insertSessionMessage(session1, now.minusDays(3));
        insertSessionMessage(session2, now);
        insertSessionMessage(session2, now.minusDays(10));
        for (int i = 0; i < 5; i++) {
            insertSessionMessage(deletedSession, now);
        }
        sessionMapper.deleteById(deletedSession);

        // 调用：近 7 天
        List<Ai1HomeMessageSummaryByAgentRespVO> list = homeService.getMessageSummaryByAgent(7);

        // 断言：按消息数量倒序，区间外和已删除对话不计入
        assertEquals(2, list.size());
        assertEquals(1L, list.get(0).getAgentId());
        assertEquals(3L, list.get(0).getCount());
        assertEquals(2L, list.get(1).getAgentId());
        assertEquals(1L, list.get(1).getCount());
    }

    // ========== 随机对象 ==========

    private static Ai1AgentDO randomAgent() {
        return randomPojo(Ai1AgentDO.class, o -> {
            o.setStatus(randomEle(new Integer[]{0, 1}));
            o.setKnowledgeBaseIds(null).setMcpIds(null).setSkillIds(null);
        });
    }

    private Long insertSession(Long agentId) {
        Ai1SessionDO session = randomPojo(Ai1SessionDO.class, o -> o.setAgentId(agentId));
        sessionMapper.insert(session);
        return session.getId();
    }

    private Long insertSessionMessage(Long sessionId, LocalDateTime createTime) {
        Ai1SessionMessageDO message = randomPojo(Ai1SessionMessageDO.class, o -> {
            o.setSessionId(sessionId).setRole("user").setStatus(1);
            o.setCreateTime(createTime);
        });
        sessionMessageMapper.insert(message);
        return message.getId();
    }

}
