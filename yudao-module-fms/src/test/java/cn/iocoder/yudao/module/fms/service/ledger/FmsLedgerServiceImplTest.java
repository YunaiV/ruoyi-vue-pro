package cn.iocoder.yudao.module.fms.service.ledger;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.FmsLedgerAuxiliaryListReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.FmsLedgerListReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.auxiliarybalance.FmsLedgerAuxiliaryBalanceRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.auxiliarydetail.FmsLedgerAuxiliaryDetailRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.detail.FmsLedgerDetailRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.multicolumn.FmsLedgerMultiColumnRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.quantitydetail.FmsLedgerQuantityDetailRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.quantitygeneral.FmsLedgerQuantityGeneralRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.subjectbalance.FmsLedgerSubjectBalanceRespVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountSetDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAuxiliaryItemDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsInitialBalanceDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsFinanceParameterDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsSubjectDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.voucher.FmsVoucherDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.voucher.FmsVoucherEntryDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsVoucherWordDO;
import cn.iocoder.yudao.module.fms.dal.mysql.config.FmsInitialBalanceMapper;
import cn.iocoder.yudao.module.fms.dal.mysql.voucher.FmsVoucherEntryMapper;
import cn.iocoder.yudao.module.fms.dal.mysql.voucher.FmsVoucherMapper;
import cn.iocoder.yudao.module.fms.dal.mysql.config.FmsVoucherWordMapper;
import cn.iocoder.yudao.module.fms.enums.common.FmsDebitCreditDirectionEnum;
import cn.iocoder.yudao.module.fms.enums.ledger.FmsLedgerBalanceModeEnum;
import cn.iocoder.yudao.module.fms.service.config.FmsAccountSetService;
import cn.iocoder.yudao.module.fms.service.config.FmsAuxiliaryItemService;
import cn.iocoder.yudao.module.fms.service.config.FmsFinanceParameterService;
import cn.iocoder.yudao.module.fms.service.config.FmsSubjectService;
import javax.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.LEDGER_PERIOD_BEFORE_ACCOUNT_START;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@Import(FmsLedgerServiceImpl.class)
public class FmsLedgerServiceImplTest extends BaseDbUnitTest {

    @Resource
    private FmsLedgerServiceImpl ledgerService;
    @Resource
    private FmsInitialBalanceMapper initialBalanceMapper;
    @Resource
    private FmsVoucherMapper voucherMapper;
    @Resource
    private FmsVoucherEntryMapper voucherEntryMapper;
    @Resource
    private FmsVoucherWordMapper voucherWordMapper;

    @MockBean
    private FmsAccountSetService accountSetService;
    @MockBean
    private FmsFinanceParameterService financeParameterService;
    @MockBean
    private FmsSubjectService subjectService;
    @MockBean
    private FmsAuxiliaryItemService auxiliaryItemService;

    private FmsSubjectDO cashSubject;
    private FmsSubjectDO bankSubject;
    private FmsSubjectDO receivableSubject;
    private FmsAuxiliaryItemDO customerItem;

    @BeforeEach
    public void before() {
        cashSubject = buildSubject(101L, "1001", "库存现金", null, 1);
        bankSubject = buildSubject(102L, "100201", "工商银行", 101L, 2);
        receivableSubject = buildSubject(103L, "1122", "应收账款", null, 1);
        customerItem = new FmsAuxiliaryItemDO().setId(301L).setCode("KH001")
                .setName("北京星河科技有限公司").setAuxiliaryTypeId(201L)
                .setStatus(1).setAccountSetId(1L);
        FmsAccountSetDO accountSet = new FmsAccountSetDO().setId(1L)
                .setStartTime(LocalDateTime.of(2026, 1, 1, 0, 0));
        when(accountSetService.validateAccountSetReadPermission(1L, 10L))
                .thenReturn(accountSet);
        when(financeParameterService.getFinanceParameter(1L, 10L))
                .thenReturn(new FmsFinanceParameterDO().setLedgerBalanceMode(1));
        when(subjectService.getSubjectList(1L, null, 10L))
                .thenReturn(Arrays.asList(cashSubject, bankSubject, receivableSubject));
        when(auxiliaryItemService.getAuxiliaryItemList(1L, 201L, 10L))
                .thenReturn(Collections.singletonList(customerItem));
        when(auxiliaryItemService.getAuxiliaryItemListByAccountSetId(1L, 10L))
                .thenReturn(Collections.singletonList(customerItem));
    }

