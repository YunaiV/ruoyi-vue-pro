package cn.iocoder.yudao.module.fms.service.config;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.accountset.FmsAccountSetInitializeReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.accountset.FmsAccountSetSaveReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountSetDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountUserDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsCurrencyDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsFinanceParameterDO;
import cn.iocoder.yudao.module.fms.dal.mysql.config.FmsAccountSetMapper;
import cn.iocoder.yudao.module.fms.enums.config.FmsAccountUserLevelEnum;
import cn.iocoder.yudao.module.fms.enums.config.FmsAccountingStandardEnum;
import cn.iocoder.yudao.module.fms.enums.config.FmsCurrencyPresetEnum;
import cn.iocoder.yudao.module.fms.enums.ledger.FmsLedgerBalanceModeEnum;
import cn.iocoder.yudao.module.fms.service.closing.FmsClosingSchemeService;
import cn.iocoder.yudao.module.fms.service.closing.FmsClosingTemplateService;
import cn.iocoder.yudao.module.fms.service.config.FmsAuxiliaryTypeService;
import cn.iocoder.yudao.module.fms.service.config.FmsCurrencyService;
import cn.iocoder.yudao.module.fms.service.config.FmsFinanceParameterService;
import cn.iocoder.yudao.module.fms.service.config.FmsSubjectService;
import cn.iocoder.yudao.module.fms.service.config.FmsVoucherWordService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.ACCOUNT_SET_ACCESS_DENIED;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.ACCOUNT_SET_ALREADY_INITIALIZED;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.ACCOUNT_SET_CODE_DUPLICATE;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.ACCOUNT_SET_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@Import(FmsAccountSetServiceImpl.class)
public class FmsAccountSetServiceImplTest extends BaseDbUnitTest {

    @Resource
    private FmsAccountSetServiceImpl accountSetService;
    @Resource
    private FmsAccountSetMapper accountSetMapper;

    @MockitoBean
    private FmsAccountUserService accountUserService;
    @MockitoBean
    private FmsCurrencyService currencyService;
    @MockitoBean
    private FmsFinanceParameterService financeParameterService;
    @MockitoBean
    private FmsVoucherWordService voucherWordService;
    @MockitoBean
    private FmsAuxiliaryTypeService auxiliaryTypeService;
    @MockitoBean
    private FmsSubjectService subjectService;
    @MockitoBean
    private FmsClosingSchemeService closingSchemeService;
    @MockitoBean
    private FmsClosingTemplateService closingTemplateService;

    @Test
    public void testCreateAccountSet_success() {
        // 准备参数
        Long userId = randomLongId();
        FmsAccountSetSaveReqVO reqVO = buildAccountSetSaveReqVO("TEST001", "测试科技有限公司");

        // 调用
        Long accountSetId = accountSetService.createAccountSet(reqVO, userId);

        // 断言
        FmsAccountSetDO accountSet = accountSetMapper.selectById(accountSetId);
        assertPojoEquals(reqVO, accountSet, "id");
        assertFalse(accountSet.getInitialized());
        verify(accountUserService).createAccountOwner(accountSetId, userId);
    }

    @Test
    public void testCreateAccountSet_codeDuplicate() {
        // mock 数据
        accountSetMapper.insert(buildAccountSetDO("WK001", false));
        // 准备参数
        FmsAccountSetSaveReqVO reqVO = buildAccountSetSaveReqVO("WK001", "芋道科技有限公司");

        // 调用，并断言异常
        assertServiceException(() -> accountSetService.createAccountSet(reqVO, randomLongId()),
                ACCOUNT_SET_CODE_DUPLICATE);
    }

    @Test
    public void testGetAccountSet_success() {
        // mock 数据
        FmsAccountSetDO accountSet = buildAccountSetDO("WK001", false);
        accountSetMapper.insert(accountSet);

        // 调用
        FmsAccountSetDO result = accountSetService.getAccountSet(accountSet.getId());

        // 断言
        assertPojoEquals(accountSet, result, "deleted");
        verifyNoInteractions(accountUserService);
    }

