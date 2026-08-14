package cn.iocoder.yudao.module.fms.service.config;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.initialbalance.FmsInitialBalanceExcelVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.initialbalance.FmsInitialBalanceRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.initialbalance.FmsTrialBalanceRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.initialbalance.FmsInitialBalanceSaveReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountSetDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAuxiliaryCombinationDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAuxiliaryItemDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAuxiliaryTypeDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsInitialBalanceDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsSubjectDO;
import cn.iocoder.yudao.module.fms.dal.mysql.config.FmsInitialBalanceMapper;
import cn.iocoder.yudao.module.fms.enums.common.FmsDebitCreditDirectionEnum;
import cn.iocoder.yudao.module.fms.enums.config.FmsSubjectTypeEnum;
import cn.iocoder.yudao.module.fms.enums.config.FmsAuxiliaryTypeEnum;
import cn.iocoder.yudao.module.fms.service.closing.FmsClosingPeriodService;
import cn.iocoder.yudao.module.fms.service.config.FmsAccountSetService;
import cn.iocoder.yudao.module.fms.service.config.FmsAuxiliaryCombinationService;
import cn.iocoder.yudao.module.fms.service.config.FmsAuxiliaryItemService;
import cn.iocoder.yudao.module.fms.service.config.FmsAuxiliaryTypeService;
import cn.iocoder.yudao.module.fms.service.config.FmsSubjectService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.INITIAL_BALANCE_PERIOD_CLOSED;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.INITIAL_BALANCE_SUBJECT_NOT_LEAF;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Import(FmsInitialBalanceServiceImpl.class)
public class FmsInitialBalanceServiceImplTest extends BaseDbUnitTest {

    @Resource
    private FmsInitialBalanceServiceImpl initialBalanceService;
    @Resource
    private FmsInitialBalanceMapper initialBalanceMapper;

    @MockitoBean
    private FmsAccountSetService accountSetService;
    @MockitoBean
    private FmsSubjectService subjectService;
    @MockitoBean
    private FmsAuxiliaryItemService auxiliaryItemService;
    @MockitoBean
    private FmsAuxiliaryTypeService auxiliaryTypeService;
    @MockitoBean
    private FmsAuxiliaryCombinationService auxiliaryCombinationService;
    @MockitoBean
    private FmsClosingPeriodService closingPeriodService;

    @Test
    public void testGetInitialBalanceList_aggregateParent() {
        // mock 数据
        Long accountSetId = 1L;
        FmsSubjectDO parent = buildSubject(10L, null, FmsDebitCreditDirectionEnum.DEBIT.getType());
        FmsSubjectDO child = buildSubject(11L, parent.getId(), FmsDebitCreditDirectionEnum.DEBIT.getType());
        mockAccountSetAndSubjects(accountSetId, Arrays.asList(parent, child));
        initialBalanceMapper.insert(buildBalance(accountSetId, child.getId(), "120.00"));

        // 调用
        List<FmsInitialBalanceRespVO> result = initialBalanceService.getInitialBalanceList(
                accountSetId, FmsSubjectTypeEnum.ASSET.getType(), 10L);

        // 断言：平铺列表父级在前，父级余额由末级科目汇总
        assertEquals(2, result.size());
        assertEquals(parent.getId(), result.get(0).getSubjectId());
        assertEquals(new BigDecimal("120.00"), result.get(0).getOpeningAmount());
        assertEquals(child.getId(), result.get(1).getSubjectId());
        assertEquals(new BigDecimal("120.00"), result.get(1).getOpeningAmount());
    }

    @Test
    public void testSaveInitialBalance_success() {
        // mock 数据
        Long accountSetId = 1L;
        FmsSubjectDO subject = buildSubject(11L, null, FmsDebitCreditDirectionEnum.DEBIT.getType());
        mockAccountSetAndSubjects(accountSetId, Collections.singletonList(subject));
        when(closingPeriodService.getCurrentMonth(eq(accountSetId), any(LocalDateTime.class)))
                .thenReturn(YearMonth.of(2026, 3));
        // 准备参数
        FmsInitialBalanceSaveReqVO reqVO = new FmsInitialBalanceSaveReqVO();
        reqVO.setAccountSetId(accountSetId).setBalances(Collections.singletonList(
                buildSaveBalance(subject.getId(), "1000.00", "200.00", "100.00", "900.00")));

        // 调用
        initialBalanceService.saveInitialBalance(reqVO, 10L);

        // 断言
        FmsInitialBalanceDO balance = initialBalanceMapper.selectByAccountSetIdAndSubjectId(
                accountSetId, subject.getId());
        assertEquals(new BigDecimal("1000.00"), balance.getOpeningAmount());
        assertEquals(new BigDecimal("200.00"), balance.getYearDebitAmount());
        assertEquals(new BigDecimal("100.00"), balance.getYearCreditAmount());
        assertEquals(new BigDecimal("900.00"), balance.getYearOpeningAmount());
        verify(accountSetService).validateAccountSetWritePermission(accountSetId, 10L);
    }

