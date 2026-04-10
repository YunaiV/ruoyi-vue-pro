package cn.iocoder.yudao.module.mes.service.qc.ipqc;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.ipqc.MesQcIpqcDO;
import cn.iocoder.yudao.module.mes.dal.mysql.qc.ipqc.MesQcIpqcMapper;
import cn.iocoder.yudao.module.mes.enums.MesBizTypeConstants;
import cn.iocoder.yudao.module.mes.enums.qc.MesQcStatusEnum;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.md.workstation.MesMdWorkstationService;
import cn.iocoder.yudao.module.mes.service.pro.feedback.MesProFeedbackService;
import cn.iocoder.yudao.module.mes.service.pro.workorder.MesProWorkOrderService;
import cn.iocoder.yudao.module.mes.service.qc.defectrecord.MesQcDefectRecordService;
import cn.iocoder.yudao.module.mes.service.qc.template.MesQcTemplateItemService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link MesQcIpqcServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
@Import(MesQcIpqcServiceImpl.class)
public class MesQcIpqcServiceImplTest extends BaseDbUnitTest {

    @Resource
    private MesQcIpqcServiceImpl ipqcService;

    @Resource
    private MesQcIpqcMapper ipqcMapper;

    @MockitoBean
    private MesQcTemplateItemService templateItemService;
    @MockitoBean
    private MesQcIpqcLineService ipqcLineService;
    @MockitoBean
    private MesProWorkOrderService workOrderService;
    @MockitoBean
    private MesMdWorkstationService workstationService;
    @MockitoBean
    private MesMdItemService itemService;
    @MockitoBean
    private MesQcDefectRecordService defectRecordService;
    @MockitoBean
    private AdminUserApi adminUserApi;
    @MockitoBean
    private MesProFeedbackService feedbackService;

    @Test
    public void testFinishIpqc_writeBack_feedback() {
        // 准备数据：插入一条草稿状态的 IPQC 单，来源为报工单（PRO_FEEDBACK=304）
        Long sourceDocId = randomLongId();
        BigDecimal qualifiedQty = BigDecimal.valueOf(80);
        BigDecimal unqualifiedQty = BigDecimal.valueOf(20);
        BigDecimal laborScrapQty = BigDecimal.valueOf(5);
        BigDecimal materialScrapQty = BigDecimal.valueOf(10);
        BigDecimal otherScrapQty = BigDecimal.valueOf(5);

        MesQcIpqcDO ipqc = randomPojo(MesQcIpqcDO.class, o -> {
            o.setStatus(MesQcStatusEnum.DRAFT.getStatus());
            o.setCheckResult(1); // 非空，满足 finishIpqc 的校验
            o.setSourceDocType(MesBizTypeConstants.PRO_FEEDBACK);
            o.setSourceDocId(sourceDocId);
            o.setSourceLineId(null); // 报工场景下预留不使用
            o.setQualifiedQuantity(qualifiedQty);
            o.setUnqualifiedQuantity(unqualifiedQty);
            o.setLaborScrapQuantity(laborScrapQty);
            o.setMaterialScrapQuantity(materialScrapQty);
            o.setOtherScrapQuantity(otherScrapQty);
        });
        ipqcMapper.insert(ipqc);

        // 调用
        ipqcService.finishIpqc(ipqc.getId());

        // 断言 1：状态更新为已完成
        MesQcIpqcDO updatedIpqc = ipqcMapper.selectById(ipqc.getId());
        assertEquals(MesQcStatusEnum.FINISHED.getStatus(), updatedIpqc.getStatus());

        // 断言 2：调用了 feedbackService.completeFeedbackFromIpqc，传递正确参数
        // 注意：数量经过 DB 存取后 scale 可能变化（例如 80 → 80.00），所以用 any() 匹配
        verify(feedbackService).updateProFeedbackWhenIpqcFinish(
                eq(sourceDocId),
                any(BigDecimal.class),
                any(BigDecimal.class),
                any(BigDecimal.class),
                any(BigDecimal.class),
                any(BigDecimal.class));
    }

