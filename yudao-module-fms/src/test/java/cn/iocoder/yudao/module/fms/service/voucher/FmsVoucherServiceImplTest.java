package cn.iocoder.yudao.module.fms.service.voucher;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.fms.controller.admin.voucher.vo.FmsVoucherEntrySaveReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.voucher.vo.FmsVoucherAttachmentUpdateReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.voucher.vo.FmsVoucherMoveReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.voucher.vo.FmsVoucherPageReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.voucher.vo.FmsVoucherSaveReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.voucher.vo.FmsVoucherStatisticsReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.voucher.vo.FmsVoucherStatisticsRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.voucher.vo.FmsVoucherSubjectAmountVO;
import cn.iocoder.yudao.module.fms.controller.admin.voucher.vo.FmsVoucherTidyReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountSetDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAuxiliaryCombinationDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAuxiliaryItemDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAuxiliaryTypeDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsSubjectDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.voucher.FmsVoucherDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.voucher.FmsVoucherEntryDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsVoucherWordDO;
import cn.iocoder.yudao.module.fms.dal.mysql.voucher.FmsVoucherEntryMapper;
import cn.iocoder.yudao.module.fms.dal.mysql.voucher.FmsVoucherMapper;
import cn.iocoder.yudao.module.fms.enums.voucher.FmsVoucherStatusEnum;
import cn.iocoder.yudao.module.fms.enums.voucher.FmsVoucherTidyTypeEnum;
import cn.iocoder.yudao.module.fms.service.closing.FmsClosingPeriodService;
import cn.iocoder.yudao.module.fms.service.closing.FmsClosingVoucherService;
import cn.iocoder.yudao.module.fms.service.config.FmsAccountSetService;
import cn.iocoder.yudao.module.fms.service.config.FmsAuxiliaryCombinationService;
import cn.iocoder.yudao.module.fms.service.config.FmsAuxiliaryItemService;
import cn.iocoder.yudao.module.fms.service.config.FmsAuxiliaryTypeService;
import cn.iocoder.yudao.module.fms.service.config.FmsSubjectService;
import cn.iocoder.yudao.module.fms.service.config.FmsVoucherWordService;
import cn.iocoder.yudao.module.fms.controller.admin.voucher.vo.FmsVoucherImportRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.voucher.vo.FmsVoucherImportExcelVO;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

@Import(FmsVoucherServiceImpl.class)
public class FmsVoucherServiceImplTest extends BaseDbUnitTest {

    @Resource
    private FmsVoucherServiceImpl voucherService;
    @Resource
    private FmsVoucherMapper voucherMapper;
    @Resource
    private FmsVoucherEntryMapper voucherEntryMapper;

    @MockitoBean
    private FmsAccountSetService accountSetService;
    @MockitoBean
    private FmsClosingPeriodService closingPeriodService;
    @MockitoBean
    private FmsClosingVoucherService closingVoucherService;
    @MockitoBean
    private FmsVoucherWordService voucherWordService;
    @MockitoBean
    private FmsSubjectService subjectService;
    @MockitoBean
    private FmsAuxiliaryItemService auxiliaryItemService;
    @MockitoBean
    private FmsAuxiliaryTypeService auxiliaryTypeService;
    @MockitoBean
    private FmsAuxiliaryCombinationService auxiliaryCombinationService;

    private FmsSubjectDO cashSubject;
    private FmsSubjectDO payableSubject;
    private FmsSubjectDO cashChildSubject;
    private FmsSubjectDO bankSubject;

    @BeforeEach
    public void before() {
        cashSubject = buildSubject(101L, "1001", "库存现金");
        payableSubject = buildSubject(201L, "2202", "应付账款");
        cashChildSubject = buildSubject(102L, "100101", "人民币现金")
                .setParentId(cashSubject.getId()).setLevel(2);
        bankSubject = buildSubject(103L, "1002", "银行存款");
        when(subjectService.getSubjectList(1L, null, 10L))
                .thenReturn(Arrays.asList(cashSubject, cashChildSubject, bankSubject, payableSubject));
        when(accountSetService.validateAccountSetReadPermission(1L, 10L))
                .thenReturn(new FmsAccountSetDO().setId(1L));
        when(voucherWordService.validateVoucherWordExists(1L, 11L))
                .thenReturn(new FmsVoucherWordDO().setId(11L).setAccountSetId(1L).setName("记"));
        when(voucherWordService.getVoucherWordList(1L)).thenReturn(Collections.singletonList(
                new FmsVoucherWordDO().setId(11L).setAccountSetId(1L).setName("记")));
        when(voucherWordService.getVoucherWordMapByName(1L)).thenReturn(Collections.singletonMap(
                "记", new FmsVoucherWordDO().setId(11L).setAccountSetId(1L).setName("记")));
        when(auxiliaryTypeService.getAuxiliaryTypeList(1L, 10L)).thenReturn(Collections.emptyList());
        when(auxiliaryTypeService.validateAuxiliaryTypeList(eq(1L), anyCollection()))
                .thenReturn(Collections.emptyList());
        when(auxiliaryItemService.getAuxiliaryItemListByAccountSetId(1L, 10L))
                .thenReturn(Collections.emptyList());
    }

    @Test
    public void testGetVoucherCountByVoucherWordId() {
        // mock 数据
        voucherMapper.insert(buildVoucher(1L, FmsVoucherStatusEnum.PENDING_REVIEW.getStatus()));
        voucherMapper.insert(buildVoucher(2L, FmsVoucherStatusEnum.PENDING_REVIEW.getStatus()));

        // 调用，并断言
        assertEquals(1L, voucherService.getVoucherCountByVoucherWordId(1L, 11L));
        assertEquals(0L, voucherService.getVoucherCountByVoucherWordId(1L, 12L));
    }

    @Test
    public void testGetVoucherSubjectAmountList() {
        // mock 数据
        voucherEntryMapper.insert(new FmsVoucherEntryDO().setAccountSetId(1L).setVoucherId(11L)
                .setSubjectId(101L).setDebitAmount(new BigDecimal("10.00"))
                .setCreditAmount(BigDecimal.ZERO));
        voucherEntryMapper.insert(new FmsVoucherEntryDO().setAccountSetId(1L).setVoucherId(12L)
                .setSubjectId(101L).setDebitAmount(new BigDecimal("20.00"))
                .setCreditAmount(BigDecimal.ZERO));
        voucherEntryMapper.insert(new FmsVoucherEntryDO().setAccountSetId(1L).setVoucherId(12L)
                .setSubjectId(102L).setDebitAmount(BigDecimal.ZERO)
                .setCreditAmount(new BigDecimal("30.00")));

        // 调用
        List<FmsVoucherSubjectAmountVO> result = voucherService.getVoucherSubjectAmountList(
                Arrays.asList(11L, 12L));

        // 断言
        assertEquals(2, result.size());
        assertEquals(0, new BigDecimal("30.00").compareTo(result.get(0).getDebitAmount()));
        assertEquals(0, new BigDecimal("30.00").compareTo(result.get(1).getCreditAmount()));
    }

    @Test
    public void testGetVoucherEntryCountByAuxiliary() {
        // mock 数据
        voucherEntryMapper.insert(new FmsVoucherEntryDO().setAccountSetId(1L)
                .setAuxiliaries(Collections.singletonList(FmsVoucherEntryDO.AuxiliaryItem.builder()
                        .typeId(31L).itemId(41L).build())));
        voucherEntryMapper.insert(new FmsVoucherEntryDO().setAccountSetId(2L)
                .setAuxiliaries(Collections.singletonList(FmsVoucherEntryDO.AuxiliaryItem.builder()
                        .typeId(31L).itemId(41L).build())));

        // 调用，并断言
        assertEquals(1L, voucherService.getVoucherEntryCountByAuxiliaryItemIds(
                1L, Collections.singletonList(41L)));
        assertEquals(1L, voucherService.getVoucherEntryCountByAuxiliaryTypeId(1L, 31L));
        assertEquals(0L, voucherService.getVoucherEntryCountByAuxiliaryItemIds(
                1L, Collections.singletonList(42L)));
        assertEquals(0L, voucherService.getVoucherEntryCountByAuxiliaryItemIds(
                1L, Collections.emptyList()));
    }

