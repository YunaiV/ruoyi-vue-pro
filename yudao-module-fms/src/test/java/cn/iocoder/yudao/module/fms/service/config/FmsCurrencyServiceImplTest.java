package cn.iocoder.yudao.module.fms.service.config;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.currency.FmsCurrencySaveReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountSetDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountUserDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsCurrencyDO;
import cn.iocoder.yudao.module.fms.dal.mysql.config.FmsCurrencyMapper;
import cn.iocoder.yudao.module.fms.enums.config.FmsCurrencyPresetEnum;
import cn.iocoder.yudao.module.fms.service.config.FmsAccountSetService;
import cn.iocoder.yudao.module.fms.service.config.FmsSubjectService;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.CURRENCY_CODE_DUPLICATE;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.CURRENCY_IN_USE;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.CURRENCY_NOT_EXISTS;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.CURRENCY_STANDARD_NOT_DELETABLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@Import(FmsCurrencyServiceImpl.class)
public class FmsCurrencyServiceImplTest extends BaseDbUnitTest {

    @Resource
    private FmsCurrencyServiceImpl currencyService;
    @Resource
    private FmsCurrencyMapper currencyMapper;

    @MockBean
    private FmsAccountSetService accountSetService;
    @MockBean
    private FmsSubjectService subjectService;

    @Test
    public void testInitializeStandardCurrency_success() {
        // 准备参数
        Long accountSetId = 1L;

        // 调用
        FmsCurrencyDO currency = currencyService.initializeStandardCurrency(
                accountSetId, FmsCurrencyPresetEnum.RMB);

        // 断言
        FmsCurrencyDO dbCurrency = currencyMapper.selectById(currency.getId());
        assertEquals(accountSetId, dbCurrency.getAccountSetId());
        assertEquals(FmsCurrencyPresetEnum.RMB.getCode(), dbCurrency.getCode());
        assertEquals(FmsCurrencyPresetEnum.RMB.getName(), dbCurrency.getName());
        assertEquals(0, BigDecimal.ONE.compareTo(dbCurrency.getExchangeRate()));
        assertTrue(dbCurrency.getStandard());
    }

    @Test
    public void testCreateCurrency_success() {
        // mock 数据
        Long accountSetId = 1L;
        mockAccountSetAccess(accountSetId);
        // 准备参数
        FmsCurrencySaveReqVO reqVO = buildSaveReqVO(null, accountSetId,
                "USD", "美元", new BigDecimal("7.120000"));

        // 调用
        Long currencyId = currencyService.createCurrency(reqVO, 10L);

        // 断言
        FmsCurrencyDO currency = currencyMapper.selectById(currencyId);
        assertEquals("USD", currency.getCode());
        assertEquals("美元", currency.getName());
        assertEquals(0, new BigDecimal("7.120000").compareTo(currency.getExchangeRate()));
        assertEquals(Boolean.FALSE, currency.getStandard());
    }

    @Test
    public void testCreateCurrency_codeDuplicate() {
        // mock 数据
        Long accountSetId = 1L;
        mockAccountSetAccess(accountSetId);
        currencyMapper.insert(buildCurrencyDO(accountSetId, "USD", "美元", false));
        // 准备参数
        FmsCurrencySaveReqVO reqVO = buildSaveReqVO(null, accountSetId,
                "USD", "美金", new BigDecimal("7.100000"));

        // 调用，并断言
        assertServiceException(() -> currencyService.createCurrency(reqVO, 10L), CURRENCY_CODE_DUPLICATE);
    }

    @Test
    public void testUpdateCurrency_standardFieldsProtected() {
        // mock 数据
        Long accountSetId = 1L;
        mockAccountSetAccess(accountSetId);
        FmsCurrencyDO currency = buildCurrencyDO(accountSetId, "RMB", "人民币", true);
        currency.setExchangeRate(BigDecimal.ONE);
        currencyMapper.insert(currency);
        // 准备参数
        FmsCurrencySaveReqVO reqVO = buildSaveReqVO(currency.getId(), accountSetId,
                "CNY", "人民币本位币", new BigDecimal("2.000000"));

        // 调用
        currencyService.updateCurrency(reqVO, 10L);

        // 断言
        FmsCurrencyDO updateCurrency = currencyMapper.selectById(currency.getId());
        assertEquals("RMB", updateCurrency.getCode());
        assertEquals("人民币本位币", updateCurrency.getName());
        assertEquals(0, BigDecimal.ONE.compareTo(updateCurrency.getExchangeRate()));
        assertTrue(updateCurrency.getStandard());
    }

