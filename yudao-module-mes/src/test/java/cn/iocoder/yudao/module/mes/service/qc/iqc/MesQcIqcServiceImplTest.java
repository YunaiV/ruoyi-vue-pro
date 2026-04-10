package cn.iocoder.yudao.module.mes.service.qc.iqc;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.iqc.MesQcIqcDO;
import cn.iocoder.yudao.module.mes.dal.mysql.qc.iqc.MesQcIqcMapper;
import cn.iocoder.yudao.module.mes.enums.MesBizTypeConstants;
import cn.iocoder.yudao.module.mes.enums.qc.MesQcStatusEnum;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.md.vendor.MesMdVendorService;
import cn.iocoder.yudao.module.mes.service.qc.defectrecord.MesQcDefectRecordService;
import cn.iocoder.yudao.module.mes.service.qc.template.MesQcTemplateItemService;
import cn.iocoder.yudao.module.mes.service.wm.arrivalnotice.MesWmArrivalNoticeService;
import cn.iocoder.yudao.module.mes.service.wm.outsourcereceipt.MesWmOutsourceReceiptService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import jakarta.annotation.Resource;

import java.math.BigDecimal;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link MesQcIqcServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
@Import(MesQcIqcServiceImpl.class)
public class MesQcIqcServiceImplTest extends BaseDbUnitTest {

    @Resource
    private MesQcIqcServiceImpl iqcService;

    @Resource
    private MesQcIqcMapper iqcMapper;

    @MockitoBean
    private MesWmArrivalNoticeService arrivalNoticeService;
    @MockitoBean
    private MesWmOutsourceReceiptService outsourceReceiptService;
    @MockitoBean
    private MesQcIqcLineService iqcLineService;
    @MockitoBean
    private MesQcDefectRecordService defectRecordService;
    @MockitoBean
    private MesMdVendorService vendorService;
    @MockitoBean
    private MesMdItemService itemService;
    @MockitoBean
    private MesQcTemplateItemService templateItemService;
    @MockitoBean
    private AdminUserApi adminUserApi;

    @Test
    public void testFinishIqc_writeBack_arrivalNotice() {
        // 准备数据：插入一条草稿状态的 IQC 单，来源为到货通知单
        Long sourceDocId = randomLongId();
        Long sourceLineId = randomLongId();
        MesQcIqcDO iqc = randomPojo(MesQcIqcDO.class, o -> {
            o.setStatus(MesQcStatusEnum.DRAFT.getStatus());
            o.setCheckResult(1); // 非空，满足 finishIqc 的校验
            o.setSourceDocType(MesBizTypeConstants.WM_ARRIVAL_NOTICE);
            o.setSourceDocId(sourceDocId);
            o.setSourceLineId(sourceLineId);
            o.setQualifiedQuantity(BigDecimal.valueOf(100));
            o.setUnqualifiedQuantity(BigDecimal.ZERO);
        });
        iqcMapper.insert(iqc);

        // 调用
        iqcService.finishIqc(iqc.getId());

        // 断言：验证状态更新为已完成
        MesQcIqcDO updatedIqc = iqcMapper.selectById(iqc.getId());
        assertEquals(MesQcStatusEnum.FINISHED.getStatus(), updatedIqc.getStatus());
        // 断言：验证回写到货通知单（参数使用 any 避免 BigDecimal 精度问题）
        verify(arrivalNoticeService).updateArrivalNoticeWhenIqcFinish(
                eq(sourceDocId), eq(sourceLineId), eq(iqc.getId()), any(BigDecimal.class));
        // 断言：不应该调用外协入库单的回写
        verify(outsourceReceiptService, never()).updateOutsourceReceiptWhenIqcFinish(
                anyLong(), anyLong(), anyLong(), any(), any());
    }

