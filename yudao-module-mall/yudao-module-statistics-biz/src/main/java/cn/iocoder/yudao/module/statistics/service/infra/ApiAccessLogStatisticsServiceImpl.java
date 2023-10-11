package cn.iocoder.yudao.module.statistics.service.infra;

import cn.iocoder.yudao.module.statistics.dal.mysql.infra.ApiAccessLogStatisticsMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * API 访问日志的统计 Service 实现类
 *
 * @author owen
 */
@Service
@Validated
public class ApiAccessLogStatisticsServiceImpl implements ApiAccessLogStatisticsService {

    @Resource
    private ApiAccessLogStatisticsMapper apiAccessLogStatisticsMapper;

    @Override
    public Integer getActiveUserCount(LocalDateTime beginTime, LocalDateTime endTime) {
        return apiAccessLogStatisticsMapper.selectCountByUserId(beginTime, endTime);
    }

    @Override
    public Integer getVisitorUserCount(LocalDateTime beginTime, LocalDateTime endTime) {
        return apiAccessLogStatisticsMapper.selectCountByIp(beginTime, endTime);
    }
}
