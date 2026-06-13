package cn.iocoder.yudao.module.wms.service.order.movement;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.MoneyUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.controller.admin.order.movement.vo.detail.WmsMovementOrderDetailSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.order.movement.vo.order.WmsMovementOrderPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.order.movement.vo.order.WmsMovementOrderSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.order.movement.WmsMovementOrderDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.order.movement.WmsMovementOrderDetailDO;
import cn.iocoder.yudao.module.wms.dal.mysql.order.movement.WmsMovementOrderMapper;
import cn.iocoder.yudao.module.wms.enums.order.WmsOrderTypeEnum;
import cn.iocoder.yudao.module.wms.enums.order.WmsOrderStatusEnum;
import cn.iocoder.yudao.module.wms.service.inventory.WmsInventoryService;
import cn.iocoder.yudao.module.wms.service.inventory.dto.WmsInventoryChangeReqDTO;
import cn.iocoder.yudao.module.wms.service.md.warehouse.WmsWarehouseService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.*;

/**
 * WMS 移库单 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class WmsMovementOrderServiceImpl implements WmsMovementOrderService {

    @Resource
    private WmsMovementOrderMapper movementOrderMapper;
    @Resource
    private WmsMovementOrderDetailService movementOrderDetailService;
    @Resource
    private WmsWarehouseService warehouseService;
    @Resource
    private WmsInventoryService inventoryService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createMovementOrder(WmsMovementOrderSaveReqVO createReqVO) {
        // 1. 校验移库单保存数据
        validateMovementOrderSaveData(createReqVO);

        // 2.1 插入移库单
        WmsMovementOrderDO order = BeanUtils.toBean(createReqVO, WmsMovementOrderDO.class);
        order.setStatus(WmsOrderStatusEnum.PREPARE.getStatus());
        fillMovementOrderTotal(order, createReqVO);
        movementOrderMapper.insert(order);
        // 2.2 插入移库单明细
        movementOrderDetailService.createMovementOrderDetailList(order.getId(), createReqVO);
        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMovementOrder(WmsMovementOrderSaveReqVO updateReqVO) {
        // 1. 校验移库单保存数据
        validateMovementOrderPrepare(updateReqVO.getId());
        validateMovementOrderSaveData(updateReqVO);

        // 2.1 更新移库单
        WmsMovementOrderDO updateObj = BeanUtils.toBean(updateReqVO, WmsMovementOrderDO.class)
                .setStatus(WmsOrderStatusEnum.PREPARE.getStatus());
        fillMovementOrderTotal(updateObj, updateReqVO);
        int updateCount = movementOrderMapper.updateByIdAndStatus(updateReqVO.getId(),
                WmsOrderStatusEnum.PREPARE.getStatus(), updateObj);
        if (updateCount == 0) {
            throw exception(MOVEMENT_ORDER_STATUS_NOT_PREPARE);
        }
        // 2.2 更新移库单明细
        movementOrderDetailService.updateMovementOrderDetailList(updateReqVO.getId(), updateReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMovementOrder(Long id) {
        // 1. 校验存在，且可删除
        WmsMovementOrderDO order = validateMovementOrderExists(id);
        if (ObjectUtil.notEqual(order.getStatus(), WmsOrderStatusEnum.PREPARE.getStatus())
                && ObjectUtil.notEqual(order.getStatus(), WmsOrderStatusEnum.CANCELED.getStatus())) {
            throw exception(MOVEMENT_ORDER_STATUS_NOT_DELETABLE);
        }

        // 2.1 删除移库单
        movementOrderMapper.deleteById(id);
        // 2.2 删除移库单明细
        movementOrderDetailService.deleteMovementOrderDetailListByOrderId(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeMovementOrder(Long id) {
        // 1.1 校验存在，且草稿
        WmsMovementOrderDO order = validateMovementOrderPrepare(id);
        // 1.2 校验移库单明细存在
        List<WmsMovementOrderDetailDO> details = movementOrderDetailService.validateMovementOrderDetailListExists(id);

        // 2. 完成移库单
        if (movementOrderMapper.updateByIdAndStatus(id, WmsOrderStatusEnum.PREPARE.getStatus(),
                new WmsMovementOrderDO().setStatus(WmsOrderStatusEnum.FINISHED.getStatus())) == 0) {
            throw exception(MOVEMENT_ORDER_STATUS_NOT_PREPARE);
        }

        // 3. 移动库存
        changeInventory(order, details);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelMovementOrder(Long id) {
        // 1. 校验存在，且草稿
        validateMovementOrderPrepare(id);

        // 2. 作废移库单
        if (movementOrderMapper.updateByIdAndStatus(id, WmsOrderStatusEnum.PREPARE.getStatus(),
                new WmsMovementOrderDO().setStatus(WmsOrderStatusEnum.CANCELED.getStatus())) == 0) {
            throw exception(MOVEMENT_ORDER_STATUS_NOT_PREPARE);
        }
    }

    @Override
    public WmsMovementOrderDO getMovementOrder(Long id) {
        return movementOrderMapper.selectById(id);
    }

    @Override
    public PageResult<WmsMovementOrderDO> getMovementOrderPage(WmsMovementOrderPageReqVO pageReqVO) {
        return movementOrderMapper.selectPage(pageReqVO);
    }

    @Override
    public long getMovementOrderCountByWarehouseId(Long warehouseId) {
        return movementOrderMapper.selectCountByWarehouseId(warehouseId);
    }

    private void validateMovementOrderSaveData(WmsMovementOrderSaveReqVO reqVO) {
        // 校验移库单号唯一
        validateMovementOrderNoUnique(reqVO.getId(), reqVO.getNo());
        // 校验仓库存在
        warehouseService.validateWarehouseExists(reqVO.getSourceWarehouseId());
        warehouseService.validateWarehouseExists(reqVO.getTargetWarehouseId());
        // 校验来源和目标不能相同
        if (ObjectUtil.equal(reqVO.getSourceWarehouseId(), reqVO.getTargetWarehouseId())) {
            throw exception(MOVEMENT_ORDER_WAREHOUSE_SAME);
        }
    }

    private void validateMovementOrderNoUnique(Long id, String no) {
        WmsMovementOrderDO order = movementOrderMapper.selectByNo(no);
        if (order == null) {
            return;
        }
        if (id == null || ObjectUtil.notEqual(order.getId(), id)) {
            throw exception(MOVEMENT_ORDER_NO_DUPLICATE);
        }
    }

    private void fillMovementOrderTotal(WmsMovementOrderDO order, WmsMovementOrderSaveReqVO reqVO) {
        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal totalPrice = BigDecimal.ZERO;
        if (CollUtil.isNotEmpty(reqVO.getDetails())) {
            for (WmsMovementOrderDetailSaveReqVO detail : reqVO.getDetails()) {
                if (detail.getQuantity() != null) {
                    totalQuantity = totalQuantity.add(detail.getQuantity());
                }
                BigDecimal detailTotalPrice = getDetailTotalPrice(detail.getQuantity(), detail.getPrice(),
                        detail.getTotalPrice());
                if (detailTotalPrice != null) {
                    totalPrice = totalPrice.add(detailTotalPrice);
                }
            }
        }
        order.setTotalQuantity(totalQuantity).setTotalPrice(totalPrice);
    }

    private static BigDecimal getDetailTotalPrice(BigDecimal quantity, BigDecimal price, BigDecimal totalPrice) {
        if (totalPrice != null) {
            return totalPrice;
        }
        return MoneyUtils.priceMultiply(price, quantity);
    }

    private WmsMovementOrderDO validateMovementOrderExists(Long id) {
        WmsMovementOrderDO order = id == null ? null : movementOrderMapper.selectById(id);
        if (order == null) {
            throw exception(MOVEMENT_ORDER_NOT_EXISTS);
        }
        return order;
    }

    /**
     * 校验移库单存在且为草稿状态
     *
     * @param id 移库单编号
     * @return 移库单
     */
    private WmsMovementOrderDO validateMovementOrderPrepare(Long id) {
        WmsMovementOrderDO order = validateMovementOrderExists(id);
        if (ObjectUtil.notEqual(order.getStatus(), WmsOrderStatusEnum.PREPARE.getStatus())) {
            throw exception(MOVEMENT_ORDER_STATUS_NOT_PREPARE);
        }
        return order;
    }

    /**
     * 移动移库单对应库存
     *
     * @param order 移库单
     * @param details 移库单明细列表
     */
    private void changeInventory(WmsMovementOrderDO order, List<WmsMovementOrderDetailDO> details) {
        List<WmsInventoryChangeReqDTO.Item> items = new ArrayList<>(details.size() * 2);
        for (WmsMovementOrderDetailDO detail : details) {
            BigDecimal detailTotalPrice = getDetailTotalPrice(detail.getQuantity(), detail.getPrice(),
                    detail.getTotalPrice());
            items.add(BeanUtils.toBean(detail, WmsInventoryChangeReqDTO.Item.class)
                    .setWarehouseId(detail.getSourceWarehouseId()).setQuantity(detail.getQuantity().negate())
                    .setTotalPrice(negate(detailTotalPrice)));
            items.add(BeanUtils.toBean(detail, WmsInventoryChangeReqDTO.Item.class)
                    .setWarehouseId(detail.getTargetWarehouseId()).setQuantity(detail.getQuantity())
                    .setTotalPrice(detailTotalPrice));
        }
        inventoryService.changeInventory(new WmsInventoryChangeReqDTO()
                .setOrderId(order.getId()).setOrderNo(order.getNo())
                .setOrderType(WmsOrderTypeEnum.MOVEMENT.getType()).setItems(items));
    }

    private static BigDecimal negate(BigDecimal value) {
        return value == null ? null : value.negate();
    }

}
