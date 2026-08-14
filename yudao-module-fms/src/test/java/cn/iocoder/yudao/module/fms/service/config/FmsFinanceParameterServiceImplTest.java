package cn.iocoder.yudao.module.fms.service.config;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.accountset.FmsAccountSetInitializeReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.financeparameter.FmsFinanceParameterUpdateReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountSetDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsFinanceParameterDO;
import cn.iocoder.yudao.module.fms.dal.mysql.config.FmsFinanceParameterMapper;
import cn.iocoder.yudao.module.fms.enums.ledger.FmsLedgerBalanceModeEnum;
import cn.iocoder.yudao.module.fms.service.config.FmsAccountSetService;
import cn.iocoder.yudao.module.fms.service.config.FmsSubjectService;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.ACCOUNT_SET_SETTINGS_RULE_INVALID;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.ACCOUNT_SET_SETTINGS_RULE_SHRINK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@Import(FmsFinanceParameterServiceImpl.class)
public class FmsFinanceParameterServiceImplTest extends BaseDbUnitTest {

    @Resource
    private FmsFinanceParameterServiceImpl financeParameterService;
    @Resource
    private FmsFinanceParameterMapper financeParameterMapper;

    @MockBean
    private FmsAccountSetService accountSetService;
    @MockBean
    private FmsSubjectService subjectService;

    @Test
    public void testInitializeFinanceParameter_success() {
        // 准备参数
        Long accountSetId = randomLongId();
        FmsAccountSetInitializeReqVO reqVO = new FmsAccountSetInitializeReqVO()
                .setLevel(4).setSubjectCodeRule("4-2-2-2")
                .setLedgerBalanceMode(FmsLedgerBalanceModeEnum.SAME_AS_SUBJECT.getMode());

        // 调用
        financeParameterService.initializeFinanceParameter(accountSetId, reqVO);

        // 断言
        FmsFinanceParameterDO financeParameter = financeParameterMapper.selectByAccountSetId(accountSetId);
        assertEquals(reqVO.getLevel(), financeParameter.getLevel());
        assertEquals(reqVO.getSubjectCodeRule(), financeParameter.getSubjectCodeRule());
        assertEquals(reqVO.getLedgerBalanceMode(), financeParameter.getLedgerBalanceMode());
        assertFalse(financeParameter.getVoucherReviewRequired());
        assertEquals(FmsFinanceParameterDO.DEFAULT_DEFICIT_CHECK, financeParameter.getDeficitCheck());
        assertEquals(FmsFinanceParameterDO.DEFAULT_ASSET_PERIOD_LOCKED,
                financeParameter.getAssetPeriodLocked());
    }

    @Test
    public void testInitializeFinanceParameter_ruleInvalid() {
        // 准备参数
        FmsAccountSetInitializeReqVO reqVO = new FmsAccountSetInitializeReqVO()
                .setLevel(2).setSubjectCodeRule("4-1")
                .setLedgerBalanceMode(FmsLedgerBalanceModeEnum.SAME_AS_SUBJECT.getMode());

        // 调用，并断言异常
        assertServiceException(() -> financeParameterService.initializeFinanceParameter(
                randomLongId(), reqVO), ACCOUNT_SET_SETTINGS_RULE_INVALID);
    }

    @Test
    public void testGetFinanceParameter_notInitialized() {
        // mock 方法
        when(accountSetService.validateAccountSetReadPermission(1L, 10L))
                .thenReturn(new FmsAccountSetDO().setId(1L).setInitialized(false));

        // 调用，并断言未初始化账套没有财务参数
        assertNull(financeParameterService.getFinanceParameter(1L, 10L));
        verify(accountSetService).validateAccountSetReadPermission(1L, 10L);
    }

    @Test
    public void testConvertStandardSubjectCode() {
        // 调用，并断言
        assertEquals("40404", financeParameterService.convertStandardSubjectCode("4404", "5-3-2-2"));
        assertEquals("10001001", financeParameterService.convertStandardSubjectCode("100101", "5-3-2-2"));
    }

