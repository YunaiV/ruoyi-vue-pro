package com.somle.esb.job;

import cn.iocoder.yudao.framework.mybatis.config.YudaoMybatisAutoConfiguration;
import cn.iocoder.yudao.framework.security.config.YudaoSecurityAutoConfiguration;
import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import cn.iocoder.yudao.module.system.dal.mysql.dept.DeptMapper;
import cn.iocoder.yudao.module.system.service.dept.DeptService;
import cn.iocoder.yudao.module.system.service.dept.PostService;
import cn.iocoder.yudao.module.system.service.permission.PermissionService;
import cn.iocoder.yudao.module.system.service.tenant.TenantService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.github.yulichang.autoconfigure.MybatisPlusJoinAutoConfiguration;
import com.somle.ai.service.AiService;
import com.somle.amazon.service.AmazonService;
import com.somle.dingtalk.service.DingTalkService;
import com.somle.eccang.service.EccangService;
import com.somle.esb.config.IntegrationConfig;
import com.somle.esb.converter.DingTalkToErpConverter;
import com.somle.esb.converter.EccangToErpConverter;
import com.somle.esb.converter.ErpToEccangConverter;
import com.somle.esb.converter.ErpToKingdeeConverter;
import com.somle.esb.service.AliyunService;
import com.somle.esb.service.EsbService;
import cn.iocoder.yudao.framework.test.core.ut.SomleBaseSpringTest;
import com.somle.kingdee.service.KingdeeService;
import com.somle.matomo.service.MatomoService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;

@Disabled
@Import({
    EsbService.class,
    AliyunService.class,
//    ErpService.class,
//    EccangService.class,
//    DingTalkService.class,
//    KingdeeService.class,
//    AmazonService.class,

//    DeptServiceImpl.class,
//    AdminUserServiceImpl.class,
//    PostServiceImpl.class,
//    RoleServiceImpl.class,
//    MenuServiceImpl.class,
//    TenantServiceImpl.class,
//    PermissionServiceImpl.class,


//    DingTalkToErpConverter.class,
//    ErpToEccangConverter.class,
//    ErpToKingdeeConverter.class,
//    EccangToErpConverter.class,

    ShopifyOrderDataJob.class,




    YudaoSecurityAutoConfiguration.class,
    SecurityAutoConfiguration.class,
    IntegrationConfig.class,
    QuartzAutoConfiguration.class,
    // MyBatis 配置类
    DataSourceAutoConfiguration.class,
    YudaoMybatisAutoConfiguration.class, //Enable DefaultDBFieldHandler
    MybatisPlusAutoConfiguration.class, // MyBatis 的自动配置类
    MybatisPlusJoinAutoConfiguration.class, // MyBatis 的Join配置类
})
//@MapperScan("cn.iocoder.yudao.module.system.dal.mysql.dept")
class EsbJobTest extends SomleBaseSpringTest {
    @Resource
    EsbService service;


    @MockBean
    EccangService eccangService;
    @MockBean
    DingTalkService dingTalkService;
    @MockBean
    KingdeeService kingdeeService;
    @MockBean
    AmazonService amazonService;
    @MockBean
    MatomoService matomoService;
    @MockBean
    AiService aiService;



    @MockBean
    DeptMapper deptMapper;
    @MockBean
    AdminUserService userService;
    @MockBean
    DeptService deptService;
    @MockBean
    PostService postService;
    @MockBean
    PermissionService permissionService;
    @MockBean
    PasswordEncoder passwordEncoder;
    @MockBean
    TenantService tenantService;
    @MockBean
    FileApi fileApi;
    @MockBean
    ConfigApi configApi;



    @MockBean
    DingTalkToErpConverter dingTalkToErpConverter;
    @MockBean
    ErpToEccangConverter erpToEccangConverter;
    @MockBean
    ErpToKingdeeConverter erpToKingdeeConverter;
    @MockBean
    EccangToErpConverter eccangToErpConverter;


    @Resource
    ShopifyOrderDataJob shopifyOrderDataJob;



    @Test
    void testDataJob() throws Exception {
        shopifyOrderDataJob.execute("2024-11-07");
    }
}