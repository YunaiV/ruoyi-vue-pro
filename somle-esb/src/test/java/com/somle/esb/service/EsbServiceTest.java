package com.somle.esb.service;


import cn.iocoder.yudao.framework.mybatis.config.YudaoMybatisAutoConfiguration;
import cn.iocoder.yudao.framework.security.config.YudaoSecurityAutoConfiguration;
import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
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


@Import({EsbService.class,
        EsbMappingService.class,
//    ErpService.class,
        EccangService.class,
        DingTalkService.class,
        KingdeeService.class,
//    AmazonService.class,


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
class EsbServiceTest extends BaseDbUnitTest {
    @Resource
    private EsbService esbService;

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


}
