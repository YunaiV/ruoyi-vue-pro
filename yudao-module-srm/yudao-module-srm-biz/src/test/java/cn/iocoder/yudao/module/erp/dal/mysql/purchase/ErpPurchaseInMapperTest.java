package cn.iocoder.yudao.module.erp.dal.mysql.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class ErpPurchaseInMapperTest extends BaseDbUnitTest {

    @Resource
    ErpPurchaseInMapper erpPurchaseInMapper;

    @Test
    void selectPage() {
        ErpPurchaseInPageReqVO vo = new ErpPurchaseInPageReqVO();
        vo.setAuditStatus(1);
        PageResult<ErpPurchaseInDO> page = erpPurchaseInMapper.selectPage(vo);


        log.info("page = {}", page);
    }
}