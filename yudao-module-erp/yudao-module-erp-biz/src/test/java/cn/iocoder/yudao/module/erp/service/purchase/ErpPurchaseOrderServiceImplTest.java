package cn.iocoder.yudao.module.erp.service.purchase;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.*;

@Import(ErpPurchaseOrderServiceImpl.class)
class ErpPurchaseOrderServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ErpPurchaseOrderService service;

    @Test
    void createPurchaseOrder() {
//        service.createPurchaseOrder(new ErpPurchaseOrderSaveReqVO());
    }

    @Test
    void updatePurchaseOrder() {
    }

    @Test
    void updatePurchaseOrderStatus() {
    }

    @Test
    void updatePurchaseOrderInCount() {
    }

    @Test
    void updatePurchaseOrderReturnCount() {
    }

    @Test
    void deletePurchaseOrder() {
    }

    @Test
    void getPurchaseOrder() {
    }

    @Test
    void validatePurchaseOrder() {
    }

    @Test
    void getPurchaseOrderItemListByOrderId() {
    }

    @Test
    void getPurchaseOrderItemListByOrderIds() {
    }
}