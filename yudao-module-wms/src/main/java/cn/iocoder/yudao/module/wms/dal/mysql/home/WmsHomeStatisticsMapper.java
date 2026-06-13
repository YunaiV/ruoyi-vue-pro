package cn.iocoder.yudao.module.wms.dal.mysql.home;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * WMS 首页统计 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface WmsHomeStatisticsMapper {

    /**
     * 按单据类型和状态统计单据数量
     *
     * @param warehouseId 仓库编号
     * @return [{ "orderType": 1, "status": 0, "count": 5 }, ...]
     */
    List<Map<String, Object>> selectOrderCountGroupByTypeAndStatus(@Param("warehouseId") Long warehouseId);

    /**
     * 按天聚合单据数量
     *
     * @param beginTime >= 开始时间
     * @param endTime < 结束时间
     * @param warehouseId 仓库编号
     * @return [{ "date": "2026-05-14", "orderType": 1, "count": 5 }, ...]
     */
    List<Map<String, Object>> selectDailyOrderTrend(@Param("beginTime") LocalDateTime beginTime,
                                                    @Param("endTime") LocalDateTime endTime,
                                                    @Param("warehouseId") Long warehouseId);

    /**
     * 统计库存总数量
     *
     * @param warehouseId 仓库编号
     * @return 库存总数量
     */
    BigDecimal selectInventoryTotalQuantity(@Param("warehouseId") Long warehouseId);

    /**
     * 按商品统计库存数量排行
     *
     * @param warehouseId 仓库编号
     * @param limit 数量限制
     * @return [{ "id": 1, "name": "A4 复印纸", "quantity": 100 }, ...]
     */
    List<Map<String, Object>> selectInventoryItemRank(@Param("warehouseId") Long warehouseId,
                                                      @Param("limit") Integer limit);

    /**
     * 按仓库统计库存数量排行
     *
     * @param warehouseId 仓库编号
     * @param limit 数量限制
     * @return [{ "id": 1, "name": "上海仓", "quantity": 100 }, ...]
     */
    List<Map<String, Object>> selectInventoryWarehouseRank(@Param("warehouseId") Long warehouseId,
                                                           @Param("limit") Integer limit);

}
