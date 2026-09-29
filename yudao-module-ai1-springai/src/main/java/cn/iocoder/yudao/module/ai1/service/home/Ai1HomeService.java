package cn.iocoder.yudao.module.ai1.service.home;

import cn.iocoder.yudao.module.ai1.controller.admin.home.vo.Ai1HomeMessageSummaryByAgentRespVO;
import cn.iocoder.yudao.module.ai1.controller.admin.home.vo.Ai1HomeMessageSummaryByDateRespVO;
import cn.iocoder.yudao.module.ai1.controller.admin.home.vo.Ai1HomeSummaryRespVO;

import java.util.List;

/**
 * AI1 首页统计 Service 接口
 *
 * @author 芋道源码
 */
public interface Ai1HomeService {

    /**
     * 获得总量统计：Agent、SKILL、MCP、模型数量
     *
     * @return 总量统计
     */
    Ai1HomeSummaryRespVO getSummary();

    /**
     * 获得近 days 个自然日（含今天）的按日消息统计，没有消息的日期补 0，按日期升序
     *
     * @param days 天数
     * @return 每日消息数量
     */
    List<Ai1HomeMessageSummaryByDateRespVO> getMessageSummaryByDate(Integer days);

    /**
     * 获得近 days 个自然日（含今天）的按 Agent 消息统计，按数量倒序
     *
     * @param days 天数
     * @return 各 Agent 消息数量
     */
    List<Ai1HomeMessageSummaryByAgentRespVO> getMessageSummaryByAgent(Integer days);

}