    @Test
    public void testUpdateFinanceParameter_success() {
        // mock 数据
        Long accountSetId = randomLongId();
        FmsFinanceParameterDO financeParameter = buildFinanceParameter(accountSetId);
        financeParameterMapper.insert(financeParameter);
        when(accountSetService.validateAccountSetWritePermission(accountSetId, 10L))
                .thenReturn(new FmsAccountSetDO().setId(accountSetId).setInitialized(true));
        // 准备参数
        FmsFinanceParameterUpdateReqVO reqVO = new FmsFinanceParameterUpdateReqVO()
                .setAccountSetId(accountSetId).setStandard(1).setLevel(5).setSubjectCodeRule("5-3-2-2-2")
                .setLedgerBalanceMode(FmsLedgerBalanceModeEnum.OPPOSITE_TO_SUBJECT.getMode())
                .setVoucherReviewRequired(false);

        // 调用
        financeParameterService.updateFinanceParameter(reqVO, 10L);

        // 断言
        FmsFinanceParameterDO dbParameter = financeParameterMapper.selectById(financeParameter.getId());
        assertEquals(reqVO.getLevel(), dbParameter.getLevel());
        assertEquals(reqVO.getSubjectCodeRule(), dbParameter.getSubjectCodeRule());
        assertEquals(FmsFinanceParameterDO.DEFAULT_DEFICIT_CHECK, dbParameter.getDeficitCheck());
        verify(subjectService).expandSubjectCodes(accountSetId,
                Arrays.asList(4, 2, 2, 2), Arrays.asList(5, 3, 2, 2, 2));
        verify(accountSetService).updateAccountSetStandard(accountSetId, reqVO.getStandard(), 10L);
    }

    @Test
    public void testUpdateFinanceParameter_ruleShrink() {
        // mock 数据
        Long accountSetId = randomLongId();
        financeParameterMapper.insert(buildFinanceParameter(accountSetId));
        when(accountSetService.validateAccountSetWritePermission(accountSetId, 10L))
                .thenReturn(new FmsAccountSetDO().setId(accountSetId).setInitialized(true));
        // 准备参数
        FmsFinanceParameterUpdateReqVO reqVO = new FmsFinanceParameterUpdateReqVO()
                .setAccountSetId(accountSetId).setStandard(1).setLevel(4).setSubjectCodeRule("3-2-2-2")
                .setLedgerBalanceMode(FmsLedgerBalanceModeEnum.SAME_AS_SUBJECT.getMode())
                .setVoucherReviewRequired(true);

        // 调用，并断言异常
        assertServiceException(() -> financeParameterService.updateFinanceParameter(reqVO, 10L),
                ACCOUNT_SET_SETTINGS_RULE_SHRINK);
        verifyNoInteractions(subjectService);
    }

    // ========== 随机对象 ==========

    private FmsFinanceParameterDO buildFinanceParameter(Long accountSetId) {
        return randomPojo(FmsFinanceParameterDO.class, financeParameter -> financeParameter.setId(null)
                .setAccountSetId(accountSetId)
                .setLevel(FmsFinanceParameterDO.DEFAULT_LEVEL)
                .setSubjectCodeRule(FmsFinanceParameterDO.DEFAULT_SUBJECT_CODE_RULE)
                .setLedgerBalanceMode(FmsFinanceParameterDO.DEFAULT_LEDGER_BALANCE_MODE)
                .setVoucherReviewRequired(FmsFinanceParameterDO.DEFAULT_VOUCHER_REVIEW_REQUIRED)
                .setDeficitCheck(FmsFinanceParameterDO.DEFAULT_DEFICIT_CHECK)
                .setAssetPeriodLocked(FmsFinanceParameterDO.DEFAULT_ASSET_PERIOD_LOCKED));
    }

}
