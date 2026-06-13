package cn.iocoder.yudao.module.wms.service.order.shipment;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.MoneyUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.controller.admin.order.shipment.vo.detail.WmsShipmentOrderDetailSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.order.shipment.vo.order.WmsShipmentOrderPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.order.shipment.vo.order.WmsShipmentOrderSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.order.shipment.WmsShipmentOrderDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.order.shipment.WmsShipmentOrderDetailDO;
import cn.iocoder.yudao.module.wms.dal.mysql.order.shipment.WmsShipmentOrderMapper;
import cn.iocoder.yudao.module.wms.enums.order.WmsOrderTypeEnum;
import cn.iocoder.yudao.module.wms.enums.order.WmsOrderStatusEnum;
import cn.iocoder.yudao.module.wms.service.inventory.WmsInventoryService;
import cn.iocoder.yudao.module.wms.service.inventory.dto.WmsInventoryChangeReqDTO;
import cn.iocoder.yudao.module.wms.service.md.merchant.WmsMerchantService;
import cn.iocoder.yudao.module.wms.service.md.warehouse.WmsWarehouseService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.*;

/**
 * WMS 出库单 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class WmsShipmentOrderServiceImpl implements WmsShipmentOrderService {

    @Resource
    private WmsShipmentOrderMapper shipmentOrderMapper;
    @Resource
    private WmsShipmentOrderDetailService shipmentOrderDetailService;
    @Resource
    private WmsWarehouseService warehouseService;
    @Resource
    private WmsMerchantService merchantService;
    @Resource
    private WmsInventoryService inventoryService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createShipmentOrder(WmsShipmentOrderSaveReqVO createReqVO) {
        // 1. 校验出库单保存数据
        validateShipmentOrderSaveData(createReqVO);

        // 2.1 插入出库单
        WmsShipmentOrderDO order = BeanUtils.toBean(createReqVO, WmsShipmentOrderDO.class);
        order.setStatus(WmsOrderStatusEnum.PREPARE.getStatus());
        fillShipmentOrderTotal(order, createReqVO);
        shipmentOrderMapper.insert(order);
        // 2.2 插入出库单明细
        shipmentOrderDetailService.createShipmentOrderDetailList(order.getId(), createReqVO);
        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateShipmentOrder(WmsShipmentOrderSaveReqVO updateReqVO) {
        // 1. 校验出库单保存数据
        validateShipmentOrderPrepare(updateReqVO.getId());
        validateShipmentOrderSaveData(updateReqVO);

        // 2.1 更新出库单
        WmsShipmentOrderDO updateObj = BeanUtils.toBean(updateReqVO, WmsShipmentOrderDO.class)
                .setStatus(WmsOrderStatusEnum.PREPARE.getStatus());
        fillShipmentOrderTotal(updateObj, updateReqVO);
        int updateCount = shipmentOrderMapper.updateByIdAndStatus(updateReqVO.getId(),
                WmsOrderStatusEnum.PREPARE.getStatus(), updateObj);
        if (updateCount == 0) {
            throw exception(SHIPMENT_ORDER_STATUS_NOT_PREPARE);
        }
        // 2.2 更新出库单明细
        shipmentOrderDetailService.updateShipmentOrderDetailList(updateReqVO.getId(), updateReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteShipmentOrder(Long id) {
        // 1. 校验存在，且可删除
        WmsShipmentOrderDO order = validateShipmentOrderExists(id);
        if (ObjectUtil.notEqual(order.getStatus(), WmsOrderStatusEnum.PREPARE.getStatus())
                && ObjectUtil.notEqual(order.getStatus(), WmsOrderStatusEnum.CANCELED.getStatus())) {
            throw exception(SHIPMENT_ORDER_STATUS_NOT_DELETABLE);
        }

        // 2.1 删除出库单
        shipmentOrderMapper.deleteById(id);
        // 2.2 删除出库单明细
        shipmentOrderDetailService.deleteShipmentOrderDetailListByOrderId(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeShipmentOrder(Long id) {
        // 1.1 校验存在，且草稿
        WmsShipmentOrderDO order = validateShipmentOrderPrepare(id);
        // 1.2 校验出库单明细存在
        List<WmsShipmentOrderDetailDO> details = shipmentOrderDetailService.validateShipmentOrderDetailListExists(id);

        // 2. 完成出库单
        if (shipmentOrderMapper.updateByIdAndStatus(id, WmsOrderStatusEnum.PREPARE.getStatus(),
                new WmsShipmentOrderDO().setStatus(WmsOrderStatusEnum.FINISHED.getStatus())) == 0) {
            throw exception(SHIPMENT_ORDER_STATUS_NOT_PREPARE);
        }

        // 3. 扣减库存
        changeInventory(order, details);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelShipmentOrder(Long id) {
        // 1. 校验存在，且草稿
        validateShipmentOrderPrepare(id);

        // 2. 作废出库单
        if (shipmentOrderMapper.updateByIdAndStatus(id, WmsOrderStatusEnum.PREPARE.getStatus(),
                new WmsShipmentOrderDO().setStatus(WmsOrderStatusEnum.CANCELED.getStatus())) == 0) {
            throw exception(SHIPMENT_ORDER_STATUS_NOT_PREPARE);
        }
    }

    @Override
    public WmsShipmentOrderDO getShipmentOrder(Long id) {
        return shipmentOrderMapper.selectById(id);
    }

    @Override
    public PageResult<WmsShipmentOrderDO> getShipmentOrderPage(WmsShipmentOrderPageReqVO pageReqVO) {
        return shipmentOrderMapper.selectPage(pageReqVO);
    }

    @Override
    public long getShipmentOrderCountByMerchantId(Long merchantId) {
        return shipmentOrderMapper.selectCountByMerchantId(merchantId);
    }

    @Override
    public long getShipmentOrderCountByWarehouseId(Long warehouseId) {
        return shipmentOrderMapper.selectCountByWarehouseId(warehouseId);
    }

    private void validateShipmentOrderSaveData(WmsShipmentOrderSaveReqVO reqVO) {
        // 校验出库单号唯一
        validateShipmentOrderNoUnique(reqVO.getId(), reqVO.getNo());
        // 校验仓库存在
        warehouseService.validateWarehouseExists(reqVO.getWarehouseId());
        // 校验客户类型
        if (reqVO.getMerchantId() != null) {
            merchantService.validateCustomerMerchantExists(reqVO.getMerchantId());
        }
    }

    private void validateShipmentOrderNoUnique(Long id, String no) {
        WmsShipmentOrderDO order = shipmentOrderMapper.selectByNo(no);
        if (order == null) {
            return;
        }
        if (id == null || ObjectUtil.notEqual(order.getId(), id)) {
            throw exception(SHIPMENT_ORDER_NO_DUPLICATE);
        }
    }

    private void fillShipmentOrderTotal(WmsShipmentOrderDO order, WmsShipmentOrderSaveReqVO reqVO) {
        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal totalPrice = BigDecimal.ZERO;
        if (CollUtil.isNotEmpty(reqVO.getDetails())) {
            for (WmsShipmentOrderDetailSaveReqVO detail : reqVO.getDetails()) {
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

    private WmsShipmentOrderDO validateShipmentOrderExists(Long id) {
        WmsShipmentOrderDO order = id == null ? null : shipmentOrderMapper.selectById(id);
        if (order == null) {
            throw exception(SHIPMENT_ORDER_NOT_EXISTS);
        }
        return order;
    }

    /**
     * 校验出库单存在且为草稿状态
     *
     * @param id 出库单编号
     * @return 出库单
     */
    private WmsShipmentOrderDO validateShipmentOrderPrepare(Long id) {
        WmsShipmentOrderDO order = validateShipmentOrderExists(id);
        if (ObjectUtil.notEqual(order.getStatus(), WmsOrderStatusEnum.PREPARE.getStatus())) {
            throw exception(SHIPMENT_ORDER_STATUS_NOT_PREPARE);
        }
        return order;
    }

    /**
     * 扣减出库单对应库存
     *
     * @param order 出库单
     * @param details 出库单明细列表
     */
    private void changeInventory(WmsShipmentOrderDO order, List<WmsShipmentOrderDetailDO> details) {
        List<WmsInventoryChangeReqDTO.Item> items = convertList(details,
                detail -> BeanUtils.toBean(detail, WmsInventoryChangeReqDTO.Item.class)
                        .setQuantity(detail.getQuantity().negate())
                        .setTotalPrice(negate(getDetailTotalPrice(detail.getQuantity(), detail.getPrice(),
                                detail.getTotalPrice()))));
        inventoryService.changeInventory(new WmsInventoryChangeReqDTO()
                .setOrderId(order.getId()).setOrderNo(order.getNo())
                .setOrderType(WmsOrderTypeEnum.SHIPMENT.getType()).setItems(items));
    }

    private static BigDecimal negate(BigDecimal value) {
        return value == null ? null : value.negate();
    }

}