    @Test
    public void testFinishIqc_writeBack_outsourceReceipt() {
        // 准备数据：插入一条草稿状态的 IQC 单，来源为外协入库单
        Long sourceDocId = randomLongId();
        Long sourceLineId = randomLongId();
        MesQcIqcDO iqc = randomPojo(MesQcIqcDO.class, o -> {
            o.setStatus(MesQcStatusEnum.DRAFT.getStatus());
            o.setCheckResult(1);
            o.setSourceDocType(MesBizTypeConstants.WM_OUTSOURCE_RECPT);
            o.setSourceDocId(sourceDocId);
            o.setSourceLineId(sourceLineId);
            o.setQualifiedQuantity(BigDecimal.valueOf(80));
            o.setUnqualifiedQuantity(BigDecimal.valueOf(20));
        });
        iqcMapper.insert(iqc);

        // 调用
        iqcService.finishIqc(iqc.getId());

        // 断言：验证状态更新为已完成
        MesQcIqcDO updatedIqc = iqcMapper.selectById(iqc.getId());
        assertEquals(MesQcStatusEnum.FINISHED.getStatus(), updatedIqc.getStatus());
        // 断言：验证回写外协入库单（参数使用 any 避免 BigDecimal 精度问题）
        verify(outsourceReceiptService).updateOutsourceReceiptWhenIqcFinish(
                eq(sourceDocId), eq(sourceLineId), eq(iqc.getId()), any(BigDecimal.class), any(BigDecimal.class));
        // 断言：不应该调用到货通知单的回写
        verify(arrivalNoticeService, never()).updateArrivalNoticeWhenIqcFinish(
                anyLong(), anyLong(), anyLong(), any());
    }

    @Test
    public void testFinishIqc_writeBack_noSourceDoc() {
        // 准备数据：sourceDocType 为空，不需要回写
        MesQcIqcDO iqc = randomPojo(MesQcIqcDO.class, o -> {
            o.setStatus(MesQcStatusEnum.DRAFT.getStatus());
            o.setCheckResult(1);
            o.setSourceDocType(null);
            o.setSourceDocId(null);
            o.setSourceLineId(null);
        });
        iqcMapper.insert(iqc);

        // 调用
        iqcService.finishIqc(iqc.getId());

        // 断言：验证状态更新为已完成
        MesQcIqcDO updatedIqc = iqcMapper.selectById(iqc.getId());
        assertEquals(MesQcStatusEnum.FINISHED.getStatus(), updatedIqc.getStatus());
        // 断言：不应该调用任何回写
        verify(arrivalNoticeService, never()).updateArrivalNoticeWhenIqcFinish(
                anyLong(), anyLong(), anyLong(), any());
        verify(outsourceReceiptService, never()).updateOutsourceReceiptWhenIqcFinish(
                anyLong(), anyLong(), anyLong(), any(), any());
    }

    @Test
    public void testFinishIqc_writeBack_sourceDocTypeDirtyData() {
        // 准备数据：sourceDocType 非空但 sourceLineId 为空，应该抛异常
        MesQcIqcDO iqc = randomPojo(MesQcIqcDO.class, o -> {
            o.setStatus(MesQcStatusEnum.DRAFT.getStatus());
            o.setCheckResult(1);
            o.setSourceDocType(MesBizTypeConstants.WM_ARRIVAL_NOTICE);
            o.setSourceDocId(randomLongId());
            o.setSourceLineId(null); // 脏数据
        });
        iqcMapper.insert(iqc);

        // 调用，并断言异常
        assertThrows(IllegalArgumentException.class, () -> iqcService.finishIqc(iqc.getId()));
    }

    @Test
    public void testFinishIqc_writeBack_unknownSourceDocType() {
        // 准备数据：sourceDocType 无法识别
        MesQcIqcDO iqc = randomPojo(MesQcIqcDO.class, o -> {
            o.setStatus(MesQcStatusEnum.DRAFT.getStatus());
            o.setCheckResult(1);
            o.setSourceDocType(99999); // 无法识别的类型
            o.setSourceDocId(randomLongId());
            o.setSourceLineId(randomLongId());
        });
        iqcMapper.insert(iqc);

        // 调用，并断言异常
        assertThrows(IllegalArgumentException.class, () -> iqcService.finishIqc(iqc.getId()));
    }

    @Test
    public void testFinishIqc_checkResultEmpty() {
        // 准备数据：checkResult 为空，应该报错
        MesQcIqcDO iqc = randomPojo(MesQcIqcDO.class, o -> {
            o.setStatus(MesQcStatusEnum.DRAFT.getStatus());
            o.setCheckResult(null); // 检测结论为空
            o.setSourceDocType(null);
        });
        iqcMapper.insert(iqc);

        // 调用，应该抛异常（检测结论必填）
        assertThrows(Exception.class, () -> iqcService.finishIqc(iqc.getId()));
    }

    @Test
    public void testFinishIqc_notDraftStatus() {
        // 准备数据：已完成状态，不能再次完成
        MesQcIqcDO iqc = randomPojo(MesQcIqcDO.class, o -> {
            o.setStatus(MesQcStatusEnum.FINISHED.getStatus());
            o.setCheckResult(1);
        });
        iqcMapper.insert(iqc);

        // 调用，应该抛异常（不是草稿状态）
        assertThrows(Exception.class, () -> iqcService.finishIqc(iqc.getId()));
    }

}
