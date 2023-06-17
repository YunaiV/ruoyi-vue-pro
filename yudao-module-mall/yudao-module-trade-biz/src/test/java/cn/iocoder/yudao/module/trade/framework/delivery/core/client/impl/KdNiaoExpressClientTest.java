package cn.iocoder.yudao.module.trade.framework.delivery.core.client.impl;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.trade.framework.delivery.config.TradeExpressProperties;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.dto.ExpressTrackQueryReqDTO;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.impl.kdniao.KdNiaoExpressClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.assertThrows;

// TODO @jason：可以参考 AliyunSmsClientTest 写，纯 mockito，无需启动 spring 容器
/**
 * {@link KdNiaoExpressClient} 的单元测试
 *
 * @author jason
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = KdNiaoExpressClientTest.Application.class)
@ActiveProfiles("unit-test")
public class KdNiaoExpressClientTest {

    @Resource
    private RestTemplateBuilder builder;
    @Resource
    private TradeExpressProperties expressQueryProperties;

    private KdNiaoExpressClient kdNiaoExpressClient;

    @BeforeEach
    public void init(){
        kdNiaoExpressClient = new KdNiaoExpressClient(builder.build(),expressQueryProperties.getKdNiao());
    }
    @Test
    @Disabled("需要 授权 key. 暂时忽略")
    void testRealTimeQueryExpressFailed() {
        assertThrows(ServiceException.class,() ->{
            ExpressTrackQueryReqDTO reqDTO = new ExpressTrackQueryReqDTO();
            reqDTO.setExpressCode("yy");
            reqDTO.setLogisticsNo("YT9383342193097");
            kdNiaoExpressClient.getExpressTrackList(reqDTO);
        });
    }

    @Import({
            RestTemplateAutoConfiguration.class
    })
    @EnableConfigurationProperties(TradeExpressProperties.class)
    public static class Application {
    }
}
