package com.somle.esb.service;


import cn.iocoder.yudao.framework.mybatis.config.YudaoMybatisAutoConfiguration;
import cn.iocoder.yudao.framework.security.config.YudaoSecurityAutoConfiguration;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.DeptApiImpl;
import cn.iocoder.yudao.module.system.convert.dept.DeptConvert;
import cn.iocoder.yudao.module.system.convert.dept.DeptConvertImpl;
import cn.iocoder.yudao.module.system.service.dept.DeptService;
import cn.iocoder.yudao.module.system.service.dept.DeptServiceImpl;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.github.yulichang.autoconfigure.MybatisPlusJoinAutoConfiguration;
import com.somle.framework.test.core.ut.BaseSpringTest;
import com.somle.framework.test.core.ut.BaseSpringUnitTest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.support.MessageBuilder;
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
import com.somle.kingdee.service.KingdeeService;
import com.somle.matomo.service.MatomoService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.MessageChannel;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Slf4j
@Import({EsbService.class,
        EsbMappingService.class,
//    ErpService.class,
        EccangService.class,
        DingTalkService.class,
        KingdeeService.class,
//    AmazonService.class,


        DeptApiImpl.class,
        DeptServiceImpl.class,
        DeptConvertImpl.class,



        DingTalkToErpConverter.class,
        ErpToEccangConverter.class,
        ErpToKingdeeConverter.class,
        EccangToErpConverter.class,


//    PostServiceImpl.class,
//    RoleServiceImpl.class,
//    MenuServiceImpl.class,
//    TenantServiceImpl.class,
//    PermissionServiceImpl.class,

        YudaoSecurityAutoConfiguration.class,
        SecurityAutoConfiguration.class,

        IntegrationConfig.class,
        QuartzAutoConfiguration.class,
        // MyBatis 配置类
        DataSourceAutoConfiguration.class,
        YudaoMybatisAutoConfiguration.class, //Enable DefaultDBFieldHandler
        MybatisPlusAutoConfiguration.class, // MyBatis 的自动配置类
        MybatisPlusJoinAutoConfiguration.class})
class EsbServiceTest extends BaseSpringTest {
    @Resource
    private EsbService esbService;

    @Resource
    private DeptApi deptApi;

//    @Resource
//    private DeptConvert deptConvert;

    @MockBean
    ErpProductService erpProductService;

    @MockBean
    ErpDepartmentService erpDepartmentService;

    @MockBean
    AmazonService amazonService;

    @MockBean
    ErpToEccangConverter erpToEccangConverter;

    @MockBean
    MatomoService matomoService;

    @MockBean
    AiService aiService;




    @Resource
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
    public void test() {
        ErpProductDTO product = new ErpProductDTO();
        product.setRuleId("R12345");
        product.setProductSku("SUP123-US");
        product.setProductTitle("Wireless Headphones");
        product.setProductTitleEn("Wireless Headphones");
        product.setCountryCode("US");
        product.setImageUrl("https://example.com/images/product1.jpg");
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
        product.setProductCategoryId1(101);
        product.setProductCategoryId2(102);
        product.setProductCategoryId3(103);
        product.setUserOrganizationId("ORG001");
        product.setBarCode("1234567890123");
        product.setProductDeptName("Electronics");
        product.setProductDeptId(50007L);

        esbService.syncProductsToEccang(MessageBuilder.withPayload(List.of(product)).build());
    }


}
