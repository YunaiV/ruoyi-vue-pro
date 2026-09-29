package cn.iocoder.yudao.module.ai1.service.home;

import cn.iocoder.yudao.module.ai1.controller.admin.home.vo.Ai1HomeMessageShareRespVO;
import cn.iocoder.yudao.module.ai1.controller.admin.home.vo.Ai1HomeMessageTrendRespVO;
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
     * 获得近 days 个自然日（含今天）的消息趋势，没有消息的日期补 0，按日期升序
     *
     * @param days 天数，超出 1~30 时按 30 处理
     * @return 每日消息数量
     */
    List<Ai1HomeMessageTrendRespVO> getMessageTrend(Integer days);

    // TODO @AI：“超出 1~30 时按 30 处理”类似这样的注释，逻辑都去掉；允许前端传递更大的值；
    // TODO @AI：“名称由调用方补充”不用说明噢；
    /**
     * 获得近 days 个自然日（含今天）各 Agent 的消息数量，按数量倒序；名称由调用方补充
     *
     * @param days 天数，超出 1~30 时按 30 处理
     * @return 各 Agent 消息数量
     */
    List<Ai1HomeMessageShareRespVO> getMessageShare(Integer days);

}
