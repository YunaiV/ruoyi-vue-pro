package cn.iocoder.yudao.module.wms.service.quantity;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.mybatis.core.util.JdbcUtils;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.logic.WmsStockLogicDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.logic.move.WmsStockLogicMoveDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.logic.move.item.WmsStockLogicMoveItemDO;
import cn.iocoder.yudao.module.wms.enums.stock.WmsStockReason;
import cn.iocoder.yudao.module.wms.service.quantity.context.LogicMoveContext;
import cn.iocoder.yudao.module.wms.service.stock.logic.move.WmsStockLogicMoveService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.WmsErrorCodeConstants.*;
import static cn.iocoder.yudao.module.wms.enums.stock.WmsStockFlowDirection.IN;
import static cn.iocoder.yudao.module.wms.enums.stock.WmsStockFlowDirection.OUT;

/**
 * @author: LeeFJ
 * @date: 2025/3/25 9:34
 * @description: 库位移动数量执行器
 */
@Slf4j
@Component
public class LogicMoveExecutor extends QuantityExecutor<LogicMoveContext> {


    @Resource
    @Lazy
    private WmsStockLogicMoveService stockLogicMoveService;

    public LogicMoveExecutor() {
        super(WmsStockReason.STOCK_LOGIC_MOVE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(LogicMoveContext context) {

        // 确认在事务内
        JdbcUtils.requireTransaction();

        // 准备需要的数据
        WmsStockLogicMoveDO logicMoveDO = context.getLogicMoveDO();
        List<WmsStockLogicMoveItemDO> logicMoveItemDOList = context.getLogicMoveItemDOS();
        if (CollectionUtils.isEmpty(logicMoveItemDOList)) {
            throw exception(STOCK_LOGIC_MOVE_ITEM_NOT_EXISTS);
        }

        // 获得源库存
        List<Long> productIdList = StreamX.from(logicMoveItemDOList).toList(WmsStockLogicMoveItemDO::getProductId);
        List<WmsStockLogicDO> stockLogicDOList = stockLogicService.selectStockLogic(logicMoveDO.getWarehouseId(), productIdList);
        Map<String, WmsStockLogicDO> stockLogicMap = StreamX.from(stockLogicDOList).toMap(e -> makeStockKey(e.getCompanyId(), e.getDeptId(), e.getProductId()));

        // 校验源库存量
        for (WmsStockLogicMoveItemDO logicMoveItemDO : logicMoveItemDOList) {

            // 校验单据内的移动数量
            if (logicMoveItemDO.getQty() <= 0) {
                throw exception(STOCK_LOGIC_MOVE_QUANTITY_ERROR);
            }

            // 校验库存余量
            WmsStockLogicDO stockLogicDO = stockLogicMap.get(makeStockKey(logicMoveItemDO.getFromCompanyId(), logicMoveItemDO.getFromDeptId(), logicMoveItemDO.getProductId()));
            Integer avaQty = 0;
            if (stockLogicDO != null) {
                avaQty = stockLogicDO.getAvailableQty();
            }
            // 库存不足
            if (avaQty < logicMoveItemDO.getQty()) {
                throw exception(STOCK_LOGIC_NOT_ENOUGH);
            }

        }

        // 逐行处理
        for (WmsStockLogicMoveItemDO logicMoveItemDO : logicMoveItemDOList) {
            WmsStockLogicDO fromStockLogic = stockLogicMap.get(makeStockKey(logicMoveItemDO.getFromCompanyId(), logicMoveItemDO.getFromDeptId(), logicMoveItemDO.getProductId()));
            this.processStockLogic(logicMoveDO.getWarehouseId(), logicMoveItemDO, fromStockLogic);
        }

        // 完成库位移动
        stockLogicMoveService.finishMove(logicMoveDO, logicMoveItemDOList);

    }


    /**
     * 处理逻辑库存
     **/
    private void processStockLogic(Long warehouseId, WmsStockLogicMoveItemDO logicMoveItemDO, WmsStockLogicDO fromStockLogicDO) {

        JdbcUtils.requireTransaction();

        // 处理出方
        fromStockLogicDO.setAvailableQty(fromStockLogicDO.getAvailableQty() - logicMoveItemDO.getQty());
        if (fromStockLogicDO.getAvailableQty() < 0) {
            throw exception(STOCK_LOGIC_NOT_ENOUGH);
        }
        // 保存
        stockLogicService.insertOrUpdate(fromStockLogicDO);
        // 记录流水
        Integer fromAfterQty = fromStockLogicDO.getAvailableQty();
        Integer fromBeforeQty = fromAfterQty - logicMoveItemDO.getQty() * OUT.getValue();
        stockFlowService.createForStockLogic(this.getReason(), OUT, logicMoveItemDO.getProductId(), fromStockLogicDO, logicMoveItemDO.getQty(), logicMoveItemDO.getLogicMoveId(), logicMoveItemDO.getId(), fromBeforeQty, fromAfterQty, null);


        // 入方
        WmsStockLogicDO toStockLogicDO = stockLogicService.getByUkProductOwner(warehouseId, logicMoveItemDO.getToCompanyId(), logicMoveItemDO.getToDeptId(), logicMoveItemDO.getProductId(), true);
        // 可用库存
        toStockLogicDO.setAvailableQty(toStockLogicDO.getAvailableQty() + logicMoveItemDO.getQty());
        // 保存
        stockLogicService.insertOrUpdate(toStockLogicDO);
        // 记录流水
        Integer afterQty = toStockLogicDO.getAvailableQty();
        Integer beforeQty = afterQty - logicMoveItemDO.getQty() * IN.getValue();
        stockFlowService.createForStockLogic(this.getReason(), IN, logicMoveItemDO.getProductId(), toStockLogicDO, logicMoveItemDO.getQty(), logicMoveItemDO.getLogicMoveId(), logicMoveItemDO.getId(), beforeQty, afterQty, null);

    }

    private String makeStockKey(Long companyId, Long deptId, Long productId) {
        return companyId + "-" + deptId + "-" + productId;
    }


}