    @Test
    public void testGetDetailLedger() {
        // mock 数据
        initialBalanceMapper.insert(new FmsInitialBalanceDO().setSubjectId(bankSubject.getId())
                .setAccountSetId(1L).setOpeningAmount(new BigDecimal("100.00"))
                .setOpeningQuantity(BigDecimal.ZERO).setYearDebitAmount(BigDecimal.ZERO)
                .setYearDebitQuantity(BigDecimal.ZERO).setYearCreditAmount(BigDecimal.ZERO)
                .setYearCreditQuantity(BigDecimal.ZERO).setYearOpeningAmount(new BigDecimal("100.00"))
                .setYearOpeningQuantity(BigDecimal.ZERO).setProfitLossAmount(BigDecimal.ZERO)
                .setAuxiliaryAccounting(false));
        FmsVoucherWordDO word = new FmsVoucherWordDO().setName("记").setPrintTitle("记账凭证")
                .setDefaultStatus(true).setSort(1).setAccountSetId(1L);
        voucherWordMapper.insert(word);
        FmsVoucherDO voucher = new FmsVoucherDO().setVoucherWordId(word.getId()).setVoucherNumber(1)
                .setVoucherTime(LocalDateTime.of(2026, 8, 2, 0, 0)).setAttachmentUrls(Collections.emptyList())
                .setDebitAmount(new BigDecimal("80.00")).setCreditAmount(new BigDecimal("80.00"))
                .setTotal(new BigDecimal("80.00")).setStatus(1).setAccountSetId(1L);
        voucherMapper.insert(voucher);
        voucherEntryMapper.insert(new FmsVoucherEntryDO().setVoucherId(voucher.getId())
                .setAccountSetId(1L).setSubjectId(bankSubject.getId()).setSubjectCode("100201")
                .setSubjectName("工商银行").setDigest("收到客户回款").setSort(1)
                .setDebitAmount(new BigDecimal("80.00")).setCreditAmount(BigDecimal.ZERO)
                .setQuantity(BigDecimal.ZERO).setUnitPrice(BigDecimal.ZERO)
                .setAuxiliaries(Collections.emptyList()));

        // 准备参数
        FmsLedgerListReqVO reqVO = buildListReqVO();
        reqVO.setSubjectId(bankSubject.getId());

        // 调用
        List<FmsLedgerDetailRespVO> result = ledgerService.getDetailList(reqVO, 10L);

        // 断言
        assertEquals(4, result.size());
        assertEquals(new BigDecimal("100.00"), result.get(0).getBalance());
        assertEquals("记-1", result.get(1).getVoucherNumber());
        assertEquals(new BigDecimal("180.00"), result.get(1).getBalance());
        assertEquals(new BigDecimal("80.00"), result.get(2).getDebitAmount());
        assertEquals("本年累计", result.get(3).getDigest());
    }

    @Test
    public void testGetDetailList_startMonthEqualsAccountSetStartMonth() {
        // mock 数据
        initialBalanceMapper.insert(new FmsInitialBalanceDO().setSubjectId(bankSubject.getId())
                .setAccountSetId(1L).setOpeningAmount(new BigDecimal("100.00"))
                .setOpeningQuantity(BigDecimal.ZERO).setYearDebitAmount(BigDecimal.ZERO)
                .setYearDebitQuantity(BigDecimal.ZERO).setYearCreditAmount(BigDecimal.ZERO)
                .setYearCreditQuantity(BigDecimal.ZERO).setYearOpeningAmount(new BigDecimal("100.00"))
                .setYearOpeningQuantity(BigDecimal.ZERO).setProfitLossAmount(BigDecimal.ZERO)
                .setAuxiliaryAccounting(false));

        // 准备参数
        FmsLedgerListReqVO reqVO = buildListReqVO();
        reqVO.setStartMonth("2026-01");
        reqVO.setEndMonth("2026-01");
        reqVO.setSubjectId(bankSubject.getId());

        // 调用
        List<FmsLedgerDetailRespVO> result = ledgerService.getDetailList(reqVO, 10L);

        // 断言
        assertEquals(new BigDecimal("100.00"), CollUtil.getFirst(result).getBalance());
    }

    @Test
    public void testGetDetailList_startMonthBeforeAccountSetStartMonth() {
        // 准备参数
        FmsLedgerListReqVO reqVO = buildListReqVO();
        reqVO.setStartMonth("2025-12");

        // 调用并断言
        assertServiceException(() -> ledgerService.getDetailList(reqVO, 10L),
                LEDGER_PERIOD_BEFORE_ACCOUNT_START);
    }

    @Test
    public void testGetQuantityDetailList_filterNonQuantityDescendant() {
        // mock 数据：父科目启用数量核算，非数量子科目存在金额分录
        cashSubject.setQuantityAccounting(true);
        FmsVoucherWordDO word = buildVoucherWord();
        voucherWordMapper.insert(word);
        FmsVoucherDO voucher = buildVoucher(word.getId(), "80.00");
        voucherMapper.insert(voucher);
        voucherEntryMapper.insert(new FmsVoucherEntryDO().setVoucherId(voucher.getId())
                .setAccountSetId(1L).setSubjectId(bankSubject.getId()).setSubjectCode(bankSubject.getCode())
                .setSubjectName(bankSubject.getName()).setDigest("非数量科目分录").setSort(1)
                .setDebitAmount(new BigDecimal("80.00")).setCreditAmount(BigDecimal.ZERO)
                .setQuantity(BigDecimal.ZERO).setUnitPrice(BigDecimal.ZERO)
                .setAuxiliaries(Collections.emptyList()));

        // 准备参数
        FmsLedgerListReqVO reqVO = buildListReqVO();
        reqVO.setSubjectId(cashSubject.getId());

        // 调用
        List<FmsLedgerQuantityDetailRespVO> result = ledgerService.getQuantityDetailList(reqVO, 10L);

        // 断言
        assertEquals(3, result.size());
        assertEquals(BigDecimal.ZERO, result.get(1).getDebitAmount());
        assertEquals(BigDecimal.ZERO, result.get(1).getBalance());
    }

