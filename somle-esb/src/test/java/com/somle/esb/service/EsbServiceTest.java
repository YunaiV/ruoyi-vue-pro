package com.somle.esb.service;

import cn.iocoder.yudao.framework.mybatis.config.YudaoMybatisAutoConfiguration;
import cn.iocoder.yudao.framework.security.config.YudaoSecurityAutoConfiguration;
import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import cn.iocoder.yudao.module.system.controller.admin.oauth2.vo.user.OAuth2UserInfoRespVO;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO;
import cn.iocoder.yudao.module.system.dal.mysql.dept.DeptMapper;
import cn.iocoder.yudao.module.system.service.dept.DeptService;
import cn.iocoder.yudao.module.system.service.dept.DeptServiceImpl;
import cn.iocoder.yudao.module.system.service.dept.PostService;
import cn.iocoder.yudao.module.system.service.dept.PostServiceImpl;
import cn.iocoder.yudao.module.system.service.permission.*;
import cn.iocoder.yudao.module.system.service.tenant.TenantService;
import cn.iocoder.yudao.module.system.service.tenant.TenantServiceImpl;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import cn.iocoder.yudao.module.system.service.user.AdminUserServiceImpl;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.github.yulichang.autoconfigure.MybatisPlusJoinAutoConfiguration;
import com.somle.ai.service.AiService;
import com.somle.amazon.service.AmazonService;
import com.somle.dingtalk.controller.DingTalkController;
import com.somle.dingtalk.service.DingTalkService;
import com.somle.eccang.model.EccangOrder;
import com.somle.eccang.service.EccangService;
import com.somle.erp.service.ErpService;
import com.somle.esb.config.IntegrationConfig;
import com.somle.esb.converter.DingTalkToErpConverter;
import com.somle.esb.converter.EccangToErpConverter;
import com.somle.esb.converter.ErpToEccangConverter;
import com.somle.esb.converter.ErpToKingdeeConverter;
import com.somle.framework.test.core.ut.BaseSpringTest;
import com.somle.kingdee.service.KingdeeService;
import com.somle.matomo.service.MatomoService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mybatis.spring.annotation.MapperScan;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.MessageChannel;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.context.ApplicationContext;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

@Import({
    EsbService.class,
    EsbMappingService.class,
    ErpService.class,
    EccangService.class,
    DingTalkService.class,
    KingdeeService.class,
//    AmazonService.class,


    DingTalkToErpConverter.class,
    ErpToEccangConverter.class,
    ErpToKingdeeConverter.class,
    EccangToErpConverter.class,

    DeptServiceImpl.class,
    AdminUserServiceImpl.class,
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
    MybatisPlusJoinAutoConfiguration.class, // MyBatis 的Join配置类
})
//@MapperScan("cn.iocoder.yudao.module.system.dal.mysql.dept")
class EsbServiceTest extends BaseSpringTest {
    @Resource
    EsbService service;

//    @Resource
//    ErpService erpService;
//
//    @Resource
//    EccangService eccangService;
//
//    @Resource
//    DingTalkService dingTalkService;
//
//    @Resource
//    KingdeeService kingdeeService;

    @MockBean
    AmazonService amazonService;

    @MockBean
    MatomoService matomoService;

    @MockBean
    AiService aiService;

    @Resource
    DeptMapper deptMapper;

    @Resource
    AdminUserService userService;

    @MockBean
    private PostService postService;
    @MockBean
    private PermissionService permissionService;
    @Resource
    private PasswordEncoder passwordEncoder;
    @MockBean
    private TenantService tenantService;
    @MockBean
    private FileApi fileApi;
    @MockBean
    private ConfigApi configApi;


    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void printAllBeans() {
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        System.out.println("Beans provided by Spring:");

        for (String beanName : beanNames) {
            System.out.println(beanName);
        }
    }

//    @Test
//    void dataCollect() {
//    }
//
    @Test
    void testMapper() {
        System.out.println(deptMapper.selectList());
    }

    @Test
    void testMapper2() {
        var test = new DeptDO()
//                .setId(115l)
                .setName("testName")
                .setSort(1024)
                .setStatus(0);
        System.out.println(deptMapper.insert(test));
        System.out.println(test.getId());
    }

    @Test
    void testDataCollect1() throws InterruptedException, SchedulerException {
        Thread.sleep(10000);
    }

    @Test
    void syncDepartments() {
        service.syncDepartments();
    }

    @Test
    void syncUsers() {
        service.syncUsers();
    }
}