package cn.iocoder.yudao.module.srm.dal.mysql.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.in.req.SrmPurchaseInPageReqVO;
import cn.iocoder.yudao.module.srm.service.purchase.bo.in.SrmPurchaseInItemBO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
@Slf4j
class SrmPurchaseInItemMapperTest extends BaseDbUnitTest {

    @Resource
    SrmPurchaseInItemMapper srmPurchaseInItemMapper;

    @Test
    void selectBOPage() {
        PageResult<SrmPurchaseInItemBO> page = srmPurchaseInItemMapper.selectBOPage(new SrmPurchaseInPageReqVO());
        page.getList().forEach(item -> log.info("item: {}", item));
    }

    @Test
    void selectBOList() {
    }

    @Test
    void selectBOById() {
    }
}