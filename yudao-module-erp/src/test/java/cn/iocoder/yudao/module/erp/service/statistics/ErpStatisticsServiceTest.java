package cn.iocoder.yudao.module.erp.service.statistics;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.erp.dal.mysql.statistics.ErpPurchaseStatisticsMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.statistics.ErpSaleStatisticsMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * ERP 统计服务测试类
 * 主要测试在多租户关闭情况下，统计查询是否能正常工作
 *
 * @author 芋道源码
 */
@SpringBootTest
@ActiveProfiles("unit-test")
public class ErpStatisticsServiceTest {

    @Resource
    private ErpSaleStatisticsService saleStatisticsService;

    @Resource
    private ErpPurchaseStatisticsService purchaseStatisticsService;

    @MockBean
    private ErpSaleStatisticsMapper saleStatisticsMapper;

    @MockBean
    private ErpPurchaseStatisticsMapper purchaseStatisticsMapper;

    @BeforeEach
    void setUp() {
        // 清理租户上下文
        TenantContextHolder.clear();
    }

    @AfterEach
    void tearDown() {
        // 清理租户上下文
        TenantContextHolder.clear();
    }

    @Test
    void testSaleStatisticsWithoutTenant() {
        // 准备参数
        LocalDateTime beginTime = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 1, 31, 23, 59, 59);
        BigDecimal expectedPrice = new BigDecimal("1000.00");

        // Mock 返回值
        when(saleStatisticsMapper.getSalePrice(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(expectedPrice);

        // 测试：在没有租户ID的情况下调用销售统计
        assertDoesNotThrow(() -> {
            BigDecimal result = saleStatisticsService.getSalePrice(beginTime, endTime);
            assertEquals(expectedPrice, result);
        }, "在多租户关闭时，销售统计查询应该能正常工作");
    }

    @Test
    void testPurchaseStatisticsWithoutTenant() {
        // 准备参数
        LocalDateTime beginTime = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 1, 31, 23, 59, 59);
        BigDecimal expectedPrice = new BigDecimal("800.00");

        // Mock 返回值
        when(purchaseStatisticsMapper.getPurchasePrice(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(expectedPrice);

        // 测试：在没有租户ID的情况下调用采购统计
        assertDoesNotThrow(() -> {
            BigDecimal result = purchaseStatisticsService.getPurchasePrice(beginTime, endTime);
            assertEquals(expectedPrice, result);
        }, "在多租户关闭时，采购统计查询应该能正常工作");
    }

    @Test
    void testSaleStatisticsWithTenant() {
        // 设置租户ID
        Long tenantId = 1L;
        TenantContextHolder.setTenantId(tenantId);

        // 准备参数
        LocalDateTime beginTime = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 1, 31, 23, 59, 59);
        BigDecimal expectedPrice = new BigDecimal("1500.00");

        // Mock 返回值
        when(saleStatisticsMapper.getSalePrice(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(expectedPrice);

        // 测试：在有租户ID的情况下调用销售统计
        assertDoesNotThrow(() -> {
            BigDecimal result = saleStatisticsService.getSalePrice(beginTime, endTime);
            assertEquals(expectedPrice, result);
        }, "在多租户开启时，销售统计查询应该能正常工作");

        // 验证租户ID是否正确设置
        assertEquals(tenantId, TenantContextHolder.getTenantId());
    }

    @Test
    void testPurchaseStatisticsWithTenant() {
        // 设置租户ID
        Long tenantId = 2L;
        TenantContextHolder.setTenantId(tenantId);

        // 准备参数
        LocalDateTime beginTime = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 1, 31, 23, 59, 59);
        BigDecimal expectedPrice = new BigDecimal("1200.00");

        // Mock 返回值
        when(purchaseStatisticsMapper.getPurchasePrice(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(expectedPrice);

        // 测试：在有租户ID的情况下调用采购统计
        assertDoesNotThrow(() -> {
            BigDecimal result = purchaseStatisticsService.getPurchasePrice(beginTime, endTime);
            assertEquals(expectedPrice, result);
        }, "在多租户开启时，采购统计查询应该能正常工作");

        // 验证租户ID是否正确设置
        assertEquals(tenantId, TenantContextHolder.getTenantId());
    }

    @Test
    void testTenantContextHolderMethods() {
        // 测试 getTenantId() 在没有设置租户时返回 null
        assertNull(TenantContextHolder.getTenantId(), "未设置租户时应该返回 null");

        // 设置租户ID
        Long tenantId = 3L;
        TenantContextHolder.setTenantId(tenantId);
        assertEquals(tenantId, TenantContextHolder.getTenantId(), "设置租户后应该能正确获取");

        // 清理租户上下文
        TenantContextHolder.clear();
        assertNull(TenantContextHolder.getTenantId(), "清理后应该返回 null");
    }
}