    @Test
    public void testSaveInitialBalance_subjectNotLeaf() {
        // mock 数据
        Long accountSetId = 1L;
        FmsSubjectDO parent = buildSubject(10L, null, FmsDebitCreditDirectionEnum.DEBIT.getType());
        FmsSubjectDO child = buildSubject(11L, parent.getId(), FmsDebitCreditDirectionEnum.DEBIT.getType());
        mockAccountSetAndSubjects(accountSetId, Arrays.asList(parent, child));
        when(closingPeriodService.getCurrentMonth(eq(accountSetId), any(LocalDateTime.class)))
                .thenReturn(YearMonth.of(2026, 3));
        // 准备参数
        FmsInitialBalanceSaveReqVO reqVO = new FmsInitialBalanceSaveReqVO();
        reqVO.setAccountSetId(accountSetId).setBalances(Collections.singletonList(
                buildSaveBalance(parent.getId(), "100.00", "0", "0", "100.00")));

        // 调用，并断言
        assertServiceException(() -> initialBalanceService.saveInitialBalance(reqVO, 10L),
                INITIAL_BALANCE_SUBJECT_NOT_LEAF);
    }

    @Test
    public void testSaveInitialBalance_periodClosed() {
        // mock 数据
        Long accountSetId = 1L;
        FmsSubjectDO subject = buildSubject(11L, null, FmsDebitCreditDirectionEnum.DEBIT.getType());
        mockAccountSetAndSubjects(accountSetId, Collections.singletonList(subject));
        // 账套已结账，当前期间晚于启用期间
        when(closingPeriodService.getCurrentMonth(eq(accountSetId), any(LocalDateTime.class)))
                .thenReturn(YearMonth.of(2026, 4));
        // 准备参数
        FmsInitialBalanceSaveReqVO reqVO = new FmsInitialBalanceSaveReqVO();
        reqVO.setAccountSetId(accountSetId).setBalances(Collections.singletonList(
                buildSaveBalance(subject.getId(), "100.00", "0", "0", "100.00")));

        // 调用，并断言
        assertServiceException(() -> initialBalanceService.saveInitialBalance(reqVO, 10L),
                INITIAL_BALANCE_PERIOD_CLOSED);
    }

    @Test
    public void testGetTrialBalance_balanced() {
        // mock 数据
        Long accountSetId = 1L;
        FmsSubjectDO debit = buildSubject(10L, null, FmsDebitCreditDirectionEnum.DEBIT.getType());
        FmsSubjectDO credit = buildSubject(20L, null, FmsDebitCreditDirectionEnum.CREDIT.getType());
        mockAccountSetAndSubjects(accountSetId, Arrays.asList(debit, credit));
        initialBalanceMapper.insert(buildBalance(accountSetId, debit.getId(), "1000.00"));
        initialBalanceMapper.insert(buildBalance(accountSetId, credit.getId(), "1000.00"));

        // 调用
        FmsTrialBalanceRespVO result = initialBalanceService.getTrialBalance(accountSetId, 10L);

        // 断言
        assertTrue(result.getBalanced());
        assertEquals(new BigDecimal("1000.00"), result.getOpeningDebitAmount());
        assertEquals(new BigDecimal("1000.00"), result.getOpeningCreditAmount());
        assertEquals(BigDecimal.ZERO.setScale(2), result.getOpeningDifferenceAmount());
    }

