package cn.iocoder.yudao.module.wms.service.quantity;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.util.JdbcUtils;
import cn.iocoder.yudao.module.system.enums.somle.BillType;
import cn.iocoder.yudao.module.wms.controller.admin.pickup.item.vo.WmsPickupItemSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.pickup.vo.WmsPickupSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.vo.WmsStockBinRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo.WmsWarehouseProductVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.WmsStockBinDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.move.WmsStockBinMoveDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.move.item.WmsStockBinMoveItemDO;
import cn.iocoder.yudao.module.wms.enums.stock.WmsStockFlowDirection;
import cn.iocoder.yudao.module.wms.enums.stock.WmsStockReason;
import cn.iocoder.yudao.module.wms.service.inbound.item.WmsInboundItemService;
import cn.iocoder.yudao.module.wms.service.pickup.WmsPickupService;
import cn.iocoder.yudao.module.wms.service.quantity.context.BinMoveContext;
import cn.iocoder.yudao.module.wms.service.stock.bin.WmsStockBinService;
import cn.iocoder.yudao.module.wms.service.stock.bin.move.WmsStockBinMoveService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.WmsErrorCodeConstants.*;
import static com.fhs.common.constant.Constant.ZERO;

/**
 * @author: LeeFJ
 * @date: 2025/3/25 9:34
 * @description: 库位移动数量执行器
 */
@Slf4j
@Component
public class BinMoveExecutor extends QuantityExecutor<BinMoveContext> {



    @Resource
    @Lazy
    private WmsStockBinService stockBinService;

    @Resource
    @Lazy
    private WmsStockBinMoveService stockBinMoveService;

    @Resource
    @Lazy
    private WmsInboundItemService inboundItemService;

    public BinMoveExecutor() {
        super(WmsStockReason.STOCK_BIN_MOVE);
    }