    @Test
    public void testValidateAccountSetReadPermission_notMemberDenied() {
        // mock 数据
        FmsAccountSetDO accountSet = buildAccountSetDO("WK001", false);
        accountSetMapper.insert(accountSet);
        Long userId = randomLongId();
        // 调用，并断言异常
        assertServiceException(() -> accountSetService.validateAccountSetReadPermission(accountSet.getId(), userId),
                ACCOUNT_SET_ACCESS_DENIED);
    }

    @Test
    public void testValidateAccountSetReadPermission_invalidLevelDenied() {
        // mock 数据
        FmsAccountSetDO accountSet = buildAccountSetDO("WK001", false);
        accountSetMapper.insert(accountSet);
        Long userId = randomLongId();
        when(accountUserService.getAccountUser(accountSet.getId(), userId))
                .thenReturn(new FmsAccountUserDO().setLevel(99));

        // 调用，并断言异常
        assertServiceException(() -> accountSetService.validateAccountSetReadPermission(accountSet.getId(), userId),
                ACCOUNT_SET_ACCESS_DENIED);
    }

    @Test
    public void testValidateAccountSetWritePermission_readDenied() {
        // mock 数据
        FmsAccountSetDO accountSet = buildAccountSetDO("WK001", false);
        accountSetMapper.insert(accountSet);
        Long userId = randomLongId();
        when(accountUserService.getAccountUser(accountSet.getId(), userId)).thenReturn(new FmsAccountUserDO()
                .setLevel(FmsAccountUserLevelEnum.READ.getLevel()));

        // 调用，并断言异常
        assertServiceException(() -> accountSetService.validateAccountSetWritePermission(accountSet.getId(), userId),
                ACCOUNT_SET_ACCESS_DENIED);
    }

    @Test
    public void testValidateAccountSetWritePermission_writeSuccess() {
        // mock 数据
        FmsAccountSetDO accountSet = buildAccountSetDO("WK001", false);
        accountSetMapper.insert(accountSet);
        Long userId = randomLongId();
        when(accountUserService.getAccountUser(accountSet.getId(), userId)).thenReturn(new FmsAccountUserDO()
                .setLevel(FmsAccountUserLevelEnum.WRITE.getLevel()));

        // 调用
        FmsAccountSetDO result = accountSetService.validateAccountSetWritePermission(accountSet.getId(), userId);

        // 断言
        assertEquals(accountSet.getId(), result.getId());
    }

    @Test
    public void testValidateAccountSetOwnerPermission_writeDenied() {
        // mock 数据
        FmsAccountSetDO accountSet = buildAccountSetDO("WK001", false);
        accountSetMapper.insert(accountSet);
        Long userId = randomLongId();
        when(accountUserService.getAccountUser(accountSet.getId(), userId)).thenReturn(new FmsAccountUserDO()
                .setLevel(FmsAccountUserLevelEnum.WRITE.getLevel()));

        // 调用，并断言异常
        assertServiceException(() -> accountSetService.validateAccountSetOwnerPermission(accountSet.getId(), userId),
                ACCOUNT_SET_ACCESS_DENIED);
    }

    @Test
    public void testGetAccountSetList() {
        // mock 数据
        FmsAccountSetDO firstAccountSet = buildAccountSetDO("WK001", true);
        accountSetMapper.insert(firstAccountSet);
        FmsAccountSetDO secondAccountSet = buildAccountSetDO("WK002", false);
        accountSetMapper.insert(secondAccountSet);

        // 调用
        List<FmsAccountSetDO> accountSets = accountSetService.getAccountSetList(
                Arrays.asList(firstAccountSet.getId(), secondAccountSet.getId()));

        // 断言
        assertEquals(2, accountSets.size());
    }

    @Test
    public void testLockAccountSet_success() {
        // mock 数据
        FmsAccountSetDO accountSet = buildAccountSetDO("WK001", true);
        accountSetMapper.insert(accountSet);

        // 调用
        accountSetService.lockAccountSet(accountSet.getId());

        // 断言
        assertEquals(accountSet.getId(), accountSetMapper.selectById(accountSet.getId()).getId());
    }

    @Test
    public void testLockAccountSet_notExists() {
        // 调用，并断言异常
        assertServiceException(() -> accountSetService.lockAccountSet(randomLongId()),
                ACCOUNT_SET_NOT_EXISTS);
    }

