package cn.iocoder.yudao.module.ai1.dal.mysql.home;

import cn.iocoder.yudao.module.ai1.controller.admin.home.vo.Ai1HomeMessageSummaryByAgentRespVO;
import cn.iocoder.yudao.module.ai1.controller.admin.home.vo.Ai1HomeMessageSummaryByDateRespVO;
import cn.iocoder.yudao.module.ai1.controller.admin.home.vo.Ai1HomeSummaryRespVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
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

    @Select("SELECT (SELECT COUNT(1) FROM ai1_agent WHERE deleted = 0) AS agentCount, " +
            "(SELECT COUNT(1) FROM ai1_skill WHERE deleted = 0) AS skillCount, " +
            "(SELECT COUNT(1) FROM ai1_mcp WHERE deleted = 0) AS mcpCount, " +
            "(SELECT COUNT(1) FROM ai1_model WHERE deleted = 0) AS modelCount " +
            "${@cn.iocoder.yudao.framework.mybatis.core.util.SqlDialectUtils@fromDual()}")
    Ai1HomeSummaryRespVO selectSummary();

    /**
     * 按天统计消息数量，仅返回有消息的日期；日期按方言输出 yyyy-MM-dd，MySQL、H2 等保持 CAST AS DATE
     */
    @Select("SELECT ${@cn.iocoder.yudao.framework.mybatis.core.util.SqlDialectUtils@castDateText('create_time')} AS statDate, COUNT(1) AS count " +
            "FROM ai1_session_message " +
            "WHERE create_time BETWEEN #{beginTime} AND #{endTime} AND deleted = 0 " +
            "GROUP BY ${@cn.iocoder.yudao.framework.mybatis.core.util.SqlDialectUtils@castDateText('create_time')} " +
            "ORDER BY statDate")
    @Results({
            @Result(column = "statDate", property = "date"),
            @Result(column = "count", property = "count")
    })
    List<Ai1HomeMessageSummaryByDateRespVO> selectMessageSummaryListByCreateTimeBetweenGroupByDate(
            @Param("beginTime") LocalDateTime beginTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 按 Agent 统计消息数量，按数量倒序，数量相同按 Agent 编号升序；已删除会话下的消息不计入
     */
    @Select("SELECT c.agent_id AS agentId, COUNT(1) AS count " +
            "FROM ai1_session_message m " +
            "INNER JOIN ai1_session c ON m.session_id = c.id AND c.deleted = 0 " +
            "WHERE m.create_time BETWEEN #{beginTime} AND #{endTime} AND m.deleted = 0 " +
            "GROUP BY c.agent_id " +
            "ORDER BY count DESC, c.agent_id")
    List<Ai1HomeMessageSummaryByAgentRespVO> selectMessageSummaryListByCreateTimeBetweenGroupByAgentId(
            @Param("beginTime") LocalDateTime beginTime, @Param("endTime") LocalDateTime endTime);

}