    @Test
    public void testMigrateVoucherEntryAuxiliaries() {
        // mock 数据
        FmsVoucherEntryDO entry = new FmsVoucherEntryDO().setAccountSetId(1L)
                .setVoucherId(11L).setSubjectId(101L);
        voucherEntryMapper.insert(entry);
        FmsAuxiliaryCombinationDO combination = new FmsAuxiliaryCombinationDO().setId(51L)
                .setItems(Collections.singletonList(FmsAuxiliaryCombinationDO.AuxiliaryItem.builder()
                        .type(1).typeId(31L).itemId(41L).name("北京客户").build()));

        // 调用
        voucherService.migrateVoucherEntryAuxiliaries(1L, 101L, combination);

        // 断言
        FmsVoucherEntryDO result = voucherEntryMapper.selectById(entry.getId());
        assertEquals(combination.getId(), result.getAssistCombinationId());
        assertEquals(1, result.getAuxiliaries().size());
        assertEquals(41L, result.getAuxiliaries().get(0).getItemId());
        assertEquals("北京客户", result.getAuxiliaries().get(0).getName());
    }

    @Test
    public void testGetVoucherEntryQuantityCountBySubjectIds() {
        // mock 数据
        voucherEntryMapper.insert(new FmsVoucherEntryDO().setAccountSetId(1L)
                .setSubjectId(101L).setQuantity(new BigDecimal("2.00")));
        voucherEntryMapper.insert(new FmsVoucherEntryDO().setAccountSetId(1L)
                .setSubjectId(101L).setQuantity(BigDecimal.ZERO));
        voucherEntryMapper.insert(new FmsVoucherEntryDO().setAccountSetId(1L)
                .setSubjectId(102L).setQuantity(new BigDecimal("3.00")));

        // 调用，并断言
        assertEquals(1L, voucherService.getVoucherEntryQuantityCountBySubjectIds(
                1L, Collections.singletonList(101L)));
        assertEquals(2L, voucherService.getVoucherEntryQuantityCountBySubjectIds(
                1L, Arrays.asList(101L, 102L)));
    }

    @Test
    public void testCreateVoucher_success() {
        // 准备参数
        FmsVoucherSaveReqVO reqVO = buildSaveReqVO("100.00", "100.00");
        reqVO.getEntries().get(0).setId(90001L);
        reqVO.getEntries().get(1).setId(90002L);
        reqVO.setVoucherTime(LocalDateTime.of(2026, 8, 2, 15, 30));
        reqVO.setAttachmentCount(3);

        // 调用
        Long voucherId = voucherService.createVoucher(reqVO, 10L);

        // 断言
        FmsVoucherDO voucher = voucherMapper.selectById(voucherId);
        assertEquals(1, voucher.getVoucherNumber());
        assertEquals(LocalDateTime.of(2026, 8, 2, 0, 0), voucher.getVoucherTime());
        assertEquals(new BigDecimal("100.00"), voucher.getDebitAmount());
        assertEquals(new BigDecimal("100.00"), voucher.getCreditAmount());
        assertEquals(3, voucher.getAttachmentCount());
        assertEquals(FmsVoucherStatusEnum.PENDING_REVIEW.getStatus(), voucher.getStatus());
        List<FmsVoucherEntryDO> entries = voucherEntryMapper.selectListByVoucherIds(
                Collections.singletonList(voucherId));
        assertEquals(2, entries.size());
        assertFalse(entries.stream().anyMatch(
                entry -> Arrays.asList(90001L, 90002L).contains(entry.getId())));
    }

    @Test
    public void testCreateVoucher_negativeAmount() {
        // 准备参数：红字分录保留在原借贷方向
        FmsVoucherSaveReqVO reqVO = buildSaveReqVO("-20.00", "-20.00");

        // 调用
        Long voucherId = voucherService.createVoucher(reqVO, 10L);

        // 断言
        FmsVoucherDO voucher = voucherMapper.selectById(voucherId);
        assertEquals(new BigDecimal("-20.00"), voucher.getDebitAmount());
        assertEquals(new BigDecimal("-20.00"), voucher.getCreditAmount());
    }

    @Test
    public void testCreateVoucher_quantityAmountFloor() {
        // mock 数据
        bankSubject.setQuantityAccounting(true);
        // 准备参数
        FmsVoucherSaveReqVO reqVO = buildSaveReqVO("1.99", "1.99");
        CollUtil.getFirst(reqVO.getEntries()).setQuantity(new BigDecimal("1"));
        CollUtil.getFirst(reqVO.getEntries()).setUnitPrice(new BigDecimal("1.999"));

        // 调用
        Long voucherId = voucherService.createVoucher(reqVO, 10L);

        // 断言
        FmsVoucherEntryDO entry = CollUtil.getFirst(voucherEntryMapper.selectListByVoucherIds(
                Collections.singletonList(voucherId)));
        assertEquals(new BigDecimal("1.99"), entry.getDebitAmount());
        assertEquals(0, new BigDecimal("1").compareTo(entry.getQuantity()));
        assertEquals(0, new BigDecimal("1.999").compareTo(entry.getUnitPrice()));
    }

    @Test
    public void testCreateVoucher_attachmentUrlsEmpty() {
        // 准备参数
        FmsVoucherSaveReqVO reqVO = buildSaveReqVO("100.00", "100.00");

        // 调用
        Long voucherId = voucherService.createVoucher(reqVO, 10L);

        // 断言
        FmsVoucherDO voucher = voucherMapper.selectById(voucherId);
        assertEquals(0, voucher.getAttachmentCount());
        assertEquals(Collections.emptyList(), voucher.getAttachmentUrls());
    }

    @Test
    public void testCreateVoucher_numberDuplicateInSameMonth() {
        // mock 数据
        voucherMapper.insert(buildVoucher(1L, FmsVoucherStatusEnum.PENDING_REVIEW.getStatus()));
        // 准备参数
        FmsVoucherSaveReqVO reqVO = buildSaveReqVO("100.00", "100.00");
        reqVO.setVoucherNumber(1);
        reqVO.setVoucherTime(LocalDateTime.of(2026, 8, 3, 0, 0));

        // 调用，并断言
        assertServiceException(() -> voucherService.createVoucher(reqVO, 10L),
                VOUCHER_NUMBER_DUPLICATE);
    }

    @Test
    public void testCreateVoucher_sameNumberInNextMonth() {
        // mock 数据
        voucherMapper.insert(buildVoucher(1L, FmsVoucherStatusEnum.PENDING_REVIEW.getStatus()));
        // 准备参数
        FmsVoucherSaveReqVO reqVO = buildSaveReqVO("100.00", "100.00");
        reqVO.setVoucherNumber(1);
        reqVO.setVoucherTime(LocalDateTime.of(2026, 9, 1, 0, 0));

        // 调用
        Long voucherId = voucherService.createVoucher(reqVO, 10L);

        // 断言
        assertEquals(1, voucherMapper.selectById(voucherId).getVoucherNumber());
    }

    @Test
    public void testCreateVoucher_periodClosed() {
        // 准备参数
        FmsVoucherSaveReqVO reqVO = buildSaveReqVO("100.00", "100.00");
        doThrow(exception(CLOSING_PERIOD_CLOSED)).when(closingPeriodService)
                .validatePeriodOpen(1L, reqVO.getVoucherTime());

        // 调用，并断言
        assertServiceException(() -> voucherService.createVoucher(reqVO, 10L),
                CLOSING_PERIOD_CLOSED);
    }