    @Test
    public void testGetQuantityDetailList_negativeDebitKeepsDebitQuantity() {
        // mock 数据：红字借方金额仍属于借方数量，不转换到贷方
        bankSubject.setQuantityAccounting(true);
        FmsVoucherWordDO word = buildVoucherWord();
        voucherWordMapper.insert(word);
        FmsVoucherDO voucher = buildVoucher(word.getId(), "-20.00");
        voucherMapper.insert(voucher);
        voucherEntryMapper.insert(new FmsVoucherEntryDO().setVoucherId(voucher.getId())
                .setAccountSetId(1L).setSubjectId(bankSubject.getId()).setSubjectCode(bankSubject.getCode())
                .setSubjectName(bankSubject.getName()).setDigest("红字冲销").setSort(1)
                .setDebitAmount(new BigDecimal("-20.00")).setCreditAmount(BigDecimal.ZERO)
                .setQuantity(new BigDecimal("2.00")).setUnitPrice(new BigDecimal("10.00"))
                .setAuxiliaries(Collections.emptyList()));
        // 准备参数
        FmsLedgerListReqVO reqVO = buildListReqVO();
        reqVO.setSubjectId(bankSubject.getId());

        // 调用
        List<FmsLedgerQuantityDetailRespVO> result = ledgerService.getQuantityDetailList(reqVO, 10L);

        // 断言
        FmsLedgerQuantityDetailRespVO voucherRow = CollUtil.findOne(result,
                row -> ObjUtil.equal(row.getRowType(), FmsLedgerQuantityDetailRespVO.ROW_TYPE_VOUCHER));
        assertEquals(new BigDecimal("2.0000"), voucherRow.getDebitQuantity());
        assertEquals(BigDecimal.ZERO, voucherRow.getCreditQuantity());
    }

    @Test
    public void testGetSubjectBalanceList_endSubjectIncludesChild() {
        // mock 数据
        FmsVoucherWordDO word = new FmsVoucherWordDO().setName("记").setPrintTitle("记账凭证")
                .setDefaultStatus(true).setSort(1).setAccountSetId(1L);
        voucherWordMapper.insert(word);
        FmsVoucherDO voucher = new FmsVoucherDO().setVoucherWordId(word.getId()).setVoucherNumber(1)
                .setVoucherTime(LocalDateTime.of(2026, 8, 2, 0, 0)).setAttachmentUrls(Collections.emptyList())
                .setDebitAmount(new BigDecimal("80.00")).setCreditAmount(new BigDecimal("80.00"))
                .setTotal(new BigDecimal("80.00")).setStatus(1).setAccountSetId(1L);
        voucherMapper.insert(voucher);
        voucherEntryMapper.insert(new FmsVoucherEntryDO().setVoucherId(voucher.getId())
                .setAccountSetId(1L).setSubjectId(bankSubject.getId()).setSubjectCode("100201")
                .setSubjectName("工商银行").setDigest("收到客户回款").setSort(1)
                .setDebitAmount(new BigDecimal("80.00")).setCreditAmount(BigDecimal.ZERO)
                .setQuantity(BigDecimal.ZERO).setUnitPrice(BigDecimal.ZERO)
                .setAuxiliaries(Collections.emptyList()));

        // 准备参数
        FmsLedgerListReqVO reqVO = buildListReqVO();
        reqVO.setMinLevel(1);
        reqVO.setMaxLevel(2);
        reqVO.setEndSubjectId(cashSubject.getId());

        // 调用
        List<FmsLedgerSubjectBalanceRespVO> result = ledgerService.getSubjectBalanceList(reqVO, 10L);

        // 断言
        FmsLedgerSubjectBalanceRespVO parent = CollUtil.getFirst(result);
        assertEquals(new BigDecimal("80.00"), parent.getPeriodDebitAmount());
        assertEquals(1, parent.getChildren().size());
        assertEquals(new BigDecimal("80.00"), CollUtil.getFirst(parent.getChildren()).getEndingDebitAmount());
    }

    @Test
    public void testGetSubjectBalanceList_filterZeroRows() {
        // 准备参数
        FmsLedgerListReqVO reqVO = buildListReqVO();

        // 调用
        List<FmsLedgerSubjectBalanceRespVO> result = ledgerService.getSubjectBalanceList(reqVO, 10L);

        // 断言
        assertEquals(0, result.size());
    }

