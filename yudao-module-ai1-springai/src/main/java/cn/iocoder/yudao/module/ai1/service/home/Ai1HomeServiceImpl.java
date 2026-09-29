package cn.iocoder.yudao.module.ai1.service.home;

import cn.iocoder.yudao.module.ai1.controller.admin.home.vo.Ai1HomeMessageSummaryByAgentRespVO;
import cn.iocoder.yudao.module.ai1.controller.admin.home.vo.Ai1HomeMessageSummaryByDateRespVO;
import cn.iocoder.yudao.module.ai1.controller.admin.home.vo.Ai1HomeSummaryRespVO;
import cn.iocoder.yudao.module.ai1.dal.mysql.home.Ai1HomeMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.getDayBeginTime;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.getDayEndTime;

/**
 * AI1 首页统计 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class Ai1HomeServiceImpl implements Ai1HomeService {

    // TODO @AI：hutool 应该有可替代的，直接使用，减少枚举；
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Resource
    private Ai1HomeMapper homeMapper;

    @Override
    public Ai1HomeSummaryRespVO getSummary() {
        return homeMapper.selectSummary();
    }

    @Override
    public List<Ai1HomeMessageSummaryByDateRespVO> getMessageSummaryByDate(Integer days) {
        // 1. 查询区间内有消息的日期
        LocalDateTime endTime = getDayEndTime(LocalDateTime.now());
        LocalDateTime beginTime = getDayBeginTime(endTime.minusDays(days - 1L));
        // 先查询按日聚合结果，再转换为“日期 -> 消息数量”的 Map，便于补齐没有消息的日期
        List<Ai1HomeMessageSummaryByDateRespVO> list = homeMapper.selectMessageSummaryListByCreateTimeBetweenGroupByDate(beginTime, endTime);
        Map<String, Long> countMap = convertMap(list, Ai1HomeMessageSummaryByDateRespVO::getDate, Ai1HomeMessageSummaryByDateRespVO::getCount);

        // 2. 生成连续日期序列，没有消息的日期补 0，保证折线不断点
        List<Ai1HomeMessageSummaryByDateRespVO> result = new ArrayList<>(days);
        for (int i = 0; i < days; i++) {
            String date = beginTime.plusDays(i).format(DATE_FORMATTER);
            result.add(new Ai1HomeMessageSummaryByDateRespVO().setDate(date).setCount(countMap.getOrDefault(date, 0L)));
        }
        return result;
    }

    @Override
    public List<Ai1HomeMessageSummaryByAgentRespVO> getMessageSummaryByAgent(Integer days) {
        LocalDateTime endTime = getDayEndTime(LocalDateTime.now());
        LocalDateTime beginTime = getDayBeginTime(endTime.minusDays(days - 1L));
        return homeMapper.selectMessageSummaryListByCreateTimeBetweenGroupByAgentId(beginTime, endTime);
    }

}