    @Test
    public void testCreateVoucher_unbalanced() {
        // 准备参数
        FmsVoucherSaveReqVO reqVO = buildSaveReqVO("100.00", "90.00");

        // 调用，并断言
        assertServiceException(() -> voucherService.createVoucher(reqVO, 10L),
                VOUCHER_AMOUNT_UNBALANCED);
    }

    @Test
    public void testCreateVoucher_auxiliarySnapshot() {
        // mock 数据
        bankSubject.setAuxiliaryTypeIds(Collections.singletonList(31L));
        FmsAuxiliaryTypeDO auxiliaryType = new FmsAuxiliaryTypeDO().setId(31L)
                .setAccountSetId(1L).setName("客户").setType(1);
        FmsAuxiliaryItemDO auxiliaryItem = new FmsAuxiliaryItemDO().setId(41L)
                .setAccountSetId(1L).setAuxiliaryTypeId(31L).setCode("KH001")
                .setName("上海客户").setStatus(CommonStatusEnum.ENABLE.getStatus());
        when(auxiliaryItemService.validateAuxiliaryItemList(eq(1L), anyCollection()))
                .thenReturn(Collections.singletonList(auxiliaryItem));
        when(auxiliaryTypeService.validateAuxiliaryTypeList(eq(1L), anyCollection()))
                .thenReturn(Collections.singletonList(auxiliaryType));
        // 准备参数
        FmsVoucherSaveReqVO reqVO = buildSaveReqVO("100.00", "100.00");
        FmsVoucherEntrySaveReqVO.AuxiliaryItem requestItem = new FmsVoucherEntrySaveReqVO.AuxiliaryItem();
        requestItem.setTypeId(31L);
        requestItem.setItemId(41L);
        CollUtil.getFirst(reqVO.getEntries()).setAuxiliaries(Collections.singletonList(requestItem));

        // 调用
        Long voucherId = voucherService.createVoucher(reqVO, 10L);

        // 断言
        FmsVoucherEntryDO entry = CollUtil.getFirst(voucherEntryMapper
                .selectListByVoucherIds(Collections.singletonList(voucherId)));
        assertEquals("上海客户", CollUtil.getFirst(entry.getAuxiliaries()).getName());
        assertEquals(31L, CollUtil.getFirst(entry.getAuxiliaries()).getTypeId());
    }

    @Test
    public void testImportVoucher_success() {
        // 准备参数
        List<FmsVoucherImportExcelVO> rows = Arrays.asList(
                buildImportRow(2, "1002", "采购材料", "100.00", null),
                buildImportRow(3, "2202", "采购材料", null, "100.00"));

        // 调用
        FmsVoucherImportRespVO result = voucherService.importVoucher(1L, rows, 10L);

        // 断言
        assertEquals(1, result.getSuccessVoucherCount());
        assertEquals(2, result.getSuccessRowCount());
        assertEquals(0, result.getFailureVoucherCount());
        FmsVoucherDO voucher = CollUtil.getFirst(voucherMapper.selectList());
        assertEquals(2, voucher.getAttachmentCount());
        assertEquals(2, voucherEntryMapper.selectListByVoucherIds(
                Collections.singletonList(voucher.getId())).size());
    }

    @Test
    public void testImportVoucher_negativeAmount() {
        // 准备参数：导入红字凭证时借贷方向保持不变
        List<FmsVoucherImportExcelVO> rows = Arrays.asList(
                buildImportRow(2, "1002", "红字冲销", "-20.00", null),
                buildImportRow(3, "2202", "红字冲销", null, "-20.00"));

        // 调用
        FmsVoucherImportRespVO result = voucherService.importVoucher(1L, rows, 10L);

        // 断言
        assertEquals(1, result.getSuccessVoucherCount());
        assertEquals(0, result.getFailureVoucherCount());
        FmsVoucherDO voucher = CollUtil.getFirst(voucherMapper.selectList());
        assertEquals(new BigDecimal("-20.00"), voucher.getDebitAmount());
        assertEquals(new BigDecimal("-20.00"), voucher.getCreditAmount());
    }

    @Test
    public void testImportVoucher_quantityAmountFloor() {
        // mock 数据
        bankSubject.setQuantityAccounting(true);
        // 准备参数
        FmsVoucherImportExcelVO debitRow = buildImportRow(2, "1002", "采购材料", "1.99", null)
                .setQuantity(new BigDecimal("1")).setUnitPrice(new BigDecimal("1.999"));
        List<FmsVoucherImportExcelVO> rows = Arrays.asList(debitRow,
                buildImportRow(3, "2202", "采购材料", null, "1.99"));

        // 调用
        FmsVoucherImportRespVO result = voucherService.importVoucher(1L, rows, 10L);

        // 断言
        assertEquals(1, result.getSuccessVoucherCount());
        assertEquals(0, result.getFailureVoucherCount());
    }

    @Test
    public void testImportVoucher_partialSuccess() {
        // mock 数据
        voucherMapper.insert(buildVoucher(1L, FmsVoucherStatusEnum.PENDING_REVIEW.getStatus())
                .setVoucherNumber(2).setVoucherTime(LocalDateTime.of(2026, 8, 1, 0, 0)));
        // 准备参数
        List<FmsVoucherImportExcelVO> rows = Arrays.asList(
                buildImportRow(2, "1002", "有效凭证", "100.00", null).setVoucherNumber(1),
                buildImportRow(3, "2202", "有效凭证", null, "100.00").setVoucherNumber(1),
                buildImportRow(4, "1002", "重复凭证", "80.00", null).setVoucherNumber(2),
                buildImportRow(5, "2202", "重复凭证", null, "80.00").setVoucherNumber(2));

        // 调用
        FmsVoucherImportRespVO result = voucherService.importVoucher(1L, rows, 10L);

        // 断言
        assertEquals(1, result.getSuccessVoucherCount());
        assertEquals(1, result.getFailureVoucherCount());
        assertEquals(2, result.getFailureRowCount());
        assertTrue(result.getErrorRows().stream().allMatch(row ->
                row.getErrors().contains("同一会计期间、凭证字和凭证号已存在")));
    }

    @Test
    public void testImportVoucher_duplicateInSameMonth() {
        // 准备参数
        List<FmsVoucherImportExcelVO> rows = Arrays.asList(
                buildImportRow(2, "1002", "月初凭证", "100.00", null),
                buildImportRow(3, "2202", "月初凭证", null, "100.00"),
                buildImportRow(4, "1002", "月末凭证", "80.00", null)
                        .setVoucherTime(LocalDateTime.of(2026, 8, 31, 0, 0)),
                buildImportRow(5, "2202", "月末凭证", null, "80.00")
                        .setVoucherTime(LocalDateTime.of(2026, 8, 31, 0, 0)));

        // 调用
        FmsVoucherImportRespVO result = voucherService.importVoucher(1L, rows, 10L);

        // 断言
        assertEquals(1, result.getSuccessVoucherCount());
        assertEquals(1, result.getFailureVoucherCount());
        assertEquals(2, result.getFailureRowCount());
        assertTrue(result.getErrorRows().stream().allMatch(row ->
                row.getErrors().contains("同一会计期间、凭证字和凭证号重复")));
    }