    @Test
    public void testGetSubjectBalanceList_appendAuxiliaryCombination() {
        // mock 数据
        receivableSubject.setAuxiliaryTypeIds(Collections.singletonList(201L));
        initialBalanceMapper.insert(new FmsInitialBalanceDO().setSubjectId(receivableSubject.getId())
                .setAccountSetId(1L).setAuxiliaryAccounting(true)
                .setOpeningAmount(new BigDecimal("40.00")).setOpeningQuantity(BigDecimal.ZERO)
                .setYearDebitAmount(BigDecimal.ZERO).setYearDebitQuantity(BigDecimal.ZERO)
                .setYearCreditAmount(BigDecimal.ZERO).setYearCreditQuantity(BigDecimal.ZERO)
                .setYearOpeningAmount(new BigDecimal("40.00")).setYearOpeningQuantity(BigDecimal.ZERO)
                .setProfitLossAmount(BigDecimal.ZERO).setAssistBalances(Collections.singletonList(
                        FmsInitialBalanceDO.AssistBalance.builder().assistCombinationId(501L)
                                .auxiliaries(Collections.singletonList(FmsInitialBalanceDO.AuxiliaryItem.builder()
                                        .type(1).typeId(201L).itemId(customerItem.getId())
                                        .name(customerItem.getName()).build()))
                                .openingAmount(new BigDecimal("40.00")).openingQuantity(BigDecimal.ZERO)
                                .yearDebitAmount(BigDecimal.ZERO).yearDebitQuantity(BigDecimal.ZERO)
                                .yearCreditAmount(BigDecimal.ZERO).yearCreditQuantity(BigDecimal.ZERO).build())));
        FmsVoucherWordDO word = buildVoucherWord();
        voucherWordMapper.insert(word);
        FmsVoucherDO voucher = buildVoucher(word.getId(), "100.00");
        voucherMapper.insert(voucher);
        voucherEntryMapper.insert(new FmsVoucherEntryDO().setVoucherId(voucher.getId())
                .setAccountSetId(1L).setSubjectId(receivableSubject.getId()).setSubjectCode("1122")
                .setSubjectName("应收账款").setDigest("确认客户应收款").setSort(1)
                .setDebitAmount(new BigDecimal("100.00")).setCreditAmount(BigDecimal.ZERO)
                .setQuantity(BigDecimal.ZERO).setUnitPrice(BigDecimal.ZERO).setAssistCombinationId(501L)
                .setAuxiliaries(Collections.singletonList(FmsVoucherEntryDO.AuxiliaryItem.builder()
                        .type(1).typeId(201L).itemId(customerItem.getId())
                        .name(customerItem.getName()).build())));

        // 准备参数
        FmsLedgerListReqVO reqVO = buildListReqVO();

        // 调用
        List<FmsLedgerSubjectBalanceRespVO> result = ledgerService.getSubjectBalanceList(reqVO, 10L);

        // 断言
        FmsLedgerSubjectBalanceRespVO subjectBalance = CollUtil.getFirst(result);
        assertEquals("S:" + receivableSubject.getId(), subjectBalance.getNodeKey());
        assertEquals(FmsLedgerSubjectBalanceRespVO.NODE_TYPE_SUBJECT, subjectBalance.getNodeType());
        assertEquals(1, subjectBalance.getChildren().size());
        FmsLedgerSubjectBalanceRespVO auxiliaryBalance = CollUtil.getFirst(subjectBalance.getChildren());
        assertEquals("A:501", auxiliaryBalance.getNodeKey());
        assertEquals(FmsLedgerSubjectBalanceRespVO.NODE_TYPE_AUXILIARY_COMBINATION,
                auxiliaryBalance.getNodeType());
        assertEquals(501L, auxiliaryBalance.getAssistCombinationId());
        assertEquals("1122_KH001", auxiliaryBalance.getSubjectCode());
        assertEquals("应收账款_北京星河科技有限公司", auxiliaryBalance.getSubjectName());
        assertEquals(new BigDecimal("40.00"), auxiliaryBalance.getOpeningDebitAmount());
        assertEquals(new BigDecimal("100.00"), auxiliaryBalance.getPeriodDebitAmount());
        assertEquals(new BigDecimal("140.00"), auxiliaryBalance.getEndingDebitAmount());
    }

    @Test
    public void testGetQuantityGeneralList_filterZeroRowsAndKeepQuantity() {
        // mock 数据
        bankSubject.setQuantityAccounting(true);
        initialBalanceMapper.insert(new FmsInitialBalanceDO().setSubjectId(bankSubject.getId())
                .setAccountSetId(1L).setOpeningAmount(BigDecimal.ZERO)
                .setOpeningQuantity(new BigDecimal("5.00")).setYearDebitAmount(BigDecimal.ZERO)
                .setYearDebitQuantity(BigDecimal.ZERO).setYearCreditAmount(BigDecimal.ZERO)
                .setYearCreditQuantity(BigDecimal.ZERO).setYearOpeningAmount(BigDecimal.ZERO)
                .setYearOpeningQuantity(new BigDecimal("5.00")).setProfitLossAmount(BigDecimal.ZERO)
                .setAuxiliaryAccounting(false));

        // 准备参数
        FmsLedgerListReqVO reqVO = buildListReqVO();

        // 调用
        List<FmsLedgerQuantityGeneralRespVO> result = ledgerService.getQuantityGeneralList(reqVO, 10L);

        // 断言
        assertEquals(1, result.size());
        FmsLedgerQuantityGeneralRespVO parent = CollUtil.getFirst(result);
        assertEquals(1, parent.getChildren().size());
        assertEquals(new BigDecimal("5.0000"), CollUtil.getFirst(parent.getChildren()).getEndingQuantity());
    }

    @Test
    public void testGetQuantityGeneralList_filterYearOnlyRows() {
        // mock 数据：本年早期有累计、查询期初和本期均为零
        bankSubject.setQuantityAccounting(true);
        initialBalanceMapper.insert(new FmsInitialBalanceDO().setSubjectId(bankSubject.getId())
                .setAccountSetId(1L).setOpeningAmount(BigDecimal.ZERO).setOpeningQuantity(BigDecimal.ZERO)
                .setYearDebitAmount(new BigDecimal("10.00")).setYearDebitQuantity(new BigDecimal("5.00"))
                .setYearCreditAmount(new BigDecimal("10.00")).setYearCreditQuantity(new BigDecimal("5.00"))
                .setYearOpeningAmount(BigDecimal.ZERO).setYearOpeningQuantity(BigDecimal.ZERO)
                .setProfitLossAmount(BigDecimal.ZERO).setAuxiliaryAccounting(false));

        // 准备参数
        FmsLedgerListReqVO reqVO = buildListReqVO();

        // 调用
        List<FmsLedgerQuantityGeneralRespVO> result = ledgerService.getQuantityGeneralList(reqVO, 10L);

        // 断言
        assertEquals(0, result.size());
    }

