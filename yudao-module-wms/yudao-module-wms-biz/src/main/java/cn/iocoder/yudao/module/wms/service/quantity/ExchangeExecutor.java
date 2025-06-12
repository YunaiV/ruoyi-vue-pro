package cn.iocoder.yudao.module.wms.service.quantity;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.util.JdbcUtils;
import cn.iocoder.yudao.module.system.enums.somle.BillType;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.item.vo.WmsOutboundItemRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.vo.WmsStockBinRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo.WmsWarehouseProductVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.exchange.WmsExchangeDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.exchange.item.WmsExchangeItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.flow.WmsItemFlowDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.WmsStockBinDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.warehouse.WmsStockWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.mysql.inbound.item.flow.WmsInboundItemFlowMapper;
import cn.iocoder.yudao.module.wms.dal.mysql.stock.flow.WmsStockFlowMapper;
import cn.iocoder.yudao.module.wms.enums.stock.WmsStockFlowDirection;
import cn.iocoder.yudao.module.wms.enums.stock.WmsStockReason;
import cn.iocoder.yudao.module.wms.service.inbound.item.flow.WmsInboundItemFlowService;
import cn.iocoder.yudao.module.wms.service.quantity.context.ExchangeContext;
import cn.iocoder.yudao.module.wms.service.stock.bin.WmsStockBinService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.WmsErrorCodeConstants.*;
import static cn.iocoder.yudao.module.wms.enums.exchange.WmsExchangeType.TO_GOOD;
import static cn.iocoder.yudao.module.wms.enums.exchange.WmsExchangeType.TO_ITEM;
import static com.fhs.common.constant.Constant.ONE;

/**
 * @author: LeeFJ
 * @date: 2025/3/25 9:34
 * @description: 库位移动数量执行器
 */
@Slf4j
@Component
public class ExchangeExecutor extends QuantityExecutor<ExchangeContext> {

    @Resource
    @Lazy
    private WmsStockBinService stockBinService;

    @Autowired
    private WmsInboundItemFlowService wmsInboundItemFlowService;

