package cn.iocoder.yudao.module.trade.framework.delivery.core.impl;

import cn.iocoder.yudao.module.trade.framework.delivery.config.TradeExpressQueryProperties;
import cn.iocoder.yudao.module.trade.framework.delivery.core.dto.ExpressQueryReqDTO;
import cn.iocoder.yudao.module.trade.framework.delivery.core.dto.ExpressQueryRespDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
    void queryExpress() {
        ExpressQueryReqDTO reqDTO = new ExpressQueryReqDTO();
        reqDTO.setExpressCompanyCode("yto");
        reqDTO.setLogisticsNo("YT1764381060802");
        List<ExpressQueryRespDTO> expressQueryRespDTOS = kdNiaoExpressQueryProvider.realTimeQueryExpress(reqDTO);
        assertNotNull(expressQueryRespDTOS);
    }

    @Import({
            RestTemplateAutoConfiguration.class
    })
    @EnableConfigurationProperties(TradeExpressQueryProperties.class)
    public static class Application {
    }
}