    @Test
    public void testGetQuantityGeneralList_sameAsSubject() {
        // mock 数据：借方科目出现贷方净额时，按科目方向保留负数
        bankSubject.setQuantityAccounting(true);
        insertQuantityInitialBalance(new BigDecimal("-100.00"), new BigDecimal("-5.00"));

        // 准备参数
        FmsLedgerListReqVO reqVO = buildListReqVO();

        // 调用
        List<FmsLedgerQuantityGeneralRespVO> result = ledgerService.getQuantityGeneralList(reqVO, 10L);

        // 断言
        FmsLedgerQuantityGeneralRespVO balance = CollUtil.getFirst(CollUtil.getFirst(result).getChildren());
        assertEquals(FmsDebitCreditDirectionEnum.DEBIT.getName(), balance.getOpeningBalanceDirection());
        assertEquals(new BigDecimal("-100.00"), balance.getOpeningDebitAmount());
        assertEquals(new BigDecimal("-5.0000"), balance.getOpeningQuantity());
        assertEquals(FmsDebitCreditDirectionEnum.DEBIT.getName(), balance.getEndingBalanceDirection());
        assertEquals(new BigDecimal("-100.00"), balance.getEndingDebitAmount());
        assertEquals(new BigDecimal("-5.0000"), balance.getEndingQuantity());
    }

    @Test
    public void testGetQuantityGeneralList_oppositeToSubject() {
        // mock 数据：按实际净额方向展示时，余额和数量使用绝对值
        bankSubject.setQuantityAccounting(true);
        insertQuantityInitialBalance(new BigDecimal("-100.00"), new BigDecimal("-5.00"));
        when(financeParameterService.getFinanceParameter(1L, 10L)).thenReturn(
                new FmsFinanceParameterDO().setLedgerBalanceMode(
                        FmsLedgerBalanceModeEnum.OPPOSITE_TO_SUBJECT.getMode()));

        // 准备参数
        FmsLedgerListReqVO reqVO = buildListReqVO();

        // 调用
        List<FmsLedgerQuantityGeneralRespVO> result = ledgerService.getQuantityGeneralList(reqVO, 10L);

        // 断言
        FmsLedgerQuantityGeneralRespVO balance = CollUtil.getFirst(CollUtil.getFirst(result).getChildren());
        assertEquals(FmsDebitCreditDirectionEnum.CREDIT.getName(), balance.getOpeningBalanceDirection());
        assertEquals(new BigDecimal("100.00"), balance.getOpeningCreditAmount());
        assertEquals(new BigDecimal("5.0000"), balance.getOpeningQuantity());
        assertEquals(FmsDebitCreditDirectionEnum.CREDIT.getName(), balance.getEndingBalanceDirection());
        assertEquals(new BigDecimal("100.00"), balance.getEndingCreditAmount());
        assertEquals(new BigDecimal("5.0000"), balance.getEndingQuantity());
    }

    @Test
    public void testGetMultiColumnLedger() {
        // mock 数据
        initialBalanceMapper.insert(new FmsInitialBalanceDO().setSubjectId(bankSubject.getId())
                .setAccountSetId(1L).setOpeningAmount(new BigDecimal("100.00"))
                .setOpeningQuantity(BigDecimal.ZERO).setYearDebitAmount(BigDecimal.ZERO)
                .setYearDebitQuantity(BigDecimal.ZERO).setYearCreditAmount(BigDecimal.ZERO)
                .setYearCreditQuantity(BigDecimal.ZERO).setYearOpeningAmount(new BigDecimal("100.00"))
                .setYearOpeningQuantity(BigDecimal.ZERO).setProfitLossAmount(BigDecimal.ZERO)
                .setAuxiliaryAccounting(false));
        FmsVoucherWordDO word = new FmsVoucherWordDO().setName("记").setPrintTitle("记账凭证")
                .setDefaultStatus(true).setSort(1).setAccountSetId(1L);
        voucherWordMapper.insert(word);
        FmsVoucherDO voucher = new FmsVoucherDO().setVoucherWordId(word.getId()).setVoucherNumber(1)
                .setVoucherTime(LocalDateTime.of(2026, 8, 2, 0, 0)).setAttachmentUrls(Collections.emptyList())
                .setDebitAmount(new BigDecimal("80.00")).setCreditAmount(new BigDecimal("80.00"))
                .setTotal(new BigDecimal("80.00")).setStatus(1).setAccountSetId(1L);
        voucherMapper.insert(voucher);
        voucherEntryMapper.insert(new FmsVoucherEntryDO().setVoucherId(voucher.getId())
                .setAccountSetId(1L).setSubjectId(bankSubject.getId()).setSubjectCode("100201")
                .setSubjectName("工商银行").setDigest("收到客户回款").setSort(1)
                .setDebitAmount(new BigDecimal("80.00")).setCreditAmount(BigDecimal.ZERO)
                .setQuantity(BigDecimal.ZERO).setUnitPrice(BigDecimal.ZERO)
                .setAuxiliaries(Collections.emptyList()));

        // 准备参数
        FmsLedgerListReqVO reqVO = buildListReqVO();
        reqVO.setSubjectId(cashSubject.getId());

        // 调用
        FmsLedgerMultiColumnRespVO result = ledgerService.getMultiColumn(reqVO, 10L);

        // 断言
        assertEquals(1, result.getColumns().size());
        assertEquals(bankSubject.getId(), CollUtil.getFirst(result.getColumns()).getSubjectId());
        FmsLedgerDetailRespVO voucherRow = CollUtil.findOne(result.getRows(),
                row -> row.getRowType().equals(FmsLedgerDetailRespVO.ROW_TYPE_VOUCHER));
        assertEquals(new BigDecimal("80.00"), voucherRow.getColumnAmounts().get(bankSubject.getId()));
        FmsLedgerDetailRespVO periodTotalRow = CollUtil.findOne(result.getRows(),
                row -> row.getRowType().equals(FmsLedgerDetailRespVO.ROW_TYPE_PERIOD_TOTAL));
        assertEquals(new BigDecimal("80.00"), periodTotalRow.getColumnAmounts().get(bankSubject.getId()));
        FmsLedgerDetailRespVO endingRow = CollUtil.findOne(result.getRows(),
                row -> row.getRowType().equals(FmsLedgerDetailRespVO.ROW_TYPE_ENDING));
        assertEquals("期末余额", endingRow.getDigest());
        assertEquals(new BigDecimal("180.00"), endingRow.getBalance());
        assertEquals(new BigDecimal("180.00"), endingRow.getColumnAmounts().get(bankSubject.getId()));
    }

