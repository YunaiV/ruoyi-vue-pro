package cn.iocoder.yudao.module.trade.framework.delivery.core.client.impl;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.trade.framework.delivery.config.TradeExpressProperties;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.dto.ExpressQueryReqDTO;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author jason
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = Kd100ExpressClientTest.Application.class)
@ActiveProfiles("unit-test") // 设置使用 trade-delivery-query 配置文件
public class Kd100ExpressClientTest {
    @Resource
    private RestTemplateBuilder builder;
    @Resource
    private TradeExpressProperties expressQueryProperties;

    private Kd100ExpressClient kd100ExpressClient;

    @BeforeEach
    public void init(){
        kd100ExpressClient = new Kd100ExpressClient(builder.build(),expressQueryProperties.getKd100());
    }
    @Test
    @Disabled("需要 授权 key. 暂时忽略")
    void testRealTimeQueryExpressFailed() {
        ServiceException t =  assertThrows(ServiceException.class, () -> {
            ExpressQueryReqDTO reqDTO = new ExpressQueryReqDTO();
            reqDTO.setExpressCode("yto");
            reqDTO.setLogisticsNo("YT9383342193097");
            kd100ExpressClient.getExpressTrackList(reqDTO);
        });
        assertEquals(1011003005, t.getCode());
    }

    @Import({
            RestTemplateAutoConfiguration.class
    })
    @EnableConfigurationProperties(TradeExpressProperties.class)
    public static class Application {
    }
}