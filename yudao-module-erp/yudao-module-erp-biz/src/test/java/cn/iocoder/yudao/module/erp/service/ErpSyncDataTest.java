package cn.iocoder.yudao.module.erp.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.erp.controller.admin.logistic.customrule.vo.ErpCustomRuleSaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.ErpSupplierProductPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.ErpSupplierProductSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.logistic.customrule.ErpCustomRuleDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpSupplierProductDO;
import cn.iocoder.yudao.module.erp.dal.mysql.logistic.customrule.ErpCustomRuleMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpSupplierProductMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.iocoder.yudao.module.erp.service.logistic.customrule.ErpCustomRuleService;
import cn.iocoder.yudao.module.erp.service.logistic.customrule.ErpCustomRuleServiceImpl;
import cn.iocoder.yudao.module.erp.service.purchase.ErpSupplierProductServiceImpl;
import cn.iocoder.yudao.module.erp.service.sale.ErpCustomerService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildBetweenTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.SUPPLIER_PRODUCT_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link } 的单元测试类
 *
 * @author 索迈管理员
 */
@Slf4j
@Import(ErpCustomRuleServiceImpl.class)
public class ErpSyncDataTest extends BaseDbUnitTest {
    @Resource
    private ErpCustomRuleService customRuleService;

    @Resource
    ErpCustomRuleMapper customRuleMapper;

    @Test
    public void testSyncData_success() {
        // mock 数据
        ErpCustomRuleSaveReqVO erpCustomRuleSaveReqVO = new ErpCustomRuleSaveReqVO();
        erpCustomRuleSaveReqVO.setId(15675L);
        erpCustomRuleSaveReqVO.setCountryCode("US");
        erpCustomRuleSaveReqVO.setType("报关");
        erpCustomRuleSaveReqVO.setSupplierProductId(67890L);
        erpCustomRuleSaveReqVO.setDeclaredTypeEn("Electronic Component");
        erpCustomRuleSaveReqVO.setDeclaredType("电子元件");
        erpCustomRuleSaveReqVO.setDeclaredValue(150.75);
        erpCustomRuleSaveReqVO.setDeclaredValueCurrencyCode("USD");
        erpCustomRuleSaveReqVO.setTaxRate(new BigDecimal("0.18"));
        erpCustomRuleSaveReqVO.setHscode("85423190");
        erpCustomRuleSaveReqVO.setLogisticAttribute("Fragile");


        // 调用
        log.info(LocalDateTime.now().toString());
        log.info("create begin");
        customRuleService.createCustomRule(erpCustomRuleSaveReqVO);
        log.info("create end");

//        customRuleService.updateCustomRule(erpCustomRuleSaveReqVO);
    }

}