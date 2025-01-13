package com.somle.esb.service;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpCustomRuleDTO;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.erp.api.supplier.dto.ErpSupplierDTO;
import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserReqDTO;
import com.somle.ai.model.AiName;
import com.somle.ai.service.AiService;
import com.somle.amazon.service.AmazonService;
import com.somle.dingtalk.service.DingTalkService;
import com.somle.eccang.model.EccangCategory;
import com.somle.eccang.model.EccangOrder;
import com.somle.eccang.model.EccangProduct;
import com.somle.eccang.model.EccangResponse;
import com.somle.eccang.service.EccangService;
import com.somle.esb.converter.DingTalkToErpConverter;
import com.somle.esb.converter.EccangToErpConverter;
import com.somle.esb.converter.ErpToEccangConverter;
import com.somle.esb.converter.ErpToKingdeeConverter;
import com.somle.esb.model.OssData;
import com.somle.kingdee.model.KingdeeAuxInfoDetail;
import com.somle.kingdee.model.KingdeeProduct;
import com.somle.kingdee.model.supplier.KingdeeSupplier;
import com.somle.kingdee.service.KingdeeService;
import com.somle.matomo.service.MatomoService;
import com.somle.shopify.service.ShopifyService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.Objects;


@Slf4j
@Service
public class EsbService {

    @Autowired
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
                    String credential = okhttp3.Credentials.basic(configApi.getConfigValueByKey("proxy.username"), configApi.getConfigValueByKey("proxy.password"));
                    return response.request().newBuilder()
                        .header("Proxy-Authorization", credential)
                        .build();
                })
                .build();

            shopifyService.client.setWebClient(client);
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
