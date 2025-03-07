package com.somle.esb.job;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.test.core.ut.SomleBaseSpringIntegrationTest;
import cn.iocoder.yudao.module.erp.api.logistic.customrule.ErpCustomRuleApi;
import cn.iocoder.yudao.module.erp.api.logistic.customrule.dto.ErpCustomRuleDTO;
import cn.iocoder.yudao.module.erp.api.product.ErpProductApi;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import com.somle.esb.handler.ErpCustomRuleHandler;
import com.somle.esb.handler.ErpProductHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class SyncErpProductJobTestSomle extends SomleBaseSpringIntegrationTest {
    @Resource
    ErpCustomRuleApi erpCustomRuleApi;
    @Resource
    ErpProductApi erpProductApi;
    @Mock
    ErpCustomRuleHandler erpCustomRuleHandler;
    @Mock
    ErpProductHandler erpProductHandler;

    @Test
    void syncErpProducts() {
        AtomicReference<List<String>> barCodes = new AtomicReference<>(new ArrayList<>());
        AtomicReference<List<ErpCustomRuleDTO>> customRuleDTOS = new AtomicReference<>();
        try {
            TenantContextHolder.setTenantId(50001L);// 自动
            // 发送消息
            Optional.ofNullable(erpCustomRuleApi.listCustomRules(null)).ifPresent(detailDTOS -> {
                barCodes.set(detailDTOS.stream().map(dto -> dto.getProductDTO().getBarCode()).toList());
                log.info("预计同步产品skus大小={{}},barCodes = {{}}", barCodes.get().size(), barCodes.get());
                int total = detailDTOS.size();
                int processed = 0;
                //输出预计同步的barcode集合
                for (ErpCustomRuleDTO detailDTO : detailDTOS) {
                    String barCode = detailDTO.getProductDTO().getBarCode();
                    log.debug("发送消息, BarCode = {}", barCode);
                    // 单独处理每个条目
                    erpCustomRuleHandler.syncCustomRulesToEccang(List.of(detailDTO));
                    erpCustomRuleHandler.syncCustomRulesToKingdee(List.of(detailDTO));
                    processed++;
                    log.info("SyncErpProduct Processed {}/{} ({}%)", processed, total, (100 * processed / total));
                }
                //根据detailDTOS获得产品id集合
                List<Long> productIds = detailDTOS.stream().map(ErpCustomRuleDTO::getProductId).toList();
                List<ErpProductDTO> productDTOs = erpProductApi.listProductDTOs(null);
                // 过滤掉已经在 `customRuleDTOS` 中的产品
                List<ErpProductDTO> productDTOS = productDTOs.stream()
                    .filter(dto -> !productIds.contains(dto.getId())).toList();
                productDTOS.forEach(dto -> {
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
