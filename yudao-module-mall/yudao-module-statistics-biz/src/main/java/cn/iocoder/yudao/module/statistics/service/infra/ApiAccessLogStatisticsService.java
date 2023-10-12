package cn.iocoder.yudao.module.statistics.service.infra;

import java.time.LocalDateTime;

/**
 * API 访问日志的统计 Service 接口
 *
 * @author owen
 */
public interface ApiAccessLogStatisticsService {

    /**
     * 获取活跃用户数量
     *
     * @param beginTime 起始时间
     * @param endTime   截止时间
     * @return 活跃用户数量
     */
    Integer getActiveUserCount(LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 获取访问用户数量
     *
     * @param beginTime 起始时间
     * @param endTime   截止时间
     * @return 访问用户数量
     */
    Integer getVisitorUserCount(LocalDateTime beginTime, LocalDateTime endTime);

}
