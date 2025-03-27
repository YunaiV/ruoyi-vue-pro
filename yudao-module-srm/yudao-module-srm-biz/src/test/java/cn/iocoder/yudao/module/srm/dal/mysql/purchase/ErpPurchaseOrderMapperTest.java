package cn.iocoder.yudao.module.srm.dal.mysql.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.order.ErpPurchaseOrderPageReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.ErpPurchaseOrderDO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;


@Slf4j
class ErpPurchaseOrderMapperTest extends BaseDbUnitTest {

    @Resource
    ErpPurchaseOrderMapper erpPurchaseOrderMapper;

    @Test
    void selectBOByPageVO() {
//        ErpPurchaseOrderPageReqVO vo = new ErpPurchaseOrderPageReqVO();
//        vo.setErpPurchaseRequestItemNo("1");
//        PageResult<ErpPurchaseOrderBO> pageVO = erpPurchaseOrderMapper.selectBOByPageVO(vo);
//        pageVO.getList().forEach(item -> {
//            log.info("item = {}", item);
//        });

    }

    @Test
    void selectPage() {
        PageResult<ErpPurchaseOrderDO> page = erpPurchaseOrderMapper.selectPage(new ErpPurchaseOrderPageReqVO().setErpPurchaseRequestItemNo("12345678")
            , (List<Long>) null);
        page.getList().forEach(item -> {
            log.info("item = {}", item);
        });
    }
}