package com.somle.esb.service;


import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.config.YudaoMybatisAutoConfiguration;
import cn.iocoder.yudao.framework.security.config.YudaoSecurityAutoConfiguration;
import cn.iocoder.yudao.module.erp.aop.SynExternalDataAspect;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.erp.controller.admin.logistic.customrule.vo.ErpCustomRuleSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.logistic.customrule.ErpCustomRuleDO;
import cn.iocoder.yudao.module.erp.dal.mysql.logistic.customrule.ErpCustomRuleMapper;
import cn.iocoder.yudao.module.erp.service.logistic.customrule.ErpCustomRuleService;
import cn.iocoder.yudao.module.erp.service.logistic.customrule.ErpCustomRuleServiceImpl;
import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.DeptApiImpl;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.convert.dept.DeptConvert;
import cn.iocoder.yudao.module.system.service.dept.DeptServiceImpl;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.github.yulichang.autoconfigure.MybatisPlusJoinAutoConfiguration;
import com.somle.ai.service.AiService;
import com.somle.amazon.service.AmazonService;
import com.somle.dingtalk.service.DingTalkService;
import com.somle.eccang.service.EccangService;
import com.somle.erp.service.ErpDepartmentService;
import com.somle.erp.service.ErpProductService;
import com.somle.esb.config.IntegrationConfig;
import com.somle.esb.converter.DingTalkToErpConverter;
import com.somle.esb.converter.EccangToErpConverter;
import com.somle.esb.converter.ErpToEccangConverter;
import com.somle.esb.converter.ErpToKingdeeConverter;
import com.somle.framework.test.core.ut.BaseDbUnitTest;
import com.somle.framework.test.core.ut.BaseSpringIntegrationTest;
import com.somle.framework.test.core.ut.BaseSpringTest;
import com.somle.kingdee.service.KingdeeService;
import com.somle.matomo.service.MatomoService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScans;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Commit;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Import({
    EsbService.class,
    EsbMappingService.class,
    EccangService.class,
    KingdeeService.class,
    AmazonService.class,

    ErpCustomRuleServiceImpl.class,



    ErpToEccangConverter.class,
    ErpToKingdeeConverter.class,
    EccangToErpConverter.class,



    DeptApiImpl.class,
    DeptServiceImpl.class,

    SynExternalDataAspect.class,


//    YudaoSecurityAutoConfiguration.class,
//    SecurityAutoConfiguration.class,
    IntegrationConfig.class,

    QuartzAutoConfiguration.class,
    // MyBatis 配置类
    DataSourceAutoConfiguration.class,
    YudaoMybatisAutoConfiguration.class, //Enable DefaultDBFieldHandler
    MybatisPlusAutoConfiguration.class, // MyBatis 的自动配置类
    MybatisPlusJoinAutoConfiguration.class})
class EsbServiceProductTest extends BaseSpringIntegrationTest {
    @Resource
    private EsbService esbService;

    @Resource
    ErpToEccangConverter erpToEccangConverter;
    @Resource
    KingdeeService kingdeeService;

    @Resource
    MessageChannel productChannel;

    @Resource
    DeptApi deptApi;

    @MockBean
    AmazonService amazonService;
    @MockBean
    MatomoService matomoService;
    @MockBean
    AiService aiService;
    @MockBean
    DingTalkService dingTalkService;
    @MockBean
    DingTalkToErpConverter dingTalkToErpConverter;

    @Resource
    ErpCustomRuleService erpCustomRuleService;

    @Resource
    ErpCustomRuleMapper erpCustomRuleMapper;


//    @Resource
//    private DeptConvert deptConvert;


    @MockBean
    AdminUserApi adminUserApi;
    @MockBean
    private PasswordEncoder passwordEncoder;
    @MockBean
    private FileApi fileApi;
    @MockBean
    private ConfigApi configApi;

    @Test
    public void test2() {
        log.info(DeptConvert.INSTANCE.toString());
    }

    @Test
    public void test1() {
        deptApi.getDept(50007L);
    }

    @Test
    public void syncProductsTest() {
        ErpProductDTO product = new ErpProductDTO();
        product.setRuleId("R12345");
        product.setProductSku("SUP123-US");
        product.setProductTitle("Wireless Headphones");
        product.setProductTitleEn("Wireless Headphones");
        product.setCountryCode("US");
//        product.setImageUrl("https://example.com/images/product1.jpg");

        product.setPdNetWeight(1.2f);
        product.setPdNetLength(15.5f);
        product.setPdNetWidth(10.3f);
        product.setPdNetHeight(5.1f);
        product.setProductWeight(1.5f);
        product.setProductLength(16.0f);
        product.setProductWidth(11.0f);
        product.setProductHeight(6.0f);
        product.setProductMaterial("Plastic and Metal");
        product.setMaterialEn("Plastic and Metal");
        product.setProductPurchaseValue(45.99f);
        product.setCurrencyCode("USD");
        product.setDefaultSupplierCode("SUP123");
        product.setSaleStatus(1);
        product.setLogisticAttribute("Fragile");
        product.setHsCode("85076000");
        product.setProductDeclaredValue(50.0f);
        product.setPdDeclareCurrencyCode("USD");
        product.setPdOverseaTypeCn("无线耳机");
        product.setPdOverseaTypeEn("Wireless Headphones");
        product.setFboTaxRate(0.12f);

        //product.setProductCategoryId1(50002);
        //product.setProductCategoryId2(50007);
//        product.setProductCategoryId3(103);
        product.setUserOrganizationId("EBD办公事业部");
        product.setBarCode("1234567890123");
        product.setProductDeptName("Electronics");
        product.setProductDeptId(50007L);

        esbService.syncProductsToEccang(MessageBuilder.withPayload(List.of(product)).build());
        kingdeeService.refreshAuths();
//        esbService.syncProductsToKingdee(MessageBuilder.withPayload(List.of(product)).build());
    }

    @Test
    @Commit
    public void aopTest() {
        printAllBeans();
        ErpCustomRuleSaveReqVO erpCustomRuleSaveReqVO = new ErpCustomRuleSaveReqVO();
        erpCustomRuleSaveReqVO.setCountryCode("MX");
        erpCustomRuleSaveReqVO.setType("import");
        erpCustomRuleSaveReqVO.setSupplierProductId(1L);
        erpCustomRuleSaveReqVO.setDeclaredTypeEn("Electronic Component");
        erpCustomRuleSaveReqVO.setDeclaredType("电子元件");
        erpCustomRuleSaveReqVO.setDeclaredValue(150.75);
        erpCustomRuleSaveReqVO.setDeclaredValueCurrencyCode("USD");
        erpCustomRuleSaveReqVO.setTaxRate(new BigDecimal("0.18"));
        erpCustomRuleSaveReqVO.setHscode("85423190");
        erpCustomRuleSaveReqVO.setLogisticAttribute("Fragile");

//        ErpCustomRuleDO customRule = BeanUtils.toBean(erpCustomRuleSaveReqVO, ErpCustomRuleDO.class);
        log.info(erpCustomRuleMapper.selectList().toString());
        erpCustomRuleService.createCustomRule(erpCustomRuleSaveReqVO);
        log.info(erpCustomRuleMapper.selectList().toString());

    }

    @Test
    public void test4() {
        log.info(erpCustomRuleMapper.selectList().toString());
    }

    @Test
    public void test5() {
        log.info(erpCustomRuleMapper.selectList().toString());

    }

}
