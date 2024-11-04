package com.somle.esb.service;

import cn.iocoder.yudao.module.system.service.dept.DeptService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import com.somle.ai.model.AiName;
import com.somle.ai.service.AiService;
import com.somle.amazon.service.AmazonService;
import com.somle.dingtalk.service.DingTalkService;
import com.somle.eccang.model.EccangOrder;
import com.somle.eccang.model.EccangProduct;
import com.somle.eccang.model.EccangResponse;
import com.somle.eccang.service.EccangService;
import com.somle.erp.model.product.ErpCountrySku;
import com.somle.erp.service.ErpProductService;
import com.somle.esb.converter.DingTalkToErpConverter;
import com.somle.esb.converter.EccangToErpConverter;
import com.somle.esb.converter.ErpToEccangConverter;
import com.somle.esb.converter.ErpToKingdeeConverter;
import com.somle.esb.model.OssData;
import com.somle.kingdee.service.KingdeeService;
import com.somle.matomo.service.MatomoService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EsbService {


    @Autowired
    MessageChannel productChannel;

    @Autowired
    MessageChannel dataChannel;

    @Autowired
    MessageChannel departmentChannel;

    @Autowired
    AmazonService amazonService;

    @Autowired
    DingTalkService dingTalkService;


    @Autowired
    MatomoService matomoService;

    @Autowired
    ErpProductService erpProductService;

    @Autowired
    DeptService deptService;

    @Autowired
    AdminUserService userService;

    @Autowired
    AiService aiService;

    @Autowired
    KingdeeService kingdeeService;

    @Autowired
    EccangService eccangService;

    @Autowired
    DingTalkToErpConverter dingTalkToErpConverter;

    @Autowired
    EccangToErpConverter eccangToErpConverter;

    @Autowired
    ErpToEccangConverter erpToEccangConverter;

    @Autowired
    ErpToKingdeeConverter erpToKingdeeConverter;

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private EsbMappingService mappingService;



    @Autowired
    private ApplicationContext applicationContext;


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


    @ServiceActivator(inputChannel = "productChannel")
    public void handleProduct(Message<ErpCountrySku> message) {
        EccangProduct product = erpToEccangConverter.toEccang(message.getPayload());
        product.setActionType("ADD");
        try {
            eccangService.getProduct(product.getProductSku());
            product.setActionType("EDIT");
        } catch (Exception e) {
            log.info("sku not exist, adding new");
            log.debug(e.toString());
            e.printStackTrace();
        }
        log.debug(product.toString());
        EccangResponse.EccangPage response = eccangService.addProduct(product);
        log.info(response.toString());
    }

    @ServiceActivator(inputChannel = "productChannel")
    public boolean handleProducts(Message<EccangProduct> message) {
        var product = message.getPayload();
        var erpProduct = eccangToErpConverter.toEsb(product);
        erpProductService.saveProduct(erpProduct);
        var kingdeeProduct = erpToKingdeeConverter.toKingdee(erpProduct);
        kingdeeService.addProduct(kingdeeProduct);
        return true;
    }

    @ServiceActivator(inputChannel = "saleChannel")
    public void handleSale(Message<EccangOrder> message) {
        var eccangOrder = message.getPayload();
        var erpSale = eccangToErpConverter.toEsb(eccangOrder);

        AiName name = AiName.builder()
            .name(erpSale.getCustomer().getName())
            .build();
        aiService.addName(name);
        aiService.addAddress(erpSale.getAddress());

    }

//    @ServiceActivator(inputChannel = "departmentChannel")
//    public void handleDepartment(Message<DingTalkDepartment> message) {
//        var dingtalkDepartment = message.getPayload();
//        log.info("receiving message: " + dingtalkDepartment.toString());
//        var erpDepartment = dingTalkToErpConverter.toErp(message.getPayload());
//        deptService.createDept(erpDepartment);
//        var eccangDepartment = erpToEccangConverter.toEccang(erpDepartment);
//        EccangResponse.BizContent response = eccangService.addDepartment(eccangDepartment);
//        log.info(response.toString());
//        var kingdeeDepartment = erpToKingdeeConverter.toKingdee(erpDepartment);
//        kingdeeService.addDepartment(kingdeeDepartment);
//    }


    public void syncDepartments() {
        dingTalkService.getDepartmentStream().forEach(dingTalkDepartment -> {
            log.info("begin syncing: " + dingTalkDepartment.toString());
            var erpDepartment = dingTalkToErpConverter.toErp(dingTalkDepartment);
            log.info("dept to add " + erpDepartment);
            if (erpDepartment.getId() != null) {
                deptService.updateDept(erpDepartment);
            } else {
                var deptId = deptService.createDept(erpDepartment);
                var mapping = mappingService.toMapping(dingTalkDepartment);
                mapping
                    .setInternalId(deptId);
                mappingService.save(mapping);
            }

//        var eccangDepartment = erpToEccangConverter.toEccang(erpDepartment);
//        EccangResponse.BizContent response = eccangService.addDepartment(eccangDepartment);
//        log.info(response.toString());
//        var kingdeeDepartment = erpToKingdeeConverter.toKingdee(erpDepartment);
//        kingdeeService.addDepartment(kingdeeDepartment);
        });
    }

    public void syncUsers() {
        dingTalkService.getUserDetailStream().forEach(dingTalkUser -> {
            log.info("begin syncing: " + dingTalkUser.toString());
            var erpUser = dingTalkToErpConverter.toErp(dingTalkUser);
            log.info("user to add " + erpUser);
            if (erpUser.getId() != null) {
                userService.updateUser(erpUser);
            } else {
                erpUser.setUsername("temp");
                Long userId = userService.createUser(erpUser);
                erpUser.setId(userId).setUsername("SM" + String.format("%06d", userId));
                userService.updateUser(erpUser);
                var mapping = mappingService.toMapping(dingTalkUser);
                mapping
                    .setInternalId(userId);
                mappingService.save(mapping);
            }
        });
    }

}