    @Resource
    private WmsPickupService pickupService;



    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(BinMoveContext context) {

        WmsPickupSaveReqVO fromPickup=new WmsPickupSaveReqVO();
        WmsPickupSaveReqVO toPickup=new WmsPickupSaveReqVO();

        // 确认在事务内
        JdbcUtils.requireTransaction();

        // 准备需要的数据
        Long inboundId = context.getInboundId();
        WmsStockBinMoveDO binMoveDO = context.getBinMoveDO();
        List<WmsStockBinMoveItemDO> binMoveItemDOList = context.getBinMoveItemDOList();
        if(CollectionUtils.isEmpty(binMoveItemDOList)) {
            throw exception(STOCK_BIN_MOVE_ITEM_EXISTS);
        }

        fromPickup.setWarehouseId(binMoveDO.getWarehouseId());
        fromPickup.setUpstreamType(BillType.WMS_BIN_MOVE.getValue());
        fromPickup.setUpstreamId(binMoveDO.getId());
        fromPickup.setUpstreamCode(binMoveDO.getNo());

        toPickup.setWarehouseId(binMoveDO.getWarehouseId());
        toPickup.setUpstreamType(BillType.WMS_BIN_MOVE.getValue());
        toPickup.setUpstreamId(binMoveDO.getId());
        toPickup.setUpstreamCode(binMoveDO.getNo());

        List<WmsWarehouseProductVO> warehouseProductList = StreamX.from(binMoveItemDOList).toList(item->{
            return WmsWarehouseProductVO.builder().warehouseId(binMoveDO.getWarehouseId()).productId(item.getProductId()).build();
        });

        // 校验源库存是否充足
        List<WmsStockBinRespVO> stockBinList = stockBinService.selectStockBinList(warehouseProductList, false);
        Map<String,WmsStockBinRespVO> stockBinMap = StreamX.from(stockBinList).toMap(e-> makeStockKey(e.getBinId(),e.getProductId()));
        for (WmsStockBinMoveItemDO binMoveItemDO : binMoveItemDOList) {

            if(binMoveItemDO.getQty()<=0) {
                throw exception(STOCK_BIN_MOVE_QUANTITY_ERROR);
            }

            WmsStockBinRespVO stockBinRespVO= stockBinMap.get(makeStockKey(binMoveItemDO.getFromBinId(),binMoveItemDO.getProductId()));
            Integer avaQty = 0;
            if(stockBinRespVO!=null) {
                avaQty = stockBinRespVO.getAvailableQty();
            }
            // 库存不足
            if(avaQty<binMoveItemDO.getQty()) {
                throw exception(STOCK_BIN_NOT_ENOUGH);
            }

        }

        List<WmsPickupItemSaveReqVO> fromPickupItemList =  new ArrayList<>();
        List<WmsPickupItemSaveReqVO> toPickupItemList =  new ArrayList<>();
        // 逐行处理
        for (WmsStockBinMoveItemDO binMoveItemDO : binMoveItemDOList) {
            WmsStockBinRespVO fromStockBin = stockBinMap.get(makeStockKey(binMoveItemDO.getFromBinId(),binMoveItemDO.getProductId()));
            WmsStockBinRespVO toStockBin = stockBinMap.get(makeStockKey(binMoveItemDO.getToBinId(),binMoveItemDO.getProductId()));
            if (fromStockBin == null) {
                throw exception(STOCK_BIN_NOT_EXISTS);
            } else if (toStockBin == null) {
                //目标库位无数据时，新建库位库存记录
                toStockBin = new WmsStockBinRespVO(fromStockBin);
                toStockBin.setBinId(binMoveItemDO.getToBinId());
                toStockBin.setId(null);
                toStockBin.setSellableQty(ZERO);
                toStockBin.setAvailableQty(ZERO);
            }
            // 未指定 inboundId 时，不通过拣货完成移库操作
            if(inboundId==null) {
                this.processStockBin(binMoveDO.getWarehouseId(), binMoveItemDO, fromStockBin, toStockBin);
            } else {
                // 指定 inboundId 时，通过拣货完成移库操作
                this.processStockBin(inboundId,binMoveDO.getWarehouseId(), binMoveItemDO, fromStockBin, toStockBin, fromPickupItemList, toPickupItemList);
            }
        }

        // 如果有指定 inboundId，则通过拣货完成移库操作
        if(inboundId!=null && !fromPickupItemList.isEmpty() && !toPickupItemList.isEmpty()) {
            fromPickup.setItemList(fromPickupItemList);
            toPickup.setItemList(toPickupItemList);
            pickupService.createForBinMove(fromPickup);
            pickupService.createForBinMove(toPickup);
        }

        // 完成库位移动
        stockBinMoveService.finishMove(binMoveDO,binMoveItemDOList);
        //todo 添加库存流水
    }

