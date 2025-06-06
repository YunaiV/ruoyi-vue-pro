package com.somle.esb.service;

import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
import com.somle.esb.model.OssData;
import com.somle.staples.service.ShopifyService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.net.Proxy;


@Slf4j
@Service
public class EsbService {

    @Resource
    MessageChannel dataChannel;

    @Autowired
    ShopifyService shopifyService;

    @Autowired
    private ConfigApi configApi;


    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    private void init() {
        try {
            var proxyHost = configApi.getConfigValueByKey("proxy.host");
            var proxyPort = Integer.valueOf(configApi.getConfigValueByKey("proxy.port"));
            var proxyUsername = configApi.getConfigValueByKey("proxy.username");
            var proxyPassword = configApi.getConfigValueByKey("proxy.password");

            // Create a Proxy instance
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));

            OkHttpClient client = new OkHttpClient.Builder()
                .proxy(proxy)
                .proxyAuthenticator((route, response) -> {
                    String credential = okhttp3.Credentials.basic(proxyUsername, proxyPassword);
                    return response.request().newBuilder()
                        .header("Proxy-Authorization", credential)
                        .build();
                })
                .build();

            shopifyService.shopifyClients.get(0).setWebClient(client);
            log.info("using proxy");
        } catch (Exception e) {
            log.error("not using proxy");
        }

    }


    public void printAllBeans() {
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        System.out.println("Beans provided by Spring:");

        for (String beanName : beanNames) {
            System.out.println(beanName);
        }
    }


    public void send(OssData data) {
        dataChannel.send(MessageBuilder.withPayload(data).build());
    }

}