    @Test
    public void testImportInitialBalance_success() {
        // mock 数据
        Long accountSetId = 1L;
        FmsSubjectDO subject = buildSubject(11L, null, FmsDebitCreditDirectionEnum.DEBIT.getType());
        mockAccountSetAndSubjects(accountSetId, Collections.singletonList(subject));
        when(closingPeriodService.getCurrentMonth(eq(accountSetId), any(LocalDateTime.class)))
                .thenReturn(YearMonth.of(2026, 3));
        when(auxiliaryTypeService.getAuxiliaryTypeList(accountSetId, 10L))
                .thenReturn(Collections.emptyList());
        when(auxiliaryItemService.getAuxiliaryItemListByAccountSetId(accountSetId, 10L))
                .thenReturn(Collections.emptyList());
        // 准备参数
        FmsInitialBalanceExcelVO row = new FmsInitialBalanceExcelVO().setRowNumber(3)
                .setSubjectCode(subject.getCode()).setDirectionName("借")
                .setOpeningAmount(new BigDecimal("1000.00"))
                .setOpeningQuantity(new BigDecimal("10.0000"))
                .setYearDebitAmount(new BigDecimal("200.00"))
                .setYearDebitQuantity(new BigDecimal("2.0000"))
                .setYearCreditAmount(new BigDecimal("100.00"))
                .setYearCreditQuantity(new BigDecimal("1.0000"));

        // 调用
        int count = initialBalanceService.importInitialBalance(
                accountSetId, Collections.singletonList(row), 10L);

        // 断言
        assertEquals(1, count);
        FmsInitialBalanceDO balance = initialBalanceMapper.selectByAccountSetIdAndSubjectId(
                accountSetId, subject.getId());
        assertEquals(new BigDecimal("900.00"), balance.getYearOpeningAmount());
        assertEquals(new BigDecimal("9.0000"), balance.getYearOpeningQuantity());
        verify(accountSetService).validateAccountSetWritePermission(accountSetId, 10L);
    }

    @Test
    public void testImportInitialBalance_auxiliaryEmbeddedJson() {
        // mock 数据
        Long accountSetId = 1L;
        Long auxiliaryTypeId = 21L;
        FmsSubjectDO subject = buildSubject(11L, null, FmsDebitCreditDirectionEnum.DEBIT.getType())
                .setAuxiliaryTypeIds(Collections.singletonList(auxiliaryTypeId));
        mockAccountSetAndSubjects(accountSetId, Collections.singletonList(subject));
        when(closingPeriodService.getCurrentMonth(eq(accountSetId), any(LocalDateTime.class)))
                .thenReturn(YearMonth.of(2026, 3));
        FmsAuxiliaryTypeDO type = new FmsAuxiliaryTypeDO().setId(auxiliaryTypeId)
                .setName("客户").setAccountSetId(accountSetId)
                .setType(FmsAuxiliaryTypeEnum.CUSTOMER.getType());
        FmsAuxiliaryItemDO item = new FmsAuxiliaryItemDO().setId(31L).setCode("C001")
                .setName("北京客户").setAuxiliaryTypeId(auxiliaryTypeId)
                .setAccountSetId(accountSetId);
        when(auxiliaryTypeService.getAuxiliaryTypeList(accountSetId, 10L))
                .thenReturn(Collections.singletonList(type));
        when(auxiliaryTypeService.validateAuxiliaryTypeList(eq(accountSetId), anyCollection()))
                .thenReturn(Collections.singletonList(type));
        when(auxiliaryItemService.getAuxiliaryItemListByAccountSetId(accountSetId, 10L))
                .thenReturn(Collections.singletonList(item));
        when(auxiliaryItemService.validateAuxiliaryItemList(eq(accountSetId), anyList()))
                .thenReturn(Collections.singletonList(item));
        when(auxiliaryCombinationService.saveAuxiliaryCombination(
                eq(accountSetId), eq(subject.getId()), anyList()))
                .thenReturn(new FmsAuxiliaryCombinationDO().setId(41L));
        // 准备参数
        FmsInitialBalanceExcelVO row = new FmsInitialBalanceExcelVO().setRowNumber(3)
                .setSubjectCode(subject.getCode()).setDirectionName("借")
                .setAuxiliaryItems("客户:北京客户").setOpeningAmount(new BigDecimal("500.00"));

        // 调用
        int count = initialBalanceService.importInitialBalance(
                accountSetId, Collections.singletonList(row), 10L);

        // 断言
        assertEquals(1, count);
        FmsInitialBalanceDO balance = initialBalanceMapper.selectByAccountSetIdAndSubjectId(
                accountSetId, subject.getId());
        assertEquals(1, balance.getAssistBalances().size());
        assertEquals(item.getId(), balance.getAssistBalances().get(0)
                .getAuxiliaries().get(0).getItemId());
        assertEquals("北京客户", balance.getAssistBalances().get(0)
                .getAuxiliaries().get(0).getName());
        assertEquals(new BigDecimal("500.00"), balance.getOpeningAmount());
    }

    @Test
    public void testGetInitialBalanceCountByAuxiliaryItemIds_embeddedJson() {
        // mock 数据
        initialBalanceMapper.insert(new FmsInitialBalanceDO().setAccountSetId(1L)
                .setAssistBalances(Collections.singletonList(
                        FmsInitialBalanceDO.AssistBalance.builder()
                                .auxiliaries(Collections.singletonList(
                                        FmsInitialBalanceDO.AuxiliaryItem.builder()
                                                .typeId(31L).itemId(41L).build()))
                                .build())));

        // 调用，并断言
        assertEquals(1L, initialBalanceService.getInitialBalanceCountByAuxiliaryItemIds(
                1L, Collections.singletonList(41L)));
        assertEquals(1L, initialBalanceService.getInitialBalanceCountByAuxiliaryTypeId(1L, 31L));
        assertEquals(0L, initialBalanceService.getInitialBalanceCountByAuxiliaryItemIds(
                1L, Collections.singletonList(42L)));
    }