    @Test
    public void testInitializeAccountSet_success() {
        // mock 数据
        Long userId = randomLongId();
        FmsAccountSetDO accountSet = buildAccountSetDO("WK001", false);
        accountSetMapper.insert(accountSet);
        when(accountUserService.getAccountUser(accountSet.getId(), userId)).thenReturn(new FmsAccountUserDO()
                .setLevel(FmsAccountUserLevelEnum.WRITE.getLevel()));
        when(currencyService.initializeStandardCurrency(accountSet.getId(), FmsCurrencyPresetEnum.RMB))
                .thenReturn(new FmsCurrencyDO().setId(100L));
        // 准备参数
        FmsAccountSetInitializeReqVO reqVO = buildInitializeReqVO(accountSet.getId());

        // 调用
        accountSetService.initializeAccountSet(reqVO, userId);

        // 断言
        FmsAccountSetDO dbAccountSet = accountSetMapper.selectById(accountSet.getId());
        assertTrue(dbAccountSet.getInitialized());
        assertEquals(LocalDateTime.of(2026, 8, 1, 0, 0), dbAccountSet.getStartTime());
        assertEquals(reqVO.getStandard(), dbAccountSet.getStandard());
        assertEquals(100L, dbAccountSet.getCurrencyId());
        verify(accountUserService).getAccountUser(accountSet.getId(), userId);
        verify(financeParameterService).initializeFinanceParameter(accountSet.getId(), reqVO);
        verify(voucherWordService).initializeDefaultVoucherWords(accountSet.getId());
        verify(auxiliaryTypeService).initializeDefaultTypes(accountSet.getId());
        verify(subjectService).initializeDefaultSubjects(accountSet.getId());
        verify(closingTemplateService).initializeClosingTemplates(accountSet.getId(), userId);
        verify(closingSchemeService).initializeDefaultClosingSchemes(accountSet.getId(), userId);
    }

    @Test
    public void testInitializeAccountSet_alreadyInitialized() {
        // mock 数据
        Long userId = randomLongId();
        FmsAccountSetDO accountSet = buildAccountSetDO("WK001", true);
        accountSetMapper.insert(accountSet);
        when(accountUserService.getAccountUser(accountSet.getId(), userId)).thenReturn(new FmsAccountUserDO()
                .setLevel(FmsAccountUserLevelEnum.OWNER.getLevel()));
        // 准备参数
        FmsAccountSetInitializeReqVO reqVO = buildInitializeReqVO(accountSet.getId());

        // 调用，并断言异常
        assertServiceException(() -> accountSetService.initializeAccountSet(reqVO, userId),
                ACCOUNT_SET_ALREADY_INITIALIZED);
        verifyNoInteractions(currencyService, financeParameterService,
                voucherWordService, auxiliaryTypeService, subjectService,
                closingTemplateService, closingSchemeService);
    }

    // ========== 随机对象 ==========

    private FmsAccountSetInitializeReqVO buildInitializeReqVO(Long accountSetId) {
        return new FmsAccountSetInitializeReqVO().setAccountSetId(accountSetId)
                .setCurrencyCode(FmsCurrencyPresetEnum.RMB.getCode())
                .setStartTime(LocalDateTime.of(2026, 8, 23, 12, 30))
                .setStandard(FmsAccountingStandardEnum.SMALL_BUSINESS_2013.getStandard())
                .setLevel(FmsFinanceParameterDO.DEFAULT_LEVEL)
                .setSubjectCodeRule(FmsFinanceParameterDO.DEFAULT_SUBJECT_CODE_RULE)
                .setLedgerBalanceMode(FmsLedgerBalanceModeEnum.SAME_AS_SUBJECT.getMode());
    }

    private FmsAccountSetSaveReqVO buildAccountSetSaveReqVO(String code, String companyName) {
        return randomPojo(FmsAccountSetSaveReqVO.class, reqVO -> reqVO
                .setCompanyCode(code).setCompanyName(companyName)
                .setCompanyProfile("企业数字化服务").setIndustry("软件和信息技术服务业")
                .setContactName("张三").setMobile("15601691399").setEmail("finance@example.com"));
    }

    private FmsAccountSetDO buildAccountSetDO(String code, Boolean initialized) {
        return randomPojo(FmsAccountSetDO.class, accountSet -> accountSet.setId(null)
                .setCompanyCode(code).setCompanyName(code + " 有限公司").setInitialized(initialized));
    }

}
