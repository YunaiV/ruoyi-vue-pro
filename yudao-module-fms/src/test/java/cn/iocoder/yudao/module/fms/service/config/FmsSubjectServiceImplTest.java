package cn.iocoder.yudao.module.fms.service.config;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.subject.FmsSubjectDeleteReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.subject.FmsSubjectImportExcelVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.subject.FmsSubjectImportRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.subject.FmsSubjectSaveReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.subject.FmsSubjectStatusReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.subject.FmsSubjectUsageRespVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountSetDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAuxiliaryCombinationDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAuxiliaryItemDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAuxiliaryTypeDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsCurrencyDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsFinanceParameterDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsSubjectDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsSubjectTemplateDO;
import cn.iocoder.yudao.module.fms.dal.mysql.config.FmsSubjectMapper;
import cn.iocoder.yudao.module.fms.dal.mysql.config.FmsSubjectTemplateMapper;
import cn.iocoder.yudao.module.fms.enums.common.FmsDebitCreditDirectionEnum;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.fms.enums.config.FmsSubjectTypeEnum;
import cn.iocoder.yudao.module.fms.service.closing.FmsClosingSchemeService;
import cn.iocoder.yudao.module.fms.service.closing.FmsClosingTemplateService;
import cn.iocoder.yudao.module.fms.service.config.FmsAccountSetService;
import cn.iocoder.yudao.module.fms.service.config.FmsAuxiliaryCombinationService;
import cn.iocoder.yudao.module.fms.service.config.FmsAuxiliaryTypeService;
import cn.iocoder.yudao.module.fms.service.config.FmsCurrencyService;
import cn.iocoder.yudao.module.fms.service.config.FmsInitialBalanceService;
import cn.iocoder.yudao.module.fms.service.config.FmsFinanceParameterService;
import cn.iocoder.yudao.module.fms.service.voucher.FmsVoucherService;
import cn.iocoder.yudao.module.fms.service.config.FmsVoucherTemplateService;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Import(FmsSubjectServiceImpl.class)
public class FmsSubjectServiceImplTest extends BaseDbUnitTest {

    @Resource
    private FmsSubjectServiceImpl subjectService;
    @Resource
    private FmsSubjectMapper subjectMapper;
    @Resource
    private FmsSubjectTemplateMapper subjectTemplateMapper;

    @MockBean
    private FmsAccountSetService accountSetService;
    @MockBean
    private FmsFinanceParameterService financeParameterService;
    @MockBean
    private FmsAuxiliaryTypeService auxiliaryTypeService;
    @MockBean
    private FmsAuxiliaryItemService auxiliaryItemService;
    @MockBean
    private FmsCurrencyService currencyService;
    @MockBean
    private FmsVoucherService voucherService;
    @MockBean
    private FmsVoucherTemplateService voucherTemplateService;
    @MockBean
    private FmsInitialBalanceService initialBalanceService;
    @MockBean
    private FmsAuxiliaryCombinationService auxiliaryCombinationService;
    @MockBean
    private FmsClosingSchemeService closingSchemeService;
    @MockBean
    private FmsClosingTemplateService closingTemplateService;

