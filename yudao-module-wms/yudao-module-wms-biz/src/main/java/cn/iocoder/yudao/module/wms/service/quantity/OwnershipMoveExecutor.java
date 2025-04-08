package cn.iocoder.yudao.module.wms.service.quantity;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.mybatis.core.util.JdbcUtils;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.ownership.WmsStockOwnershipDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.ownership.move.WmsStockOwnershipMoveDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.ownership.move.item.WmsStockOwnershipMoveItemDO;
import cn.iocoder.yudao.module.wms.enums.stock.WmsStockFlowDirection;
import cn.iocoder.yudao.module.wms.enums.stock.WmsStockReason;
import cn.iocoder.yudao.module.wms.service.quantity.context.OwnershipMoveContext;
import cn.iocoder.yudao.module.wms.service.stock.ownership.move.WmsStockOwnershipMoveService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.STOCK_BIN_NOT_ENOUGH;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.STOCK_OWNERSHIP_MOVE_ITEM_NOT_EXISTS;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.STOCK_OWNERSHIP_MOVE_QUANTITY_ERROR;

/**
 * @author: LeeFJ
 * @date: 2025/3/25 9:34
 * @description: 库位移动数量执行器
 */
@Slf4j
@Component
public class OwnershipMoveExecutor extends ActionExecutor<OwnershipMoveContext> {





    @Resource
    @Lazy
    private WmsStockOwnershipMoveService stockOwnershipMoveService;

    public OwnershipMoveExecutor() {
        super(WmsStockReason.STOCK_OWNERSHIP_MOVE);
    }

    @Override
    public void execute(OwnershipMoveContext context) {

        // 确认在事务内
        JdbcUtils.requireTransaction();

        // 准备需要的数据
        WmsStockOwnershipMoveDO ownershipMoveDO = context.getOwnershipMoveDO();
        List<WmsStockOwnershipMoveItemDO>  ownershipMoveItemDOList = context.getOwnershipMoveItemDOS();
        if(CollectionUtils.isEmpty(ownershipMoveItemDOList)) {
            throw exception(STOCK_OWNERSHIP_MOVE_ITEM_NOT_EXISTS);
        }

        // 获得源库存
        List<Long> productIdList = StreamX.from(ownershipMoveItemDOList).toList(WmsStockOwnershipMoveItemDO::getProductId);
        List<WmsStockOwnershipDO> stockOwnershipDOList = stockOwnershipService.selectStockOwnership(ownershipMoveDO.getWarehouseId(),productIdList);
        Map<String,WmsStockOwnershipDO> stockOwnershipMap = StreamX.from(stockOwnershipDOList).toMap(e-> makeStockKey(e.getCompanyId(),e.getDeptId(),e.getProductId()));

        // 校验源库存量
        for (WmsStockOwnershipMoveItemDO ownershipMoveItemDO : ownershipMoveItemDOList) {

            // 校验单据内的移动数量
            if(ownershipMoveItemDO.getQty()<=0) {
                throw exception(STOCK_OWNERSHIP_MOVE_QUANTITY_ERROR);
            }

            // 校验库存余量
            WmsStockOwnershipDO stockOwnershipDO= stockOwnershipMap.get(makeStockKey(ownershipMoveItemDO.getFromCompanyId(),ownershipMoveItemDO.getFromDeptId(),ownershipMoveItemDO.getProductId()));
            Integer avaQty = 0;
            if(stockOwnershipDO!=null) {
                avaQty = stockOwnershipDO.getAvailableQty();
            }
            // 库存不足
            if(avaQty<ownershipMoveItemDO.getQty()) {
                throw exception(STOCK_BIN_NOT_ENOUGH);
            }

        }


        // 逐行处理
        for (WmsStockOwnershipMoveItemDO ownershipMoveItemDO : ownershipMoveItemDOList) {
            WmsStockOwnershipDO fromStockOwnership = stockOwnershipMap.get(makeStockKey(ownershipMoveItemDO.getFromCompanyId(),ownershipMoveItemDO.getFromDeptId(),ownershipMoveItemDO.getProductId()));
            this.processStockBin(ownershipMoveDO.getWarehouseId(),ownershipMoveItemDO,fromStockOwnership);
        }

        // 完成库位移动
        stockOwnershipMoveService.finishMove(ownershipMoveDO,ownershipMoveItemDOList);

    }



    /**
     * 处理库存仓位
     **/
    private void processStockBin(Long warehouseId,WmsStockOwnershipMoveItemDO ownershipMoveItemDO,WmsStockOwnershipDO fromStockOwnershipDO) {

        JdbcUtils.requireTransaction();

        // 处理出方
        fromStockOwnershipDO.setAvailableQty(fromStockOwnershipDO.getAvailableQty()-ownershipMoveItemDO.getQty());
        // 保存
        stockOwnershipService.insertOrUpdate(fromStockOwnershipDO);
        // 记录流水
        stockFlowService.createForStockOwnership(this.getReason(), WmsStockFlowDirection.OUT, ownershipMoveItemDO.getProductId(), fromStockOwnershipDO , ownershipMoveItemDO.getQty(), ownershipMoveItemDO.getOwnershipMoveId(), ownershipMoveItemDO.getId());

        // 入方
        WmsStockOwnershipDO toStockOwnershipDO = stockOwnershipService.getByUkProductOwner(warehouseId, ownershipMoveItemDO.getToCompanyId(), ownershipMoveItemDO.getToDeptId(), ownershipMoveItemDO.getProductId(), true);
        // 可用库存
        toStockOwnershipDO.setAvailableQty(toStockOwnershipDO.getAvailableQty() + ownershipMoveItemDO.getQty());
        // 保存
        stockOwnershipService.insertOrUpdate(toStockOwnershipDO);
        // 记录流水
        stockFlowService.createForStockOwnership(this.getReason(), WmsStockFlowDirection.IN, ownershipMoveItemDO.getProductId(), toStockOwnershipDO , ownershipMoveItemDO.getQty(), ownershipMoveItemDO.getOwnershipMoveId(), ownershipMoveItemDO.getId());

    }

    private String makeStockKey(Long companyId,Long deptId,Long productId) {
        return companyId+"-"+deptId+"-"+productId;
    }



}