    /**
     * 通过拣货完成上下架移库操作
     **/
    private void processStockBin(Long inboundId, Long warehouseId, WmsStockBinMoveItemDO binMoveItemDO, WmsStockBinRespVO fromStockBinVO, WmsStockBinRespVO toStockBinVO, List<WmsPickupItemSaveReqVO> fromPickupItemList, List<WmsPickupItemSaveReqVO> toPickupItemList) {

        JdbcUtils.requireTransaction();

        // 处理出方
        WmsStockBinDO fromStockBinDO = BeanUtils.toBean(fromStockBinVO,WmsStockBinDO.class);

        fromStockBinDO.setAvailableQty(fromStockBinDO.getAvailableQty()-binMoveItemDO.getQty());
        if(fromStockBinDO.getAvailableQty()<0) {
            throw exception(STOCK_BIN_NOT_ENOUGH);
        }
        //
        fromStockBinDO.setSellableQty(fromStockBinDO.getSellableQty()-binMoveItemDO.getQty());
        if(fromStockBinDO.getSellableQty()<0) {
            throw exception(STOCK_BIN_NOT_ENOUGH);
        }

        WmsInboundItemDO inboundItemDO = inboundItemService.getByInboundIdAndProductId(inboundId, binMoveItemDO.getProductId());
        if(inboundItemDO==null) {
            throw exception(INBOUND_ITEM_NOT_EXISTS);
        }


        WmsPickupItemSaveReqVO fromPickupItem = new WmsPickupItemSaveReqVO();
        fromPickupItem.setInboundId(inboundId);
        fromPickupItem.setInboundItemId(inboundItemDO.getId());
        fromPickupItem.setProductId(binMoveItemDO.getProductId());
        fromPickupItem.setBinId(fromStockBinVO.getBinId());
        fromPickupItem.setQty(-binMoveItemDO.getQty());
        fromPickupItemList.add(fromPickupItem);

        WmsPickupItemSaveReqVO toPickupItem = new WmsPickupItemSaveReqVO();
        toPickupItem.setInboundId(inboundId);
        toPickupItem.setInboundItemId(inboundItemDO.getId());
        toPickupItem.setProductId(binMoveItemDO.getProductId());
        toPickupItem.setBinId(toStockBinVO.getBinId());
        toPickupItem.setQty(binMoveItemDO.getQty());
        toPickupItemList.add(toPickupItem);

    }



    /**
     * 处理仓位库存
     **/
    private void processStockBin(Long warehouseId,WmsStockBinMoveItemDO binMoveItemDO,WmsStockBinRespVO fromStockBinVO,WmsStockBinRespVO toStockBinVO) {

        JdbcUtils.requireTransaction();

        // 处理出方
        WmsStockBinDO fromStockBinDO = BeanUtils.toBean(fromStockBinVO,WmsStockBinDO.class);
        //


        fromStockBinDO.setAvailableQty(fromStockBinDO.getAvailableQty()-binMoveItemDO.getQty());
        if(fromStockBinDO.getAvailableQty()<0) {
            throw exception(STOCK_BIN_NOT_ENOUGH);
        }
        //
        fromStockBinDO.setSellableQty(fromStockBinDO.getSellableQty()-binMoveItemDO.getQty());
        if(fromStockBinDO.getSellableQty()<0) {
            throw exception(STOCK_BIN_NOT_ENOUGH);
        }
        // 保存
        stockBinService.insertOrUpdate(fromStockBinDO);
        // 记录流水
        stockFlowService.createForStockBin(this.getReason(), WmsStockFlowDirection.OUT, binMoveItemDO.getProductId(), fromStockBinDO, binMoveItemDO.getQty(), binMoveItemDO.getBinMoveId(), binMoveItemDO.getId(), fromStockBinDO.getBinId(), fromStockBinDO.getSellableQty(), fromStockBinDO.getSellableQty() - binMoveItemDO.getQty(), null);

        // 入方
        WmsStockBinDO toStockBinDO = stockBinService.getStockBin(binMoveItemDO.getToBinId(),binMoveItemDO.getProductId(), true);
        // 可用库存
        toStockBinDO.setAvailableQty(toStockBinDO.getAvailableQty() + binMoveItemDO.getQty());
        // 可售库存
        toStockBinDO.setSellableQty(toStockBinDO.getSellableQty() + binMoveItemDO.getQty());
        // 保存
        stockBinService.insertOrUpdate(toStockBinDO);
        // 记录流水
        stockFlowService.createForStockBin(this.getReason(), WmsStockFlowDirection.IN, binMoveItemDO.getProductId(), toStockBinDO, binMoveItemDO.getQty(), binMoveItemDO.getBinMoveId(), binMoveItemDO.getId(), toStockBinDO.getBinId(), toStockBinDO.getSellableQty(), toStockBinDO.getSellableQty() - binMoveItemDO.getQty(), null);
    }


    private String makeStockKey(Long binId,Long productId) {
        return binId+"-"+productId;
    }



}
