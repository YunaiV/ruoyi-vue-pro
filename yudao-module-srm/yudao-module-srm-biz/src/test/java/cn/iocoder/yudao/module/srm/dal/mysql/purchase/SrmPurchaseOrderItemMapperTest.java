package cn.iocoder.yudao.module.srm.dal.mysql.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.order.SrmPurchaseOrderPageReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.bo.SrmPurchaseOrderItemBO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class SrmPurchaseOrderItemMapperTest extends BaseDbUnitTest {
    @Resource
    SrmPurchaseOrderItemMapper srmPurchaseOrderItemMapper;

    @Test
    void selectBOPage() {
        SrmPurchaseOrderPageReqVO vo = new SrmPurchaseOrderPageReqVO();
        vo.setErpPurchaseRequestItemNo("1");
        PageResult<SrmPurchaseOrderItemBO> page = srmPurchaseOrderItemMapper.selectErpPurchaseOrderItemBOPage(vo);
        for (SrmPurchaseOrderItemBO itemBO : page.getList()) {
            //log
            log.info("itemBO:{}", itemBO);
        }
    }
}