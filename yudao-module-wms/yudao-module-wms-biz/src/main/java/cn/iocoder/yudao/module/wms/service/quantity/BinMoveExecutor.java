package cn.iocoder.yudao.module.wms.service.quantity;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.util.JdbcUtils;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.vo.WmsStockBinRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo.WmsWarehouseProductVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.WmsStockBinDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.move.WmsStockBinMoveDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.move.item.WmsStockBinMoveItemDO;
import cn.iocoder.yudao.module.wms.enums.stock.WmsStockFlowDirection;
import cn.iocoder.yudao.module.wms.enums.stock.WmsStockReason;
import cn.iocoder.yudao.module.wms.service.quantity.context.BinMoveContext;
import cn.iocoder.yudao.module.wms.service.stock.bin.WmsStockBinService;
import cn.iocoder.yudao.module.wms.service.stock.bin.move.WmsStockBinMoveService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.STOCK_BIN_MOVE_ITEM_EXISTS;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.STOCK_BIN_MOVE_QUANTITY_ERROR;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.STOCK_BIN_NOT_ENOUGH;

/**
 * @author: LeeFJ
 * @date: 2025/3/25 9:34
 * @description: 库位移动数量执行器
 */
@Slf4j
@Component
public class BinMoveExecutor extends ActionExecutor<BinMoveContext> {



    @Resource
    @Lazy
    private WmsStockBinService stockBinService;

    @Resource
    @Lazy
    private WmsStockBinMoveService stockBinMoveService;

    public BinMoveExecutor() {
        super(WmsStockReason.STOCK_BIN_MOVE);
    }



    @Override
    public void execute(BinMoveContext context) {

        // 确认在事务内
        JdbcUtils.requireTransaction();

        // 准备需要的数据
        WmsStockBinMoveDO binMoveDO = context.getBinMoveDO();
        List<WmsStockBinMoveItemDO> binMoveItemDOList = context.getBinMoveItemDOList();
        if(CollectionUtils.isEmpty(binMoveItemDOList)) {
            throw exception(STOCK_BIN_MOVE_ITEM_EXISTS);
        }

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


        // 逐行处理
        for (WmsStockBinMoveItemDO binMoveItemDO : binMoveItemDOList) {
            WmsStockBinRespVO fromStockBin = stockBinMap.get(makeStockKey(binMoveItemDO.getFromBinId(),binMoveItemDO.getProductId()));
            WmsStockBinRespVO toStockBin = stockBinMap.get(makeStockKey(binMoveItemDO.getToBinId(),binMoveItemDO.getProductId()));
            this.processStockBin(binMoveDO.getWarehouseId(),binMoveItemDO,fromStockBin,toStockBin);
        }

        // 完成库位移动
        stockBinMoveService.finishMove(binMoveDO,binMoveItemDOList);

    }



    /**
     * 处理库存仓位
     **/
    private void processStockBin(Long warehouseId,WmsStockBinMoveItemDO binMoveItemDO,WmsStockBinRespVO fromStockBinVO,WmsStockBinRespVO toStockBinVO) {

        JdbcUtils.requireTransaction();

        // 处理出方
        WmsStockBinDO fromStockBinDO = BeanUtils.toBean(fromStockBinVO,WmsStockBinDO.class);
        fromStockBinDO.setAvailableQty(fromStockBinDO.getAvailableQty()-binMoveItemDO.getQty());
        fromStockBinDO.setSellableQty(fromStockBinDO.getSellableQty()-binMoveItemDO.getQty());
        // 保存
        stockBinService.insertOrUpdate(fromStockBinDO);
        // 记录流水
        stockFlowService.createForStockBin(this.getReason(), WmsStockFlowDirection.OUT, binMoveItemDO.getProductId(), fromStockBinDO , binMoveItemDO.getQty(), binMoveItemDO.getBinMoveId(), binMoveItemDO.getId());


        // 入方
        WmsStockBinDO toStockBinDO = stockBinService.getStockBin(binMoveItemDO.getToBinId(),toStockBinVO.getProductId(), true);
        // 可用库存
        toStockBinDO.setAvailableQty(toStockBinDO.getAvailableQty() + binMoveItemDO.getQty());
        // 可售库存
        toStockBinDO.setSellableQty(toStockBinDO.getSellableQty() + binMoveItemDO.getQty());
        // 保存
        stockBinService.insertOrUpdate(toStockBinDO);
        // 记录流水
        stockFlowService.createForStockBin(this.getReason(),WmsStockFlowDirection.IN, binMoveItemDO.getProductId(),toStockBinDO , binMoveItemDO.getQty(), binMoveItemDO.getBinMoveId(), binMoveItemDO.getId());
    }


    private String makeStockKey(Long binId,Long productId) {
        return binId+"-"+productId;
    }



}
