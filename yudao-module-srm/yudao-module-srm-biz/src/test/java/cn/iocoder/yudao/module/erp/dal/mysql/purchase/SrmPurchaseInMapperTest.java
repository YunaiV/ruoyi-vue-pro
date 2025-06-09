package cn.iocoder.yudao.module.erp.dal.mysql.purchase;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseInMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;

@Disabled
@Slf4j
class SrmPurchaseInMapperTest extends BaseDbUnitTest {

    @Resource
    SrmPurchaseInMapper srmPurchaseInMapper;

//    @Test
//    void selectPage() {
//        SrmPurchaseInPageReqVO vo = new SrmPurchaseInPageReqVO();
//        vo.setAuditStatus(1);
//        PageResult<SrmPurchaseInDO> page = srmPurchaseInMapper.selectPage(vo);
//
//        log.info("page = {}", page);
//    }
}