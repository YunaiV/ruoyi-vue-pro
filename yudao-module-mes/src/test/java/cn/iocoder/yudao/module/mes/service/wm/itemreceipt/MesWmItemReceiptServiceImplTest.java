package cn.iocoder.yudao.module.mes.service.wm.itemreceipt;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.itemreceipt.MesWmItemReceiptDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.itemreceipt.MesWmItemReceiptDetailDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.itemreceipt.MesWmItemReceiptMapper;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmItemReceiptStatusEnum;
import cn.iocoder.yudao.module.mes.service.md.vendor.MesMdVendorService;
import cn.iocoder.yudao.module.mes.service.qc.iqc.MesQcIqcService;
import cn.iocoder.yudao.module.mes.service.wm.arrivalnotice.MesWmArrivalNoticeService;
import cn.iocoder.yudao.module.mes.service.wm.transaction.MesWmTransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import jakarta.annotation.Resource;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link MesWmItemReceiptServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
@Import(MesWmItemReceiptServiceImpl.class)
public class MesWmItemReceiptServiceImplTest extends BaseDbUnitTest {

    @Resource
    private MesWmItemReceiptServiceImpl itemReceiptService;

    @Resource
    private MesWmItemReceiptMapper itemReceiptMapper;

    @MockitoBean
    private MesWmItemReceiptLineService itemReceiptLineService;
    @MockitoBean
    private MesWmItemReceiptDetailService itemReceiptDetailService;
    @MockitoBean
    private MesWmArrivalNoticeService arrivalNoticeService;
    @MockitoBean
    private MesMdVendorService vendorService;
    @MockitoBean
    private MesQcIqcService iqcService;
    @MockitoBean
    private MesWmTransactionService wmTransactionService;

    // ========== finishItemReceipt ==========

    @Test
    public void testFinishItemReceipt_success_withNoticeId() {
        // mock 数据：插入一条待入库状态的入库单（有关联到货通知单）
        Long noticeId = randomLongId();
        MesWmItemReceiptDO receipt = randomPojo(MesWmItemReceiptDO.class, o -> {
            o.setStatus(MesWmItemReceiptStatusEnum.APPROVED.getStatus());
            o.setNoticeId(noticeId);
        });
        itemReceiptMapper.insert(receipt);
        Long receiptId = receipt.getId();

        // mock 明细列表
        MesWmItemReceiptDetailDO detail1 = randomPojo(MesWmItemReceiptDetailDO.class, o -> {
            o.setReceiptId(receiptId);
            o.setQuantity(BigDecimal.valueOf(100));
        });
        MesWmItemReceiptDetailDO detail2 = randomPojo(MesWmItemReceiptDetailDO.class, o -> {
            o.setReceiptId(receiptId);
            o.setQuantity(BigDecimal.valueOf(50));
        });
        when(itemReceiptDetailService.getItemReceiptDetailListByReceiptId(eq(receiptId)))
                .thenReturn(Arrays.asList(detail1, detail2));

        // 调用
        itemReceiptService.finishItemReceipt(receiptId);

        // 断言 1：状态更新为已完成
        MesWmItemReceiptDO updated = itemReceiptMapper.selectById(receiptId);
        assertEquals(MesWmItemReceiptStatusEnum.FINISHED.getStatus(), updated.getStatus());
        // 断言 2：库存事务被创建（2 条明细）
        verify(wmTransactionService).createTransactionList(anyList());
        // 断言 3：到货通知单被完成
        verify(arrivalNoticeService).finishArrivalNotice(eq(noticeId));
    }

    @Test
    public void testFinishItemReceipt_success_withoutNoticeId() {
        // mock 数据：插入一条待入库状态的入库单（无关联到货通知单）
        MesWmItemReceiptDO receipt = randomPojo(MesWmItemReceiptDO.class, o -> {
            o.setStatus(MesWmItemReceiptStatusEnum.APPROVED.getStatus());
            o.setNoticeId(null); // 无到货通知单
        });
        itemReceiptMapper.insert(receipt);
        Long receiptId = receipt.getId();

        // mock 空明细
        when(itemReceiptDetailService.getItemReceiptDetailListByReceiptId(eq(receiptId)))
                .thenReturn(Collections.emptyList());

        // 调用
        itemReceiptService.finishItemReceipt(receiptId);

        // 断言 1：状态更新为已完成
        MesWmItemReceiptDO updated = itemReceiptMapper.selectById(receiptId);
        assertEquals(MesWmItemReceiptStatusEnum.FINISHED.getStatus(), updated.getStatus());
        // 断言 2：库存事务仍被调用（空列表）
        verify(wmTransactionService).createTransactionList(anyList());
        // 断言 3：到货通知单完成方法不被调用
        verify(arrivalNoticeService, never()).finishArrivalNotice(any());
    }

    @Test
    public void testFinishItemReceipt_notExists() {
        // 准备参数
        Long receiptId = randomLongId();

        // 调用，并断言异常
        assertServiceException(
                () -> itemReceiptService.finishItemReceipt(receiptId),
                WM_ITEM_RECEIPT_NOT_EXISTS);
    }

    @Test
    public void testFinishItemReceipt_statusNotApproved() {
        // mock 数据：插入一条草稿状态
        MesWmItemReceiptDO receipt = randomPojo(MesWmItemReceiptDO.class, o -> {
            o.setStatus(MesWmItemReceiptStatusEnum.PREPARE.getStatus());
        });
        itemReceiptMapper.insert(receipt);

        // 调用，并断言异常
        assertServiceException(
                () -> itemReceiptService.finishItemReceipt(receipt.getId()),
                WM_ITEM_RECEIPT_STATUS_ERROR);
    }

    @Test
    public void testFinishItemReceipt_statusAlreadyFinished() {
        // mock 数据：插入一条已完成状态
        MesWmItemReceiptDO receipt = randomPojo(MesWmItemReceiptDO.class, o -> {
            o.setStatus(MesWmItemReceiptStatusEnum.FINISHED.getStatus());
        });
        itemReceiptMapper.insert(receipt);

        // 调用，并断言异常
        assertServiceException(
                () -> itemReceiptService.finishItemReceipt(receipt.getId()),
                WM_ITEM_RECEIPT_STATUS_ERROR);
    }

}
