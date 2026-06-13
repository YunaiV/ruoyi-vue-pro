package cn.iocoder.yudao.module.wms.service.home;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.module.wms.controller.admin.home.vo.WmsHomeInventorySummaryRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.home.vo.WmsHomeOrderStatusRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.home.vo.WmsHomeOrderSummaryRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.home.vo.WmsHomeOrderTrendRespVO;
import cn.iocoder.yudao.module.wms.dal.mysql.home.WmsHomeStatisticsMapper;
import cn.iocoder.yudao.module.wms.enums.order.WmsOrderStatusEnum;
import cn.iocoder.yudao.module.wms.enums.order.WmsOrderTypeEnum;
import cn.iocoder.yudao.module.wms.service.md.warehouse.WmsWarehouseService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.getSumValue;
import static cn.iocoder.yudao.framework.common.util.collection.MapUtils.getBigDecimal;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.getDateList;

/**
 * WMS 首页统计 Service 实现类
 *
 * @author 芋道源码
 */
@Service
public class WmsHomeStatisticsServiceImpl implements WmsHomeStatisticsService {

    @Resource
    private WmsHomeStatisticsMapper homeStatisticsMapper;
    @Resource
    private WmsWarehouseService warehouseService;

    @Override
    public List<WmsHomeOrderSummaryRespVO> getOrderSummary(Long warehouseId) {
        validateWarehouseIfPresent(warehouseId);
        // 查询单据数量
        List<Map<String, Object>> stats = homeStatisticsMapper.selectOrderCountGroupByTypeAndStatus(warehouseId);
        // 按照单据类型 + 单据状态分组
        return convertList(WmsOrderTypeEnum.values(), orderTypeEnum -> {
            List<WmsHomeOrderStatusRespVO> statuses = convertList(WmsOrderStatusEnum.values(), statusEnum ->
                    new WmsHomeOrderStatusRespVO().setStatus(statusEnum.getStatus())
                            .setCount(getOrderCount(stats, orderTypeEnum.getType(), statusEnum.getStatus())));
            Long total = getSumValue(statuses, WmsHomeOrderStatusRespVO::getCount, Long::sum, 0L);
            return new WmsHomeOrderSummaryRespVO().setType(orderTypeEnum.getType()).setTotal(total).setStatuses(statuses);
        });
    }

    @Override
    public List<WmsHomeOrderTrendRespVO> getOrderTrend(Integer days, Long warehouseId) {
        validateWarehouseIfPresent(warehouseId);
        // 统计区间为 [今天 - days + 1, 明天)，保证最近 days 天包含今天。
        LocalDate endDate = LocalDate.now().plusDays(1);
        LocalDate startDate = endDate.minusDays(days);
        LocalDateTime beginTime = startDate.atStartOfDay();
        LocalDateTime endTime = endDate.atStartOfDay();
        // 查询每天的单据数量
        List<Map<String, Object>> dbData = homeStatisticsMapper.selectDailyOrderTrend(beginTime, endTime, warehouseId);
        Map<String, Map<Integer, Long>> dateMap = new LinkedHashMap<>();
        for (Map<String, Object> row : dbData) {
            String date = MapUtil.getStr(row, "date");
            Integer orderType = MapUtil.getInt(row, "orderType");
            Long count = MapUtil.getLong(row, "count", 0L);
            dateMap.computeIfAbsent(date, key -> new HashMap<>()).put(orderType, count);
        }
        // 构造结果，保证每天都有数据
        return convertList(getDateList(startDate, days), d -> {
            String dateStr = DatePattern.NORM_DATE_FORMATTER.format(d);
            Map<Integer, Long> row = dateMap.getOrDefault(dateStr, Collections.emptyMap());
            return new WmsHomeOrderTrendRespVO().setTime(d.atStartOfDay())
                    .setReceiptCount(row.getOrDefault(WmsOrderTypeEnum.RECEIPT.getType(), 0L))
                    .setShipmentCount(row.getOrDefault(WmsOrderTypeEnum.SHIPMENT.getType(), 0L))
                    .setMovementCount(row.getOrDefault(WmsOrderTypeEnum.MOVEMENT.getType(), 0L))
                    .setCheckCount(row.getOrDefault(WmsOrderTypeEnum.CHECK.getType(), 0L));
        });
    }

    @Override
    public WmsHomeInventorySummaryRespVO getInventorySummary(Long warehouseId, Integer goodsLimit, Integer warehouseLimit) {
        validateWarehouseIfPresent(warehouseId);
        BigDecimal totalQuantity = ObjectUtil.defaultIfNull(homeStatisticsMapper.selectInventoryTotalQuantity(warehouseId), BigDecimal.ZERO);
        List<Map<String, Object>> itemRows = homeStatisticsMapper.selectInventoryItemRank(warehouseId, goodsLimit);
        List<Map<String, Object>> warehouseRows = homeStatisticsMapper.selectInventoryWarehouseRank(warehouseId, warehouseLimit);
        return new WmsHomeInventorySummaryRespVO().setTotalQuantity(totalQuantity)
                .setGoodsShareList(buildInventoryItemRankList(itemRows))
                .setWarehouseDistributionList(buildInventoryWarehouseRankList(warehouseRows));
    }

    // ==================== 工具方法 ====================

    /**
     * 仓库编号非空时，校验仓库是否存在，避免前端误传任意 id 直接落到 SQL。
     */
    private void validateWarehouseIfPresent(Long warehouseId) {
        if (warehouseId != null) {
            warehouseService.validateWarehouseExists(warehouseId);
        }
    }

    private long getOrderCount(List<Map<String, Object>> stats, Integer orderType, Integer status) {
        for (Map<String, Object> stat : stats) {
            if (ObjectUtil.equal(MapUtil.getInt(stat, "orderType"), orderType)
                    && ObjectUtil.equal(MapUtil.getInt(stat, "status"), status)) {
                return MapUtil.getLong(stat, "count", 0L);
            }
        }
        return 0L;
    }

    private List<WmsHomeInventorySummaryRespVO.ItemRank> buildInventoryItemRankList(List<Map<String, Object>> rows) {
        return convertList(rows, row -> new WmsHomeInventorySummaryRespVO.ItemRank()
                .setId(ObjectUtil.defaultIfNull(MapUtil.getLong(row, "id"), MapUtil.getLong(row, "itemId")))
                .setName(ObjectUtil.defaultIfNull(MapUtil.getStr(row, "name"), MapUtil.getStr(row, "itemName")))
                .setQuantity(getBigDecimal(row, "quantity", BigDecimal.ZERO)));
    }

    private List<WmsHomeInventorySummaryRespVO.WarehouseRank> buildInventoryWarehouseRankList(List<Map<String, Object>> rows) {
        return convertList(rows, row -> new WmsHomeInventorySummaryRespVO.WarehouseRank()
                .setId(ObjectUtil.defaultIfNull(MapUtil.getLong(row, "id"), MapUtil.getLong(row, "warehouseId")))
                .setName(ObjectUtil.defaultIfNull(MapUtil.getStr(row, "name"), MapUtil.getStr(row, "warehouseName")))
                .setQuantity(getBigDecimal(row, "quantity", BigDecimal.ZERO)));
    }

}
