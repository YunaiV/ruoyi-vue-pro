package com.somle.esb.service;


import cn.iocoder.yudao.framework.mybatis.config.YudaoMybatisAutoConfiguration;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpCustomRuleDTO;
import cn.iocoder.yudao.module.erp.controller.admin.logistic.customrule.vo.ErpCustomRuleSaveReqVO;
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
import com.somle.eccang.model.EccangCategory;
import com.somle.eccang.model.EccangProduct;
import com.somle.eccang.model.EccangResponse;
import com.somle.eccang.service.EccangService;
import com.somle.esb.config.IntegrationConfig;
import com.somle.esb.converter.DingTalkToErpConverter;
import com.somle.esb.converter.EccangToErpConverter;
import com.somle.esb.converter.ErpToEccangConverter;
import com.somle.esb.converter.ErpToKingdeeConverter;
import com.somle.esb.job.EccangProductDataJob;
import com.somle.framework.common.util.general.CoreUtils;
import com.somle.framework.test.core.ut.BaseSpringIntegrationTest;
import com.somle.kingdee.model.KingdeeAuxInfo;
import com.somle.kingdee.model.KingdeeAuxInfoDetail;
import com.somle.kingdee.service.KingdeeClient;
import com.somle.kingdee.service.KingdeeService;
import com.somle.matomo.service.MatomoService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Commit;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.fasterxml.jackson.databind.type.LogicalType.Collection;

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
        DingTalkService.class,
    DeptApiImpl.class,
    DeptServiceImpl.class,


//    YudaoSecurityAutoConfiguration.class,
//    SecurityAutoConfiguration.class,
    IntegrationConfig.class,
        DingTalkToErpConverter.class,
        EccangProductDataJob.class,
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
    private EccangService eccangService;
    @Resource
    private EccangProductDataJob eccangProductDataJob;

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
    @Resource
    DingTalkService dingTalkService;
    @Resource
    DingTalkToErpConverter dingTalkToErpConverter;

    @Resource
    ErpCustomRuleService erpCustomRuleService;

    @Resource
    ErpCustomRuleMapper erpCustomRuleMapper;


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
        ErpCustomRuleDTO product = new ErpCustomRuleDTO();
        product.setId("R12345");
        product.setProductName("Wireless Headphones");
        product.setCountryCode(1);
//        product.setImageUrl("https://example.com/images/product1.jpg");

        product.setPackageWeight(1.2f);
        product.setPackageLength(15.5f);
        product.setPackageWidth(10.3f);
        product.setPackageHeight(5.1f);
        product.setProductWeight(1.5f);
        product.setProductLength(16.0f);
        product.setProductWidth(11.0f);
        product.setProductHeight(6.0f);
        product.setProductMaterial("Plastic and Metal");
        product.setPurchasePriceCurrencyCode(1);
        product.setLogisticAttribute(1);
        product.setHscode("85076000");
        product.setDeclaredValue(50.0f);
        product.setDeclaredValueCurrencyCode(1);
        product.setDeclaredType("无线耳机");
        product.setDeclaredTypeEn("Wireless Headphones");
        product.setTaxRate(0.12f);
        product.setProductCreatorId("50325");
        product.setSupplierProductCode("DDDDDDD");

        //product.setProductCategoryId1(50002);
        //product.setProductCategoryId2(50007);
//        product.setProductCategoryId3(103);
        product.setBarCode("1234567890123");
        product.setProductDeptId(50007L);

        esbService.syncCustomRuleToEccang(MessageBuilder.withPayload(List.of(product)).build());
        kingdeeService.refreshAuths();
//        esbService.syncProductsToKingdee(MessageBuilder.withPayload(List.of(product)).build());
    }

    @Test
    @Commit
    public void aopTest() {
        printAllBeans();
        ErpCustomRuleSaveReqVO erpCustomRuleSaveReqVO = new ErpCustomRuleSaveReqVO();
        erpCustomRuleSaveReqVO.setCountryCode(1);
        erpCustomRuleSaveReqVO.setType("import");
        erpCustomRuleSaveReqVO.setSupplierProductId(1L);
        erpCustomRuleSaveReqVO.setDeclaredTypeEn("Electronic Component");
        erpCustomRuleSaveReqVO.setDeclaredType("电子元件");
        erpCustomRuleSaveReqVO.setDeclaredValue(150.75);
        erpCustomRuleSaveReqVO.setDeclaredValueCurrencyCode(1);
        erpCustomRuleSaveReqVO.setTaxRate(new BigDecimal("0.18"));
        erpCustomRuleSaveReqVO.setHscode("85423190");

//        ErpCustomRuleDO customRule = BeanUtils.toBean(erpCustomRuleSaveReqVO, ErpCustomRuleDO.class);
        log.info(erpCustomRuleMapper.selectList().toString());
        erpCustomRuleService.createCustomRule(erpCustomRuleSaveReqVO);
        log.info(erpCustomRuleMapper.selectList().toString());

        // wait for sub threads to finish
        CoreUtils.sleep(20000);

    }

    @Test
    public void test4() {
        log.info(erpCustomRuleMapper.selectList().toString());
    }

    @Test
    public void test5() {
        log.info(erpCustomRuleMapper.selectList().toString());

    }

    @Test
    public void test6() {
        esbService.syncDepartments();
    }

}
