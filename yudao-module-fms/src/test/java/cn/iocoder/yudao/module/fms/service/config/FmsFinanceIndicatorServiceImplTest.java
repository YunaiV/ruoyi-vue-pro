package cn.iocoder.yudao.module.fms.service.config;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.financeindicator.FmsFinanceIndicatorSaveReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsFinanceIndicatorDO;
import cn.iocoder.yudao.module.fms.dal.mysql.config.FmsFinanceIndicatorMapper;
import cn.iocoder.yudao.module.fms.service.report.FmsReportCommonService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Collections;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.HOME_METRIC_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link FmsFinanceIndicatorServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
public class FmsFinanceIndicatorServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private FmsFinanceIndicatorServiceImpl financeIndicatorService;

    @Mock
    private FmsFinanceIndicatorMapper financeIndicatorMapper;
    @Mock
    private FmsAccountSetService accountSetService;
    @Mock
    private FmsSubjectService subjectService;
    @Mock
    private FmsReportCommonService reportCommonService;

    @ParameterizedTest
    @ValueSource(strings = {"L1", "+L01-L2+L3", "-L0", " L1 + L2 "})
    public void testCreateFinanceIndicator_lineFormula(String formula) {
        // 准备参数
        FmsFinanceIndicatorSaveReqVO reqVO = new FmsFinanceIndicatorSaveReqVO()
                .setAccountSetId(1L).setCode("synthetic").setFormula(formula);
        // mock subjectService 的方法
        when(subjectService.getSubjectList(1L, null, 10L)).thenReturn(Collections.emptyList());
        // mock reportCommonService 的方法
        when(reportCommonService.isLineFormula(anyString())).thenReturn(true);

        // 调用
        financeIndicatorService.createFinanceIndicator(reqVO, 10L);
        // 断言
        ArgumentCaptor<FmsFinanceIndicatorDO> indicatorCaptor = ArgumentCaptor.forClass(FmsFinanceIndicatorDO.class);
        verify(financeIndicatorMapper).insert(indicatorCaptor.capture());
        assertEquals(Collections.singletonList(formula.replace(" ", "")),
                JsonUtils.parseArray(indicatorCaptor.getValue().getFormula(), String.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"L", "L1+", "L1++L2", "L1.L2", "l1", "L1\n", "+", "L١"})
    public void testCreateFinanceIndicator_invalidLineFormula(String formula) {
        // 准备参数
        FmsFinanceIndicatorSaveReqVO reqVO = new FmsFinanceIndicatorSaveReqVO()
                .setAccountSetId(1L).setCode("synthetic").setFormula(formula);

        // 调用，并断言异常
        assertServiceException(() -> financeIndicatorService.createFinanceIndicator(reqVO, 10L), HOME_METRIC_INVALID);
        verifyNoInteractions(financeIndicatorMapper, subjectService, reportCommonService);
    }

    @Test
    public void testCreateFinanceIndicator_longLineFormula() {
        // 准备参数：模拟绕过 HTTP DTO 限长的内部调用，分段数量不应造成正则递归栈溢出
        String formula = "L1+".repeat(10_000) + "L2";
        FmsFinanceIndicatorSaveReqVO reqVO = new FmsFinanceIndicatorSaveReqVO()
                .setAccountSetId(1L).setCode("synthetic").setFormula(formula);
        // mock subjectService 的方法
        when(subjectService.getSubjectList(1L, null, 10L)).thenReturn(Collections.emptyList());
        // mock reportCommonService 的方法
        when(reportCommonService.isLineFormula(anyString())).thenReturn(true);

        // 调用
        financeIndicatorService.createFinanceIndicator(reqVO, 10L);
        // 断言
        ArgumentCaptor<FmsFinanceIndicatorDO> indicatorCaptor = ArgumentCaptor.forClass(FmsFinanceIndicatorDO.class);
        verify(financeIndicatorMapper).insert(indicatorCaptor.capture());
        assertEquals(Collections.singletonList(formula),
                JsonUtils.parseArray(indicatorCaptor.getValue().getFormula(), String.class));
    }

    @Test
    public void testCreateFinanceIndicator_longInvalidLineFormula() {
        // 准备参数
        String formula = "L1+".repeat(10_000) + "L";
        FmsFinanceIndicatorSaveReqVO reqVO = new FmsFinanceIndicatorSaveReqVO()
                .setAccountSetId(1L).setCode("synthetic").setFormula(formula);

        // 调用，并断言异常
        assertServiceException(() -> financeIndicatorService.createFinanceIndicator(reqVO, 10L), HOME_METRIC_INVALID);
        verifyNoInteractions(financeIndicatorMapper, subjectService, reportCommonService);
    }

    @Test
    public void testCreateFinanceIndicator_subjectJsonFormula() {
        // 准备参数
        String formula = "[\"synthetic-subject\"]";
        FmsFinanceIndicatorSaveReqVO reqVO = new FmsFinanceIndicatorSaveReqVO()
                .setAccountSetId(1L).setCode("synthetic").setFormula(formula);
        // mock subjectService 的方法
        when(subjectService.getSubjectList(1L, null, 10L)).thenReturn(Collections.emptyList());
        // mock reportCommonService 的方法
        when(reportCommonService.bindSubjectFormula(eq(formula), anyMap())).thenReturn(formula);

        // 调用
        financeIndicatorService.createFinanceIndicator(reqVO, 10L);
        // 断言
        ArgumentCaptor<FmsFinanceIndicatorDO> indicatorCaptor = ArgumentCaptor.forClass(FmsFinanceIndicatorDO.class);
        verify(financeIndicatorMapper).insert(indicatorCaptor.capture());
        assertEquals(formula, indicatorCaptor.getValue().getFormula());
    }
}
