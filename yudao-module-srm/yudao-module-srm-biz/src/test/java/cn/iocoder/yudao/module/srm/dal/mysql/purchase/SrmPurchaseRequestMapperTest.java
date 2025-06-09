package cn.iocoder.yudao.module.srm.dal.mysql.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.request.req.SrmPurchaseRequestPageReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseRequestDO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
@Slf4j
class SrmPurchaseRequestMapperTest extends BaseDbUnitTest {

    @Resource
    SrmPurchaseRequestMapper srmPurchaseRequestMapper;

    @Test
    void selectPage() {
        SrmPurchaseRequestPageReqVO vo = new SrmPurchaseRequestPageReqVO();
        vo.setProductCode("W11F7142L");
        PageResult<SrmPurchaseRequestDO> page = srmPurchaseRequestMapper.selectPage(vo);
        //log
        log.info("page:{}", page);
    }
}