    @Test
    public void testGetAuxiliaryDetailList() {
        // mock 数据
        insertAuxiliaryVoucherEntry();
        // 准备参数
        FmsLedgerAuxiliaryListReqVO reqVO = buildAuxiliaryListReqVO();
        reqVO.setAuxiliaryItemId(customerItem.getId());

        // 调用
        List<FmsLedgerAuxiliaryDetailRespVO> result = ledgerService.getAuxiliaryDetailList(reqVO, 10L);

        // 断言
        assertEquals(4, result.size());
        FmsLedgerAuxiliaryDetailRespVO voucherRow = CollUtil.findOne(result,
                row -> row.getRowType().equals(FmsLedgerAuxiliaryDetailRespVO.ROW_TYPE_VOUCHER));
        assertEquals("记-1", voucherRow.getVoucherNumber());
        assertEquals(new BigDecimal("800.00"), voucherRow.getBalance());
    }

    @Test
    public void testGetAuxiliaryDetailList_sameAsSubject() {
        // mock 数据
        receivableSubject.setBalanceDirection(FmsDebitCreditDirectionEnum.CREDIT.getType());
        insertAuxiliaryVoucherEntry();
        // 准备参数
        FmsLedgerAuxiliaryListReqVO reqVO = buildAuxiliaryListReqVO();
        reqVO.setAuxiliaryItemId(customerItem.getId());
        reqVO.setSubjectId(receivableSubject.getId());

        // 调用
        List<FmsLedgerAuxiliaryDetailRespVO> result = ledgerService.getAuxiliaryDetailList(reqVO, 10L);

        // 断言
        FmsLedgerAuxiliaryDetailRespVO voucherRow = CollUtil.findOne(result,
                row -> row.getRowType().equals(FmsLedgerAuxiliaryDetailRespVO.ROW_TYPE_VOUCHER));
        assertEquals(FmsDebitCreditDirectionEnum.CREDIT.getName(), voucherRow.getBalanceDirection());
        assertEquals(new BigDecimal("-800.00"), voucherRow.getBalance());
    }

    @Test
    public void testGetAuxiliaryDetailList_oppositeToSubject() {
        // mock 数据
        receivableSubject.setBalanceDirection(FmsDebitCreditDirectionEnum.CREDIT.getType());
        insertAuxiliaryVoucherEntry();
        // 准备参数
        FmsLedgerAuxiliaryListReqVO reqVO = buildAuxiliaryListReqVO();
        reqVO.setAuxiliaryItemId(customerItem.getId());
        reqVO.setSubjectId(receivableSubject.getId());

        // mock 财务参数为按实际净额方向展示
        when(financeParameterService.getFinanceParameter(1L, 10L)).thenReturn(
                new FmsFinanceParameterDO().setLedgerBalanceMode(
                        FmsLedgerBalanceModeEnum.OPPOSITE_TO_SUBJECT.getMode()));

        // 调用
        List<FmsLedgerAuxiliaryDetailRespVO> result = ledgerService.getAuxiliaryDetailList(reqVO, 10L);

        // 断言
        FmsLedgerAuxiliaryDetailRespVO voucherRow = CollUtil.findOne(result,
                row -> row.getRowType().equals(FmsLedgerAuxiliaryDetailRespVO.ROW_TYPE_VOUCHER));
        assertEquals(FmsDebitCreditDirectionEnum.DEBIT.getName(), voucherRow.getBalanceDirection());
        assertEquals(new BigDecimal("800.00"), voucherRow.getBalance());
    }

    @Test
    public void testGetAuxiliaryDetailList_withoutSubject() {
        // mock 数据
        receivableSubject.setBalanceDirection(FmsDebitCreditDirectionEnum.CREDIT.getType());
        insertAuxiliaryVoucherEntry();
        // 准备参数：不指定科目时聚合多个科目，不任取其中一个科目方向
        FmsLedgerAuxiliaryListReqVO reqVO = buildAuxiliaryListReqVO();
        reqVO.setAuxiliaryItemId(customerItem.getId());

        // 调用
        List<FmsLedgerAuxiliaryDetailRespVO> result = ledgerService.getAuxiliaryDetailList(reqVO, 10L);

        // 断言
        FmsLedgerAuxiliaryDetailRespVO voucherRow = CollUtil.findOne(result,
                row -> row.getRowType().equals(FmsLedgerAuxiliaryDetailRespVO.ROW_TYPE_VOUCHER));
        assertEquals(FmsDebitCreditDirectionEnum.DEBIT.getName(), voucherRow.getBalanceDirection());
        assertEquals(new BigDecimal("800.00"), voucherRow.getBalance());
    }