    public ExchangeExecutor() {
        super(WmsStockReason.STOCK_BIN_MOVE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(ExchangeContext context) {

        // 确认在事务内
        JdbcUtils.requireTransaction();

        // 准备需要的数据
        WmsExchangeDO exchangeDO = context.getExchangeDO();
        List<WmsExchangeItemDO> exchangeItemDOList = context.getExchangeItemDOList();
        if (CollectionUtils.isEmpty(exchangeItemDOList)) {
            throw exception(EXCHANGE_ITEM_ITEM_NOT_EXISTS);
        }

        List<WmsWarehouseProductVO> warehouseProductList = StreamX.from(exchangeItemDOList).toList(item -> {
            return WmsWarehouseProductVO.builder().warehouseId(exchangeDO.getWarehouseId()).productId(item.getProductId()).build();
        });

        // 校验源库存是否充足
        List<WmsStockBinRespVO> stockBinList = stockBinService.selectStockBinList(warehouseProductList, false);

        Map<String,WmsStockBinRespVO> stockBinMap = StreamX.from(stockBinList).toMap(e-> makeStockKey(e.getBinId(),e.getProductId()));
        for (WmsExchangeItemDO itemDO : exchangeItemDOList) {

            if (itemDO.getQty() <= 0) {
                throw exception(EXCHANGE_QUANTITY_ERROR);
            }

            WmsStockBinRespVO stockBinRespVO = stockBinMap.get(makeStockKey(itemDO.getFromBinId(), itemDO.getProductId()));
            Integer availableQty = 0;
            if(stockBinRespVO!=null) {
                availableQty = stockBinRespVO.getAvailableQty();
            }
            // 库存不足
            if (availableQty < itemDO.getQty()) {
                throw exception(STOCK_BIN_NOT_ENOUGH);
            }

        }

        //逐行处理
        for (WmsExchangeItemDO itemDO : exchangeItemDOList) {
            WmsStockBinRespVO fromStockBin = stockBinMap.get(makeStockKey(itemDO.getFromBinId(), itemDO.getProductId()));
            WmsStockBinRespVO toStockBin = stockBinMap.get(makeStockKey(itemDO.getToBinId(), itemDO.getProductId()));
            //处理仓位库存
            this.processStockBin(exchangeDO.getWarehouseId(), itemDO, fromStockBin, toStockBin);
            //处理仓库库存
            this.processStockWarehouseItem(exchangeDO, itemDO);
        }

        // 完成交换单
//        exchangeService.finishExchange(exchangeDO, exchangeItemDOList);

    }


    /**
     * 处理仓位库存
     **/
    private void processStockBin(Long warehouseId, WmsExchangeItemDO itemDO, WmsStockBinRespVO fromStockBinVO, WmsStockBinRespVO toStockBinVO) {

        JdbcUtils.requireTransaction();

        // 处理出方
        WmsStockBinDO fromStockBinDO = BeanUtils.toBean(fromStockBinVO,WmsStockBinDO.class);

        fromStockBinDO.setAvailableQty(fromStockBinDO.getAvailableQty() - itemDO.getQty());
        if(fromStockBinDO.getAvailableQty()<0) {
            throw exception(STOCK_BIN_NOT_ENOUGH);
        }
        //
        fromStockBinDO.setSellableQty(fromStockBinDO.getSellableQty() - itemDO.getQty());
        if(fromStockBinDO.getSellableQty()<0) {
            throw exception(STOCK_BIN_NOT_ENOUGH);
        }
        // 保存
        stockBinService.insertOrUpdate(fromStockBinDO);
        //查询入库批次
        WmsInboundDO inboundDO = inboundService.getByWarehouseIdAndProductId(warehouseId, itemDO.getProductId());
        WmsItemFlowDO inboundItemFlowDO = wmsInboundItemFlowService.selectByInboundId(inboundDO.getId(), 1).get(0);
        // 记录流水
        stockFlowService.createForStockBin(WmsStockReason.EXCHANGE, WmsStockFlowDirection.OUT, itemDO.getProductId(), fromStockBinDO, itemDO.getQty(), itemDO.getExchangeId(), itemDO.getId(), inboundItemFlowDO.getId());


        // 入方
        WmsStockBinDO toStockBinDO = stockBinService.getStockBin(itemDO.getToBinId(), itemDO.getProductId(), true);
        // 可用库存
        toStockBinDO.setAvailableQty(toStockBinDO.getAvailableQty() + itemDO.getQty());
        // 可售库存
        toStockBinDO.setSellableQty(toStockBinDO.getSellableQty() + itemDO.getQty());
        // 保存
        stockBinService.insertOrUpdate(toStockBinDO);
        // 记录流水
        stockFlowService.createForStockBin(WmsStockReason.EXCHANGE, WmsStockFlowDirection.IN, itemDO.getProductId(), toStockBinDO, itemDO.getQty(), itemDO.getExchangeId(), itemDO.getId(), inboundItemFlowDO.getId());
    }

    /**
     * 处理仓库库存
     **/
    private void processStockWarehouseItem(WmsExchangeDO exchangeDO, WmsExchangeItemDO item) {
        // 校验本方法在事务中
        JdbcUtils.requireTransaction();
        // 获得仓库库存记录
        WmsStockWarehouseDO stockWarehouseDO = stockWarehouseService.getStockWarehouse(exchangeDO.getWarehouseId(), item.getProductId(), false);

        WmsStockFlowDirection wmsStockFlowDirection = null;
        // 如果没有就创建
        if (stockWarehouseDO == null) {
            throw exception(STOCK_WAREHOUSE_NOT_EXISTS);
        } else {
            //校验数据转换
            wmsStockFlowDirection = this.updateStockWarehouseQty(exchangeDO, stockWarehouseDO, BeanUtils.toBean(item, WmsOutboundItemRespVO.class), item.getQty());
        }

        // 更新库存
        stockWarehouseService.insertOrUpdate(stockWarehouseDO);
        // 记录流水
        stockFlowService.createForStockWarehouse(this.getReason(), wmsStockFlowDirection, item.getProductId(), stockWarehouseDO, item.getQty(), null, null);

    }

    /**
     * 更新仓库库存
     */
    private WmsStockFlowDirection updateStockWarehouseQty(WmsExchangeDO exchangeDO, WmsStockWarehouseDO stockWarehouseDO, WmsOutboundItemRespVO item, Integer quantity) {

        // 类型 : 1-良品转次品 , 2-次品转良品
        int type = exchangeDO.getType();
        if (type == TO_ITEM.getValue()) {
            // 可用量
            stockWarehouseDO.setAvailableQty(stockWarehouseDO.getAvailableQty() - quantity);
            if (stockWarehouseDO.getAvailableQty() < 0) {
                throw exception(STOCK_WAREHOUSE_NOT_ENOUGH);
            }
            // 可售量
            stockWarehouseDO.setSellableQty(stockWarehouseDO.getSellableQty() - quantity);
            if (stockWarehouseDO.getSellableQty() < 0) {
                throw exception(STOCK_WAREHOUSE_NOT_ENOUGH);
            }
            //不良品数量
            stockWarehouseDO.setDefectiveQty(stockWarehouseDO.getDefectiveQty() + quantity);
            if (stockWarehouseDO.getDefectiveQty() < 0) {
                throw exception(STOCK_WAREHOUSE_NOT_ENOUGH);
            }
        }
        if (type == TO_GOOD.getValue()) {
            // 可用量
            stockWarehouseDO.setAvailableQty(stockWarehouseDO.getAvailableQty() - quantity);
            if (stockWarehouseDO.getAvailableQty() < 0) {
                throw exception(STOCK_WAREHOUSE_NOT_ENOUGH);
            }
            // 可售量
            stockWarehouseDO.setSellableQty(stockWarehouseDO.getSellableQty() - quantity);
            if (stockWarehouseDO.getSellableQty() < 0) {
                throw exception(STOCK_WAREHOUSE_NOT_ENOUGH);
            }
            //不良品数量
            stockWarehouseDO.setDefectiveQty(stockWarehouseDO.getDefectiveQty() - quantity);
            if (stockWarehouseDO.getDefectiveQty() < 0) {
                throw exception(STOCK_WAREHOUSE_NOT_ENOUGH);
            }
        }

        return WmsStockFlowDirection.OUT;
    }


    private String makeStockKey(Long binId,Long productId) {
        return binId+"-"+productId;
    }



}
