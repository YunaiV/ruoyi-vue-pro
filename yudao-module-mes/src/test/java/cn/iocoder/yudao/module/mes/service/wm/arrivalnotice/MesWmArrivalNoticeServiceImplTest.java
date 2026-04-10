package cn.iocoder.yudao.module.mes.service.wm.arrivalnotice;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.arrivalnotice.MesWmArrivalNoticeDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.arrivalnotice.MesWmArrivalNoticeLineDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.arrivalnotice.MesWmArrivalNoticeMapper;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmArrivalNoticeStatusEnum;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import jakarta.annotation.Resource;

import java.math.BigDecimal;
import java.util.Arrays;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link MesWmArrivalNoticeServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
@Import(MesWmArrivalNoticeServiceImpl.class)
public class MesWmArrivalNoticeServiceImplTest extends BaseDbUnitTest {

    @Resource
    private MesWmArrivalNoticeServiceImpl arrivalNoticeService;

    @Resource
    private MesWmArrivalNoticeMapper arrivalNoticeMapper;

    @MockitoBean
    private MesWmArrivalNoticeLineService arrivalNoticeLineService;

    @Test
    public void testUpdateArrivalNoticeWhenIqcFinish_success_allLinesChecked() {
        // mock 数据：插入一条待质检的到货通知单
        MesWmArrivalNoticeDO notice = randomPojo(MesWmArrivalNoticeDO.class, o -> {
            o.setStatus(MesWmArrivalNoticeStatusEnum.PENDING_QC.getStatus());
        });
        arrivalNoticeMapper.insert(notice);
        // 准备参数
        Long noticeId = notice.getId();
        Long lineId = randomLongId();
        Long iqcId = randomLongId();
        BigDecimal qualifiedQuantity = BigDecimal.valueOf(100);

        // mock 行服务方法：所有需检行都已完成（iqcId 不为空）
        MesWmArrivalNoticeLineDO line1 = randomPojo(MesWmArrivalNoticeLineDO.class, o -> {
            o.setNoticeId(noticeId);
            o.setIqcCheckFlag(true);
            o.setIqcId(iqcId); // 已检完
        });
        MesWmArrivalNoticeLineDO line2 = randomPojo(MesWmArrivalNoticeLineDO.class, o -> {
            o.setNoticeId(noticeId);
            o.setIqcCheckFlag(false); // 不需要检验
            o.setIqcId(null);
        });
        when(arrivalNoticeLineService.getArrivalNoticeLineListByNoticeId(eq(noticeId)))
                .thenReturn(Arrays.asList(line1, line2));

        // 调用
        arrivalNoticeService.updateArrivalNoticeWhenIqcFinish(noticeId, lineId, iqcId, qualifiedQuantity);

        // 断言：行回写被调用
        verify(arrivalNoticeLineService).updateArrivalNoticeLineWhenIqcFinish(eq(lineId), eq(iqcId), eq(qualifiedQuantity));
        // 断言：主表状态更新为待入库
        MesWmArrivalNoticeDO updatedNotice = arrivalNoticeMapper.selectById(noticeId);
        assertEquals(MesWmArrivalNoticeStatusEnum.PENDING_RECEIPT.getStatus(), updatedNotice.getStatus());
    }

    @Test
    public void testUpdateArrivalNoticeWhenIqcFinish_success_hasUncheckedLines() {
        // mock 数据：插入一条待质检的到货通知单
        MesWmArrivalNoticeDO notice = randomPojo(MesWmArrivalNoticeDO.class, o -> {
            o.setStatus(MesWmArrivalNoticeStatusEnum.PENDING_QC.getStatus());
        });
        arrivalNoticeMapper.insert(notice);
        // 准备参数
        Long noticeId = notice.getId();
        Long lineId = randomLongId();
        Long iqcId = randomLongId();
        BigDecimal qualifiedQuantity = BigDecimal.valueOf(50);

        // mock 行服务方法：还有未完成的需检行
        MesWmArrivalNoticeLineDO line1 = randomPojo(MesWmArrivalNoticeLineDO.class, o -> {
            o.setNoticeId(noticeId);
            o.setIqcCheckFlag(true);
            o.setIqcId(iqcId); // 已检完
        });
        MesWmArrivalNoticeLineDO line2 = randomPojo(MesWmArrivalNoticeLineDO.class, o -> {
            o.setNoticeId(noticeId);
            o.setIqcCheckFlag(true);
            o.setIqcId(null); // 还未检验
        });
        when(arrivalNoticeLineService.getArrivalNoticeLineListByNoticeId(eq(noticeId)))
                .thenReturn(Arrays.asList(line1, line2));

        // 调用
        arrivalNoticeService.updateArrivalNoticeWhenIqcFinish(noticeId, lineId, iqcId, qualifiedQuantity);

        // 断言：行回写被调用
        verify(arrivalNoticeLineService).updateArrivalNoticeLineWhenIqcFinish(eq(lineId), eq(iqcId), eq(qualifiedQuantity));
        // 断言：主表状态不变，仍然是待质检
        MesWmArrivalNoticeDO updatedNotice = arrivalNoticeMapper.selectById(noticeId);
        assertEquals(MesWmArrivalNoticeStatusEnum.PENDING_QC.getStatus(), updatedNotice.getStatus());
    }

    @Test
    public void testUpdateArrivalNoticeWhenIqcFinish_noticeNotExists() {
        // 准备参数
        Long noticeId = randomLongId();
        Long lineId = randomLongId();
        Long iqcId = randomLongId();
        BigDecimal qualifiedQuantity = BigDecimal.valueOf(100);

        // 调用，并断言异常
        assertServiceException(
                () -> arrivalNoticeService.updateArrivalNoticeWhenIqcFinish(noticeId, lineId, iqcId, qualifiedQuantity),
                WM_ARRIVAL_NOTICE_NOT_EXISTS);
    }

    @Test
    public void testUpdateArrivalNoticeWhenIqcFinish_statusNotPendingQc() {
        // mock 数据：插入一条草稿状态的到货通知单（非待质检）
        MesWmArrivalNoticeDO notice = randomPojo(MesWmArrivalNoticeDO.class, o -> {
            o.setStatus(MesWmArrivalNoticeStatusEnum.PREPARE.getStatus());
        });
        arrivalNoticeMapper.insert(notice);
        // 准备参数
        Long noticeId = notice.getId();
        Long lineId = randomLongId();
        Long iqcId = randomLongId();
        BigDecimal qualifiedQuantity = BigDecimal.valueOf(100);

        // 调用，并断言异常
        assertServiceException(
                () -> arrivalNoticeService.updateArrivalNoticeWhenIqcFinish(noticeId, lineId, iqcId, qualifiedQuantity),
                WM_ARRIVAL_NOTICE_STATUS_NOT_PENDING_QC);
    }

}