    @Test
    public void testGetAuxiliaryBalanceList() {
        // mock 数据
        insertAuxiliaryVoucherEntry();
        // 准备参数
        FmsLedgerAuxiliaryListReqVO reqVO = buildAuxiliaryListReqVO();

        // 调用
        List<FmsLedgerAuxiliaryBalanceRespVO> result = ledgerService.getAuxiliaryBalanceList(reqVO, 10L);

        // 断言
        FmsLedgerAuxiliaryBalanceRespVO balance = CollUtil.getFirst(result);
        assertEquals(customerItem.getName(), balance.getName());
        assertEquals(new BigDecimal("800.00"), balance.getPeriodDebitAmount());
        assertEquals(new BigDecimal("800.00"), balance.getEndingDebitAmount());
    }

    @Test
    public void testGetAuxiliaryBalanceList_filterZeroRows() {
        // mock 数据
        initialBalanceMapper.insert(new FmsInitialBalanceDO().setSubjectId(receivableSubject.getId())
                .setAccountSetId(1L).setAuxiliaryAccounting(true)
                .setOpeningAmount(BigDecimal.ZERO).setOpeningQuantity(BigDecimal.ZERO)
                .setYearDebitAmount(BigDecimal.ZERO).setYearDebitQuantity(BigDecimal.ZERO)
                .setYearCreditAmount(BigDecimal.ZERO).setYearCreditQuantity(BigDecimal.ZERO)
                .setYearOpeningAmount(BigDecimal.ZERO).setYearOpeningQuantity(BigDecimal.ZERO)
                .setProfitLossAmount(BigDecimal.ZERO).setAssistBalances(Collections.singletonList(
                        FmsInitialBalanceDO.AssistBalance.builder()
                                .assistCombinationId(501L)
                                .auxiliaries(Collections.singletonList(
                                        FmsInitialBalanceDO.AuxiliaryItem.builder()
                                                .type(1).typeId(201L).itemId(customerItem.getId())
                                                .name(customerItem.getName()).build()))
                                .openingAmount(BigDecimal.ZERO).openingQuantity(BigDecimal.ZERO)
                                .yearDebitAmount(BigDecimal.ZERO).yearDebitQuantity(BigDecimal.ZERO)
                                .yearCreditAmount(BigDecimal.ZERO).yearCreditQuantity(BigDecimal.ZERO)
                                .yearOpeningAmount(BigDecimal.ZERO).yearOpeningQuantity(BigDecimal.ZERO)
                                .profitLossAmount(BigDecimal.ZERO).profitLossQuantity(BigDecimal.ZERO).build())));

        // 准备参数
        FmsLedgerAuxiliaryListReqVO reqVO = buildAuxiliaryListReqVO();

        // 调用
        List<FmsLedgerAuxiliaryBalanceRespVO> result = ledgerService.getAuxiliaryBalanceList(reqVO, 10L);

        // 断言
        assertEquals(0, result.size());
    }

    @Test
    public void testGetAuxiliaryBalanceList_startMonthBeforeAccountSetStartMonth() {
        // 准备参数
        FmsLedgerAuxiliaryListReqVO reqVO = buildAuxiliaryListReqVO();
        reqVO.setStartMonth("2025-12");

        // 调用并断言
        assertServiceException(() -> ledgerService.getAuxiliaryBalanceList(reqVO, 10L),
                LEDGER_PERIOD_BEFORE_ACCOUNT_START);
    }

    @Test
    public void testGetAuxiliaryCombinationBalance() {
        // mock 数据
        receivableSubject.setAuxiliaryTypeIds(Collections.singletonList(201L));
        when(auxiliaryItemService.validateAuxiliaryItemList(
                1L, Collections.singletonList(customerItem.getId())))
                .thenReturn(Collections.singletonList(customerItem));
        initialBalanceMapper.insert(new FmsInitialBalanceDO().setSubjectId(receivableSubject.getId())
                .setAccountSetId(1L).setAuxiliaryAccounting(true)
                .setOpeningAmount(new BigDecimal("40.00")).setOpeningQuantity(BigDecimal.ZERO)
                .setYearDebitAmount(BigDecimal.ZERO).setYearDebitQuantity(BigDecimal.ZERO)
                .setYearCreditAmount(BigDecimal.ZERO).setYearCreditQuantity(BigDecimal.ZERO)
                .setYearOpeningAmount(new BigDecimal("40.00")).setYearOpeningQuantity(BigDecimal.ZERO)
                .setProfitLossAmount(BigDecimal.ZERO).setAssistBalances(Collections.singletonList(
                        FmsInitialBalanceDO.AssistBalance.builder()
                                .assistCombinationId(501L)
                                .auxiliaries(Collections.singletonList(
                                        FmsInitialBalanceDO.AuxiliaryItem.builder()
                                                .type(1).typeId(201L).itemId(customerItem.getId())
                                                .name(customerItem.getName()).build()))
                                .openingAmount(new BigDecimal("40.00")).build())));
        FmsVoucherWordDO word = buildVoucherWord();
        voucherWordMapper.insert(word);
        FmsVoucherDO voucher = buildVoucher(word.getId(), "100.00");
        voucherMapper.insert(voucher);
        voucherEntryMapper.insert(new FmsVoucherEntryDO().setVoucherId(voucher.getId())
                .setAccountSetId(1L).setSubjectId(receivableSubject.getId()).setSubjectCode("1122")
                .setSubjectName("应收账款").setDigest("客户一应收款").setSort(1)
                .setDebitAmount(new BigDecimal("100.00")).setCreditAmount(BigDecimal.ZERO)
                .setQuantity(BigDecimal.ZERO).setUnitPrice(BigDecimal.ZERO)
                .setAuxiliaries(Collections.singletonList(FmsVoucherEntryDO.AuxiliaryItem.builder()
                        .type(1).typeId(201L).itemId(customerItem.getId())
                        .name(customerItem.getName()).build())));
        voucherEntryMapper.insert(new FmsVoucherEntryDO().setVoucherId(voucher.getId())
                .setAccountSetId(1L).setSubjectId(receivableSubject.getId()).setSubjectCode("1122")
                .setSubjectName("应收账款").setDigest("客户二冲减").setSort(2)
                .setDebitAmount(BigDecimal.ZERO).setCreditAmount(new BigDecimal("100.00"))
                .setQuantity(BigDecimal.ZERO).setUnitPrice(BigDecimal.ZERO)
                .setAuxiliaries(Collections.singletonList(FmsVoucherEntryDO.AuxiliaryItem.builder()
                        .type(1).typeId(201L).itemId(302L).name("上海客户").build())));

        // 调用
        BigDecimal balance = ledgerService.getAuxiliaryCombinationBalance(
                1L, "2026-08", receivableSubject.getId(),
                Collections.singletonList(customerItem.getId()), 10L);

        // 断言
        assertEquals(new BigDecimal("140.00"), balance);
    }

