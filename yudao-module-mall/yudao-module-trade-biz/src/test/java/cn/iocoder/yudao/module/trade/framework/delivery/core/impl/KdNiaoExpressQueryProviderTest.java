package cn.iocoder.yudao.module.trade.framework.delivery.core.impl;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.trade.framework.delivery.config.TradeExpressQueryProperties;
import cn.iocoder.yudao.module.trade.framework.delivery.core.dto.ExpressQueryReqDTO;
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

/**
 * @author jason
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = KdNiaoExpressQueryProviderTest.Application.class)
@ActiveProfiles("trade-delivery-query") // 设置使用 trade-delivery-query 配置文件
public class KdNiaoExpressQueryProviderTest {
    @Resource
    private RestTemplateBuilder builder;
    @Resource
    private TradeExpressQueryProperties expressQueryProperties;

    private KdNiaoExpressQueryProvider kdNiaoExpressQueryProvider;

    @BeforeEach
    public void init(){
        kdNiaoExpressQueryProvider = new KdNiaoExpressQueryProvider(builder.build(),expressQueryProperties.getKdNiao());
    }
    @Test
    @Disabled("需要 授权 key. 暂时忽略")
    void testRealTimeQueryExpressFailed() {
        assertThrows(ServiceException.class,() ->{
            ExpressQueryReqDTO reqDTO = new ExpressQueryReqDTO();
            reqDTO.setExpressCompanyCode("yy");
            reqDTO.setLogisticsNo("YT9383342193097");
            kdNiaoExpressQueryProvider.realTimeQueryExpress(reqDTO);
        });
    }

    @Import({
            RestTemplateAutoConfiguration.class
    })
    @EnableConfigurationProperties(TradeExpressQueryProperties.class)
    public static class Application {
    }
}