    @Test
    public void testImportVoucher_auxiliary() {
        // mock 数据
        bankSubject.setAuxiliaryTypeIds(Collections.singletonList(31L));
        FmsAuxiliaryTypeDO auxiliaryType = new FmsAuxiliaryTypeDO().setId(31L)
                .setAccountSetId(1L).setName("客户").setType(1);
        FmsAuxiliaryItemDO auxiliaryItem = new FmsAuxiliaryItemDO().setId(41L)
                .setAccountSetId(1L).setAuxiliaryTypeId(31L).setCode("KH001")
                .setName("上海客户").setStatus(CommonStatusEnum.ENABLE.getStatus());
        when(auxiliaryTypeService.getAuxiliaryTypeList(1L, 10L))
                .thenReturn(Collections.singletonList(auxiliaryType));
        when(auxiliaryItemService.getAuxiliaryItemListByAccountSetId(1L, 10L))
                .thenReturn(Collections.singletonList(auxiliaryItem));
        when(auxiliaryTypeService.getAuxiliaryTypeMap(1L, 10L))
                .thenReturn(Collections.singletonMap(31L, auxiliaryType));
        when(auxiliaryItemService.getAuxiliaryItemMapByTypeIdAndCode(1L, 10L))
                .thenReturn(Collections.singletonMap("31|KH001", auxiliaryItem));
        // 准备参数
        FmsVoucherImportExcelVO debitRow = buildImportRow(2, "1002", "采购材料", "100.00", null);
        debitRow.getAuxiliaryCodes().put(31L, "KH001");

        // 调用
        FmsVoucherImportRespVO result = voucherService.importVoucher(1L, Arrays.asList(
                debitRow, buildImportRow(3, "2202", "采购材料", null, "100.00")), 10L);

        // 断言
        assertEquals(1, result.getSuccessVoucherCount());
        FmsVoucherDO voucher = CollUtil.getFirst(voucherMapper.selectList());
        FmsVoucherEntryDO entry = CollUtil.getFirst(voucherEntryMapper.selectListByVoucherIds(
                Collections.singletonList(voucher.getId())));
        assertEquals("上海客户", CollUtil.getFirst(entry.getAuxiliaries()).getName());
    }

    @Test
    public void testDeleteVoucherList_approved() {
        // mock 数据
        FmsVoucherDO voucher = buildVoucher(1L, FmsVoucherStatusEnum.APPROVED.getStatus());
        voucherMapper.insert(voucher);

        // 调用，并断言
        assertServiceException(() -> voucherService.deleteVoucherList(
                1L, Collections.singletonList(voucher.getId()), 10L), VOUCHER_APPROVED_NOT_EDITABLE);
    }

    @Test
    public void testDeleteVoucherList_reusedVoucherNumber() {
        // mock 数据
        FmsVoucherDO firstVoucher = buildVoucher(1L, FmsVoucherStatusEnum.PENDING_REVIEW.getStatus());
        voucherMapper.insert(firstVoucher);
        voucherService.deleteVoucherList(1L, Collections.singletonList(firstVoucher.getId()), 10L);
        FmsVoucherDO secondVoucher = buildVoucher(1L, FmsVoucherStatusEnum.PENDING_REVIEW.getStatus());
        voucherMapper.insert(secondVoucher);

        // 调用
        voucherService.deleteVoucherList(1L, Collections.singletonList(secondVoucher.getId()), 10L);

        // 断言
        assertNull(voucherMapper.selectById(secondVoucher.getId()));
    }

    @Test
    public void testUpdateVoucher_closingGenerated() {
        // mock 数据
        FmsVoucherDO voucher = buildVoucher(1L, FmsVoucherStatusEnum.PENDING_REVIEW.getStatus());
        voucherMapper.insert(voucher);
        when(closingVoucherService.getClosingVoucherIdSet(
                1L, Collections.singletonList(voucher.getId())))
                .thenReturn(Collections.singleton(voucher.getId()));
        // 准备参数
        FmsVoucherSaveReqVO reqVO = buildSaveReqVO("100.00", "100.00");
        reqVO.setId(voucher.getId());

        // 调用，并断言
        assertServiceException(() -> voucherService.updateVoucher(reqVO, 10L),
                VOUCHER_CLOSING_GENERATED_NOT_EDITABLE);
    }

    @Test
    public void testUpdateVoucher_attachmentCount() {
        // mock 数据
        FmsVoucherDO voucher = buildVoucher(1L, FmsVoucherStatusEnum.PENDING_REVIEW.getStatus())
                .setAttachmentUrls(Collections.singletonList("/admin-api/infra/file/4/get"))
                .setAttachmentCount(1);
        voucherMapper.insert(voucher);
        // 准备参数
        FmsVoucherSaveReqVO reqVO = buildSaveReqVO("100.00", "100.00");
        reqVO.setId(voucher.getId());
        reqVO.setAttachmentCount(3);

        // 调用
        voucherService.updateVoucher(reqVO, 10L);

        // 断言
        FmsVoucherDO actual = voucherMapper.selectById(voucher.getId());
        assertEquals(3, actual.getAttachmentCount());
        assertEquals(Collections.singletonList("/admin-api/infra/file/4/get"), actual.getAttachmentUrls());
    }

    @Test
    public void testUpdateVoucher_attachmentCountZero() {
        // mock 数据
        FmsVoucherDO voucher = buildVoucher(1L, FmsVoucherStatusEnum.PENDING_REVIEW.getStatus())
                .setAttachmentUrls(Collections.singletonList("/admin-api/infra/file/4/get"))
                .setAttachmentCount(1);
        voucherMapper.insert(voucher);
        // 准备参数
        FmsVoucherSaveReqVO reqVO = buildSaveReqVO("100.00", "100.00");
        reqVO.setId(voucher.getId());

        // 调用
        voucherService.updateVoucher(reqVO, 10L);

        // 断言
        FmsVoucherDO actual = voucherMapper.selectById(voucher.getId());
        assertEquals(0, actual.getAttachmentCount());
        assertEquals(Collections.singletonList("/admin-api/infra/file/4/get"), actual.getAttachmentUrls());
    }

    @Test
    public void testUpdateVoucher_diffEntries() {
        // mock 数据
        FmsVoucherDO voucher = buildVoucher(1L, FmsVoucherStatusEnum.PENDING_REVIEW.getStatus());
        voucherMapper.insert(voucher);
        FmsVoucherEntryDO debitEntry = new FmsVoucherEntryDO().setAccountSetId(1L).setVoucherId(voucher.getId())
                .setDigest("原借方摘要").setSubjectId(101L).setSubjectCode("1001").setSubjectName("库存现金")
                .setQuantity(BigDecimal.ZERO).setUnitPrice(BigDecimal.ZERO)
                .setDebitAmount(new BigDecimal("100.00")).setCreditAmount(BigDecimal.ZERO)
                .setSort(1).setAuxiliaries(Collections.emptyList());
        FmsVoucherEntryDO creditEntry = new FmsVoucherEntryDO().setAccountSetId(1L).setVoucherId(voucher.getId())
                .setDigest("原贷方摘要").setSubjectId(201L).setSubjectCode("2202").setSubjectName("应付账款")
                .setQuantity(BigDecimal.ZERO).setUnitPrice(BigDecimal.ZERO)
                .setDebitAmount(BigDecimal.ZERO).setCreditAmount(new BigDecimal("100.00"))
                .setSort(2).setAuxiliaries(Collections.emptyList());
        voucherEntryMapper.insertBatch(Arrays.asList(debitEntry, creditEntry));
        // 准备参数
        FmsVoucherSaveReqVO reqVO = buildSaveReqVO("100.00", "100.00");
        reqVO.setId(voucher.getId());
        reqVO.getEntries().get(0).setId(debitEntry.getId());
        reqVO.getEntries().get(0).setDigest("新借方摘要");
        reqVO.getEntries().get(1).setId(creditEntry.getId());

        // 调用
        voucherService.updateVoucher(reqVO, 10L);

        // 断言
        List<FmsVoucherEntryDO> entries = voucherEntryMapper.selectListByVoucherIds(
                Collections.singletonList(voucher.getId()));
        assertEquals(Arrays.asList(debitEntry.getId(), creditEntry.getId()),
                Arrays.asList(entries.get(0).getId(), entries.get(1).getId()));
        assertEquals("新借方摘要", entries.get(0).getDigest());
    }

