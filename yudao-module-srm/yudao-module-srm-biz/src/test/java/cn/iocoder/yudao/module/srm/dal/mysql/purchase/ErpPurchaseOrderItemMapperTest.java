package cn.iocoder.yudao.module.srm.dal.mysql.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.order.ErpPurchaseOrderPageReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.bo.ErpPurchaseOrderItemBO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class ErpPurchaseOrderItemMapperTest extends BaseDbUnitTest {
    @Resource
    ErpPurchaseOrderItemMapper erpPurchaseOrderItemMapper;

    @Test
    void selectBOPage() {
        ErpPurchaseOrderPageReqVO vo = new ErpPurchaseOrderPageReqVO();
        vo.setErpPurchaseRequestItemNo("1");
        PageResult<ErpPurchaseOrderItemBO> page = erpPurchaseOrderItemMapper.selectErpPurchaseOrderItemBOPage(vo);
        for (ErpPurchaseOrderItemBO itemBO : page.getList()) {
            //log
            log.info("itemBO:{}", itemBO);
        }
    }
}