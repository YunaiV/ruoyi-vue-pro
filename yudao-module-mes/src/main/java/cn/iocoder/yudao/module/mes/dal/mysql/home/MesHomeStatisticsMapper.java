package cn.iocoder.yudao.module.mes.dal.mysql.home;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * MES 首页统计 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface MesHomeStatisticsMapper {

    /**
     * 按状态统计工单数量
     *
     * @return [{ "status": 0, "count": 5 }, ...]
     */
    List<Map<String, Object>> selectWorkOrderCountGroupByStatus();

    /**
     * 统计报工产量合计
     *
     * @param beginTime >= 开始时间
     * @param endTime   < 结束时间
     * @return [feedbackQuantity, qualifiedQuantity, unqualifiedQuantity]
     */
    Map<String, BigDecimal> selectFeedbackSummary(@Param("beginTime") LocalDateTime beginTime,
                                                  @Param("endTime") LocalDateTime endTime);

    /**
     * 按设备状态统计设备台数
     *
     * @return [{ "status": 1, "count": 3 }, ...]
     */
    List<Map<String, Object>> selectMachineryCountGroupByStatus();

    /**
     * 统计未处置安灯记录数
     *
     * @return 数量
     */
    Long selectAndonActiveCount();

    /**
     * 统计未完成维修工单数（草稿 + 维修中 + 待验收）
     *
     * @return 数量
     */
    Long selectRepairActiveCount();

    /**
     * 按天聚合报工产量（近 N 天）
     *
     * @param beginTime >= 开始时间
     * @param endTime   < 结束时间
     * @return [{ "date": "2026-04-05", "quantity": 1234, "qualifiedQuantity": 1200, "unqualifiedQuantity": 34 }, ...]
     */
    List<Map<String, Object>> selectDailyFeedbackTrend(@Param("beginTime") LocalDateTime beginTime,
                                                       @Param("endTime") LocalDateTime endTime);

}
