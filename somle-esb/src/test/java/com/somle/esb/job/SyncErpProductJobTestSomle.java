package com.somle.esb.job;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.test.core.ut.SomleBaseSpringIntegrationTest;
import cn.iocoder.yudao.module.erp.api.product.ErpProductApi;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.tms.api.logistic.customrule.TmsCustomRuleApi;
import cn.iocoder.yudao.module.tms.api.logistic.customrule.dto.TmsCustomRuleDTO;
import com.somle.esb.handler.erp.ErpCustomRuleHandler;
import com.somle.esb.handler.erp.ErpProductHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Disabled
@Slf4j
public class SyncErpProductJobTestSomle extends SomleBaseSpringIntegrationTest {
    @Resource
    TmsCustomRuleApi tmsCustomRuleApi;
    @Resource
    ErpProductApi erpProductApi;
    @Mock
    ErpCustomRuleHandler erpCustomRuleHandler;
    @Mock
    ErpProductHandler erpProductHandler;

    @Test
    void syncErpProducts() {
        AtomicReference<List<String>> barCodes = new AtomicReference<>(new ArrayList<>());
        AtomicReference<List<TmsCustomRuleDTO>> customRuleDTOS = new AtomicReference<>();
        try {
            TenantContextHolder.setTenantId(50001L);// 自动
            // 发送消息
            Optional.ofNullable(tmsCustomRuleApi.listCustomRules(null)).ifPresent(detailDTOS -> {
                barCodes.set(detailDTOS.stream().map(dto -> dto.getProductDTO().getCode()).toList());
                log.info("预计同步产品skus大小={{}},barCodes = {{}}", barCodes.get().size(), barCodes.get());
                int total = detailDTOS.size();
                int processed = 0;
                //输出预计同步的barcode集合
                for (TmsCustomRuleDTO detailDTO : detailDTOS) {
                    String barCode = detailDTO.getProductDTO().getCode();
                    log.debug("发送消息, BarCode = {}", barCode);
                    // 单独处理每个条目
                    erpCustomRuleHandler.syncCustomRulesToEccang(List.of(detailDTO));
                    erpCustomRuleHandler.syncCustomRulesToKingdee(List.of(detailDTO));
                    processed++;
                    log.info("SyncErpProduct Processed {}/{} ({}%)", processed, total, (100 * processed / total));
                }
                //根据detailDTOS获得产品id集合
                List<Long> productIds = detailDTOS.stream().map(TmsCustomRuleDTO::getProductId).toList();
                // 过滤掉已经在 `customRuleDTOS` 中的产品
                List<ErpProductDTO> pDTOs = erpProductApi.listProductDTOs(null).stream()
                    .filter(dto -> !productIds.contains(dto.getId())).toList();
                pDTOs.forEach(dto -> {
                    erpProductHandler.syncProductsToKingdee(List.of(dto));
                    erpProductHandler.syncProductsToEccang(List.of(dto));
                });
            });
        } finally {
            TenantContextHolder.clear(); // 清理租户上下文，避免线程复用导致问题
        }
        // 返回数据总量和 barCodes
        int total = Optional.ofNullable(customRuleDTOS.get())
            .map(List::size)
            .orElse(0);
        String string = String.format("success, total=%d, barCodes=%s", total, barCodes.get());
        log.info(string);
    }
}
