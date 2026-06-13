package cn.iocoder.yudao.module.wms.service.md.warehouse;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.controller.admin.md.warehouse.vo.WmsWarehousePageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.md.warehouse.vo.WmsWarehouseSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.warehouse.WmsWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.mysql.md.warehouse.WmsWarehouseMapper;
import cn.iocoder.yudao.module.wms.enums.order.WmsOrderTypeEnum;
import cn.iocoder.yudao.module.wms.service.inventory.WmsInventoryService;
import cn.iocoder.yudao.module.wms.service.order.check.WmsCheckOrderService;
import cn.iocoder.yudao.module.wms.service.order.movement.WmsMovementOrderService;
import cn.iocoder.yudao.module.wms.service.order.receipt.WmsReceiptOrderService;
import cn.iocoder.yudao.module.wms.service.order.shipment.WmsShipmentOrderService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.*;

/**
 * WMS 仓库 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class WmsWarehouseServiceImpl implements WmsWarehouseService {

    @Resource
    private WmsWarehouseMapper warehouseMapper;

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private WmsInventoryService inventoryService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private WmsReceiptOrderService receiptOrderService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private WmsShipmentOrderService shipmentOrderService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private WmsMovementOrderService movementOrderService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private WmsCheckOrderService checkOrderService;

    @Override
    public Long createWarehouse(WmsWarehouseSaveReqVO createReqVO) {
        // 校验数据
        validateWarehouseSaveData(createReqVO);

        // 插入
        WmsWarehouseDO warehouse = BeanUtils.toBean(createReqVO, WmsWarehouseDO.class);
        warehouseMapper.insert(warehouse);
        return warehouse.getId();
    }

    @Override
    public void updateWarehouse(WmsWarehouseSaveReqVO updateReqVO) {
        // 校验存在
        validateWarehouseExists(updateReqVO.getId());
        // 校验数据
        validateWarehouseSaveData(updateReqVO);

        // 更新
        WmsWarehouseDO updateObj = BeanUtils.toBean(updateReqVO, WmsWarehouseDO.class);
        warehouseMapper.updateById(updateObj);
    }

    private void validateWarehouseSaveData(WmsWarehouseSaveReqVO reqVO) {
        validateWarehouseNameUnique(reqVO.getId(), reqVO.getName());
        validateWarehouseCodeUnique(reqVO.getId(), reqVO.getCode());
    }

    private void validateWarehouseNameUnique(Long id, String name) {
        WmsWarehouseDO warehouse = warehouseMapper.selectByName(name);
        if (warehouse == null) {
            return;
        }
        if (ObjUtil.notEqual(warehouse.getId(), id)) {
            throw exception(WAREHOUSE_NAME_DUPLICATE);
        }
    }

    private void validateWarehouseCodeUnique(Long id, String code) {
        WmsWarehouseDO warehouse = warehouseMapper.selectByCode(code);
        if (warehouse == null) {
            return;
        }
        if (ObjUtil.notEqual(warehouse.getId(), id)) {
            throw exception(WAREHOUSE_CODE_DUPLICATE);
        }
    }

    @Override
    public void deleteWarehouse(Long id) {
        // 校验存在
        validateWarehouseExists(id);
        // 校验未被单据使用
        validateWarehouseUnused(id);

        // 删除
        warehouseMapper.deleteById(id);
    }

    private void validateWarehouseUnused(Long id) {
        if (inventoryService.getInventoryCountByWarehouseId(id) > 0) {
            throw exception(WAREHOUSE_HAS_INVENTORY);
        }
        if (receiptOrderService.getReceiptOrderCountByWarehouseId(id) > 0) {
            throw exception(WAREHOUSE_HAS_ORDER, WmsOrderTypeEnum.RECEIPT.getName());
        }
        if (shipmentOrderService.getShipmentOrderCountByWarehouseId(id) > 0) {
            throw exception(WAREHOUSE_HAS_ORDER, WmsOrderTypeEnum.SHIPMENT.getName());
        }
        if (movementOrderService.getMovementOrderCountByWarehouseId(id) > 0) {
            throw exception(WAREHOUSE_HAS_ORDER, WmsOrderTypeEnum.MOVEMENT.getName());
        }
        if (checkOrderService.getCheckOrderCountByWarehouseId(id) > 0) {
            throw exception(WAREHOUSE_HAS_ORDER, WmsOrderTypeEnum.CHECK.getName());
        }
    }

    @Override
    public WmsWarehouseDO validateWarehouseExists(Long id) {
        WmsWarehouseDO warehouse = warehouseMapper.selectById(id);
        if (warehouse == null) {
            throw exception(WAREHOUSE_NOT_EXISTS);
        }
        return warehouse;
    }

    @Override
    public WmsWarehouseDO getWarehouse(Long id) {
        return warehouseMapper.selectById(id);
    }

    @Override
    public PageResult<WmsWarehouseDO> getWarehousePage(WmsWarehousePageReqVO pageReqVO) {
        return warehouseMapper.selectPage(pageReqVO);
    }

    @Override
    public List<WmsWarehouseDO> getWarehouseList() {
        return warehouseMapper.selectSimpleList();
    }

    @Override
    public List<WmsWarehouseDO> getWarehouseList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return warehouseMapper.selectByIds(ids);
    }

}
