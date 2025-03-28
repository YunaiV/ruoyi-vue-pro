package cn.iocoder.yudao.module.srm.dal.mysql.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.order.SrmPurchaseOrderPageReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderDO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;


@Slf4j
class SrmPurchaseOrderMapperTest extends BaseDbUnitTest {

    @Resource
    SrmPurchaseOrderMapper srmPurchaseOrderMapper;

    @Test
    void selectBOByPageVO() {
//        SrmPurchaseOrderPageReqVO vo = new SrmPurchaseOrderPageReqVO();
//        vo.setErpPurchaseRequestItemNo("1");
//        PageResult<SrmPurchaseOrderBO> pageVO = erpPurchaseOrderMapper.selectBOByPageVO(vo);
//        pageVO.getList().forEach(item -> {
//            log.info("item = {}", item);
//        });

    }

    @Test
    void selectPage() {
        PageResult<SrmPurchaseOrderDO> page = srmPurchaseOrderMapper.selectPage(new SrmPurchaseOrderPageReqVO().setErpPurchaseRequestItemNo("12345678")
            , (List<Long>) null);
        page.getList().forEach(item -> {
            log.info("item = {}", item);
        });
    }
}