package com.somle.esb.service;

import cn.iocoder.yudao.module.system.controller.admin.user.vo.user.UserSaveReqVO;
import cn.iocoder.yudao.module.system.service.dept.DeptService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import com.somle.ai.model.AiName;
import com.somle.ai.service.AiService;
import com.somle.amazon.service.AmazonService;
import com.somle.dingtalk.model.DingTalkDepartment;
import com.somle.dingtalk.service.DingTalkService;
import com.somle.eccang.model.EccangOrder;
import com.somle.eccang.model.EccangProduct;
import com.somle.eccang.model.EccangResponse;
import com.somle.eccang.service.EccangService;
import com.somle.erp.model.ErpCountrySku;
import com.somle.erp.service.ErpService;
import com.somle.esb.converter.DingTalkToErpConverter;
import com.somle.esb.converter.EccangToErpConverter;
import com.somle.esb.converter.ErpToEccangConverter;
import com.somle.esb.converter.ErpToKingdeeConverter;
import com.somle.esb.job.DataJob;
import com.somle.esb.model.Domain;
import com.somle.esb.model.EsbMapping;
import com.somle.esb.model.OssData;
import com.somle.esb.repository.EsbMappingRepository;
import com.somle.framework.common.util.general.CoreUtils;
import com.somle.kingdee.service.KingdeeService;
import com.somle.matomo.service.MatomoService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.IntStream;

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
    ErpService erpService;

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



    @PostConstruct
    public void scheduleJob() throws SchedulerException {
        JobDetail dataJobDetail = JobBuilder.newJob(DataJob.class)
            .withIdentity("dataJob")
            .storeDurably()  // Keeps the job even if no trigger is associated
            .build();

        Trigger dataTrigger = TriggerBuilder.newTrigger()
            .forJob(dataJobDetail)
            .withIdentity("dataTrigger")
            .withSchedule(CronScheduleBuilder.cronSchedule("0 10 1 * * ?"))
//            .withSchedule(CronScheduleBuilder.cronSchedule("0/1 * * * * ?"))
            .build();

        // Schedule the job using the scheduler
        scheduler.scheduleJob(dataJobDetail, dataTrigger);
    }




    
    // @PostConstruct
    // public void init() {
    // }

    // @EventListener(ApplicationReadyEvent.class)
    // @Transactional(readOnly = true)
    // public void test() {
    //     EsbCountrySku x = countrySkuRepository.findAll().get(0);
    //     log.debug(x.getStyleSku().toString());
    // }