    @Test
    public void testUpdateVoucherAttachments_success() {
        // mock 数据
        FmsVoucherDO voucher = buildVoucher(1L, FmsVoucherStatusEnum.PENDING_REVIEW.getStatus())
                .setAttachmentCount(3);
        voucherMapper.insert(voucher);
        // 准备参数
        FmsVoucherAttachmentUpdateReqVO reqVO = new FmsVoucherAttachmentUpdateReqVO();
        reqVO.setId(voucher.getId());
        reqVO.setAccountSetId(1L);
        reqVO.setAttachmentUrls(Arrays.asList(
                "/admin-api/infra/file/4/get", "/admin-api/infra/file/5/get"));

        // 调用
        voucherService.updateVoucherAttachments(reqVO, 10L);

        // 断言
        FmsVoucherDO actual = voucherMapper.selectById(voucher.getId());
        assertEquals(Arrays.asList("/admin-api/infra/file/4/get", "/admin-api/infra/file/5/get"),
                actual.getAttachmentUrls());
        assertEquals(3, actual.getAttachmentCount());
    }

    @Test
    public void testUpdateVoucherAttachments_accountSetAccessDenied() {
        // 准备参数
        FmsVoucherAttachmentUpdateReqVO reqVO = new FmsVoucherAttachmentUpdateReqVO();
        reqVO.setId(1L);
        reqVO.setAccountSetId(1L);
        reqVO.setAttachmentUrls(Collections.singletonList("/admin-api/infra/file/4/get"));
        // mock 方法
        doThrow(exception(ACCOUNT_SET_ACCESS_DENIED)).when(accountSetService)
                .validateAccountSetWritePermission(1L, 10L);

        // 调用，并断言
        assertServiceException(() -> voucherService.updateVoucherAttachments(reqVO, 10L),
                ACCOUNT_SET_ACCESS_DENIED);
    }

    @Test
    public void testUpdateVoucherAttachments_approved() {
        // mock 数据
        FmsVoucherDO voucher = buildVoucher(1L, FmsVoucherStatusEnum.APPROVED.getStatus());
        voucherMapper.insert(voucher);
        // 准备参数
        FmsVoucherAttachmentUpdateReqVO reqVO = new FmsVoucherAttachmentUpdateReqVO();
        reqVO.setId(voucher.getId());
        reqVO.setAccountSetId(1L);
        reqVO.setAttachmentUrls(Collections.singletonList("/admin-api/infra/file/4/get"));

        // 调用，并断言
        assertServiceException(() -> voucherService.updateVoucherAttachments(reqVO, 10L),
                VOUCHER_APPROVED_NOT_EDITABLE);
    }

    @Test
    public void testDeleteVoucherList_closingGenerated() {
        // mock 数据
        FmsVoucherDO voucher = buildVoucher(1L, FmsVoucherStatusEnum.PENDING_REVIEW.getStatus());
        voucherMapper.insert(voucher);
        when(closingVoucherService.getClosingVoucherIdSet(
                1L, Collections.singletonList(voucher.getId())))
                .thenReturn(Collections.singleton(voucher.getId()));

        // 调用，并断言
        assertServiceException(() -> voucherService.deleteVoucherList(
                1L, Collections.singletonList(voucher.getId()), 10L),
                VOUCHER_CLOSING_GENERATED_NOT_EDITABLE);
    }

    @Test
    public void testUpdateVoucherReviewStatus_closingGenerated() {
        // mock 数据
        FmsVoucherDO voucher = buildVoucher(1L, FmsVoucherStatusEnum.PENDING_REVIEW.getStatus());
        voucherMapper.insert(voucher);
        when(closingVoucherService.getClosingVoucherIdSet(
                1L, Collections.singletonList(voucher.getId())))
                .thenReturn(Collections.singleton(voucher.getId()));

        // 调用，并断言
        assertServiceException(() -> voucherService.updateVoucherReviewStatus(
                1L, Collections.singletonList(voucher.getId()),
                FmsVoucherStatusEnum.APPROVED.getStatus(), 10L),
                VOUCHER_CLOSING_GENERATED_NOT_EDITABLE);
    }

    @Test
    public void testUpdateVoucherReviewStatus_success() {
        // mock 数据
        FmsVoucherDO voucher = buildVoucher(1L, FmsVoucherStatusEnum.PENDING_REVIEW.getStatus());
        voucherMapper.insert(voucher);

        // 调用，并断言
        voucherService.updateVoucherReviewStatus(1L, Collections.singletonList(voucher.getId()),
                FmsVoucherStatusEnum.APPROVED.getStatus(), 10L);
        assertEquals(10L, voucherMapper.selectById(voucher.getId()).getReviewerUserId());
        voucherService.updateVoucherReviewStatus(1L, Collections.singletonList(voucher.getId()),
                FmsVoucherStatusEnum.PENDING_REVIEW.getStatus(), 10L);
        assertNull(voucherMapper.selectById(voucher.getId()).getReviewerUserId());
    }

    @Test
    public void testTidyVoucher_fillGaps() {
        // mock 数据
        FmsVoucherDO first = buildVoucher(1L, FmsVoucherStatusEnum.PENDING_REVIEW.getStatus())
                .setVoucherNumber(1).setVoucherTime(LocalDateTime.of(2026, 8, 1, 0, 0));
        FmsVoucherDO second = buildVoucher(1L, FmsVoucherStatusEnum.PENDING_REVIEW.getStatus())
                .setVoucherNumber(3).setVoucherTime(LocalDateTime.of(2026, 8, 2, 0, 0));
        FmsVoucherDO third = buildVoucher(1L, FmsVoucherStatusEnum.APPROVED.getStatus())
                .setVoucherNumber(5).setVoucherTime(LocalDateTime.of(2026, 8, 3, 0, 0))
                .setReviewerUserId(99L);
        voucherMapper.insert(first);
        voucherMapper.insert(second);
        voucherMapper.insert(third);
        // 准备参数
        FmsVoucherTidyReqVO reqVO = buildTidyReqVO(
                11L, 2, FmsVoucherTidyTypeEnum.FILL_GAPS.getType());

        // 调用
        voucherService.tidyVoucher(reqVO, 10L);

        // 断言
        assertEquals(1, voucherMapper.selectById(first.getId()).getVoucherNumber());
        assertEquals(2, voucherMapper.selectById(second.getId()).getVoucherNumber());
        assertEquals(3, voucherMapper.selectById(third.getId()).getVoucherNumber());
        assertEquals(99L, voucherMapper.selectById(third.getId()).getReviewerUserId());
    }

    @Test
    public void testTidyVoucher_reorderByTime() {
        // mock 数据
        FmsVoucherDO first = buildVoucher(1L, FmsVoucherStatusEnum.PENDING_REVIEW.getStatus())
                .setVoucherNumber(1).setVoucherTime(LocalDateTime.of(2026, 8, 3, 0, 0));
        FmsVoucherDO second = buildVoucher(1L, FmsVoucherStatusEnum.PENDING_REVIEW.getStatus())
                .setVoucherNumber(2).setVoucherTime(LocalDateTime.of(2026, 8, 1, 0, 0));
        FmsVoucherDO third = buildVoucher(1L, FmsVoucherStatusEnum.PENDING_REVIEW.getStatus())
                .setVoucherNumber(3).setVoucherTime(LocalDateTime.of(2026, 8, 2, 0, 0));
        voucherMapper.insert(first);
        voucherMapper.insert(second);
        voucherMapper.insert(third);
        // 准备参数
        FmsVoucherTidyReqVO reqVO = buildTidyReqVO(
                11L, 1, FmsVoucherTidyTypeEnum.REORDER_BY_TIME.getType());

        // 调用
        voucherService.tidyVoucher(reqVO, 10L);

        // 断言
        assertEquals(3, voucherMapper.selectById(first.getId()).getVoucherNumber());
        assertEquals(1, voucherMapper.selectById(second.getId()).getVoucherNumber());
        assertEquals(2, voucherMapper.selectById(third.getId()).getVoucherNumber());
    }

