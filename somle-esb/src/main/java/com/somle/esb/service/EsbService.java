package com.somle.esb.service;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.erp.api.supplier.dto.ErpSupplierDTO;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptReqDTO;
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
import com.somle.erp.model.product.ErpCountrySku;
import com.somle.erp.service.ErpProductService;
import com.somle.esb.converter.DingTalkToErpConverter;
import com.somle.esb.converter.EccangToErpConverter;
import com.somle.esb.converter.ErpToEccangConverter;
import com.somle.esb.converter.ErpToKingdeeConverter;
import com.somle.esb.model.OssData;
import com.somle.kingdee.model.supplier.KingdeeSupplier;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    private DeptApi deptApi;

    @Autowired
    private AdminUserApi adminUserApi;

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
        EccangProduct eccangServiceProduct = eccangService.getProduct(product.getProductSku());
        if (ObjUtil.isNotEmpty(eccangServiceProduct)){
            product.setActionType("EDIT");
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

    /**
    * @Author Wqh
    * @Description 上传eccang产品信息
    * @Date 11:18 2024/11/5
    * @Param [message]
    * @return void
    **/
    @ServiceActivator(inputChannel = "syncExternalDataChannel")
    public void syncProductsToEccang(Message<List<ErpProductDTO>> message) {
        List<EccangProduct> eccangProducts = erpToEccangConverter.toEccang(message.getPayload());
        for (EccangProduct eccangProduct : eccangProducts){
            eccangProduct.setActionType("ADD");
            EccangProduct eccangServiceProduct = eccangService.getProduct(eccangProduct.getProductSku());
            //根据sku从eccang中获取产品，如果产品不为空，则表示已存在，操作则变为修改
            if (ObjUtil.isNotEmpty(eccangServiceProduct)){
                eccangProduct.setActionType("EDIT");
                //如果是修改就要上传默认采购单价
                //TODO 后续有变更，请修改
                eccangProduct.setProductPurchaseValue(0.001F);
            }
            log.debug(eccangProduct.toString());
            eccangService.addBatchProduct(List.of(eccangProduct));
        }
    }

    /**
     * @Author Wqh
     * @Description 上传金蝶产品信息
     * @Date 11:18 2024/11/5
     * @Param [message]
     * @return void
     **/
    @ServiceActivator(inputChannel = "syncExternalDataChannel")
    public void syncProductsToKingdee(Message<List<ErpProductDTO>> message) {
       /* List<KingdeeProduct> kingdee = erpToKingdeeConverter.toKingdee(message.getPayload());
        for (KingdeeProduct kingdeeProduct : kingdee){
            kingdeeService.addProduct(kingdeeProduct);
        }*/
    }

    /**
    * @Author Wqh
    * @Description 上传金蝶供应商信息
    * @Date 9:45 2024/11/6
    * @Param []
    * @return void
    **/
    public void syncSupplierToKingdee(ErpSupplierDTO erpSupplierDTO) {
        KingdeeSupplier kingdeeProduct = erpToKingdeeConverter.toKingdee(erpSupplierDTO);
        kingdeeService.addSupplier(kingdeeProduct);
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
    public void syncEccangDepartments() {
        dingTalkService.getDepartmentStream().forEach(dingTalkDepartment -> {
            log.info("begin syncing: " + dingTalkDepartment.toString());
            DeptReqDTO erpDepartment = dingTalkToErpConverter.toErp(dingTalkDepartment);
            log.info("dept to add " + erpDepartment);
            Long deptId = erpDepartment.getId();
            if (deptId != null) {
                deptApi.updateDept(erpDepartment);
            } else {
                deptId = deptApi.createDept(erpDepartment);
                var mapping = mappingService.toMapping(dingTalkDepartment);
                mapping
                    .setInternalId(deptId);
                mappingService.save(mapping);
            }
            //获取部门名称
            String name = erpDepartment.getName();
            if (!Objects.equals(name, "宁波索迈")){
                EccangCategory eccang = erpToEccangConverter.toEccang(String.valueOf(deptId));
                EccangResponse.EccangPage response = eccangService.addDepartment(eccang);
                log.info(response.toString());
            }
//        var eccangDepartment = erpToEccangConverter.toEccang(erpDepartment);
//        EccangResponse.BizContent response = eccangService.addDepartment(eccangDepartment);
//        log.info(response.toString());
//        var kingdeeDepartment = erpToKingdeeConverter.toKingdee(erpDepartment);
//        kingdeeService.addDepartment(kingdeeDepartment);
        });
    }

    public void syncKingDeeDepartments() {
        dingTalkService.getDepartmentStream().forEach(dingTalkDepartment -> {
            log.info("begin syncing: " + dingTalkDepartment.toString());
            DeptReqDTO erpDepartment = dingTalkToErpConverter.toErp(dingTalkDepartment);
            log.info("dept to add " + erpDepartment);
            Long deptId = erpDepartment.getId();
            if (deptId != null) {
                deptApi.updateDept(erpDepartment);
            } else {
                deptId = deptApi.createDept(erpDepartment);
                var mapping = mappingService.toMapping(dingTalkDepartment);
                mapping
                        .setInternalId(deptId);
                mappingService.save(mapping);
            }

        });
    }

    public void syncUsers() {
        dingTalkService.getUserDetailStream().forEach(dingTalkUser -> {
            log.info("begin syncing: " + dingTalkUser.toString());
            AdminUserReqDTO erpUser = dingTalkToErpConverter.toErp(dingTalkUser);
            log.info("user to add " + erpUser);
            if (erpUser.getId() != null) {
                adminUserApi.updateUser(erpUser);
            } else {
                erpUser.setUsername("temp");
                Long userId = adminUserApi.createUser(erpUser);
                erpUser.setId(userId).setUsername("SM" + String.format("%06d", userId));
                adminUserApi.updateUser(erpUser);
                var mapping = mappingService.toMapping(dingTalkUser);
                mapping
                    .setInternalId(userId);
                mappingService.save(mapping);
            }
        });
    }

}
