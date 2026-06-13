package cn.iocoder.yudao.module.mes.service.qc.rqc;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.rqc.MesQcRqcDO;
import cn.iocoder.yudao.module.mes.dal.mysql.qc.rqc.MesQcRqcMapper;
import cn.iocoder.yudao.module.mes.enums.MesBizTypeConstants;
import cn.iocoder.yudao.module.mes.enums.qc.MesQcStatusEnum;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.qc.defectrecord.MesQcDefectRecordService;
import cn.iocoder.yudao.module.mes.service.qc.template.MesQcTemplateItemService;
import cn.iocoder.yudao.module.mes.service.wm.returnissue.MesWmReturnIssueLineService;
import cn.iocoder.yudao.module.mes.service.wm.returnsales.MesWmReturnSalesLineService;
import cn.iocoder.yudao.module.mes.service.qc.indicatorresult.MesQcIndicatorResultService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.QC_RQC_NOT_EXISTS;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.QC_RQC_NOT_PREPARE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * {@link MesQcRqcServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(MesQcRqcServiceImpl.class)
public class MesQcRqcServiceImplTest extends BaseDbUnitTest {

    @Resource
    private MesQcRqcServiceImpl rqcService;

    @Resource
    private MesQcRqcMapper rqcMapper;

    @MockitoBean
    private MesWmReturnIssueLineService returnIssueLineService;
    @MockitoBean
    private MesWmReturnSalesLineService returnSalesLineService;
    @MockitoBean
    private MesQcRqcLineService rqcLineService;
    @MockitoBean
    private MesMdItemService itemService;
    @MockitoBean
    private MesQcTemplateItemService templateItemService;
    @MockitoBean
    private MesQcDefectRecordService defectRecordService;
    @MockitoBean
    private AdminUserApi adminUserApi;
    @MockitoBean
    private MesQcIndicatorResultService indicatorResultService;

    @Test
    public void testFinishRqc_successWithReturnIssue() {
        // 准备参数
        Long sourceLineId = randomLongId();
        Long sourceDocId = randomLongId();
        Integer checkResult = 1;
        BigDecimal qualifiedQty = new BigDecimal("80");
        BigDecimal unqualifiedQty = new BigDecimal("20");

        // mock 数据
        MesQcRqcDO rqc = randomPojo(MesQcRqcDO.class, o -> {
            o.setStatus(MesQcStatusEnum.DRAFT.getStatus());
            o.setSourceDocType(MesBizTypeConstants.WM_RETURN_ISSUE);
            o.setSourceDocId(sourceDocId);
            o.setSourceLineId(sourceLineId);
            o.setCheckResult(checkResult);
            // 设置数量通过 finishRqc 的数量校验：合格 + 不合格 = 检测数量
            o.setCheckQuantity(new BigDecimal("100"));
            o.setQualifiedQuantity(qualifiedQty);
            o.setUnqualifiedQuantity(unqualifiedQty);
        });
        rqcMapper.insert(rqc);

        // 调用
        rqcService.finishRqc(rqc.getId());

        // 断言：验证状态更新
        MesQcRqcDO updatedRqc = rqcMapper.selectById(rqc.getId());
        assertEquals(MesQcStatusEnum.FINISHED.getStatus(), updatedRqc.getStatus());

        // 断言：验证回写调用生产退料（含拆分参数）
        verify(returnIssueLineService).updateReturnIssueLineWhenRqcFinish(
                eq(sourceLineId), eq(sourceDocId), eq(checkResult),
                argThat(v -> v.compareTo(qualifiedQty) == 0),
                argThat(v -> v.compareTo(unqualifiedQty) == 0));
        // 断言：未调用销售退货
        verify(returnSalesLineService, never()).updateReturnSalesLineWhenRqcFinish(
                anyLong(), anyLong(), anyInt(), any(BigDecimal.class), any(BigDecimal.class));
    }

    @Test
    public void testFinishRqc_successWithReturnSales() {
        // 准备参数
        Long sourceLineId = randomLongId();
        Long sourceDocId = randomLongId();
        Integer checkResult = 1;
        BigDecimal qualifiedQty = new BigDecimal("90");
        BigDecimal unqualifiedQty = new BigDecimal("10");

        // mock 数据
        MesQcRqcDO rqc = randomPojo(MesQcRqcDO.class, o -> {
            o.setStatus(MesQcStatusEnum.DRAFT.getStatus());
            o.setSourceDocType(MesBizTypeConstants.WM_RETURN_SALES);
            o.setSourceDocId(sourceDocId);
            o.setSourceLineId(sourceLineId);
            o.setCheckResult(checkResult);
            // 设置数量通过 finishRqc 的数量校验：合格 + 不合格 = 检测数量
            o.setCheckQuantity(new BigDecimal("100"));
            o.setQualifiedQuantity(qualifiedQty);
            o.setUnqualifiedQuantity(unqualifiedQty);
        });
        rqcMapper.insert(rqc);

        // 调用
        rqcService.finishRqc(rqc.getId());

        // 断言：验证状态更新
        MesQcRqcDO updatedRqc = rqcMapper.selectById(rqc.getId());
        assertEquals(MesQcStatusEnum.FINISHED.getStatus(), updatedRqc.getStatus());

        // 断言：验证回写调用销售退货（含拆分参数）
        verify(returnSalesLineService).updateReturnSalesLineWhenRqcFinish(
                eq(sourceLineId), eq(sourceDocId), eq(checkResult),
                argThat(v -> v.compareTo(qualifiedQty) == 0),
                argThat(v -> v.compareTo(unqualifiedQty) == 0));
        // 断言：未调用生产退料
        verify(returnIssueLineService, never()).updateReturnIssueLineWhenRqcFinish(
                anyLong(), anyLong(), anyInt(), any(BigDecimal.class), any(BigDecimal.class));
    }

    @Test
    public void testFinishRqc_notExists() {
        // 准备参数
        Long rqcId = randomLongId();

        // 调用，并断言异常
        assertServiceException(() -> rqcService.finishRqc(rqcId), QC_RQC_NOT_EXISTS);
    }

    @Test
    public void testFinishRqc_statusNotDraft() {
        // 准备参数
        // mock 数据：状态为已完成
        MesQcRqcDO rqc = randomPojo(MesQcRqcDO.class, o -> {
            o.setStatus(MesQcStatusEnum.FINISHED.getStatus());
        });
        rqcMapper.insert(rqc);

        // 调用，并断言异常
        assertServiceException(() -> rqcService.finishRqc(rqc.getId()), QC_RQC_NOT_PREPARE);
    }

}