    @Test
    public void testGetInitialBalanceQuantityCountBySubjectIds() {
        // mock 数据
        initialBalanceMapper.insert(buildBalance(1L, 101L, "100.00")
                .setOpeningQuantity(new BigDecimal("2.00")));
        initialBalanceMapper.insert(buildBalance(1L, 102L, "200.00"));
        initialBalanceMapper.insert(buildBalance(1L, 103L, "300.00")
                .setAssistBalances(Collections.singletonList(FmsInitialBalanceDO.AssistBalance.builder()
                        .yearDebitQuantity(new BigDecimal("3.00")).build())));

        // 调用，并断言
        assertEquals(1L, initialBalanceService.getInitialBalanceQuantityCountBySubjectIds(
                1L, Arrays.asList(101L, 102L)));
        assertEquals(2L, initialBalanceService.getInitialBalanceQuantityCountBySubjectIds(
                1L, Arrays.asList(101L, 102L, 103L)));
    }

    // ========== 随机对象 ==========

    private FmsSubjectDO buildSubject(Long id, Long parentId, Integer direction) {
        return randomPojo(FmsSubjectDO.class, subject -> subject.setId(id)
                .setCode(String.valueOf(id)).setName("测试科目")
                .setParentId(parentId).setType(FmsSubjectTypeEnum.ASSET.getType()).setCategory(1)
                .setBalanceDirection(direction).setQuantityAccounting(false)
                .setAuxiliaryTypeIds(Collections.emptyList()).setCurrencyIds(Collections.emptyList()));
    }

    private FmsInitialBalanceDO buildBalance(Long accountSetId, Long subjectId, String amount) {
        return randomPojo(FmsInitialBalanceDO.class, balance -> balance.setId(null)
                .setAccountSetId(accountSetId).setSubjectId(subjectId)
                .setAuxiliaryAccounting(false).setOpeningAmount(new BigDecimal(amount))
                .setOpeningQuantity(BigDecimal.ZERO).setYearDebitAmount(BigDecimal.ZERO)
                .setYearDebitQuantity(BigDecimal.ZERO).setYearCreditAmount(BigDecimal.ZERO)
                .setYearCreditQuantity(BigDecimal.ZERO).setYearOpeningAmount(new BigDecimal(amount))
                .setYearOpeningQuantity(BigDecimal.ZERO).setProfitLossAmount(BigDecimal.ZERO)
                .setProfitLossQuantity(BigDecimal.ZERO)
                .setAssistBalances(Collections.emptyList()));
    }

    private FmsInitialBalanceSaveReqVO.Balance buildSaveBalance(Long subjectId,
            String openingAmount, String yearDebitAmount, String yearCreditAmount,
            String yearOpeningAmount) {
        FmsInitialBalanceSaveReqVO.Balance balance = new FmsInitialBalanceSaveReqVO.Balance();
        balance.setSubjectId(subjectId).setAssistBalances(Collections.emptyList());
        balance.setOpeningAmount(new BigDecimal(openingAmount)).setOpeningQuantity(BigDecimal.ZERO)
                .setYearDebitAmount(new BigDecimal(yearDebitAmount)).setYearDebitQuantity(BigDecimal.ZERO)
                .setYearCreditAmount(new BigDecimal(yearCreditAmount)).setYearCreditQuantity(BigDecimal.ZERO)
                .setYearOpeningAmount(new BigDecimal(yearOpeningAmount)).setYearOpeningQuantity(BigDecimal.ZERO)
                .setProfitLossAmount(BigDecimal.ZERO).setProfitLossQuantity(BigDecimal.ZERO);
        return balance;
    }

    private void mockAccountSetAndSubjects(Long accountSetId, List<FmsSubjectDO> subjects) {
        FmsAccountSetDO accountSet = new FmsAccountSetDO().setId(accountSetId)
                .setStartTime(LocalDateTime.of(2026, 3, 1, 0, 0));
        when(accountSetService.validateAccountSetReadPermission(accountSetId, 10L))
                .thenReturn(accountSet);
        when(accountSetService.validateAccountSetWritePermission(accountSetId, 10L))
                .thenReturn(accountSet);
        when(subjectService.getSubjectList(eq(accountSetId), any(), eq(10L))).thenReturn(subjects);
    }
}
