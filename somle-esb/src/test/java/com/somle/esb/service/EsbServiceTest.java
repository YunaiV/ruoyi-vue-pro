package com.somle.esb.service;

import com.somle.ai.service.AiService;
import com.somle.amazon.service.AmazonService;
import com.somle.dingtalk.controller.DingTalkController;
import com.somle.dingtalk.service.DingTalkService;
import com.somle.eccang.model.EccangOrder;
import com.somle.eccang.service.EccangService;
import com.somle.erp.service.ErpService;
import com.somle.esb.config.QuartzConfig;
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
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

@Import({
    EsbService.class,
    ErpService.class,
    EccangService.class,
    DingTalkService.class,
    KingdeeService.class,
//    AmazonService.class,
    DingTalkToErpConverter.class,
    ErpToEccangConverter.class,
    ErpToKingdeeConverter.class,
    QuartzConfig.class,
})
//@ExtendWith(SpringExtension.class)
class EsbServiceTest extends BaseSpringTest {
    @Resource
    EsbService service;

    @Resource
    ErpService erpService;

    @Resource
    EccangService eccangService;

    @Resource
    DingTalkService dingTalkService;

    @Resource
    KingdeeService kingdeeService;

    @MockBean
    AmazonService amazonService;

    @MockBean
    MatomoService matomoService;

    @MockBean
    AiService aiService;

    @MockBean(name="productChannel")
    MessageChannel productChannel;

    @MockBean(name="saleChannel")
    MessageChannel saleChannel;

    @MockBean(name="dataChannel")
    MessageChannel dataChannel;

    @MockBean(name="departmentChannel")
    MessageChannel departmentChannel;

    @Resource
    DingTalkToErpConverter dingTalkToErpConverter;

    @MockBean
    EccangToErpConverter eccangToErpConverter;

    @Resource
    ErpToEccangConverter erpToEccangConverter;

    @Resource
    ErpToKingdeeConverter erpToKingdeeConverter;

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
//    @Test
//    void testDataCollect() {
//    }
//
    @Test
    void testDataCollect1() throws InterruptedException {
        Thread.sleep(10000);
    }

    @Test
    void syncDepartments() {
        service.syncDepartments();
    }
}