    @Test
    public void testTidyVoucher_allWordsIndependent() {
        // mock 数据
        FmsVoucherDO firstWordVoucher = buildVoucher(1L, FmsVoucherStatusEnum.PENDING_REVIEW.getStatus())
                .setVoucherWordId(11L).setVoucherNumber(4);
        FmsVoucherDO secondWordVoucher = buildVoucher(1L, FmsVoucherStatusEnum.PENDING_REVIEW.getStatus())
                .setVoucherWordId(12L).setVoucherNumber(7);
        voucherMapper.insert(firstWordVoucher);
        voucherMapper.insert(secondWordVoucher);
        // 准备参数
        FmsVoucherTidyReqVO reqVO = buildTidyReqVO(
                null, 2, FmsVoucherTidyTypeEnum.FILL_GAPS.getType());

        // 调用
        voucherService.tidyVoucher(reqVO, 10L);

        // 断言
        assertEquals(2, voucherMapper.selectById(firstWordVoucher.getId()).getVoucherNumber());
        assertEquals(2, voucherMapper.selectById(secondWordVoucher.getId()).getVoucherNumber());
    }

    @Test
    public void testTidyVoucher_periodClosed() {
        // 准备参数
        FmsVoucherTidyReqVO reqVO = buildTidyReqVO(
                11L, 1, FmsVoucherTidyTypeEnum.FILL_GAPS.getType());
        doThrow(exception(CLOSING_PERIOD_CLOSED)).when(closingPeriodService)
                .validatePeriodOpen(1L, LocalDateTime.of(2026, 8, 1, 0, 0));

        // 调用，并断言
        assertServiceException(() -> voucherService.tidyVoucher(reqVO, 10L),
                CLOSING_PERIOD_CLOSED);
    }

    @Test
    public void testMoveVoucher_success() {
        // mock 数据
        FmsVoucherDO first = buildVoucher(1L, FmsVoucherStatusEnum.PENDING_REVIEW.getStatus())
                .setVoucherNumber(1);
        FmsVoucherDO second = buildVoucher(1L, FmsVoucherStatusEnum.APPROVED.getStatus())
                .setVoucherNumber(2).setReviewerUserId(99L);
        FmsVoucherDO third = buildVoucher(1L, FmsVoucherStatusEnum.PENDING_REVIEW.getStatus())
                .setVoucherNumber(3);
        FmsVoucherDO fourth = buildVoucher(1L, FmsVoucherStatusEnum.PENDING_REVIEW.getStatus())
                .setVoucherNumber(4);
        FmsVoucherDO fifth = buildVoucher(1L, FmsVoucherStatusEnum.PENDING_REVIEW.getStatus())
                .setVoucherNumber(5);
        voucherMapper.insert(first);
        voucherMapper.insert(second);
        voucherMapper.insert(third);
        voucherMapper.insert(fourth);
        voucherMapper.insert(fifth);
        // 准备参数
        FmsVoucherMoveReqVO reqVO = buildMoveReqVO(5, 2);

        // 调用
        voucherService.moveVoucher(reqVO, 10L);

        // 断言
        assertEquals(1, voucherMapper.selectById(first.getId()).getVoucherNumber());
        assertEquals(3, voucherMapper.selectById(second.getId()).getVoucherNumber());
        assertEquals(99L, voucherMapper.selectById(second.getId()).getReviewerUserId());
        assertEquals(4, voucherMapper.selectById(third.getId()).getVoucherNumber());
        assertEquals(5, voucherMapper.selectById(fourth.getId()).getVoucherNumber());
        assertEquals(2, voucherMapper.selectById(fifth.getId()).getVoucherNumber());
    }

    @Test
    public void testMoveVoucher_rangeInvalid() {
        // 准备参数
        FmsVoucherMoveReqVO reqVO = buildMoveReqVO(2, 2);

        // 调用，并断言
        assertServiceException(() -> voucherService.moveVoucher(reqVO, 10L),
                VOUCHER_MOVE_RANGE_INVALID);
    }

    @Test
    public void testMoveVoucher_sourceNotExists() {
        // mock 数据
        voucherMapper.insert(buildVoucher(1L, FmsVoucherStatusEnum.PENDING_REVIEW.getStatus())
                .setVoucherNumber(2));
        // 准备参数
        FmsVoucherMoveReqVO reqVO = buildMoveReqVO(5, 2);

        // 调用，并断言
        assertServiceException(() -> voucherService.moveVoucher(reqVO, 10L),
                VOUCHER_MOVE_SOURCE_NOT_EXISTS);
    }

    @Test
    public void testMoveVoucher_periodClosed() {
        // 准备参数
        FmsVoucherMoveReqVO reqVO = buildMoveReqVO(5, 2);
        doThrow(exception(CLOSING_PERIOD_CLOSED)).when(closingPeriodService)
                .validatePeriodOpen(1L, LocalDateTime.of(2026, 8, 1, 0, 0));

        // 调用，并断言
        assertServiceException(() -> voucherService.moveVoucher(reqVO, 10L),
                CLOSING_PERIOD_CLOSED);
    }

    @Test
    public void testGetVoucherPage_digest() {
        // mock 数据
        FmsVoucherDO first = buildVoucher(1L, FmsVoucherStatusEnum.PENDING_REVIEW.getStatus());
        first.setVoucherNumber(1);
        voucherMapper.insert(first);
        FmsVoucherDO second = buildVoucher(1L, FmsVoucherStatusEnum.PENDING_REVIEW.getStatus());
        second.setVoucherNumber(2);
        voucherMapper.insert(second);
        voucherEntryMapper.insert(new FmsVoucherEntryDO().setVoucherId(first.getId()).setAccountSetId(1L)
                .setDigest("购买办公用品").setSubjectId(101L).setSubjectCode("1001")
                .setSubjectName("库存现金").setSort(1).setQuantity(BigDecimal.ZERO)
                .setUnitPrice(BigDecimal.ZERO).setDebitAmount(new BigDecimal("100.00"))
                .setCreditAmount(BigDecimal.ZERO).setAuxiliaries(Collections.emptyList()));
        // 准备参数
        FmsVoucherPageReqVO reqVO = new FmsVoucherPageReqVO();
        reqVO.setAccountSetId(1L);
        reqVO.setDigest("办公");

        // 调用
        PageResult<FmsVoucherDO> pageResult = voucherService.getVoucherPage(reqVO, 10L);

        // 断言
        assertEquals(1L, pageResult.getTotal());
        assertEquals(first.getId(), CollUtil.getFirst(pageResult.getList()).getId());
    }