    // ========== 随机对象 ==========

    private FmsLedgerListReqVO buildListReqVO() {
        FmsLedgerListReqVO reqVO = new FmsLedgerListReqVO();
        reqVO.setAccountSetId(1L);
        reqVO.setStartMonth("2026-08");
        reqVO.setEndMonth("2026-08");
        return reqVO;
    }

    private FmsLedgerAuxiliaryListReqVO buildAuxiliaryListReqVO() {
        FmsLedgerAuxiliaryListReqVO reqVO = new FmsLedgerAuxiliaryListReqVO();
        reqVO.setAccountSetId(1L);
        reqVO.setStartMonth("2026-08");
        reqVO.setEndMonth("2026-08");
        reqVO.setAuxiliaryTypeId(201L);
        return reqVO;
    }

    private FmsVoucherWordDO buildVoucherWord() {
        return randomPojo(FmsVoucherWordDO.class, voucherWord -> voucherWord.setId(null)
                .setName("记").setPrintTitle("记账凭证").setDefaultStatus(true)
                .setSort(1).setAccountSetId(1L));
    }

    private FmsVoucherDO buildVoucher(Long voucherWordId, String amount) {
        return randomPojo(FmsVoucherDO.class, voucher -> voucher.setId(null)
                .setVoucherWordId(voucherWordId).setVoucherNumber(1)
                .setVoucherTime(LocalDateTime.of(2026, 8, 2, 0, 0)).setAttachmentUrls(Collections.emptyList())
                .setDebitAmount(new BigDecimal(amount)).setCreditAmount(new BigDecimal(amount))
                .setTotal(new BigDecimal(amount)).setStatus(1).setAccountSetId(1L));
    }

    private void insertQuantityInitialBalance(BigDecimal openingAmount, BigDecimal openingQuantity) {
        initialBalanceMapper.insert(new FmsInitialBalanceDO().setSubjectId(bankSubject.getId())
                .setAccountSetId(1L).setOpeningAmount(openingAmount).setOpeningQuantity(openingQuantity)
                .setYearDebitAmount(BigDecimal.ZERO).setYearDebitQuantity(BigDecimal.ZERO)
                .setYearCreditAmount(BigDecimal.ZERO).setYearCreditQuantity(BigDecimal.ZERO)
                .setYearOpeningAmount(openingAmount).setYearOpeningQuantity(openingQuantity)
                .setProfitLossAmount(BigDecimal.ZERO).setAuxiliaryAccounting(false));
    }

    private void insertAuxiliaryVoucherEntry() {
        FmsVoucherWordDO word = buildVoucherWord();
        voucherWordMapper.insert(word);
        FmsVoucherDO voucher = buildVoucher(word.getId(), "800.00");
        voucherMapper.insert(voucher);
        voucherEntryMapper.insert(new FmsVoucherEntryDO().setVoucherId(voucher.getId())
                .setAccountSetId(1L).setSubjectId(receivableSubject.getId()).setSubjectCode("1122")
                .setSubjectName("应收账款").setDigest("确认客户应收款").setSort(1)
                .setDebitAmount(new BigDecimal("800.00")).setCreditAmount(BigDecimal.ZERO)
                .setQuantity(BigDecimal.ZERO).setUnitPrice(BigDecimal.ZERO)
                .setAuxiliaries(Collections.singletonList(FmsVoucherEntryDO.AuxiliaryItem.builder()
                        .type(1).typeId(201L).itemId(customerItem.getId())
                        .name(customerItem.getName()).build())));
    }

    private FmsSubjectDO buildSubject(Long id, String code, String name, Long parentId, Integer level) {
        return randomPojo(FmsSubjectDO.class, subject -> subject.setId(id)
                .setCode(code).setName(name).setParentId(parentId)
                .setType(1).setCategory(1).setBalanceDirection(1).setQuantityAccounting(false)
                .setCash(false).setStatus(1).setLevel(level).setAccountSetId(1L)
                .setAuxiliaryTypeIds(Collections.emptyList()).setCurrencyIds(Collections.emptyList()));
    }

}