    @Test
    public void testFinishIpqc_writeBack_feedback_withNullQuantities() {
        // 准备数据：合格品/不合格品/废品数量为 null 的场景，应该 defaultIfNull 为 0
        Long sourceDocId = randomLongId();
        MesQcIpqcDO ipqc = randomPojo(MesQcIpqcDO.class, o -> {
            o.setStatus(MesQcStatusEnum.DRAFT.getStatus());
            o.setCheckResult(1);
            o.setSourceDocType(MesBizTypeConstants.PRO_FEEDBACK);
            o.setSourceDocId(sourceDocId);
            o.setSourceLineId(null);
            o.setQualifiedQuantity(null);
            o.setUnqualifiedQuantity(null);
            o.setLaborScrapQuantity(null);
            o.setMaterialScrapQuantity(null);
            o.setOtherScrapQuantity(null);
        });
        ipqcMapper.insert(ipqc);

        // 调用
        ipqcService.finishIpqc(ipqc.getId());

        // 断言：所有数量参数都是 BigDecimal.ZERO（defaultIfNull 处理）
        verify(feedbackService).updateProFeedbackWhenIpqcFinish(
                eq(sourceDocId),
                eq(BigDecimal.ZERO),
                eq(BigDecimal.ZERO),
                eq(BigDecimal.ZERO),
                eq(BigDecimal.ZERO),
                eq(BigDecimal.ZERO));
    }

    @Test
    public void testFinishIpqc_writeBack_noSourceDoc() {
        // 准备数据：sourceDocType 为空，不需要回写
        MesQcIpqcDO ipqc = randomPojo(MesQcIpqcDO.class, o -> {
            o.setStatus(MesQcStatusEnum.DRAFT.getStatus());
            o.setCheckResult(1);
            o.setSourceDocType(null);
            o.setSourceDocId(null);
            o.setSourceLineId(null);
        });
        ipqcMapper.insert(ipqc);

        // 调用
        ipqcService.finishIpqc(ipqc.getId());

        // 断言 1：状态更新成功
        MesQcIpqcDO updatedIpqc = ipqcMapper.selectById(ipqc.getId());
        assertEquals(MesQcStatusEnum.FINISHED.getStatus(), updatedIpqc.getStatus());

        // 断言 2：不应该调用 feedbackService
        verify(feedbackService, never()).updateProFeedbackWhenIpqcFinish(
                anyLong(), any(), any(), any(), any(), any());
    }

    @Test
    public void testFinishIpqc_writeBack_sourceDocTypeDirtyData() {
        // 准备数据：sourceDocType 非空但 sourceDocId 为空，应该抛异常
        MesQcIpqcDO ipqc = randomPojo(MesQcIpqcDO.class, o -> {
            o.setStatus(MesQcStatusEnum.DRAFT.getStatus());
            o.setCheckResult(1);
            o.setSourceDocType(MesBizTypeConstants.PRO_FEEDBACK);
            o.setSourceDocId(null); // 脏数据
            o.setSourceLineId(null);
        });
        ipqcMapper.insert(ipqc);

        // 调用，并断言异常
        assertThrows(IllegalArgumentException.class, () -> ipqcService.finishIpqc(ipqc.getId()));
    }

    @Test
    public void testFinishIpqc_writeBack_unknownSourceDocType() {
        // 准备数据：sourceDocType 无法识别
        MesQcIpqcDO ipqc = randomPojo(MesQcIpqcDO.class, o -> {
            o.setStatus(MesQcStatusEnum.DRAFT.getStatus());
            o.setCheckResult(1);
            o.setSourceDocType(99999); // 无法识别的类型
            o.setSourceDocId(randomLongId());
            o.setSourceLineId(null);
        });
        ipqcMapper.insert(ipqc);

        // 调用，并断言异常
        assertThrows(IllegalArgumentException.class, () -> ipqcService.finishIpqc(ipqc.getId()));
    }

    @Test
    public void testFinishIpqc_checkResultEmpty() {
        // 准备数据：checkResult 为空，应该报错
        MesQcIpqcDO ipqc = randomPojo(MesQcIpqcDO.class, o -> {
            o.setStatus(MesQcStatusEnum.DRAFT.getStatus());
            o.setCheckResult(null); // 检测结论为空
            o.setSourceDocType(null);
        });
        ipqcMapper.insert(ipqc);

        // 调用，应该抛异常（检测结论必填）
        assertThrows(Exception.class, () -> ipqcService.finishIpqc(ipqc.getId()));
    }

    @Test
    public void testFinishIpqc_notDraftStatus() {
        // 准备数据：已完成状态，不能再次完成
        MesQcIpqcDO ipqc = randomPojo(MesQcIpqcDO.class, o -> {
            o.setStatus(MesQcStatusEnum.FINISHED.getStatus());
            o.setCheckResult(1);
        });
        ipqcMapper.insert(ipqc);

        // 调用，应该抛异常（不是草稿状态）
        assertThrows(Exception.class, () -> ipqcService.finishIpqc(ipqc.getId()));
    }

}