    @Test
    public void testGetVoucherPage_parentSubject() {
        // mock 数据
        FmsVoucherDO childVoucher = buildVoucher(1L, FmsVoucherStatusEnum.PENDING_REVIEW.getStatus())
                .setVoucherNumber(1);
        voucherMapper.insert(childVoucher);
        FmsVoucherDO otherVoucher = buildVoucher(1L, FmsVoucherStatusEnum.PENDING_REVIEW.getStatus())
                .setVoucherNumber(2);
        voucherMapper.insert(otherVoucher);
        voucherEntryMapper.insert(new FmsVoucherEntryDO().setVoucherId(childVoucher.getId()).setAccountSetId(1L)
                .setDigest("子科目分录").setSubjectId(101L).setSubjectCode("100101")
                .setSubjectName("人民币现金").setSort(1).setQuantity(BigDecimal.ZERO)
                .setUnitPrice(BigDecimal.ZERO).setDebitAmount(new BigDecimal("100.00"))
                .setCreditAmount(BigDecimal.ZERO).setAuxiliaries(Collections.emptyList()));
        voucherEntryMapper.insert(new FmsVoucherEntryDO().setVoucherId(otherVoucher.getId()).setAccountSetId(1L)
                .setDigest("其他科目分录").setSubjectId(201L).setSubjectCode("2202")
                .setSubjectName("应付账款").setSort(1).setQuantity(BigDecimal.ZERO)
                .setUnitPrice(BigDecimal.ZERO).setDebitAmount(new BigDecimal("100.00"))
                .setCreditAmount(BigDecimal.ZERO).setAuxiliaries(Collections.emptyList()));
        when(subjectService.getSubjectIdListWithChildren(1L, 100L))
                .thenReturn(Arrays.asList(100L, 101L));
        // 准备参数
        FmsVoucherPageReqVO reqVO = new FmsVoucherPageReqVO();
        reqVO.setAccountSetId(1L);
        reqVO.setSubjectId(100L);

        // 调用
        PageResult<FmsVoucherDO> pageResult = voucherService.getVoucherPage(reqVO, 10L);

        // 断言
        assertEquals(1L, pageResult.getTotal());
        assertEquals(childVoucher.getId(), CollUtil.getFirst(pageResult.getList()).getId());
    }

    @Test
    public void testGetVoucherPage_entryAmount() {
        // mock 数据
        FmsVoucherDO matchingVoucher = buildVoucher(1L, FmsVoucherStatusEnum.PENDING_REVIEW.getStatus())
                .setVoucherNumber(1).setDebitAmount(new BigDecimal("250.00"))
                .setCreditAmount(new BigDecimal("250.00")).setTotal(new BigDecimal("250.00"));
        voucherMapper.insert(matchingVoucher);
        FmsVoucherDO otherVoucher = buildVoucher(1L, FmsVoucherStatusEnum.PENDING_REVIEW.getStatus())
                .setVoucherNumber(2);
        voucherMapper.insert(otherVoucher);
        voucherEntryMapper.insert(new FmsVoucherEntryDO().setVoucherId(matchingVoucher.getId()).setAccountSetId(1L)
                .setDigest("匹配分录").setSubjectId(101L).setSubjectCode("1001")
                .setSubjectName("库存现金").setSort(1).setQuantity(BigDecimal.ZERO)
                .setUnitPrice(BigDecimal.ZERO).setDebitAmount(new BigDecimal("100.00"))
                .setCreditAmount(BigDecimal.ZERO).setAuxiliaries(Collections.emptyList()));
        voucherEntryMapper.insert(new FmsVoucherEntryDO().setVoucherId(otherVoucher.getId()).setAccountSetId(1L)
                .setDigest("不匹配分录").setSubjectId(101L).setSubjectCode("1001")
                .setSubjectName("库存现金").setSort(1).setQuantity(BigDecimal.ZERO)
                .setUnitPrice(BigDecimal.ZERO).setDebitAmount(new BigDecimal("300.00"))
                .setCreditAmount(BigDecimal.ZERO).setAuxiliaries(Collections.emptyList()));
        // 准备参数
        FmsVoucherPageReqVO reqVO = new FmsVoucherPageReqVO();
        reqVO.setAccountSetId(1L);
        reqVO.setMinAmount(new BigDecimal("90.00"));
        reqVO.setMaxAmount(new BigDecimal("110.00"));

        // 调用
        PageResult<FmsVoucherDO> pageResult = voucherService.getVoucherPage(reqVO, 10L);

        // 断言
        assertEquals(1L, pageResult.getTotal());
        assertEquals(matchingVoucher.getId(), CollUtil.getFirst(pageResult.getList()).getId());
    }

    @Test
    public void testGetVoucherPage_ids() {
        // mock 数据
        FmsVoucherDO first = buildVoucher(1L, FmsVoucherStatusEnum.PENDING_REVIEW.getStatus())
                .setVoucherNumber(1);
        FmsVoucherDO second = buildVoucher(1L, FmsVoucherStatusEnum.PENDING_REVIEW.getStatus())
                .setVoucherNumber(2);
        voucherMapper.insert(first);
        voucherMapper.insert(second);
        // 准备参数
        FmsVoucherPageReqVO reqVO = new FmsVoucherPageReqVO();
        reqVO.setAccountSetId(1L);
        reqVO.setIds(Collections.singletonList(second.getId()));

        // 调用
        PageResult<FmsVoucherDO> pageResult = voucherService.getVoucherPage(reqVO, 10L);

        // 断言
        assertEquals(1L, pageResult.getTotal());
        assertEquals(second.getId(), CollUtil.getFirst(pageResult.getList()).getId());
    }

    @Test
    public void testGetVoucherPage_orderByTimeAndNumberAsc() {
        // mock 数据
        FmsVoucherDO laterVoucher = buildVoucher(1L, FmsVoucherStatusEnum.PENDING_REVIEW.getStatus())
                .setVoucherNumber(1).setVoucherTime(LocalDateTime.of(2026, 8, 3, 0, 0));
        FmsVoucherDO secondVoucher = buildVoucher(1L, FmsVoucherStatusEnum.PENDING_REVIEW.getStatus())
                .setVoucherNumber(2).setVoucherTime(LocalDateTime.of(2026, 8, 2, 0, 0));
        FmsVoucherDO firstVoucher = buildVoucher(1L, FmsVoucherStatusEnum.PENDING_REVIEW.getStatus())
                .setVoucherNumber(1).setVoucherTime(LocalDateTime.of(2026, 8, 2, 0, 0));
        voucherMapper.insert(laterVoucher);
        voucherMapper.insert(secondVoucher);
        voucherMapper.insert(firstVoucher);
        // 准备参数
        FmsVoucherPageReqVO reqVO = new FmsVoucherPageReqVO().setAccountSetId(1L);

        // 调用
        PageResult<FmsVoucherDO> pageResult = voucherService.getVoucherPage(reqVO, 10L);

        // 断言
        assertEquals(Arrays.asList(firstVoucher.getId(), secondVoucher.getId(), laterVoucher.getId()),
                pageResult.getList().stream().map(FmsVoucherDO::getId).collect(Collectors.toList()));
    }

    @Test
    public void testGetVoucherStatisticsList_parentIncludesChild() {
        // mock 数据
        FmsVoucherDO voucher = buildVoucher(1L, FmsVoucherStatusEnum.PENDING_REVIEW.getStatus())
                .setVoucherWordId(11L).setVoucherNumber(1);
        voucherMapper.insert(voucher);
        voucherEntryMapper.insert(buildVoucherEntry(voucher.getId(), cashChildSubject,
                new BigDecimal("80.00"), BigDecimal.ZERO));
        voucherEntryMapper.insert(buildVoucherEntry(voucher.getId(), bankSubject,
                BigDecimal.ZERO, new BigDecimal("80.00")));
        FmsVoucherDO ignoredVoucher = buildVoucher(1L, FmsVoucherStatusEnum.PENDING_REVIEW.getStatus())
                .setVoucherWordId(12L).setVoucherNumber(2)
                .setVoucherTime(LocalDateTime.of(2026, 9, 2, 0, 0));
        voucherMapper.insert(ignoredVoucher);
        voucherEntryMapper.insert(buildVoucherEntry(ignoredVoucher.getId(), cashChildSubject,
                new BigDecimal("20.00"), BigDecimal.ZERO));
        // 准备参数
        FmsVoucherStatisticsReqVO reqVO = buildStatisticsReqVO();
        reqVO.setVoucherWordId(11L).setMinVoucherNumber(1).setMaxVoucherNumber(1);

        // 调用
        List<FmsVoucherStatisticsRespVO> result = voucherService.getVoucherStatisticsList(reqVO, 10L);

        // 断言
        assertEquals(2, result.size());
        assertEquals(cashSubject.getId(), result.get(0).getSubjectId());
        assertEquals(new BigDecimal("80.00"), result.get(0).getDebitAmount());
        assertEquals(bankSubject.getId(), result.get(1).getSubjectId());
        assertEquals(new BigDecimal("80.00"), result.get(1).getCreditAmount());
    }

