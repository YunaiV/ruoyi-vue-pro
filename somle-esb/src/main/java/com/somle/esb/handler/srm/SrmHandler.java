package com.somle.esb.handler.srm;

import cn.iocoder.yudao.module.srm.api.purchase.SrmPurchaseInApi;
import cn.iocoder.yudao.module.srm.api.purchase.SrmPurchaseOrderApi;
import cn.iocoder.yudao.module.srm.api.purchase.SrmPurchaseReturnApi;
import cn.iocoder.yudao.module.srm.api.purchase.dto.SrmPurchaseInDTO;
import cn.iocoder.yudao.module.srm.api.purchase.dto.SrmPurchaseOrderDTO;
import cn.iocoder.yudao.module.srm.api.purchase.dto.SrmPurchaseReturnDTO;
import cn.iocoder.yudao.module.srm.api.supplier.SrmSupplierApi;
import cn.iocoder.yudao.module.srm.api.supplier.dto.SrmSupplierDTO;
import cn.iocoder.yudao.module.srm.enums.SrmChannelEnum;
import com.somle.esb.converter.ErpToKingdeeConverter;
import com.somle.kingdee.model.KingdeePurInboundSaveReqVO;
import com.somle.kingdee.model.KingdeePurOrderSaveReqVO;
import com.somle.kingdee.model.KingdeePurReturnSaveReqVO;
import com.somle.kingdee.model.supplier.KingdeeSupplierSaveVO;
import com.somle.kingdee.service.KingdeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * srm 消费端
 */
@Slf4j
@Component
@Profile("prod")
@RequiredArgsConstructor
@SuppressWarnings("all")
public class SrmHandler {
    private final KingdeeService kingdeeService;
    private final SrmSupplierApi srmSupplierApi;
    private final SrmPurchaseOrderApi srmPurchaseOrderApi;
    private final SrmPurchaseInApi srmPurchaseInApi;
    private final SrmPurchaseReturnApi srmPurchaseReturnApi;
    private final ErpToKingdeeConverter erpToKingdeeConverter;

    //消费供应商
    @ServiceActivator(inputChannel = SrmChannelEnum.SUPPLIER)
    public void syncSuppliersToKingdee(@Payload List<Long> supplierIds) {
        log.info("[syncSuppliersToKingdee] 开始同步供应商到金蝶，数量：{}", supplierIds.size());
        try {
            // 通过API获取供应商信息
            List<SrmSupplierDTO> suppliers = srmSupplierApi.getSupplierList(supplierIds);
            if (suppliers.isEmpty()) {
                log.warn("[syncSuppliersToKingdee] 未找到需要同步的供应商信息");
                return;
            }

            // 转换为金蝶供应商
            List<KingdeeSupplierSaveVO> kingdeeSupplierSaveVOS = erpToKingdeeConverter.convertSupplierDTOList(suppliers);
            // 同步到金蝶
            int total = kingdeeSupplierSaveVOS.size();
            for (int i = 0; i < total; i++) {
                KingdeeSupplierSaveVO supplier = kingdeeSupplierSaveVOS.get(i);
                kingdeeService.addSupplier(supplier);
                log.info("[syncSuppliersToKingdee] 同步进度：{}/{}，供应商：{}", i + 1, total, supplier.getNumber());
            }
            log.info("[syncSuppliersToKingdee] 同步完成，共处理：{}个供应商", total);
        } catch (Exception e) {
            log.error("[syncSuppliersToKingdee] 同步异常：{}", e.getMessage());
            throw e;
        }
    }