//    @Scheduled(cron = "0 10 1 * * *") // Executes at 1:10 AM every day
//    @Scheduled(fixedDelay = 999999999, initialDelay = 5000)
    public void dataCollect() {
        LocalDate scheduleDate = LocalDate.now();
        dataCollect(scheduleDate);
    }

    public void dataCollect(Domain domain) {
        LocalDate scheduleDate = LocalDate.now();
        dataCollect(scheduleDate);
    }


    public void dataCollect(LocalDate startScheduleDate, LocalDate endScheduleDate) {
        LocalDate scheduleDate = startScheduleDate;
        while (! scheduleDate.isAfter(endScheduleDate)) {
            dataCollect(scheduleDate);
            scheduleDate = scheduleDate.plusDays(1);
        }
    }

    public void dataCollect(LocalDate startScheduleDate, LocalDate endScheduleDate, Domain domain) {
        LocalDate scheduleDate = startScheduleDate;
        while (! scheduleDate.isAfter(endScheduleDate)) {
            dataCollect(scheduleDate, domain);
            scheduleDate = scheduleDate.plusDays(1);
        }
    }

    public void dataCollect(LocalDate scheduleDate) {
        Arrays.stream(Domain.values()).forEach(domain->{dataCollect(scheduleDate, domain);});
    }


    public void dataCollect(LocalDate scheduleDate, Domain domain) {
        LocalDate today = scheduleDate;
        LocalDate yesterday = today.minusDays(1);
        LocalDate beforeYesterday = today.minusDays(2);
        LocalDateTime yesterdayFirstSecond = yesterday.atStartOfDay();
        LocalDateTime yesterdayLastSecond = today.atStartOfDay().minusSeconds(1);

        switch (domain) {
            case AI:
                dataChannel.send(org.springframework.messaging.support.MessageBuilder.withPayload(
                    OssData.builder()
                        .database(domain.getValue())
                        .tableName("country")
                        .syncType("full")
                        .requestTimestamp(System.currentTimeMillis())
                        .folderDate(LocalDate.now())
                        .content(aiService.getCountries().toList())
                        .headers(null)
                        .build()
                ).build());

                dataChannel.send(org.springframework.messaging.support.MessageBuilder.withPayload(
                    OssData.builder()
                        .database(domain.getValue())
                        .tableName("currency")
                        .syncType("full")
                        .requestTimestamp(System.currentTimeMillis())
                        .folderDate(LocalDate.now())
                        .content(aiService.getCurrencies().toList())
                        .headers(null)
                        .build()
                ).build());

                dataChannel.send(org.springframework.messaging.support.MessageBuilder.withPayload(
                    OssData.builder()
                        .database(domain.getValue())
                        .tableName("person")
                        .syncType("inc")
                        .requestTimestamp(System.currentTimeMillis())
                        .folderDate(yesterday)
                        .content(aiService.getNames(yesterday).toList())
                        .headers(null)
                        .build()
                ).build());

                dataChannel.send(org.springframework.messaging.support.MessageBuilder.withPayload(
                    OssData.builder()
                        .database(domain.getValue())
                        .tableName("address")
                        .syncType("inc")
                        .requestTimestamp(System.currentTimeMillis())
                        .folderDate(yesterday)
                        .content(aiService.getAddresses(yesterday).toList())
                        .headers(null)
                        .build()
                ).build());

                break;

            case ECCANG:
                dataChannel.send(MessageBuilder.withPayload(
                    OssData.builder()
                        .database(domain.getValue())
                        .tableName("warehouse")
                        .syncType("full")
                        .requestTimestamp(System.currentTimeMillis())
                        .folderDate(today)
                        .content(eccangService.getWarehouseList())
                        .headers(null)
                        .build()
                ).build());

                eccangService.getOrderPlatformShipPage(yesterdayFirstSecond, yesterdayLastSecond)
                    .forEach(page -> {
                        OssData data = OssData.builder()
                            .database(domain.getValue())
                            .tableName("order_platform_ship")
                            .syncType("inc")
                            .requestTimestamp(System.currentTimeMillis())
                            .folderDate(yesterday)
                            .content(page)
                            .headers(null)
                            .build();
                        dataChannel.send(MessageBuilder.withPayload(data).build());
                    });

                eccangService.getOrderWarehouseShipPage(yesterdayFirstSecond, yesterdayLastSecond)
                    .forEach(page -> {
                        OssData data = OssData.builder()
                            .database(domain.getValue())
                            .tableName("order_warehouse_ship")
                            .syncType("inc")
                            .requestTimestamp(System.currentTimeMillis())
                            .folderDate(yesterday)
                            .content(page)
                            .headers(null)
                            .build();
                        dataChannel.send(MessageBuilder.withPayload(data).build());
                    });

                eccangService.getOrderUnShipPage()
                    .forEach(page -> {
                        OssData data = OssData.builder()
                            .database(domain.getValue())
                            .tableName("order_unship")
                            .syncType("inc")
                            .requestTimestamp(System.currentTimeMillis())
                            .folderDate(yesterday)
                            .content(page)
                            .headers(null)
                            .build();
                        dataChannel.send(MessageBuilder.withPayload(data).build());
                    });

                eccangService.getInventoryBatchLog(yesterdayFirstSecond, yesterdayLastSecond)
                    .forEach(page -> {
                        CoreUtils.sleep(2000);
                        OssData data = OssData.builder()
                            .database(domain.getValue())
                            .tableName("stock_log")
                            .syncType("inc")
                            .requestTimestamp(System.currentTimeMillis())
                            .folderDate(yesterday)
                            .content(page)
                            .headers(null)
                            .build();
                        dataChannel.send(MessageBuilder.withPayload(data).build());
                    });

                break;

            case MATOMO:
                IntStream.range(1, 7).boxed().forEach(idSite -> {
                    OssData data = OssData.builder()
                        .database(domain.getValue())
                        .tableName("visit")
                        .syncType("inc")
                        .requestTimestamp(System.currentTimeMillis())
                        .folderDate(yesterday)
                        .content(matomoService.getVisits(idSite, yesterday).toList())
                        .headers(null)
                        .build();
                    dataChannel.send(org.springframework.messaging.support.MessageBuilder.withPayload(data).build());
                });

                break;

            case AMAZONAD:
                amazonService.adClient.getAllAdReport(beforeYesterday)
                    .forEach(page -> {
                        OssData data = OssData.builder()
                            .database(domain.getValue())
                            .tableName("ad_report")
                            .syncType("inc")
                            .requestTimestamp(System.currentTimeMillis())
                            .folderDate(beforeYesterday)
                            .content(page)
                            .headers(null)
                            .build();
                        dataChannel.send(MessageBuilder.withPayload(data).build());
                    });
                break;

            case AMAZONSP:
                amazonService.spClient.getAllAsinReport(beforeYesterday).parallel()
                    .forEach(page -> {
                        OssData data = OssData.builder()
                            .database(domain.getValue())
                            .tableName("asin_report")
                            .syncType("inc")
                            .requestTimestamp(System.currentTimeMillis())
                            .folderDate(beforeYesterday)
                            .content(page)
                            .headers(null)
                            .build();
                        dataChannel.send(MessageBuilder.withPayload(data).build());
                    });
                break;

        }
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
        EccangResponse.BizContent response = eccangService.addProduct(product);
        log.info(response.toString());
    }

    @ServiceActivator(inputChannel = "productChannel")
    public boolean handleProducts(Message<EccangProduct> message) {
        var product = message.getPayload();
        var erpProduct = eccangToErpConverter.toEsb(product);
        erpService.saveProduct(erpProduct);
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
