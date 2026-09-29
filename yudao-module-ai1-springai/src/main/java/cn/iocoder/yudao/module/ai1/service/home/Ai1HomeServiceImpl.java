package cn.iocoder.yudao.module.ai1.service.home;

import cn.iocoder.yudao.module.ai1.controller.admin.home.vo.Ai1HomeMessageShareRespVO;
import cn.iocoder.yudao.module.ai1.controller.admin.home.vo.Ai1HomeMessageTrendRespVO;
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

    // TODO @AI：必须传递值，且不需要 DAYS_DEFAULT、DAYS_MAX 变量；
    private static final int DAYS_DEFAULT = 30;
    private static final int DAYS_MAX = 30;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Resource
    private Ai1HomeMapper homeMapper;

    @Override
    public Ai1HomeSummaryRespVO getSummary() {
        return homeMapper.selectSummary();
    }

    @Override
    public List<Ai1HomeMessageTrendRespVO> getMessageTrend(Integer days) {
        // 1. 查询区间内有消息的日期
        int dayCount = normalizeDays(days);
        LocalDateTime endTime = getDayEndTime(LocalDateTime.now());
        LocalDateTime beginTime = getDayBeginTime(endTime.minusDays(dayCount - 1L));
        // TODO @AI：先查询；查询后，再去 convertmap；
        Map<String, Long> countMap = convertMap(homeMapper.selectMessageTrendListByCreateTimeBetween(beginTime, endTime),
                Ai1HomeMessageTrendRespVO::getDate, Ai1HomeMessageTrendRespVO::getCount);

        // 2. 生成连续日期序列，没有消息的日期补 0，保证折线不断点
        List<Ai1HomeMessageTrendRespVO> result = new ArrayList<>(dayCount);
        for (int i = 0; i < dayCount; i++) {
            String date = beginTime.plusDays(i).format(DATE_FORMATTER);
            result.add(new Ai1HomeMessageTrendRespVO().setDate(date).setCount(countMap.getOrDefault(date, 0L)));
        }
        return result;
    }

    @Override
    public List<Ai1HomeMessageShareRespVO> getMessageShare(Integer days) {
        LocalDateTime endTime = getDayEndTime(LocalDateTime.now());
        LocalDateTime beginTime = getDayBeginTime(endTime.minusDays(normalizeDays(days) - 1L));
        return homeMapper.selectMessageShareListByCreateTimeBetween(beginTime, endTime);
    }

    // TODO @AI：不用考虑这个归一化；
    /**
     * 归一化统计天数：为空或超出 1~30 时按 30 处理
     */
    private static int normalizeDays(Integer days) {
        return days == null || days < 1 || days > DAYS_MAX ? DAYS_DEFAULT : days;
    }

}
