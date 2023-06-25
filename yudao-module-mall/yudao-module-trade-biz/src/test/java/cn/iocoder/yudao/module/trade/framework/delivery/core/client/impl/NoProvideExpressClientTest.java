package cn.iocoder.yudao.module.trade.framework.delivery.core.client.impl;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.trade.framework.delivery.config.ExpressClientConfig;
import cn.iocoder.yudao.module.trade.framework.delivery.config.TradeExpressProperties;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.ExpressClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// TODO @jason：可以参考 AliyunSmsClientTest 写，纯 mockito，无需启动 spring 容器
/**
 * @author jason
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = NoProvideExpressClientTest.Application.class)
@ActiveProfiles("unit-test") // 设置使用 trade-delivery-query 配置文件
@Import({ExpressClientConfig.class})
public class NoProvideExpressClientTest {

    @Resource
    private ExpressClient expressClient;

    @Test
    void getExpressTrackList() {
        ServiceException t =  assertThrows(ServiceException.class, () -> {
            expressClient.getExpressTrackList(null);
        });
        assertEquals(1011003006, t.getCode());
    }

    @Import({
            RestTemplateAutoConfiguration.class,
    })
    @EnableConfigurationProperties(TradeExpressProperties.class)
    public static class Application {

        @Bean
        private RestTemplate restTemplate(RestTemplateBuilder builder) {
            return builder.build();
        }
    }
}
