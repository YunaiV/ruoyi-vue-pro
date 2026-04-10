package cn.iocoder.yudao.module.mes.service.home;

import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.module.mes.controller.admin.home.vo.MesHomeProductionTrendRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.home.vo.MesHomeSummaryRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.home.vo.MesHomeWorkOrderStatusRespVO;
import cn.iocoder.yudao.module.mes.dal.mysql.home.MesHomeStatisticsMapper;
import cn.iocoder.yudao.module.mes.enums.dv.MesDvMachineryStatusEnum;
import cn.iocoder.yudao.module.mes.enums.pro.MesProWorkOrderStatusEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * MES 首页统计 Service 实现类
 *
 * @author 芋道源码
 */
@Service
public class MesHomeStatisticsServiceImpl implements MesHomeStatisticsService {

    @Resource
    private MesHomeStatisticsMapper homeStatisticsMapper;

    @Override
    public MesHomeSummaryRespVO getHomeSummary() {
        MesHomeSummaryRespVO summary = new MesHomeSummaryRespVO();

        // 1. 工单统计
        List<Map<String, Object>> workOrderStats = homeStatisticsMapper.selectWorkOrderCountGroupByStatus();
        long activeCount = 0L;
        long prepareCount = 0L;
        long finishedCount = 0L;
        for (Map<String, Object> stat : workOrderStats) {
            int status = ((Number) stat.get("status")).intValue();
            long count = ((Number) stat.get("count")).longValue();
            if (status == MesProWorkOrderStatusEnum.CONFIRMED.getStatus()) {
                activeCount = count;
            } else if (status == MesProWorkOrderStatusEnum.PREPARE.getStatus()) {
                prepareCount = count;
            } else if (status == MesProWorkOrderStatusEnum.FINISHED.getStatus()) {
                finishedCount = count;
            }
        }
        summary.setWorkOrderActiveCount(activeCount);
        summary.setWorkOrderPrepareCount(prepareCount);
        summary.setWorkOrderFinishedCount(finishedCount);

        // 2. 今日产量
        LocalDateTime today = LocalDateTimeUtils.getToday();
        LocalDateTime yesterday = LocalDateTimeUtils.getYesterday();
        Map<String, BigDecimal> todayFeedback = homeStatisticsMapper.selectFeedbackSummary(today, null);
        summary.setTodayOutput(getDecimal(todayFeedback, "feedbackQuantity"));
        summary.setTodayQualifiedQuantity(getDecimal(todayFeedback, "qualifiedQuantity"));
        summary.setTodayUnqualifiedQuantity(getDecimal(todayFeedback, "unqualifiedQuantity"));

        // 3. 昨日产量
        Map<String, BigDecimal> yesterdayFeedback = homeStatisticsMapper.selectFeedbackSummary(yesterday, today);
        summary.setYesterdayOutput(getDecimal(yesterdayFeedback, "feedbackQuantity"));

        // 4. 设备统计
        List<Map<String, Object>> machineryStats = homeStatisticsMapper.selectMachineryCountGroupByStatus();
        long total = 0L;
        long producing = 0L;
        long stop = 0L;
        long maintenance = 0L;
        for (Map<String, Object> stat : machineryStats) {
            int status = ((Number) stat.get("status")).intValue();
            long count = ((Number) stat.get("count")).longValue();
            total += count;
            if (status == MesDvMachineryStatusEnum.PRODUCING.getStatus()) {
                producing = count;
            } else if (status == MesDvMachineryStatusEnum.STOP.getStatus()) {
                stop = count;
            } else if (status == MesDvMachineryStatusEnum.MAINTENANCE.getStatus()) {
                maintenance = count;
            }
        }
        summary.setMachineryTotal(total);
        summary.setMachineryProducing(producing);
        summary.setMachineryStop(stop);
        summary.setMachineryMaintenance(maintenance);

        // 5. 异常/待办
        summary.setAndonActiveCount(homeStatisticsMapper.selectAndonActiveCount());
        summary.setRepairActiveCount(homeStatisticsMapper.selectRepairActiveCount());

        return summary;
    }

    @Override
    public List<MesHomeWorkOrderStatusRespVO> getWorkOrderStatusDistribution() {
        List<Map<String, Object>> stats = homeStatisticsMapper.selectWorkOrderCountGroupByStatus();
        List<MesHomeWorkOrderStatusRespVO> result = new ArrayList<>();
        for (MesProWorkOrderStatusEnum statusEnum : MesProWorkOrderStatusEnum.values()) {
            long count = 0L;
            for (Map<String, Object> stat : stats) {
                if (((Number) stat.get("status")).intValue() == statusEnum.getStatus()) {
                    count = ((Number) stat.get("count")).longValue();
                    break;
                }
            }
            result.add(new MesHomeWorkOrderStatusRespVO(statusEnum.getStatus(), statusEnum.getName(), count));
        }
        return result;
    }

    @Override
    public List<MesHomeProductionTrendRespVO> getProductionTrend(Integer days) {
        LocalDate endDate = LocalDate.now().plusDays(1);
        LocalDate startDate = endDate.minusDays(days);
        LocalDateTime beginTime = startDate.atStartOfDay();
        LocalDateTime endTime = endDate.atStartOfDay();

        // 查询数据库
        List<Map<String, Object>> dbData = homeStatisticsMapper.selectDailyFeedbackTrend(beginTime, endTime);
        Map<String, Map<String, Object>> dateMap = new LinkedHashMap<>();
        for (Map<String, Object> row : dbData) {
            dateMap.put((String) row.get("date"), row);
        }

        // 补全所有日期（无数据的日期填 0）
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<MesHomeProductionTrendRespVO> result = new ArrayList<>();
        for (LocalDate d = startDate; d.isBefore(endDate); d = d.plusDays(1)) {
            String dateStr = d.format(fmt);
            Map<String, Object> row = dateMap.get(dateStr);
            MesHomeProductionTrendRespVO vo = new MesHomeProductionTrendRespVO();
            vo.setDate(dateStr);
            if (row != null) {
                vo.setQuantity(getDecimalFromObj(row.get("quantity")));
                vo.setQualifiedQuantity(getDecimalFromObj(row.get("qualifiedQuantity")));
                vo.setUnqualifiedQuantity(getDecimalFromObj(row.get("unqualifiedQuantity")));
            } else {
                vo.setQuantity(BigDecimal.ZERO);
                vo.setQualifiedQuantity(BigDecimal.ZERO);
                vo.setUnqualifiedQuantity(BigDecimal.ZERO);
            }
            result.add(vo);
        }
        return result;
    }

    // ========== 工具方法 ==========

    private BigDecimal getDecimal(Map<String, BigDecimal> map, String key) {
        if (map == null || map.get(key) == null) {
            return BigDecimal.ZERO;
        }
        return map.get(key);
    }

    private BigDecimal getDecimalFromObj(Object obj) {
        if (obj == null) {
            return BigDecimal.ZERO;
        }
        if (obj instanceof BigDecimal) {
            return (BigDecimal) obj;
        }
        return new BigDecimal(obj.toString());
    }

}
