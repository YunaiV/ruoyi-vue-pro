package com.somle.amazon.service;

import com.somle.amazon.controller.vo.AmazonSpReportVO;
import com.somle.amazon.model.AmazonSeller;
import com.somle.amazon.model.enums.ProcessingStatuses;
import com.somle.amazon.repository.AmazonAccountRepository;
import com.somle.framework.common.util.general.CoreUtils;
import com.somle.framework.common.util.object.BeanUtils;
import com.somle.framework.test.core.ut.BaseSpringTest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.List;


@Slf4j
@Import({
        AmazonService.class,
})
class AmazonSpClientTest extends BaseSpringTest {
    @Resource
    AmazonService amazonService;

    @Resource
    AmazonAccountRepository accountRepository;

    @Test
    void getSettlementReport() {
        var shop = amazonService.shopRepository.findByCountryCode("UK");
        var report = amazonService.spClient.getSettlementReport(shop, LocalDate.of(2024,8,10));
        log.info(report.toString());
    }


    @Test
    void getAsinReport() {
        var shop = amazonService.shopRepository.findByCountryCode("UK");
        var report = amazonService.spClient.getAsinReport(shop, LocalDate.of(2024,8,10));
        log.info(report.toString());
    }

    @Test
    void getReports() {
        var shop = amazonService.shopRepository.findByCountryCode("UK");
        var vo = AmazonSpReportVO.builder()
                .reportTypes(List.of("GET_V2_SETTLEMENT_REPORT_DATA_FLAT_FILE"))
                .processingStatuses(List.of(ProcessingStatuses.DONE))
                .pageSize(1)
                .build();
        System.out.println(BeanUtils.beanToStringMap(vo));
        var report = amazonService.spClient.getReports(shop, vo);
        log.info(report.toString());
    }


}