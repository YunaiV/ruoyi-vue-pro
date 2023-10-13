package cn.iocoder.yudao.module.statistics.service.infra;

import java.time.LocalDateTime;

/**
 * API 访问日志的统计 Service 接口
 *
 * @author owen
 */
public interface ApiAccessLogStatisticsService {

    // TODO @疯狂：需要传递 userType；因为访问日志，可能涉及多种用户类型；
    // TODO @疯狂：方法名，要不改成 getUserCount；原因：让它更业务无关
    /**
     * 获取活跃用户数量
     *
     * @param beginTime 起始时间
     * @param endTime   截止时间
     * @return 活跃用户数量
     */
    Integer getActiveUserCount(LocalDateTime beginTime, LocalDateTime endTime);

    // TODO @疯狂：需要传递 userType；因为访问日志，可能涉及多种用户类型；
    // TODO @疯狂：方法名，要不改成 getIpCount；原因：让它更业务无关
    /**
     * 获取访问用户数量
     *
     * @param beginTime 起始时间
     * @param endTime   截止时间
     * @return 访问用户数量
     */
    Integer getVisitorUserCount(LocalDateTime beginTime, LocalDateTime endTime);

}
