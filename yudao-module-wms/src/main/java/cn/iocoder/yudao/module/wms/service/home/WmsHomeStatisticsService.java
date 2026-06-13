package cn.iocoder.yudao.module.wms.service.home;

import cn.iocoder.yudao.module.wms.controller.admin.home.vo.WmsHomeInventorySummaryRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.home.vo.WmsHomeOrderSummaryRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.home.vo.WmsHomeOrderTrendRespVO;

import java.util.List;

/**
 * WMS 首页统计 Service 接口
 *
 * @author 芋道源码
 */
public interface WmsHomeStatisticsService {

    /**
     * 获得单据汇总统计
     *
     * @param warehouseId 仓库编号
     * @return 单据汇总统计
     */
    List<WmsHomeOrderSummaryRespVO> getOrderSummary(Long warehouseId);

    /**
     * 获得单据趋势，按日期统计入库、出库、移库、盘库单据数量。
     *
     * @param days 最近天数，包含今天
     * @param warehouseId 仓库编号，为空时统计全部仓库
     * @return 单据趋势
     */
    List<WmsHomeOrderTrendRespVO> getOrderTrend(Integer days, Long warehouseId);

    /**
     * 获得库存汇总统计
     *
     * @param warehouseId 仓库编号
     * @param goodsLimit 商品排行数量
     * @param warehouseLimit 仓库排行数量
     * @return 库存汇总统计
     */
    WmsHomeInventorySummaryRespVO getInventorySummary(Long warehouseId, Integer goodsLimit, Integer warehouseLimit);

}