    @Test
    public void testGetVoucherStatisticsList_filterLevel() {
        // mock 数据
        FmsVoucherDO voucher = buildVoucher(1L, FmsVoucherStatusEnum.PENDING_REVIEW.getStatus());
        voucherMapper.insert(voucher);
        voucherEntryMapper.insert(buildVoucherEntry(voucher.getId(), cashChildSubject,
                new BigDecimal("80.00"), BigDecimal.ZERO));
        // 准备参数
        FmsVoucherStatisticsReqVO reqVO = buildStatisticsReqVO();
        reqVO.setMinLevel(2).setMaxLevel(2);

        // 调用
        List<FmsVoucherStatisticsRespVO> result = voucherService.getVoucherStatisticsList(reqVO, 10L);

        // 断言
        assertEquals(1, result.size());
        assertEquals(cashChildSubject.getId(), result.get(0).getSubjectId());
        assertEquals(new BigDecimal("80.00"), result.get(0).getDebitAmount());
    }

    @Test
    public void testGetVoucherStatisticsList_periodInvalid() {
        // 准备参数
        FmsVoucherStatisticsReqVO reqVO = buildStatisticsReqVO();
        reqVO.setStartMonth("2026-09").setEndMonth("2026-08");

        // 调用，并断言异常
        assertServiceException(() -> voucherService.getVoucherStatisticsList(reqVO, 10L),
                LEDGER_PERIOD_INVALID);
    }

    @Test
    public void testGetVoucherStatisticsList_rangeInvalid() {
        // 准备参数
        FmsVoucherStatisticsReqVO reqVO = buildStatisticsReqVO();
        reqVO.setMinVoucherNumber(2).setMaxVoucherNumber(1);

        // 调用，并断言异常
        assertServiceException(() -> voucherService.getVoucherStatisticsList(reqVO, 10L),
                VOUCHER_STATISTICS_RANGE_INVALID);
    }

    // ========== 随机对象 ==========

    private FmsVoucherSaveReqVO buildSaveReqVO(String debitAmount, String creditAmount) {
        FmsVoucherSaveReqVO reqVO = new FmsVoucherSaveReqVO();
        reqVO.setAccountSetId(1L);
        reqVO.setVoucherWordId(11L);
        reqVO.setVoucherNumber(1);
        reqVO.setVoucherTime(LocalDateTime.of(2026, 8, 2, 0, 0));
        reqVO.setAttachmentCount(0);
        reqVO.setEntries(Arrays.asList(
                buildEntry("采购材料", bankSubject.getId(), debitAmount, null),
                buildEntry("采购材料", payableSubject.getId(), null, creditAmount)));
        return reqVO;
    }

    private FmsVoucherStatisticsReqVO buildStatisticsReqVO() {
        return new FmsVoucherStatisticsReqVO().setAccountSetId(1L)
                .setStartMonth("2026-08").setEndMonth("2026-08").setMinLevel(1).setMaxLevel(1);
    }

    private FmsVoucherImportExcelVO buildImportRow(Integer rowNumber, String subjectCode,
            String digest, String debitAmount, String creditAmount) {
        return new FmsVoucherImportExcelVO().setRowNumber(rowNumber)
                .setVoucherTime(LocalDateTime.of(2026, 8, 2, 0, 0)).setVoucherWordName("记")
                .setVoucherNumber(1).setAttachmentCount(2).setSubjectCode(subjectCode)
                .setDigest(digest)
                .setDebitAmount(debitAmount == null ? null : new BigDecimal(debitAmount))
                .setCreditAmount(creditAmount == null ? null : new BigDecimal(creditAmount));
    }

    private FmsVoucherTidyReqVO buildTidyReqVO(
            Long voucherWordId, Integer startNumber, Integer type) {
        FmsVoucherTidyReqVO reqVO = new FmsVoucherTidyReqVO();
        reqVO.setAccountSetId(1L);
        reqVO.setMonth("2026-08");
        reqVO.setVoucherWordId(voucherWordId);
        reqVO.setStartNumber(startNumber);
        reqVO.setType(type);
        return reqVO;
    }

    private FmsVoucherMoveReqVO buildMoveReqVO(Integer sourceNumber, Integer targetNumber) {
        FmsVoucherMoveReqVO reqVO = new FmsVoucherMoveReqVO();
        reqVO.setAccountSetId(1L);
        reqVO.setMonth("2026-08");
        reqVO.setVoucherWordId(11L);
        reqVO.setSourceNumber(sourceNumber);
        reqVO.setTargetNumber(targetNumber);
        return reqVO;
    }

    private FmsVoucherEntrySaveReqVO buildEntry(
            String digest, Long subjectId, String debitAmount, String creditAmount) {
        FmsVoucherEntrySaveReqVO entry = new FmsVoucherEntrySaveReqVO();
        entry.setDigest(digest);
        entry.setSubjectId(subjectId);
        entry.setDebitAmount(debitAmount == null ? null : new BigDecimal(debitAmount));
        entry.setCreditAmount(creditAmount == null ? null : new BigDecimal(creditAmount));
        entry.setAuxiliaries(Collections.emptyList());
        return entry;
    }

    private FmsSubjectDO buildSubject(Long id, String code, String name) {
        return randomPojo(FmsSubjectDO.class, subject -> subject.setId(id).setAccountSetId(1L)
                .setCode(code).setName(name).setParentId(FmsSubjectDO.PARENT_ID_ROOT).setLevel(1)
                .setStatus(CommonStatusEnum.ENABLE.getStatus()).setQuantityAccounting(false)
                .setAuxiliaryTypeIds(Collections.emptyList()).setCurrencyIds(Collections.emptyList()));
    }

    private FmsVoucherEntryDO buildVoucherEntry(Long voucherId, FmsSubjectDO subject,
            BigDecimal debitAmount, BigDecimal creditAmount) {
        return randomPojo(FmsVoucherEntryDO.class, entry -> entry.setId(null)
                .setVoucherId(voucherId).setAccountSetId(1L).setSubjectId(subject.getId())
                .setSubjectCode(subject.getCode()).setSubjectName(subject.getName()).setDigest("测试摘要")
                .setSort(1).setDebitAmount(debitAmount).setCreditAmount(creditAmount)
                .setQuantity(BigDecimal.ZERO).setUnitPrice(BigDecimal.ZERO)
                .setAssistCombinationId(null).setAuxiliaries(Collections.emptyList()));
    }

    private FmsVoucherDO buildVoucher(Long accountSetId, Integer status) {
        return new FmsVoucherDO().setAccountSetId(accountSetId).setVoucherWordId(11L)
                .setVoucherNumber(1).setVoucherTime(LocalDateTime.of(2026, 8, 2, 0, 0))
                .setAttachmentUrls(Collections.emptyList()).setDebitAmount(new BigDecimal("100.00"))
                .setCreditAmount(new BigDecimal("100.00")).setTotal(new BigDecimal("100.00")).setStatus(status);
    }

}