    //消费采购订单
    @ServiceActivator(inputChannel = SrmChannelEnum.PURCHASE_ORDER)
    public void syncPurchaseOrdersToKingdee(@Payload List<Long> orderIds) {
        log.info("[syncPurchaseOrdersToKingdee] 开始同步采购订单到金蝶，数量：{}", orderIds.size());
        try {
            // 通过API获取采购订单信息
            List<SrmPurchaseOrderDTO> orders = srmPurchaseOrderApi.getPurchaseOrderList(orderIds);
            if (orders.isEmpty()) {
                log.warn("[syncPurchaseOrdersToKingdee] 未找到需要同步的采购订单信息");
                return;
            }

            // 转换为金蝶采购订单
            List<KingdeePurOrderSaveReqVO> kingdeeOrders = erpToKingdeeConverter.convertOrderDTOList(orders);
            // 同步到金蝶
            int total = kingdeeOrders.size();
            for (int i = 0; i < total; i++) {
                KingdeePurOrderSaveReqVO order = kingdeeOrders.get(i);
                kingdeeService.savePurchaseOrder(order);
                log.info("[syncPurchaseOrdersToKingdee] 同步进度：{}/{}，订单：{}", i + 1, total, order.getBillNo());
            }
            log.info("[syncPurchaseOrdersToKingdee] 同步完成，共处理：{}个订单", total);
        } catch (Exception e) {
            log.error("[syncPurchaseOrdersToKingdee] 同步异常：{}", e.getMessage());
            throw e;
        }
    }

    //消费采购入库(到货)单
    @ServiceActivator(inputChannel = SrmChannelEnum.PURCHASE_IN)
    public void syncPurchaseInToKingdee(@Payload List<Long> inIds) {
        log.info("[syncPurchaseInToKingdee] 开始同步采购入库单到金蝶，数量：{}", inIds.size());
        try {
            // 通过API获取采购入库单信息
            List<SrmPurchaseInDTO> inOrders = srmPurchaseInApi.getPurchaseInList(inIds);
            if (inOrders.isEmpty()) {
                log.warn("[syncPurchaseInToKingdee] 未找到需要同步的采购入库单信息");
                return;
            }

            // 转换为金蝶采购入库单
            List<KingdeePurInboundSaveReqVO> kingdeeInOrders = erpToKingdeeConverter.convertInDTOList(inOrders);
            // 同步到金蝶
            int total = kingdeeInOrders.size();
            for (int i = 0; i < total; i++) {
                KingdeePurInboundSaveReqVO inOrder = kingdeeInOrders.get(i);
                kingdeeService.savePurInbound(inOrder);
                log.info("[syncPurchaseInToKingdee] 同步进度：{}/{}，入库单：{}", i + 1, total, inOrder.getBillNo());
            }
            log.info("[syncPurchaseInToKingdee] 同步完成，共处理：{}个入库单", total);
        } catch (Exception e) {
            log.error("[syncPurchaseInToKingdee] 同步异常：{}", e.getMessage());
            throw e;
        }
    }

    //消费采购退货单
    @ServiceActivator(inputChannel = SrmChannelEnum.PURCHASE_RETURN)
    public void syncPurchaseReturnToKingdee(@Payload List<Long> returnIds) {
        log.info("[syncPurchaseReturnToKingdee] 开始同步采购退货单到金蝶，数量：{}", returnIds.size());
        try {
            // 通过API获取采购退货单信息
            List<SrmPurchaseReturnDTO> returnOrders = srmPurchaseReturnApi.getPurchaseReturnList(returnIds);
            if (returnOrders.isEmpty()) {
                log.warn("[syncPurchaseReturnToKingdee] 未找到需要同步的采购退货单信息");
                return;
            }

            // 转换为金蝶采购退货单
            List<KingdeePurReturnSaveReqVO> kingdeeReturnOrders = erpToKingdeeConverter.convertReturnDTOList(returnOrders);
            // 同步到金蝶
            int total = kingdeeReturnOrders.size();
            for (int i = 0; i < total; i++) {
                KingdeePurReturnSaveReqVO returnOrder = kingdeeReturnOrders.get(i);
                kingdeeService.savePurOutbound(returnOrder);
                log.info("[syncPurchaseReturnToKingdee] 同步进度：{}/{}，退货单：{}", i + 1, total, returnOrder.getBillNo());
            }
            log.info("[syncPurchaseReturnToKingdee] 同步完成，共处理：{}个退货单", total);
        } catch (Exception e) {
            log.error("[syncPurchaseReturnToKingdee] 同步异常：{}", e.getMessage());
            throw e;
        }
    }
}
