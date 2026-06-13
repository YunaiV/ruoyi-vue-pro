package cn.iocoder.yudao.module.wms.service.order.receipt;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.MoneyUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.controller.admin.order.receipt.vo.detail.WmsReceiptOrderDetailSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.order.receipt.vo.order.WmsReceiptOrderPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.order.receipt.vo.order.WmsReceiptOrderSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.order.receipt.WmsReceiptOrderDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.order.receipt.WmsReceiptOrderDetailDO;
import cn.iocoder.yudao.module.wms.dal.mysql.order.receipt.WmsReceiptOrderMapper;
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
 * WMS 入库单 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class WmsReceiptOrderServiceImpl implements WmsReceiptOrderService {

    @Resource
    private WmsReceiptOrderMapper receiptOrderMapper;
    @Resource
    private WmsReceiptOrderDetailService receiptOrderDetailService;
    @Resource
    private WmsWarehouseService warehouseService;
    @Resource
    private WmsMerchantService merchantService;
    @Resource
    private WmsInventoryService inventoryService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createReceiptOrder(WmsReceiptOrderSaveReqVO createReqVO) {
        // 1. 校验入库单保存数据
        validateReceiptOrderSaveData(createReqVO);

        // 2.1 插入入库单
        WmsReceiptOrderDO order = BeanUtils.toBean(createReqVO, WmsReceiptOrderDO.class);
        order.setStatus(WmsOrderStatusEnum.PREPARE.getStatus());
        fillReceiptOrderTotal(order, createReqVO);
        receiptOrderMapper.insert(order);
        // 2.2 插入入库单明细
        receiptOrderDetailService.createReceiptOrderDetailList(order.getId(), createReqVO);
        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateReceiptOrder(WmsReceiptOrderSaveReqVO updateReqVO) {
        // 1. 校验入库单保存数据
        validateReceiptOrderPrepare(updateReqVO.getId());
        validateReceiptOrderSaveData(updateReqVO);

        // 2.1 更新入库单
        WmsReceiptOrderDO updateObj = BeanUtils.toBean(updateReqVO, WmsReceiptOrderDO.class)
                .setStatus(WmsOrderStatusEnum.PREPARE.getStatus());
        fillReceiptOrderTotal(updateObj, updateReqVO);
        int updateCount = receiptOrderMapper.updateByIdAndStatus(updateReqVO.getId(),
                WmsOrderStatusEnum.PREPARE.getStatus(), updateObj);
        if (updateCount == 0) {
            throw exception(RECEIPT_ORDER_STATUS_NOT_PREPARE);
        }
        // 2.2 更新入库单明细
        receiptOrderDetailService.updateReceiptOrderDetailList(updateReqVO.getId(), updateReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteReceiptOrder(Long id) {
        // 1. 校验存在，且可删除
        WmsReceiptOrderDO order = validateReceiptOrderExists(id);
        if (ObjectUtil.notEqual(order.getStatus(), WmsOrderStatusEnum.PREPARE.getStatus())
                && ObjectUtil.notEqual(order.getStatus(), WmsOrderStatusEnum.CANCELED.getStatus())) {
            throw exception(RECEIPT_ORDER_STATUS_NOT_DELETABLE);
        }

        // 2.1 删除入库单
        receiptOrderMapper.deleteById(id);
        // 2.2 删除入库单明细
        receiptOrderDetailService.deleteReceiptOrderDetailListByOrderId(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeReceiptOrder(Long id) {
        // 1.1 校验存在，且草稿
        WmsReceiptOrderDO order = validateReceiptOrderPrepare(id);
        // 1.2 校验入库单明细存在
        List<WmsReceiptOrderDetailDO> details = receiptOrderDetailService.validateReceiptOrderDetailListExists(id);

        // 2. 完成入库单
        if (receiptOrderMapper.updateByIdAndStatus(id, WmsOrderStatusEnum.PREPARE.getStatus(),
                new WmsReceiptOrderDO().setStatus(WmsOrderStatusEnum.FINISHED.getStatus())) == 0) {
            throw exception(RECEIPT_ORDER_STATUS_NOT_PREPARE);
        }

        // 3. 写入库存
        createInventory(order, details);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelReceiptOrder(Long id) {
        // 1. 校验存在，且草稿
        validateReceiptOrderPrepare(id);

        // 2. 作废入库单
        if (receiptOrderMapper.updateByIdAndStatus(id, WmsOrderStatusEnum.PREPARE.getStatus(),
                new WmsReceiptOrderDO().setStatus(WmsOrderStatusEnum.CANCELED.getStatus())) == 0) {
            throw exception(RECEIPT_ORDER_STATUS_NOT_PREPARE);
        }
    }

    @Override
    public WmsReceiptOrderDO getReceiptOrder(Long id) {
        return receiptOrderMapper.selectById(id);
    }

    @Override
    public PageResult<WmsReceiptOrderDO> getReceiptOrderPage(WmsReceiptOrderPageReqVO pageReqVO) {
        return receiptOrderMapper.selectPage(pageReqVO);
    }

    @Override
    public long getReceiptOrderCountByMerchantId(Long merchantId) {
        return receiptOrderMapper.selectCountByMerchantId(merchantId);
    }

    @Override
    public long getReceiptOrderCountByWarehouseId(Long warehouseId) {
        return receiptOrderMapper.selectCountByWarehouseId(warehouseId);
    }

    private void validateReceiptOrderSaveData(WmsReceiptOrderSaveReqVO reqVO) {
        // 校验入库单号唯一
        validateReceiptOrderNoUnique(reqVO.getId(), reqVO.getNo());
        // 校验仓库存在
        warehouseService.validateWarehouseExists(reqVO.getWarehouseId());
        // 校验供应商类型
        if (reqVO.getMerchantId() != null) {
            merchantService.validateSupplierMerchantExists(reqVO.getMerchantId());
        }
    }

    private void validateReceiptOrderNoUnique(Long id, String no) {
        WmsReceiptOrderDO order = receiptOrderMapper.selectByNo(no);
        if (order == null) {
            return;
        }
        if (id == null || ObjectUtil.notEqual(order.getId(), id)) {
            throw exception(RECEIPT_ORDER_NO_DUPLICATE);
        }
    }

    private void fillReceiptOrderTotal(WmsReceiptOrderDO order, WmsReceiptOrderSaveReqVO reqVO) {
        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal totalPrice = BigDecimal.ZERO;
        if (CollUtil.isNotEmpty(reqVO.getDetails())) {
            for (WmsReceiptOrderDetailSaveReqVO detail : reqVO.getDetails()) {
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

    private WmsReceiptOrderDO validateReceiptOrderExists(Long id) {
        WmsReceiptOrderDO order = id == null ? null : receiptOrderMapper.selectById(id);
        if (order == null) {
            throw exception(RECEIPT_ORDER_NOT_EXISTS);
        }
        return order;
    }

    /**
     * 校验入库单存在且为草稿状态
     *
     * @param id 入库单编号
     * @return 入库单
     */
    private WmsReceiptOrderDO validateReceiptOrderPrepare(Long id) {
        WmsReceiptOrderDO order = validateReceiptOrderExists(id);
        if (ObjectUtil.notEqual(order.getStatus(), WmsOrderStatusEnum.PREPARE.getStatus())) {
            throw exception(RECEIPT_ORDER_STATUS_NOT_PREPARE);
        }
        return order;
    }

    /**
     * 增加入库单对应库存。
     *
     * 入库在库存变更模型中使用正数数量。
     *
     * @param order 入库单
     * @param details 入库单明细列表
     */
    private void createInventory(WmsReceiptOrderDO order, List<WmsReceiptOrderDetailDO> details) {
        List<WmsInventoryChangeReqDTO.Item> items = convertList(details,
                detail -> BeanUtils.toBean(detail, WmsInventoryChangeReqDTO.Item.class)
                        .setTotalPrice(getDetailTotalPrice(detail.getQuantity(), detail.getPrice(),
                                detail.getTotalPrice())));
        inventoryService.changeInventory(new WmsInventoryChangeReqDTO()
                .setOrderId(order.getId()).setOrderNo(order.getNo())
                .setOrderType(WmsOrderTypeEnum.RECEIPT.getType()).setItems(items));
    }

}
