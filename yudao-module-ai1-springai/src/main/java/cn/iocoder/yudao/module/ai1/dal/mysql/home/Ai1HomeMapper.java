package cn.iocoder.yudao.module.ai1.dal.mysql.home;

import cn.iocoder.yudao.module.ai1.controller.admin.home.vo.Ai1HomeMessageSummaryByAgentRespVO;
import cn.iocoder.yudao.module.ai1.controller.admin.home.vo.Ai1HomeMessageSummaryByDateRespVO;
import cn.iocoder.yudao.module.ai1.controller.admin.home.vo.Ai1HomeSummaryRespVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI1 首页统计 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface Ai1HomeMapper {

    @Select("SELECT (SELECT COUNT(1) FROM ai1_agent WHERE deleted = FALSE) AS agentCount, " +
            "(SELECT COUNT(1) FROM ai1_skill WHERE deleted = FALSE) AS skillCount, " +
            "(SELECT COUNT(1) FROM ai1_mcp WHERE deleted = FALSE) AS mcpCount, " +
            "(SELECT COUNT(1) FROM ai1_provider_model WHERE deleted = FALSE) AS modelCount")
    Ai1HomeSummaryRespVO selectSummary();

    /**
     * 按天统计消息数量，仅返回有消息的日期；使用 CAST AS DATE 兼容 MySQL 与 H2
     */
    @Select("SELECT CAST(create_time AS DATE) AS `date`, COUNT(1) AS `count` " +
            "FROM ai1_chat_message " +
            "WHERE create_time BETWEEN #{beginTime} AND #{endTime} AND deleted = FALSE " +
            "GROUP BY CAST(create_time AS DATE) " +
            "ORDER BY `date`")
    List<Ai1HomeMessageSummaryByDateRespVO> selectMessageSummaryListByCreateTimeBetweenGroupByDate(
            @Param("beginTime") LocalDateTime beginTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 按 Agent 统计消息数量，按数量倒序，数量相同按 Agent 编号升序；已删除对话下的消息不计入
     */
    @Select("SELECT c.agent_id AS agentId, COUNT(1) AS `count` " +
            "FROM ai1_chat_message m " +
            "INNER JOIN ai1_chat_conversation c ON m.conversation_id = c.id AND c.deleted = FALSE " +
            "WHERE m.create_time BETWEEN #{beginTime} AND #{endTime} AND m.deleted = FALSE " +
            "GROUP BY c.agent_id " +
            "ORDER BY `count` DESC, c.agent_id")
    List<Ai1HomeMessageSummaryByAgentRespVO> selectMessageSummaryListByCreateTimeBetweenGroupByAgentId(
            @Param("beginTime") LocalDateTime beginTime, @Param("endTime") LocalDateTime endTime);

}
