package cn.iocoder.yudao.module.ai1.service.home;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.ai1.controller.admin.home.vo.Ai1HomeMessageSummaryByAgentRespVO;
import cn.iocoder.yudao.module.ai1.controller.admin.home.vo.Ai1HomeMessageSummaryByDateRespVO;
import cn.iocoder.yudao.module.ai1.controller.admin.home.vo.Ai1HomeSummaryRespVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.agent.Ai1AgentDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.chat.Ai1ChatConversationDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.chat.Ai1ChatMessageDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.mcp.Ai1McpDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.provider.Ai1ModelDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.skill.Ai1SkillDO;
import cn.iocoder.yudao.module.ai1.dal.mysql.agent.Ai1AgentMapper;
import cn.iocoder.yudao.module.ai1.dal.mysql.chat.Ai1ChatConversationMapper;
import cn.iocoder.yudao.module.ai1.dal.mysql.chat.Ai1ChatMessageMapper;
import cn.iocoder.yudao.module.ai1.dal.mysql.home.Ai1HomeMapper;
import cn.iocoder.yudao.module.ai1.dal.mysql.mcp.Ai1McpMapper;
import cn.iocoder.yudao.module.ai1.dal.mysql.provider.Ai1ModelMapper;
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
    private Ai1ChatConversationMapper conversationMapper;
    @Resource
    private Ai1ChatMessageMapper messageMapper;

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
        Long conversationId = insertConversation(1L);
        insertMessage(conversationId, now);
        insertMessage(conversationId, now);
        insertMessage(conversationId, now.minusDays(2));
        insertMessage(conversationId, now.minusDays(8));
        Long deletedMessageId = insertMessage(conversationId, now);
        messageMapper.deleteById(deletedMessageId);

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
        Long conversation1 = insertConversation(1L);
        Long conversation2 = insertConversation(2L);
        Long deletedConversation = insertConversation(3L);
        insertMessage(conversation1, now);
        insertMessage(conversation1, now.minusDays(1));
        insertMessage(conversation1, now.minusDays(3));
        insertMessage(conversation2, now);
        insertMessage(conversation2, now.minusDays(10));
        for (int i = 0; i < 5; i++) {
            insertMessage(deletedConversation, now);
        }
        conversationMapper.deleteById(deletedConversation);

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

    private Long insertConversation(Long agentId) {
        Ai1ChatConversationDO conversation = randomPojo(Ai1ChatConversationDO.class, o -> o.setAgentId(agentId));
        conversationMapper.insert(conversation);
        return conversation.getId();
    }

    private Long insertMessage(Long conversationId, LocalDateTime createTime) {
        Ai1ChatMessageDO message = randomPojo(Ai1ChatMessageDO.class, o -> {
            o.setConversationId(conversationId).setRole("user").setStatus(1);
            o.setCreateTime(createTime);
        });
        messageMapper.insert(message);
        return message.getId();
    }

}