    @Test
    public void testInitializeDefaultSubjects_success() {
        // mock 数据
        when(financeParameterService.getFinanceParameter(10L)).thenReturn(new FmsFinanceParameterDO()
                .setSubjectCodeRule(FmsFinanceParameterDO.DEFAULT_SUBJECT_CODE_RULE));
        when(financeParameterService.convertStandardSubjectCode(anyString(), anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        FmsSubjectTemplateDO parent = buildSubjectTemplate(
                "1001", "库存现金", FmsSubjectTemplateDO.PARENT_ID_ROOT, 1);
        subjectTemplateMapper.insert(parent);
        FmsSubjectTemplateDO child = buildSubjectTemplate("100101", "人民币现金", parent.getId(), 2);
        subjectTemplateMapper.insert(child);
        // 准备参数
        Long accountSetId = 10L;

        // 调用
        subjectService.initializeDefaultSubjects(accountSetId);

        // 断言
        List<FmsSubjectDO> subjects = subjectMapper.selectListByAccountSetIdAndType(
                accountSetId, FmsSubjectTypeEnum.ASSET.getType());
        assertEquals(2, subjects.size());
        assertEquals(FmsSubjectDO.PARENT_ID_ROOT, subjects.get(0).getParentId());
        assertEquals(subjects.get(0).getId(), subjects.get(1).getParentId());
        assertEquals("100101", subjects.get(1).getCode());
        assertTrue(subjects.get(1).getAuxiliaryTypeIds().isEmpty());
    }

    @Test
    public void testCreateSubject_success() {
        // 准备参数
        Long accountSetId = 1L;
        mockAccountSetAccess(accountSetId);
        FmsSubjectSaveReqVO reqVO = buildSubjectSaveReqVO(accountSetId, "1001", "库存现金");

        // 调用
        Long subjectId = subjectService.createSubject(reqVO, 10L);

        // 断言
        FmsSubjectDO subject = subjectMapper.selectById(subjectId);
        assertEquals(accountSetId, subject.getAccountSetId());
        assertEquals(reqVO.getCode(), subject.getCode());
        assertEquals(reqVO.getName(), subject.getName());
        assertEquals(1, subject.getLevel());
        assertEquals(CommonStatusEnum.ENABLE.getStatus(), subject.getStatus());
        assertTrue(subject.getAuxiliaryTypeIds().isEmpty());
        assertTrue(subject.getCurrencyIds().isEmpty());
    }

    @Test
    public void testCreateSubject_withCurrency() {
        // mock 数据
        Long accountSetId = 1L;
        mockAccountSetAccess(accountSetId);
        FmsCurrencyDO currency = new FmsCurrencyDO().setId(20L).setAccountSetId(accountSetId)
                .setCode("USD").setName("美元").setStandard(false);
        when(currencyService.validateCurrencyList(accountSetId, Arrays.asList(currency.getId(), currency.getId())))
                .thenReturn(Collections.singletonList(currency));
        // 准备参数
        FmsSubjectSaveReqVO reqVO = buildSubjectSaveReqVO(accountSetId, "1001", "库存现金");
        reqVO.setCurrencyIds(Arrays.asList(currency.getId(), currency.getId()));

        // 调用
        Long subjectId = subjectService.createSubject(reqVO, 10L);

        // 断言
        assertEquals(Arrays.asList(currency.getId(), currency.getId()),
                subjectMapper.selectById(subjectId).getCurrencyIds());
    }

    @Test
    public void testCreateSubject_auxiliaryTypeNotExistsInAccountSet() {
        // mock 数据
        Long accountSetId = 1L;
        Long auxiliaryTypeId = 20L;
        mockAccountSetAccess(accountSetId);
        doThrow(exception(AUXILIARY_TYPE_NOT_EXISTS)).when(auxiliaryTypeService)
                .validateAuxiliaryTypeList(accountSetId, Collections.singletonList(auxiliaryTypeId));
        // 准备参数
        FmsSubjectSaveReqVO reqVO = buildSubjectSaveReqVO(accountSetId, "1001", "库存现金")
                .setAuxiliaryTypeIds(Collections.singletonList(auxiliaryTypeId));

        // 调用，并断言
        assertServiceException(() -> subjectService.createSubject(reqVO, 10L), AUXILIARY_TYPE_NOT_EXISTS);
        assertNull(subjectMapper.selectByAccountSetIdAndCode(accountSetId, reqVO.getCode()));
    }

    @Test
    public void testCreateSubject_standardCurrencyInvalid() {
        // mock 数据
        Long accountSetId = 1L;
        mockAccountSetAccess(accountSetId);
        FmsCurrencyDO currency = new FmsCurrencyDO().setId(20L).setAccountSetId(accountSetId)
                .setCode("RMB").setName("人民币").setStandard(true);
        when(currencyService.validateCurrencyList(accountSetId, Collections.singletonList(currency.getId())))
                .thenReturn(Collections.singletonList(currency));
        // 准备参数
        FmsSubjectSaveReqVO reqVO = buildSubjectSaveReqVO(accountSetId, "1001", "库存现金");
        reqVO.setCurrencyIds(Collections.singletonList(currency.getId()));

        // 调用，并断言
        assertServiceException(() -> subjectService.createSubject(reqVO, 10L), SUBJECT_CURRENCY_INVALID);
    }

    @Test
    public void testCreateSubject_codeDuplicate() {
        // mock 数据
        Long accountSetId = 1L;
        mockAccountSetAccess(accountSetId);
        subjectMapper.insert(buildSubjectDO(accountSetId, "1001", FmsSubjectDO.PARENT_ID_ROOT, 1));
        // 准备参数
        FmsSubjectSaveReqVO reqVO = buildSubjectSaveReqVO(accountSetId, "1001", "库存现金");

        // 调用，并断言
        assertServiceException(() -> subjectService.createSubject(reqVO, 10L), SUBJECT_CODE_DUPLICATE);
    }

    @Test
    public void testCreateSubject_childCodeRuleInvalid() {
        // mock 数据
        Long accountSetId = 1L;
        mockAccountSetAccess(accountSetId);
        FmsSubjectDO parent = buildSubjectDO(accountSetId, "1001", FmsSubjectDO.PARENT_ID_ROOT, 1);
        subjectMapper.insert(parent);
        // 准备参数
        FmsSubjectSaveReqVO reqVO = buildSubjectSaveReqVO(accountSetId, "10010101", "人民币现金");
        reqVO.setParentId(parent.getId());

        // 调用，并断言
        assertServiceException(() -> subjectService.createSubject(reqVO, 10L), SUBJECT_CODE_RULE_INVALID);
    }

    @Test
    public void testCreateSubject_parentCategoryInvalid() {
        // mock 数据
        Long accountSetId = 1L;
        mockAccountSetAccess(accountSetId);
        FmsSubjectDO parent = buildSubjectDO(accountSetId, "1001", FmsSubjectDO.PARENT_ID_ROOT, 1);
        subjectMapper.insert(parent);
        // 准备参数
        FmsSubjectSaveReqVO reqVO = buildSubjectSaveReqVO(accountSetId, "100101", "人民币现金")
                .setParentId(parent.getId()).setCategory(2);

        // 调用，并断言
        assertServiceException(() -> subjectService.createSubject(reqVO, 10L), SUBJECT_PARENT_INVALID);
    }

    @Test
    public void testCreateSubject_clearParentAuxiliaryTypeIds() {
        // mock 数据
        Long accountSetId = 1L;
        Long auxiliaryTypeId = 20L;
        mockAccountSetAccess(accountSetId);
        FmsSubjectDO parent = buildSubjectDO(accountSetId, "1001", FmsSubjectDO.PARENT_ID_ROOT, 1)
                .setAuxiliaryTypeIds(Collections.singletonList(auxiliaryTypeId));
        subjectMapper.insert(parent);
        // 准备参数
        FmsSubjectSaveReqVO reqVO = buildSubjectSaveReqVO(accountSetId, "100101", "人民币现金")
                .setParentId(parent.getId())
                .setAuxiliaryTypeIds(Collections.singletonList(auxiliaryTypeId));

        // 调用
        Long subjectId = subjectService.createSubject(reqVO, 10L);

        // 断言
        assertTrue(subjectMapper.selectById(parent.getId()).getAuxiliaryTypeIds().isEmpty());
        assertEquals(Collections.singletonList(auxiliaryTypeId),
                subjectMapper.selectById(subjectId).getAuxiliaryTypeIds());
    }

    @Test
    public void testCreateSubject_parentInUse() {
        // mock 数据
        Long accountSetId = 1L;
        mockAccountSetAccess(accountSetId);
        FmsSubjectDO parent = buildSubjectDO(accountSetId, "1001", FmsSubjectDO.PARENT_ID_ROOT, 1);
        subjectMapper.insert(parent);
        when(voucherService.getVoucherEntryCountBySubjectIds(
                accountSetId, Collections.singletonList(parent.getId()))).thenReturn(1L);
        // 准备参数
        FmsSubjectSaveReqVO reqVO = buildSubjectSaveReqVO(accountSetId, "100101", "人民币现金")
                .setParentId(parent.getId());

        // 调用，并断言
        assertServiceException(() -> subjectService.createSubject(reqVO, 10L), SUBJECT_PARENT_IN_USE);
    }

    @Test
    public void testCreateSubject_migrateParentDataSuccess() {
        // mock 数据
        Long accountSetId = 1L;
        mockAccountSetAccess(accountSetId);
        FmsSubjectDO parent = buildSubjectDO(accountSetId, "1001", FmsSubjectDO.PARENT_ID_ROOT, 1);
        subjectMapper.insert(parent);
        when(voucherService.getVoucherEntryCountBySubjectIds(
                accountSetId, Collections.singletonList(parent.getId()))).thenReturn(2L);
        when(initialBalanceService.getInitialBalanceCountBySubjectIds(
                accountSetId, Collections.singletonList(parent.getId()))).thenReturn(1L);
        when(auxiliaryCombinationService.getAuxiliaryCombinationCountBySubjectIds(
                accountSetId, Collections.singletonList(parent.getId()))).thenReturn(1L);
        // 准备参数
        FmsSubjectSaveReqVO reqVO = buildSubjectSaveReqVO(accountSetId, "100101", "人民币现金")
                .setParentId(parent.getId()).setMigrateParentData(true);

        // 调用
        Long subjectId = subjectService.createSubject(reqVO, 10L);

        // 断言
        FmsSubjectDO subject = subjectMapper.selectById(subjectId);
        verify(voucherService).updateVoucherEntrySubject(eq(accountSetId), eq(parent.getId()),
                argThat(target -> target.getId().equals(subjectId)
                        && target.getCode().equals(subject.getCode())
                        && target.getParentId().equals(parent.getId())));
        verify(initialBalanceService).updateInitialBalanceSubject(accountSetId, parent.getId(), subjectId);
        verify(auxiliaryCombinationService).updateAuxiliaryCombinationSubject(
                accountSetId, parent.getId(), subjectId);
    }

    @Test
    public void testCreateSubject_migrateParentDataConfigIncompatible() {
        // mock 数据
        Long accountSetId = 1L;
        mockAccountSetAccess(accountSetId);
        FmsSubjectDO parent = buildSubjectDO(accountSetId, "1001", FmsSubjectDO.PARENT_ID_ROOT, 1);
        subjectMapper.insert(parent);
        when(voucherService.getVoucherEntryCountBySubjectIds(
                accountSetId, Collections.singletonList(parent.getId()))).thenReturn(1L);
        // 准备参数
        FmsSubjectSaveReqVO reqVO = buildSubjectSaveReqVO(accountSetId, "100101", "人民币现金")
                .setParentId(parent.getId()).setMigrateParentData(true)
                .setBalanceDirection(FmsDebitCreditDirectionEnum.CREDIT.getType());

        // 调用，并断言
        assertServiceException(() -> subjectService.createSubject(reqVO, 10L),
                SUBJECT_PARENT_CONFIG_INCOMPATIBLE);
        assertNull(subjectMapper.selectByAccountSetIdAndCode(accountSetId, reqVO.getCode()));
    }

    @Test
    public void testCreateSubject_migrateParentDataRollback() {
        // mock 数据
        Long accountSetId = 1L;
        mockAccountSetAccess(accountSetId);
        FmsSubjectDO parent = buildSubjectDO(accountSetId, "1001", FmsSubjectDO.PARENT_ID_ROOT, 1);
        subjectMapper.insert(parent);
        when(voucherService.getVoucherEntryCountBySubjectIds(
                accountSetId, Collections.singletonList(parent.getId()))).thenReturn(1L);
        doThrow(new IllegalStateException("迁移失败")).when(initialBalanceService)
                .updateInitialBalanceSubject(eq(accountSetId), eq(parent.getId()), anyLong());
        // 准备参数
        FmsSubjectSaveReqVO reqVO = buildSubjectSaveReqVO(accountSetId, "100101", "人民币现金")
                .setParentId(parent.getId()).setMigrateParentData(true);

        // 调用，并断言
        assertThrows(IllegalStateException.class, () -> subjectService.createSubject(reqVO, 10L));
        assertNull(subjectMapper.selectByAccountSetIdAndCode(accountSetId, reqVO.getCode()));
    }

    @Test
    public void testUpdateSubject_usedBalanceDirectionImmutable() {
        // mock 数据
        Long accountSetId = 1L;
        mockAccountSetAccess(accountSetId);
        FmsSubjectDO subject = buildSubjectDO(accountSetId, "1001", FmsSubjectDO.PARENT_ID_ROOT, 1);
        subjectMapper.insert(subject);
        when(voucherService.getVoucherEntryCountBySubjectIds(
                accountSetId, Collections.singletonList(subject.getId()))).thenReturn(1L);
        // 准备参数
        FmsSubjectSaveReqVO reqVO = buildSubjectSaveReqVO(accountSetId, subject.getCode(), subject.getName())
                .setId(subject.getId()).setBalanceDirection(FmsDebitCreditDirectionEnum.CREDIT.getType());

        // 调用，并断言
        assertServiceException(() -> subjectService.updateSubject(reqVO, 10L),
                SUBJECT_USED_BALANCE_DIRECTION_IMMUTABLE);
        assertEquals(subject.getBalanceDirection(), subjectMapper.selectById(subject.getId()).getBalanceDirection());
    }

    @Test
    public void testUpdateSubject_usedAuxiliaryImmutable() {
        // mock 数据
        Long accountSetId = 1L;
        mockAccountSetAccess(accountSetId);
        FmsSubjectDO subject = buildSubjectDO(accountSetId, "1001", FmsSubjectDO.PARENT_ID_ROOT, 1)
                .setAuxiliaryTypeIds(Collections.singletonList(20L));
        subjectMapper.insert(subject);
        when(initialBalanceService.getInitialBalanceCountBySubjectIds(
                accountSetId, Collections.singletonList(subject.getId()))).thenReturn(1L);
        // 准备参数
        FmsSubjectSaveReqVO reqVO = buildSubjectSaveReqVO(accountSetId, subject.getCode(), subject.getName())
                .setId(subject.getId()).setAuxiliaryTypeIds(Collections.singletonList(30L));

        // 调用，并断言
        assertServiceException(() -> subjectService.updateSubject(reqVO, 10L),
                SUBJECT_USED_AUXILIARY_IMMUTABLE);
        assertEquals(Collections.singletonList(20L),
                subjectMapper.selectById(subject.getId()).getAuxiliaryTypeIds());
    }

    @Test
    public void testUpdateSubject_migrateAuxiliarySuccess() {
        // mock 数据
        Long accountSetId = 1L;
        Long typeId = 20L;
        Long itemId = 30L;
        mockAccountSetAccess(accountSetId);
        FmsSubjectDO subject = buildSubjectDO(accountSetId, "1001", FmsSubjectDO.PARENT_ID_ROOT, 1);
        subjectMapper.insert(subject);
        FmsAuxiliaryTypeDO auxiliaryType = new FmsAuxiliaryTypeDO().setId(typeId)
                .setAccountSetId(accountSetId).setType(1).setName("客户");
        FmsAuxiliaryItemDO auxiliaryItem = new FmsAuxiliaryItemDO().setId(itemId)
                .setAccountSetId(accountSetId).setAuxiliaryTypeId(typeId).setName("测试客户")
                .setStatus(CommonStatusEnum.ENABLE.getStatus());
        FmsAuxiliaryCombinationDO combination = new FmsAuxiliaryCombinationDO().setId(40L)
                .setAccountSetId(accountSetId).setSubjectId(subject.getId());
        when(voucherService.getVoucherEntryCountBySubjectIds(
                accountSetId, Collections.singletonList(subject.getId()))).thenReturn(1L);
        when(auxiliaryTypeService.validateAuxiliaryTypeList(accountSetId, Collections.singletonList(typeId)))
                .thenReturn(Collections.singletonList(auxiliaryType));
        when(auxiliaryItemService.validateAuxiliaryItemList(eq(accountSetId), anyCollection()))
                .thenReturn(Collections.singletonList(auxiliaryItem));
        when(auxiliaryCombinationService.saveAuxiliaryCombination(
                anyLong(), anyLong(), anyList())).thenReturn(combination);
        // 准备参数
        FmsSubjectSaveReqVO.AuxiliaryMapping mapping = new FmsSubjectSaveReqVO.AuxiliaryMapping()
                .setTypeId(typeId).setItemId(itemId);
        FmsSubjectSaveReqVO reqVO = buildSubjectSaveReqVO(accountSetId, subject.getCode(), subject.getName())
                .setId(subject.getId()).setAuxiliaryTypeIds(Collections.singletonList(typeId))
                .setAuxiliaryMappings(Collections.singletonList(mapping));

        // 调用
        subjectService.updateSubject(reqVO, 10L);

        // 断言
        assertEquals(Collections.singletonList(typeId),
                subjectMapper.selectById(subject.getId()).getAuxiliaryTypeIds());
        verify(voucherService).migrateVoucherEntryAuxiliaries(accountSetId, subject.getId(), combination);
    }

    @Test
    public void testUpdateSubject_migrateAuxiliaryMissingMapping() {
        // mock 数据
        Long accountSetId = 1L;
        Long typeId = 20L;
        mockAccountSetAccess(accountSetId);
        FmsSubjectDO subject = buildSubjectDO(accountSetId, "1001", FmsSubjectDO.PARENT_ID_ROOT, 1);
        subjectMapper.insert(subject);
        when(voucherService.getVoucherEntryCountBySubjectIds(
                accountSetId, Collections.singletonList(subject.getId()))).thenReturn(1L);
        when(auxiliaryTypeService.validateAuxiliaryTypeList(accountSetId, Collections.singletonList(typeId)))
                .thenReturn(Collections.singletonList(new FmsAuxiliaryTypeDO().setId(typeId)));
        // 准备参数
        FmsSubjectSaveReqVO reqVO = buildSubjectSaveReqVO(accountSetId, subject.getCode(), subject.getName())
                .setId(subject.getId()).setAuxiliaryTypeIds(Collections.singletonList(typeId));

        // 调用，并断言
        assertServiceException(() -> subjectService.updateSubject(reqVO, 10L),
                SUBJECT_AUXILIARY_MIGRATION_INVALID);
        assertTrue(subjectMapper.selectById(subject.getId()).getAuxiliaryTypeIds().isEmpty());
    }

    @Test
    public void testUpdateSubject_nonLeafConfigImmutable() {
        // mock 数据
        Long accountSetId = 1L;
        mockAccountSetAccess(accountSetId);
        FmsSubjectDO parent = buildSubjectDO(accountSetId, "1001", FmsSubjectDO.PARENT_ID_ROOT, 1);
        subjectMapper.insert(parent);
        subjectMapper.insert(buildSubjectDO(accountSetId, "100101", parent.getId(), 2));
        // 准备参数
        FmsSubjectSaveReqVO reqVO = buildSubjectSaveReqVO(accountSetId, parent.getCode(), parent.getName())
                .setId(parent.getId()).setCategory(2);

        // 调用，并断言
        assertServiceException(() -> subjectService.updateSubject(reqVO, 10L),
                SUBJECT_NON_LEAF_CONFIG_IMMUTABLE);
    }

    @Test
    public void testUpdateSubject_quantityAccountingInUse() {
        // mock 数据
        Long accountSetId = 1L;
        mockAccountSetAccess(accountSetId);
        FmsSubjectDO subject = buildSubjectDO(accountSetId, "1001", FmsSubjectDO.PARENT_ID_ROOT, 1)
                .setQuantityAccounting(true).setQuantityUnit("件");
        subjectMapper.insert(subject);
        when(voucherService.getVoucherEntryQuantityCountBySubjectIds(
                accountSetId, Collections.singletonList(subject.getId()))).thenReturn(1L);
        // 准备参数
        FmsSubjectSaveReqVO reqVO = buildSubjectSaveReqVO(accountSetId, subject.getCode(), subject.getName())
                .setId(subject.getId()).setQuantityAccounting(false);

        // 调用，并断言
        assertServiceException(() -> subjectService.updateSubject(reqVO, 10L),
                SUBJECT_QUANTITY_ACCOUNTING_IN_USE);
    }

    @Test
    public void testGetSubjectUsage() {
        // mock 数据
        Long accountSetId = 1L;
        mockAccountSetAccess(accountSetId);
        FmsSubjectDO subject = buildSubjectDO(accountSetId, "1001", FmsSubjectDO.PARENT_ID_ROOT, 1);
        subjectMapper.insert(subject);
        subjectMapper.insert(buildSubjectDO(accountSetId, "100101", subject.getId(), 2));
        when(voucherService.getVoucherEntryCountBySubjectIds(
                accountSetId, Collections.singletonList(subject.getId()))).thenReturn(2L);
        when(initialBalanceService.getInitialBalanceCountBySubjectIds(
                accountSetId, Collections.singletonList(subject.getId()))).thenReturn(1L);
        when(auxiliaryCombinationService.getAuxiliaryCombinationCountBySubjectIds(
                accountSetId, Collections.singletonList(subject.getId()))).thenReturn(3L);
        when(voucherService.getVoucherEntryQuantityCountBySubjectIds(
                accountSetId, Collections.singletonList(subject.getId()))).thenReturn(1L);
        when(initialBalanceService.getInitialBalanceQuantityCountBySubjectIds(
                accountSetId, Collections.singletonList(subject.getId()))).thenReturn(2L);

        // 调用
        FmsSubjectUsageRespVO usage = subjectService.getSubjectUsage(accountSetId, subject.getId(), 10L);

        // 断言
        assertEquals(1L, usage.getChildCount());
        assertEquals(2L, usage.getVoucherEntryCount());
        assertEquals(1L, usage.getInitialBalanceCount());
        assertEquals(3L, usage.getAuxiliaryCombinationCount());
        assertEquals(3L, usage.getQuantityDataCount());
        assertTrue(usage.getUsed());
    }

    @Test
    public void testDeleteSubjectList_hasChildren() {
        // mock 数据
        Long accountSetId = 1L;
        mockAccountSetAccess(accountSetId);
        FmsSubjectDO parent = buildSubjectDO(accountSetId, "1001", FmsSubjectDO.PARENT_ID_ROOT, 1);
        subjectMapper.insert(parent);
        subjectMapper.insert(buildSubjectDO(accountSetId, "100101", parent.getId(), 2));
        // 准备参数
        FmsSubjectDeleteReqVO deleteReqVO = new FmsSubjectDeleteReqVO()
                .setAccountSetId(accountSetId).setIds(Collections.singletonList(parent.getId()));

        // 调用，并断言
        assertServiceException(() -> subjectService.deleteSubjectList(deleteReqVO, 10L), SUBJECT_HAS_CHILDREN);
    }

    @Test
    public void testDeleteSubjectList_inUse() {
        // mock 数据
        Long accountSetId = 1L;
        mockAccountSetAccess(accountSetId);
        FmsSubjectDO subject = buildSubjectDO(accountSetId, "1001", FmsSubjectDO.PARENT_ID_ROOT, 1);
        subjectMapper.insert(subject);
        when(voucherService.getVoucherEntryCountBySubjectIds(
                accountSetId, Collections.singletonList(subject.getId()))).thenReturn(1L);
        // 准备参数
        FmsSubjectDeleteReqVO deleteReqVO = new FmsSubjectDeleteReqVO()
                .setAccountSetId(accountSetId).setIds(Collections.singletonList(subject.getId()));

        // 调用，并断言
        assertServiceException(() -> subjectService.deleteSubjectList(deleteReqVO, 10L),
                SUBJECT_VOUCHER_ENTRY_IN_USE, 1L);
        assertNotNull(subjectMapper.selectById(subject.getId()));
    }

    @Test
    public void testDeleteSubjectList_success() {
        // mock 数据
        Long accountSetId = 1L;
        mockAccountSetAccess(accountSetId);
        FmsSubjectDO subject = buildSubjectDO(accountSetId, "1001", FmsSubjectDO.PARENT_ID_ROOT, 1);
        subjectMapper.insert(subject);
        // 准备参数
        FmsSubjectDeleteReqVO deleteReqVO = new FmsSubjectDeleteReqVO()
                .setAccountSetId(accountSetId).setIds(Collections.singletonList(subject.getId()));

        // 调用
        subjectService.deleteSubjectList(deleteReqVO, 10L);

        // 断言
        assertNull(subjectMapper.selectById(subject.getId()));
    }

    @Test
    public void testDeleteSubjectList_recreatedSameCode() {
        // mock 数据
        Long accountSetId = 1L;
        mockAccountSetAccess(accountSetId);
        FmsSubjectDO firstSubject = buildSubjectDO(accountSetId, "1001", FmsSubjectDO.PARENT_ID_ROOT, 1);
        subjectMapper.insert(firstSubject);
        // 准备参数
        FmsSubjectDeleteReqVO firstDeleteReqVO = new FmsSubjectDeleteReqVO()
                .setAccountSetId(accountSetId).setIds(Collections.singletonList(firstSubject.getId()));

        // 删除后使用相同编码重新创建
        subjectService.deleteSubjectList(firstDeleteReqVO, 10L);
        FmsSubjectDO secondSubject = buildSubjectDO(accountSetId, "1001", FmsSubjectDO.PARENT_ID_ROOT, 1);
        subjectMapper.insert(secondSubject);
        FmsSubjectDeleteReqVO secondDeleteReqVO = new FmsSubjectDeleteReqVO()
                .setAccountSetId(accountSetId).setIds(Collections.singletonList(secondSubject.getId()));
        subjectService.deleteSubjectList(secondDeleteReqVO, 10L);

        // 断言
        assertNull(subjectMapper.selectById(firstSubject.getId()));
        assertNull(subjectMapper.selectById(secondSubject.getId()));
    }

    @Test
    public void testUpdateSubjectStatus_success() {
        // mock 数据
        Long accountSetId = 1L;
        mockAccountSetAccess(accountSetId);
        FmsSubjectDO first = buildSubjectDO(accountSetId, "1001", FmsSubjectDO.PARENT_ID_ROOT, 1);
        FmsSubjectDO second = buildSubjectDO(accountSetId, "1002", FmsSubjectDO.PARENT_ID_ROOT, 1);
        subjectMapper.insert(first);
        subjectMapper.insert(second);
        FmsSubjectDO child = buildSubjectDO(accountSetId, "100101", first.getId(), 2);
        subjectMapper.insert(child);
        FmsSubjectDO grandchild = buildSubjectDO(accountSetId, "10010101", child.getId(), 3);
        subjectMapper.insert(grandchild);
        // 准备参数
        FmsSubjectStatusReqVO statusReqVO = new FmsSubjectStatusReqVO().setAccountSetId(accountSetId)
                .setIds(Arrays.asList(first.getId(), second.getId()))
                .setStatus(CommonStatusEnum.DISABLE.getStatus());

        // 调用
        subjectService.updateSubjectStatus(statusReqVO, 10L);

        // 断言
        assertEquals(CommonStatusEnum.DISABLE.getStatus(),
                subjectMapper.selectById(first.getId()).getStatus());
        assertEquals(CommonStatusEnum.DISABLE.getStatus(),
                subjectMapper.selectById(second.getId()).getStatus());
        assertEquals(CommonStatusEnum.DISABLE.getStatus(),
                subjectMapper.selectById(child.getId()).getStatus());
        assertEquals(CommonStatusEnum.DISABLE.getStatus(),
                subjectMapper.selectById(grandchild.getId()).getStatus());
    }

    @Test
    public void testGetSubjectIdListWithChildren_success() {
        // mock 数据
        Long accountSetId = 1L;
        mockAccountSetAccess(accountSetId);
        FmsSubjectDO parent = buildSubjectDO(accountSetId, "1001", FmsSubjectDO.PARENT_ID_ROOT, 1);
        subjectMapper.insert(parent);
        FmsSubjectDO child = buildSubjectDO(accountSetId, "100101", parent.getId(), 2)
                .setStatus(CommonStatusEnum.DISABLE.getStatus());
        subjectMapper.insert(child);
        FmsSubjectDO grandchild = buildSubjectDO(accountSetId, "10010101", child.getId(), 3);
        subjectMapper.insert(grandchild);
        subjectMapper.insert(buildSubjectDO(accountSetId, "1002", FmsSubjectDO.PARENT_ID_ROOT, 1));

        // 调用
        List<Long> subjectIds = subjectService.getSubjectIdListWithChildren(accountSetId, parent.getId());

        // 断言
        assertEquals(Arrays.asList(parent.getId(), child.getId(), grandchild.getId()), subjectIds);
    }

    @Test
    public void testGetSubjectCountByAuxiliaryTypeId() {
        // mock 数据
        subjectMapper.insert(buildSubjectDO(1L, "1001", FmsSubjectDO.PARENT_ID_ROOT, 1)
                .setAuxiliaryTypeIds(Collections.singletonList(20L)));
        subjectMapper.insert(buildSubjectDO(2L, "1001", FmsSubjectDO.PARENT_ID_ROOT, 1)
                .setAuxiliaryTypeIds(Collections.singletonList(30L)));

        // 调用，并断言
        assertEquals(1L, subjectService.getSubjectCountByAuxiliaryTypeId(1L, 20L));
        assertEquals(0L, subjectService.getSubjectCountByAuxiliaryTypeId(1L, 30L));
        assertEquals(0L, subjectService.getSubjectCountByAuxiliaryTypeId(2L, 20L));
    }

    @Test
    public void testGetSubjectCountByCurrencyId() {
        // mock 数据
        subjectMapper.insert(buildSubjectDO(1L, "1001", FmsSubjectDO.PARENT_ID_ROOT, 1)
                .setCurrencyIds(Collections.singletonList(20L)));
        subjectMapper.insert(buildSubjectDO(2L, "1001", FmsSubjectDO.PARENT_ID_ROOT, 1)
                .setCurrencyIds(Collections.singletonList(30L)));

        // 调用，并断言
        assertEquals(1L, subjectService.getSubjectCountByCurrencyId(1L, 20L));
        assertEquals(0L, subjectService.getSubjectCountByCurrencyId(1L, 30L));
        assertEquals(0L, subjectService.getSubjectCountByCurrencyId(2L, 20L));
    }

    @Test
    public void testExpandSubjectCodes() {
        // mock 数据
        Long accountSetId = 1L;
        FmsSubjectDO levelOne = buildSubjectDO(accountSetId, "1001", FmsSubjectDO.PARENT_ID_ROOT, 1);
        subjectMapper.insert(levelOne);
        FmsSubjectDO levelTwo = buildSubjectDO(accountSetId, "100101", levelOne.getId(), 2);
        subjectMapper.insert(levelTwo);
        FmsSubjectDO levelThree = buildSubjectDO(accountSetId, "10010101", levelTwo.getId(), 3);
        subjectMapper.insert(levelThree);

        // 调用
        subjectService.expandSubjectCodes(accountSetId,
                Arrays.asList(4, 2, 2, 2), Arrays.asList(5, 3, 2, 2));

        // 断言
        assertEquals("10001", subjectMapper.selectById(levelOne.getId()).getCode());
        assertEquals("10001001", subjectMapper.selectById(levelTwo.getId()).getCode());
        assertEquals("1000100101", subjectMapper.selectById(levelThree.getId()).getCode());
    }

    @Test
    public void testImportSubjectList_success() {
        // mock 数据
        Long accountSetId = 1L;
        mockAccountSetAccess(accountSetId);
        // 准备参数
        List<FmsSubjectImportExcelVO> importSubjects = Arrays.asList(
                FmsSubjectImportExcelVO.builder().code("100101").name("人民币现金")
                        .parentSubjectCode("1001").balanceDirection("借").categoryName("流动资产").build(),
                FmsSubjectImportExcelVO.builder().code("1001").name("库存现金")
                        .parentSubjectCode("0").balanceDirection("借").categoryName("流动资产").build());

        // 调用
        FmsSubjectImportRespVO result = subjectService.importSubjectList(accountSetId, importSubjects, 10L);

        // 断言
        assertEquals(2, result.getTotalCount());
        assertEquals(Arrays.asList("1001", "100101"), result.getSuccessSubjectCodes());
        assertTrue(result.getFailureReasons().isEmpty());
        FmsSubjectDO parent = subjectMapper.selectByAccountSetIdAndCode(accountSetId, "1001");
        FmsSubjectDO child = subjectMapper.selectByAccountSetIdAndCode(accountSetId, "100101");
        assertEquals(parent.getId(), child.getParentId());
        assertEquals(2, child.getLevel());
    }

    @Test
    public void testImportSubjectList_empty() {
        // 准备参数
        Long accountSetId = 1L;

        // 调用，并断言
        assertServiceException(() -> subjectService.importSubjectList(
                accountSetId, Collections.emptyList(), 10L), SUBJECT_IMPORT_LIST_IS_EMPTY);
    }

    @Test
    public void testImportSubjectList_requiredFieldInvalid() {
        // mock 数据
        Long accountSetId = 1L;
        mockAccountSetAccess(accountSetId);
        // 准备参数
        FmsSubjectImportExcelVO importSubject = FmsSubjectImportExcelVO.builder()
                .code(" ").name("测试科目").parentSubjectCode("0")
                .balanceDirection("借").categoryName("流动资产").build();

        // 调用
        FmsSubjectImportRespVO result = subjectService.importSubjectList(
                accountSetId, Collections.singletonList(importSubject), 10L);

        // 断言
        assertTrue(result.getSuccessSubjectCodes().isEmpty());
        assertEquals(1, result.getFailureReasons().size());
        assertTrue(CollUtil.getFirst(result.getFailureReasons().values()).contains("科目编码不能为空"));
    }

    @Test
    public void testImportSubjectList_partialFailure() {
        // mock 数据
        Long accountSetId = 1L;
        mockAccountSetAccess(accountSetId);
        subjectMapper.insert(buildSubjectDO(accountSetId, "1001", FmsSubjectDO.PARENT_ID_ROOT, 1));
        // 准备参数
        List<FmsSubjectImportExcelVO> importSubjects = Arrays.asList(
                FmsSubjectImportExcelVO.builder().code("1001").name("重复科目")
                        .parentSubjectCode("0").balanceDirection("借").categoryName("流动资产").build(),
                FmsSubjectImportExcelVO.builder().code("1002").name("银行存款")
                        .parentSubjectCode("0").balanceDirection("错误方向").categoryName("流动资产").build(),
                FmsSubjectImportExcelVO.builder().code("1003").name("其他货币资金")
                        .parentSubjectCode("0").balanceDirection("借").categoryName("流动资产").build());

        // 调用
        FmsSubjectImportRespVO result = subjectService.importSubjectList(accountSetId, importSubjects, 10L);

        // 断言
        assertEquals(Arrays.asList("1001", "1003"), result.getSuccessSubjectCodes());
        assertEquals(1, result.getFailureReasons().size());
        assertTrue(result.getFailureReasons().values().contains("余额方向只能填写借或贷"));
        assertEquals("重复科目", subjectMapper.selectByAccountSetIdAndCode(accountSetId, "1001").getName());
        assertNotNull(subjectMapper.selectByAccountSetIdAndCode(accountSetId, "1003"));
    }

    // ========== 随机对象 ==========

    private FmsSubjectSaveReqVO buildSubjectSaveReqVO(Long accountSetId, String subjectCode, String name) {
        FmsSubjectSaveReqVO reqVO = new FmsSubjectSaveReqVO();
        reqVO.setAccountSetId(accountSetId).setCode(subjectCode).setName(name)
                .setParentId(FmsSubjectDO.PARENT_ID_ROOT)
                .setType(FmsSubjectTypeEnum.ASSET.getType()).setCategory(1)
                .setBalanceDirection(FmsDebitCreditDirectionEnum.DEBIT.getType())
                .setAuxiliaryTypeIds(Collections.emptyList()).setCurrencyIds(Collections.emptyList())
                .setQuantityAccounting(false).setCash(false);
        return reqVO;
    }

    private FmsSubjectDO buildSubjectDO(Long accountSetId, String subjectCode, Long parentId, int level) {
        return randomPojo(FmsSubjectDO.class, subject -> subject.setId(null)
                .setCode(subjectCode).setName("测试科目").setParentId(parentId)
                .setType(FmsSubjectTypeEnum.ASSET.getType()).setCategory(1)
                .setBalanceDirection(FmsDebitCreditDirectionEnum.DEBIT.getType())
                .setCash(false).setStatus(CommonStatusEnum.ENABLE.getStatus()).setLevel(level)
                .setQuantityAccounting(false).setAccountSetId(accountSetId)
                .setAuxiliaryTypeIds(Collections.emptyList()).setCurrencyIds(Collections.emptyList()));
    }

    private FmsSubjectTemplateDO buildSubjectTemplate(
            String subjectCode, String name, Long parentId, int level) {
        return randomPojo(FmsSubjectTemplateDO.class, subject -> subject.setId(null)
                .setCode(subjectCode).setName(name).setParentId(parentId)
                .setType(FmsSubjectTypeEnum.ASSET.getType()).setCategory(1)
                .setBalanceDirection(FmsDebitCreditDirectionEnum.DEBIT.getType())
                .setCash(true).setStatus(CommonStatusEnum.ENABLE.getStatus()).setLevel(level)
                .setQuantityAccounting(false));
    }

    private void mockAccountSetAccess(Long accountSetId) {
        FmsAccountSetDO accountSet = new FmsAccountSetDO().setId(accountSetId);
        when(accountSetService.validateAccountSetReadPermission(accountSetId, 10L))
                .thenReturn(accountSet);
        when(accountSetService.validateAccountSetWritePermission(accountSetId, 10L))
                .thenReturn(accountSet);
        when(financeParameterService.getFinanceParameter(accountSetId))
                .thenReturn(new FmsFinanceParameterDO()
                        .setLevel(FmsFinanceParameterDO.DEFAULT_LEVEL)
                        .setSubjectCodeRule(FmsFinanceParameterDO.DEFAULT_SUBJECT_CODE_RULE));
    }

}
