package cn.iocoder.yudao.module.wms.service.quantity;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.util.JdbcUtils;
import cn.iocoder.yudao.module.system.enums.somle.BillType;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.WmsInboundItemSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.WmsInboundItemFlowDetailVO;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.item.vo.WmsOutboundItemRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.vo.WmsStockBinRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo.WmsWarehouseProductVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.exchange.WmsExchangeDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.exchange.item.WmsExchangeItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.flow.WmsItemFlowDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.WmsStockBinDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.logic.WmsStockLogicDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.warehouse.WmsStockWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.mysql.inbound.item.flow.WmsItemFlowMapper;
import cn.iocoder.yudao.module.wms.dal.mysql.stock.flow.WmsStockFlowMapper;
import cn.iocoder.yudao.module.wms.enums.stock.WmsStockFlowDirection;
import cn.iocoder.yudao.module.wms.enums.stock.WmsStockReason;
import cn.iocoder.yudao.module.wms.service.inbound.item.WmsInboundItemService;
import cn.iocoder.yudao.module.wms.service.inbound.item.flow.WmsItemFlowService;
import cn.iocoder.yudao.module.wms.service.quantity.context.ExchangeContext;
import cn.iocoder.yudao.module.wms.service.stock.bin.WmsStockBinService;
import cn.iocoder.yudao.module.wms.service.stock.flow.WmsStockFlowService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.WmsErrorCodeConstants.*;
import static cn.iocoder.yudao.module.wms.enums.exchange.WmsExchangeType.TO_GOOD;
import static cn.iocoder.yudao.module.wms.enums.exchange.WmsExchangeType.TO_ITEM;
import static cn.iocoder.yudao.module.wms.enums.stock.WmsStockFlowDirection.IN;
import static cn.iocoder.yudao.module.wms.enums.stock.WmsStockFlowDirection.OUT;
import static com.fhs.common.constant.Constant.MAX_INT;
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
    private WmsItemFlowService wmsInboundItemFlowService;

    @Resource
    @Lazy
    private WmsItemFlowMapper inboundItemFlowMapper;

    @Resource
    protected WmsStockFlowService stockFlowService;

    @Resource
    private WmsInboundItemService inboundItemService;

    @Resource
    @Lazy
    private WmsItemFlowService itemFlowService;

    @Resource
    private WmsStockFlowMapper stockFlowMapper;

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
            this.processSingleItem(exchangeDO, itemDO, stockBinMap);
        }

    }

    private void processSingleItem(WmsExchangeDO exchangeDO, WmsExchangeItemDO itemDO, Map<String, WmsStockBinRespVO> stockBinMap) {
        WmsStockBinRespVO fromStockBin = stockBinMap.get(makeStockKey(itemDO.getFromBinId(), itemDO.getProductId()));
        WmsStockBinRespVO toStockBin = stockBinMap.get(makeStockKey(itemDO.getToBinId(), itemDO.getProductId()));
        //处理入库批次库存
        this.processInboundItem(exchangeDO, itemDO);
        //处理仓库库存
        this.processStockWarehouseItem(exchangeDO, itemDO);
        //处理仓位库存
        this.processStockBin(exchangeDO, itemDO, fromStockBin, toStockBin);
        //处理逻辑库存
        this.processStockLogicItem(exchangeDO, itemDO);
    }

    /**
     * 处理逻辑库存
     */
    private void processStockLogicItem(WmsExchangeDO exchangeDO, WmsExchangeItemDO itemDO) {
        // 校验本方法在事务中
        JdbcUtils.requireTransaction();

        Integer type = exchangeDO.getType();
        Integer qty = itemDO.getQty();
        Integer direction = Objects.equals(type, TO_ITEM.getValue()) ? OUT.getValue() : IN.getValue();

        // 查询逻辑库存
        List<WmsStockLogicDO> stockLogicDOS = stockLogicService.selectByWarehouseIdAndProductId(exchangeDO.getWarehouseId(), itemDO.getProductId());
        if (stockLogicDOS == null) {
            throw exception(STOCK_LOGIC_NOT_EXISTS);
        }
        //更新数值
        this.updateStockLogicItemQty(qty, direction, stockLogicDOS, itemDO, type);

        // 记录批次流水...
    }

    private void updateStockLogicItemQty(Integer qty, Integer direction, List<WmsStockLogicDO> stockLogicDOS, WmsExchangeItemDO itemDO, Integer type) {
        // 更新逻辑库存
        if (Objects.equals(type, TO_ITEM.getValue())) {
            for (WmsStockLogicDO stockLogicDO : stockLogicDOS) {
                if (qty <= 0) {
                    break;
                }
                Integer deltaQty = Math.min(qty, stockLogicDO.getAvailableQty());
                stockLogicDO.setAvailableQty(stockLogicDO.getAvailableQty() + deltaQty * direction);
                stockLogicService.insertOrUpdate(stockLogicDO);
                // 记录库存流水
                int beforeQty = stockLogicDO.getAvailableQty() == null ? 0 : stockLogicDO.getAvailableQty();
                Integer afterQty = beforeQty + (itemDO.getQty() * direction);
                stockFlowService.createForStockLogic(this.getReason(), WmsStockFlowDirection.parse(direction), itemDO.getProductId(), stockLogicDO, itemDO.getQty(), null, null, beforeQty, afterQty, null);
            }
        } else {
            WmsStockLogicDO stockLogicDO = stockLogicDOS.get(0);
            stockLogicDO.setAvailableQty(stockLogicDO.getAvailableQty() + qty * direction);
            stockLogicService.insertOrUpdate(stockLogicDO);
            // 记录库存流水
            int beforeQty = stockLogicDO.getAvailableQty() == null ? 0 : stockLogicDO.getAvailableQty();
            Integer afterQty = beforeQty + (itemDO.getQty() * direction);
            stockFlowService.createForStockLogic(this.getReason(), WmsStockFlowDirection.parse(direction), itemDO.getProductId(), stockLogicDO, itemDO.getQty(), null, null, beforeQty, afterQty, null);
        }
    }

    /**
     * 处理入库批次库存
     **/
    private void processInboundItem(WmsExchangeDO exchangeDO, WmsExchangeItemDO itemDO) {
        // 校验本方法在事务中
        JdbcUtils.requireTransaction();

        Integer type = exchangeDO.getType();
        Integer qty = itemDO.getQty();
        Integer direction = Objects.equals(type, TO_ITEM.getValue()) ? OUT.getValue() : IN.getValue();

        //找出批次
        List<WmsInboundItemDO> inboundItemList = inboundItemService.selectByWarehouseIdAndProductId(exchangeDO.getWarehouseId(), itemDO.getProductId());
        if (inboundItemList.isEmpty()) {
            throw exception(INBOUND_ITEM_NOT_EXISTS);
        }
        this.updateInboundItemQty(qty, direction, inboundItemList, itemDO, type);
        // 记录批次流水...
    }

    private void updateInboundItemQty(Integer qty, Integer direction, List<WmsInboundItemDO> inboundItemList, WmsExchangeItemDO itemDO, Integer type) {
        // 更新批次库存
        if (Objects.equals(type, TO_ITEM.getValue())) {
            for (WmsInboundItemDO inboundItemDO : inboundItemList) {
                if (qty <= 0) {
                    break;
                }
                Integer deltaQty = Math.min(qty, inboundItemDO.getOutboundAvailableQty());
                inboundItemDO.setShelveClosedQty(inboundItemDO.getShelveClosedQty() + deltaQty * direction);
                inboundItemDO.setOutboundAvailableQty(inboundItemDO.getOutboundAvailableQty() + deltaQty * direction);
                inboundItemService.updateInboundItem(BeanUtils.toBean(inboundItemDO, WmsInboundItemSaveReqVO.class));
                qty = qty - deltaQty;
            }
        } else {
            WmsInboundItemDO inboundItemDO = inboundItemList.get(0);
            inboundItemDO.setShelveClosedQty(inboundItemDO.getShelveClosedQty() + qty * direction);
            inboundItemDO.setOutboundAvailableQty(inboundItemDO.getOutboundAvailableQty() + qty * direction);
            inboundItemService.updateInboundItem(BeanUtils.toBean(inboundItemDO, WmsInboundItemSaveReqVO.class));
        }
    }


    /**
     * 处理仓位库存
     **/
    private void processStockBin(WmsExchangeDO exchangeDO, WmsExchangeItemDO itemDO, WmsStockBinRespVO fromStockBinVO, WmsStockBinRespVO toStockBinVO) {

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
//        WmsInboundDO inboundDO = inboundService.getByWarehouseIdAndProductId(exchangeDO.getWarehouseId(), itemDO.getProductId());
//        WmsItemFlowDO itemFlowDO = wmsInboundItemFlowService.selectByInboundId(inboundDO.getId(), 1).get(0);

        // 记录批次流水(item_flow)
//        List<WmsItemFlowDO> itemFlowOutList = updateStockFlow(exchangeDO, itemDO, OUT.getValue());
        // 记录库存流水(stockFlow)
//        for (WmsItemFlowDO itemFlow : itemFlowOutList) {
        stockFlowService.createForStockBin(WmsStockReason.EXCHANGE, OUT, itemDO.getProductId(), fromStockBinDO, itemDO.getQty(), itemDO.getExchangeId(),
            itemDO.getId(), fromStockBinDO.getBinId(), fromStockBinVO.getSellableQty(), fromStockBinVO.getSellableQty() - itemDO.getQty(), null);
//        }

        // 入方
        WmsStockBinDO toStockBinDO = stockBinService.getStockBin(itemDO.getToBinId(), itemDO.getProductId(), true);
        // 可用库存
        toStockBinDO.setAvailableQty(toStockBinDO.getAvailableQty() + itemDO.getQty());
        // 可售库存
        toStockBinDO.setSellableQty(toStockBinDO.getSellableQty() + itemDO.getQty());
        // 保存
        stockBinService.insertOrUpdate(toStockBinDO);

        // 记录批次流水(item_flow)
//        List<WmsItemFlowDO> itemFlowInList = updateStockFlow(exchangeDO, itemDO, IN.getValue());
        // 记录库存流水(stockFlow)
        stockFlowService.createForStockBin(WmsStockReason.EXCHANGE, IN, itemDO.getProductId(), toStockBinDO, itemDO.getQty(), itemDO.getExchangeId(),
            itemDO.getId(), toStockBinDO.getBinId(), toStockBinDO.getSellableQty() - itemDO.getQty(), toStockBinDO.getSellableQty(), null);
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
        int beforeQty = stockWarehouseDO.getAvailableQty() == null ? 0 : stockWarehouseDO.getAvailableQty();
        Integer afterQty = beforeQty + (item.getQty() * wmsStockFlowDirection.getValue());
        stockFlowService.createForStockWarehouse(this.getReason(), wmsStockFlowDirection, item.getProductId(), stockWarehouseDO, item.getQty(), null, null, beforeQty, afterQty, null);

    }

    //生成批次可用库存流水，并把id更新到库存流水表
    protected List<WmsItemFlowDO> updateStockFlow(WmsExchangeDO exchangeDO, WmsExchangeItemDO item, Integer direction) {
        //找到入库单的批次流水
        List<WmsInboundItemFlowDetailVO> inboundDOList = itemFlowService.selectByProductIdAndBinIdAndWarehouseId(exchangeDO.getWarehouseId(), item.getFromBinId(), item.getProductId(), MAX_INT);
        if (inboundDOList.isEmpty()) {
            throw exception(INBOUND_ITEM_FLOW_NOT_EXISTS);
        }
        //找出所有批次的最新记录
        Map<String, WmsInboundItemFlowDetailVO> latestRecords = inboundDOList.stream()
            .collect(Collectors.toMap(
                record -> record.getInboundId() + "_" + record.getProductId(),
                record -> record,
                (existing, replacement) ->
                    existing.getCreateTime().after(replacement.getCreateTime()) ? existing : replacement
            ));
        Integer totalQty = item.getQty();
        List<Long> inboundIds = new ArrayList<>();
        //找到足够的批次来满足换货数量
        for (Map.Entry<String, WmsInboundItemFlowDetailVO> entry : latestRecords.entrySet()) {
            if (totalQty <= 0) {
                break;
            }
            WmsInboundItemFlowDetailVO value = entry.getValue();
            inboundIds.add(value.getInboundId());
            totalQty -= value.getOutboundAvailableQty();
        }

        //找到批次流水
        List<WmsItemFlowDO> flowDOList = itemFlowService.selectByInboundIds(inboundIds, ONE);

        List<Long> inboundItemIds = StreamX.from(flowDOList).toList(WmsItemFlowDO::getInboundItemId);
        List<WmsInboundItemDO> inboundItemsList = inboundItemService.selectByIds(inboundItemIds);
        List<WmsItemFlowDO> itemFlowList = new ArrayList<>();
        Map<Long, WmsInboundItemDO> map = StreamX.from(inboundItemsList).toMap(WmsInboundItemDO::getId);
        Integer deltaQty = item.getQty() * (direction);
        //1-良品转次品 , 2-次品转良品
        Integer type = exchangeDO.getType();
        for (WmsItemFlowDO flowDO : flowDOList) {
            WmsInboundItemDO inboundItemDO = map.get(flowDO.getInboundItemId());
            // 记录流水
            WmsItemFlowDO newFlowDO = new WmsItemFlowDO();
            newFlowDO.setInboundId(inboundItemDO.getInboundId());
            newFlowDO.setInboundItemId(inboundItemDO.getId());
            newFlowDO.setProductId(inboundItemDO.getProductId());
            newFlowDO.setBillType(BillType.WMS_EXCHANGE.getValue());

            newFlowDO.setDirection(direction);
            newFlowDO.setOutboundAvailableDeltaQty((item.getQty()) * (direction));
            //流入 转良品 && 流出转次品时，批次数量发生变更
            if ((direction.equals(IN.getValue()) && type.equals(TO_GOOD.getValue())) || direction.equals(OUT.getValue()) && type.equals(TO_ITEM.getValue())) {
                newFlowDO.setOutboundAvailableQty(inboundItemDO.getOutboundAvailableQty() + deltaQty);
                newFlowDO.setActualQty(inboundItemDO.getActualQty() + deltaQty);
                newFlowDO.setShelveClosedQty(inboundItemDO.getShelveClosedQty() + deltaQty);
                //更新明细行详情
                //inboundItemDO.setPlanQty(inboundItemDO.getPlanQty() + deltaQty);
                //inboundItemDO.setActualQty(inboundItemDO.getActualQty() + deltaQty);
                inboundItemDO.setOutboundAvailableQty(inboundItemDO.getOutboundAvailableQty() + deltaQty);
                inboundItemDO.setShelveClosedQty(inboundItemDO.getShelveClosedQty() + deltaQty);
            } else {
                newFlowDO.setOutboundAvailableQty(inboundItemDO.getOutboundAvailableQty());
                newFlowDO.setActualQty(inboundItemDO.getActualQty());
                newFlowDO.setShelveClosedQty(inboundItemDO.getShelveClosedQty());
            }

            itemFlowList.add(newFlowDO);


        }
        // 保存详情与流水
        inboundItemService.saveItems(inboundItemsList, itemFlowList);

        return itemFlowList;

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
            stockWarehouseDO.setAvailableQty(stockWarehouseDO.getAvailableQty() + quantity);
            if (stockWarehouseDO.getAvailableQty() < 0) {
                throw exception(STOCK_WAREHOUSE_NOT_ENOUGH);
            }
            // 可售量
            stockWarehouseDO.setSellableQty(stockWarehouseDO.getSellableQty() + quantity);
            if (stockWarehouseDO.getSellableQty() < 0) {
                throw exception(STOCK_WAREHOUSE_NOT_ENOUGH);
            }
            //不良品数量
            stockWarehouseDO.setDefectiveQty(stockWarehouseDO.getDefectiveQty() - quantity);
            if (stockWarehouseDO.getDefectiveQty() < 0) {
                throw exception(STOCK_WAREHOUSE_NOT_ENOUGH);
            }
        }

        return OUT;
    }


    private String makeStockKey(Long binId,Long productId) {
        return binId+"-"+productId;
    }



}
