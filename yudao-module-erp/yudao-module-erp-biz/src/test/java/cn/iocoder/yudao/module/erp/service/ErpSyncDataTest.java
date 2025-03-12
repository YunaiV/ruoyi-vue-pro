//package cn.iocoder.yudao.module.erp.service;
//
//import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
//import cn.iocoder.yudao.module.erp.controller.admin.logistic.customrule.vo.ErpCustomRuleSaveReqVO;
//import cn.iocoder.yudao.module.erp.dal.mysql.logistic.customrule.ErpCustomRuleMapper;
//import cn.iocoder.yudao.module.erp.service.logistic.customrule.ErpCustomRuleService;
//import cn.iocoder.yudao.module.erp.service.logistic.customrule.ErpCustomRuleServiceImpl;
//import jakarta.annotation.Resource;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.Test;
//import org.springframework.context.annotation.Import;
//
//import java.time.LocalDateTime;
//
///**
// * {@link } 的单元测试类
// *
// * @author 索迈管理员
// */
//@Slf4j
//@Import(ErpCustomRuleServiceImpl.class)
//public class ErpSyncDataTest extends BaseDbUnitTest {
//    @Resource
//    ErpCustomRuleMapper customRuleMapper;
//    @Resource
//    private ErpCustomRuleService customRuleService;
//
//    @Test
//    public void testSyncData_success() {
//        // mock 数据
//        ErpCustomRuleSaveReqVO erpCustomRuleSaveReqVO = new ErpCustomRuleSaveReqVO();
//        erpCustomRuleSaveReqVO.setId(15675L);
////        erpCustomRuleSaveReqVO.setCountryCode(1);
////        erpCustomRuleSaveReqVO.setType("报关");
////        erpCustomRuleSaveReqVO.setSupplierProductId(67890L);
////        erpCustomRuleSaveReqVO.setDeclaredTypeEn("Electronic Component");
////        erpCustomRuleSaveReqVO.setDeclaredType("电子元件");
//        erpCustomRuleSaveReqVO.setDeclaredValue(150.75);
//        erpCustomRuleSaveReqVO.setDeclaredValueCurrencyCode(1);
////        erpCustomRuleSaveReqVO.setTaxRate(new BigDecimal("0.18"));
////        erpCustomRuleSaveReqVO.setHscode("85423190");
//
//
//        // 调用
//        log.info(LocalDateTime.now().toString());
//        log.info("create begin");
//        customRuleService.createCustomRule(erpCustomRuleSaveReqVO);
//        log.info("create end");
//
////        customRuleService.updateCustomRule(erpCustomRuleSaveReqVO);
//    }
//
//}