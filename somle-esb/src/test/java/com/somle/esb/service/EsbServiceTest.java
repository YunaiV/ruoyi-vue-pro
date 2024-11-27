package com.somle.esb.service;


import cn.iocoder.yudao.framework.mybatis.config.YudaoMybatisAutoConfiguration;
import cn.iocoder.yudao.framework.security.config.YudaoSecurityAutoConfiguration;
import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
import cn.iocoder.yudao.module.infra.api.config.ConfigApiImpl;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import cn.iocoder.yudao.module.infra.service.config.ConfigService;
import cn.iocoder.yudao.module.infra.service.config.ConfigServiceImpl;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.DeptApiImpl;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.convert.dept.DeptConvert;
import cn.iocoder.yudao.module.system.service.dept.DeptServiceImpl;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.github.yulichang.autoconfigure.MybatisPlusJoinAutoConfiguration;
import com.somle.framework.test.core.ut.BaseSpringTest;
import com.somle.shopify.service.ShopifyService;
import lombok.extern.slf4j.Slf4j;
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
import com.somle.kingdee.service.KingdeeService;
import com.somle.matomo.service.MatomoService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Import({
    EsbService.class,
    EsbMappingService.class,
    EccangService.class,
//    DingTalkService.class,
    KingdeeService.class,
    AmazonService.class,
    ShopifyService.class,



//    DingTalkToErpConverter.class,
    ErpToEccangConverter.class,
    ErpToKingdeeConverter.class,
    EccangToErpConverter.class,


//    AdminUserApiImpl.class,
//    AdminUserServiceImpl.class,
    DeptApiImpl.class,
    DeptServiceImpl.class,
//    PostServiceImpl.class,
//    RoleServiceImpl.class,
//    MenuServiceImpl.class,
//    TenantServiceImpl.class,
//    PermissionApiImpl.class,
//    PermissionServiceImpl.class,
    ConfigApiImpl.class,
    ConfigServiceImpl.class,


//    YudaoSecurityAutoConfiguration.class,
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

    @MockBean
    AmazonService amazonService;
    @Resource
    ErpToEccangConverter erpToEccangConverter;
    @Resource
    KingdeeService kingdeeService;
    @MockBean
    MatomoService matomoService;
    @MockBean
    AiService aiService;
    @MockBean
    DingTalkService dingTalkService;

    @MockBean
    DingTalkToErpConverter dingTalkToErpConverter;

    @Resource
    private DeptApi deptApi;
//    @Resource
//    private DeptConvert deptConvert;

    @MockBean
    ErpProductService erpProductService;

    @MockBean
    ErpDepartmentService erpDepartmentService;

    @MockBean
    AdminUserApi adminUserApi;
    @MockBean
    private PasswordEncoder passwordEncoder;
    @MockBean
    private FileApi fileApi;
    @Resource
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
    public void test3() {
        log.info(configApi.getConfigValueByKey("system.user.init-passwor"));
    }


}
