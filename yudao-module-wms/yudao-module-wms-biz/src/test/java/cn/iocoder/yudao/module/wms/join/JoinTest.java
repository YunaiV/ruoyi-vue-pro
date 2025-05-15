package cn.iocoder.yudao.module.wms.join;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.erp.api.product.ErpProductApi;
import cn.iocoder.yudao.module.wms.service.inbound.item.WmsInboundItemServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Disabled
@Slf4j
@Import({
    JoinTest.App.class,
})
@ComponentScan(value = {"cn.iocoder.yudao.module.erp","cn.iocoder.yudao.module.wms"})
class JoinTest extends BaseDbUnitTest {

//    @Resource
//    WmsInboundMapper inboundMapper;
//
//    @Resource
    @Resource
    WmsInboundItemServiceImpl inboundService;
    @MockitoBean
    private ErpProductApi productApi;
//
//
//    @Test
//    void testSubscription() {
//        WmsInboundPageReqVO pageReqVO = new WmsInboundPageReqVO();
//        PageResult<WmsInboundDO> pageResult = inboundMapper.selectPage(pageReqVO);
//        System.out.println();
//    }


    @Resource
    private ApplicationContext applicationContext;

    @Test
    public void printAllBeans() {
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        System.out.println("Beans provided by Spring:");

        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            //System.out.println(beanName);
            System.out.println(beanName+"\t\t"+bean.getClass().getName());
        }
    }


    public static class App  extends  BaseDbUnitTest.Application {
    }


}