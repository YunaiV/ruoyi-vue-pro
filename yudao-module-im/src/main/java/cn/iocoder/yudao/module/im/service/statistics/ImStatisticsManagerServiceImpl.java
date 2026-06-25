package cn.iocoder.yudao.module.im.service.statistics;

import cn.hutool.core.convert.Convert;
import cn.iocoder.yudao.module.im.dal.mysql.statistics.ImStatisticsManagerMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * IM 数据看板 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ImStatisticsManagerServiceImpl implements ImStatisticsManagerService {

    @Resource
    private ImStatisticsManagerMapper statisticsMapper;

    // ==================== 用户 ====================

    @Override
    public Long getTotalUserCount() {
        return statisticsMapper.selectTotalUserCount();
    }

    @Override
    public Long getNewUserCount(LocalDateTime beginTime, LocalDateTime endTime) {
        return statisticsMapper.selectNewUserCount(beginTime, endTime);
    }

    @Override
    public Long getActiveUserCount(LocalDateTime beginTime, LocalDateTime endTime) {
        return statisticsMapper.selectActiveUserCount(beginTime, endTime);
    }

    @Override
    public Map<LocalDateTime, Long> getNewUserDailyCountMap(LocalDateTime beginTime, LocalDateTime endTime) {
        List<Map<String, Object>> rows = statisticsMapper.selectNewUserDailyCount(beginTime, endTime);
        return toDailyCountMap(rows);
    }

    @Override
    public Map<LocalDateTime, Long> getActiveUserDailyCountMap(LocalDateTime beginTime, LocalDateTime endTime) {
        List<Map<String, Object>> rows = statisticsMapper.selectActiveUserDailyCount(beginTime, endTime);
        return toDailyCountMap(rows);
    }

    // ==================== 群 ====================

    @Override
    public Long getTotalGroupCount() {
        return statisticsMapper.selectTotalGroupCount();
    }

    @Override
    public Long getNewGroupCount(LocalDateTime beginTime, LocalDateTime endTime) {
        return statisticsMapper.selectNewGroupCount(beginTime, endTime);
    }

    @Override
    public Map<String, Long> getGroupSizeCountMap() {
        List<Map<String, Object>> rows = statisticsMapper.selectGroupSizeDistribution();
        return convertMap(rows,
                row -> (String) row.get("range"),
                row -> Convert.toLong(row.get("count")));
    }

    // ==================== 消息 ====================

    @Override
    public Long getPrivateMessageCount(LocalDateTime beginTime, LocalDateTime endTime) {
        return statisticsMapper.selectPrivateMessageCount(beginTime, endTime);
    }

    @Override
    public Long getGroupMessageCount(LocalDateTime beginTime, LocalDateTime endTime) {
        return statisticsMapper.selectGroupMessageCount(beginTime, endTime);
    }

    @Override
    public Map<LocalDateTime, Long> getPrivateMessageDailyCountMap(LocalDateTime beginTime, LocalDateTime endTime) {
        List<Map<String, Object>> rows = statisticsMapper.selectPrivateMessageDailyCount(beginTime, endTime);
        return toDailyCountMap(rows);
    }

    @Override
    public Map<LocalDateTime, Long> getGroupMessageDailyCountMap(LocalDateTime beginTime, LocalDateTime endTime) {
        List<Map<String, Object>> rows = statisticsMapper.selectGroupMessageDailyCount(beginTime, endTime);
        return toDailyCountMap(rows);
    }

    @Override
    public Map<Integer, Long> getMessageTypeCountMap(LocalDateTime beginTime, LocalDateTime endTime) {
        List<Map<String, Object>> rows = statisticsMapper.selectMessageTypeDistribution(beginTime, endTime);
        return convertMap(rows,
                row -> Convert.toInt(row.get("type")),
                row -> Convert.toLong(row.get("count")));
    }

    @Override
    public Map<Long, Long> getTopSenderCountMap(LocalDateTime beginTime, LocalDateTime endTime, int limit) {
        List<Map<String, Object>> rows = statisticsMapper.selectTopSenders(beginTime, endTime, limit);
        return convertMap(rows,
                row -> Convert.toLong(row.get("userId")),
                row -> Convert.toLong(row.get("messageCount")));
    }

    /**
     * 把 [{date, count}] 行映射为 {LocalDateTime -> Long}；
     */
    private static Map<LocalDateTime, Long> toDailyCountMap(List<Map<String, Object>> rows) {
        return convertMap(rows,
                row -> Convert.convert(LocalDate.class, row.get("date")).atStartOfDay(),
                row -> Convert.toLong(row.get("count")));
    }

}