    @Test
    public void testDeleteCurrency_standardNotDeletable() {
        // mock 数据
        Long accountSetId = 1L;
        mockAccountSetAccess(accountSetId);
        FmsCurrencyDO currency = buildCurrencyDO(accountSetId, "RMB", "人民币", true);
        currencyMapper.insert(currency);

        // 调用，并断言
        assertServiceException(() -> currencyService.deleteCurrency(
                accountSetId, currency.getId(), 10L), CURRENCY_STANDARD_NOT_DELETABLE);
    }

    @Test
    public void testDeleteCurrency_success() {
        // mock 数据
        Long accountSetId = 1L;
        mockAccountSetAccess(accountSetId);
        FmsCurrencyDO currency = buildCurrencyDO(accountSetId, "USD", "美元", false);
        currencyMapper.insert(currency);

        // 调用
        currencyService.deleteCurrency(accountSetId, currency.getId(), 10L);

        // 断言
        assertNull(currencyMapper.selectById(currency.getId()));
    }

    @Test
    public void testDeleteCurrency_inUse() {
        // mock 数据
        Long accountSetId = 1L;
        mockAccountSetAccess(accountSetId);
        FmsCurrencyDO currency = buildCurrencyDO(accountSetId, "USD", "美元", false);
        currencyMapper.insert(currency);
        when(subjectService.getSubjectCountByCurrencyId(accountSetId, currency.getId())).thenReturn(1L);

        // 调用，并断言
        assertServiceException(() -> currencyService.deleteCurrency(
                accountSetId, currency.getId(), 10L), CURRENCY_IN_USE);
    }

    @Test
    public void testGetCurrencyList_standardFirst() {
        // mock 数据
        Long accountSetId = 1L;
        mockAccountSetAccess(accountSetId);
        currencyMapper.insert(buildCurrencyDO(accountSetId, "USD", "美元", false));
        currencyMapper.insert(buildCurrencyDO(accountSetId, "RMB", "人民币", true));

        // 调用
        List<FmsCurrencyDO> currencies = currencyService.getCurrencyList(accountSetId, 10L);

        // 断言
        assertEquals(2, currencies.size());
        assertEquals("RMB", currencies.get(0).getCode());
        assertEquals("USD", currencies.get(1).getCode());
    }

    @Test
    public void testValidateCurrencyList_success() {
        // mock 数据
        Long accountSetId = 1L;
        FmsCurrencyDO usd = buildCurrencyDO(accountSetId, "USD", "美元", false);
        FmsCurrencyDO eur = buildCurrencyDO(accountSetId, "EUR", "欧元", false);
        currencyMapper.insert(usd);
        currencyMapper.insert(eur);

        // 调用
        List<FmsCurrencyDO> currencies = currencyService.validateCurrencyList(
                accountSetId, Arrays.asList(eur.getId(), usd.getId()));

        // 断言
        assertEquals(2, currencies.size());
        assertEquals(new HashSet<>(Arrays.asList(eur.getId(), usd.getId())),
                new HashSet<>(Arrays.asList(currencies.get(0).getId(), currencies.get(1).getId())));
    }

    @Test
    public void testValidateCurrencyList_duplicateId() {
        // mock 数据
        Long accountSetId = 1L;
        FmsCurrencyDO currency = buildCurrencyDO(accountSetId, "USD", "美元", false);
        currencyMapper.insert(currency);

        // 调用，并断言
        assertServiceException(() -> currencyService.validateCurrencyList(
                accountSetId, Arrays.asList(currency.getId(), currency.getId())), CURRENCY_NOT_EXISTS);
    }

    // ========== 随机对象 ==========

    private FmsCurrencyDO buildCurrencyDO(Long accountSetId, String code, String name, boolean standard) {
        return randomPojo(FmsCurrencyDO.class, currency -> currency.setId(null)
                .setAccountSetId(accountSetId).setCode(code).setName(name)
                .setExchangeRate(new BigDecimal("7.120000")).setStandard(standard));
    }

    private FmsCurrencySaveReqVO buildSaveReqVO(Long id, Long accountSetId, String code,
            String name, BigDecimal exchangeRate) {
        return randomPojo(FmsCurrencySaveReqVO.class, reqVO -> reqVO.setId(id)
                .setAccountSetId(accountSetId).setCode(code).setName(name).setExchangeRate(exchangeRate));
    }

    private void mockAccountSetAccess(Long accountSetId) {
        when(accountSetService.validateAccountSetReadPermission(accountSetId, 10L))
                .thenReturn(new FmsAccountSetDO().setId(accountSetId));
        when(accountSetService.validateAccountSetWritePermission(accountSetId, 10L))
                .thenReturn(new FmsAccountSetDO().setId(accountSetId));
    }

}
