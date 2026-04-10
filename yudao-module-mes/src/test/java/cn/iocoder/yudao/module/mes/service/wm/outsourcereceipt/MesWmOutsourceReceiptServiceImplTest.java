package cn.iocoder.yudao.module.mes.service.wm.outsourcereceipt;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.outsourcereceipt.MesWmOutsourceReceiptDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.outsourcereceipt.MesWmOutsourceReceiptLineDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.outsourcereceipt.MesWmOutsourceReceiptMapper;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmOutsourceReceiptStatusEnum;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmQualityStatusEnum;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.md.vendor.MesMdVendorService;
import cn.iocoder.yudao.module.mes.service.pro.workorder.MesProWorkOrderService;
import cn.iocoder.yudao.module.mes.service.wm.transaction.MesWmTransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import jakarta.annotation.Resource;

import java.math.BigDecimal;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link MesWmOutsourceReceiptServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
@Import(MesWmOutsourceReceiptServiceImpl.class)
public class MesWmOutsourceReceiptServiceImplTest extends BaseDbUnitTest {

    @Resource
    private MesWmOutsourceReceiptServiceImpl outsourceReceiptService;

    @Resource
    private MesWmOutsourceReceiptMapper outsourceReceiptMapper;

    @MockitoBean
    private MesWmOutsourceReceiptLineService outsourceReceiptLineService;
    @MockitoBean
    private MesWmOutsourceReceiptDetailService outsourceReceiptDetailService;
    @MockitoBean
    private MesWmTransactionService wmTransactionService;
    @MockitoBean
    private MesMdVendorService vendorService;
    @MockitoBean
    private MesMdItemService itemService;
    @MockitoBean
    private MesProWorkOrderService workOrderService;

    @Test
    public void testUpdateOutsourceReceiptWhenIqcFinish_success_bothQualifiedAndUnqualified() {
        // mock 数据：插入一条待检验状态的外协入库单
        MesWmOutsourceReceiptDO receipt = randomPojo(MesWmOutsourceReceiptDO.class, o -> {
            o.setStatus(MesWmOutsourceReceiptStatusEnum.CONFIRMED.getStatus());
        });
        outsourceReceiptMapper.insert(receipt);
        // 准备参数
        Long receiptId = receipt.getId();
        Long lineId = randomLongId();
        Long iqcId = randomLongId();
        BigDecimal qualifiedQuantity = BigDecimal.valueOf(80);
        BigDecimal unqualifiedQuantity = BigDecimal.valueOf(20);

        // mock 行查询
        MesWmOutsourceReceiptLineDO line = randomPojo(MesWmOutsourceReceiptLineDO.class, o -> {
            o.setId(lineId);
            o.setReceiptId(receiptId);
            o.setQuantity(BigDecimal.valueOf(100));
        });
        when(outsourceReceiptLineService.getOutsourceReceiptLine(eq(lineId))).thenReturn(line);

        // 调用
        outsourceReceiptService.updateOutsourceReceiptWhenIqcFinish(
                receiptId, lineId, iqcId, qualifiedQuantity, unqualifiedQuantity);

        // 断言：合格品行更新
        verify(outsourceReceiptLineService).updateOutsourceReceiptLineDO(argThat(updateLine ->
                lineId.equals(updateLine.getId())
                        && MesWmQualityStatusEnum.PASS.getStatus().equals(updateLine.getQualityStatus())
                        && iqcId.equals(updateLine.getIqcId())
                        && qualifiedQuantity.equals(updateLine.getQuantity())));
        // 断言：不合格品新增行
        verify(outsourceReceiptLineService).createOutsourceReceiptLineDO(argThat(newLine ->
                newLine.getId() == null
                        && MesWmQualityStatusEnum.FAIL.getStatus().equals(newLine.getQualityStatus())
                        && iqcId.equals(newLine.getIqcId())
                        && unqualifiedQuantity.equals(newLine.getQuantity())));
        // 断言：主表状态更新为待上架
        MesWmOutsourceReceiptDO updatedReceipt = outsourceReceiptMapper.selectById(receiptId);
        assertEquals(MesWmOutsourceReceiptStatusEnum.APPROVING.getStatus(), updatedReceipt.getStatus());
    }

    @Test
    public void testUpdateOutsourceReceiptWhenIqcFinish_success_onlyQualified() {
        // mock 数据：插入一条待检验状态的外协入库单
        MesWmOutsourceReceiptDO receipt = randomPojo(MesWmOutsourceReceiptDO.class, o -> {
            o.setStatus(MesWmOutsourceReceiptStatusEnum.CONFIRMED.getStatus());
        });
        outsourceReceiptMapper.insert(receipt);
        // 准备参数
        Long receiptId = receipt.getId();
        Long lineId = randomLongId();
        Long iqcId = randomLongId();
        BigDecimal qualifiedQuantity = BigDecimal.valueOf(100);
        BigDecimal unqualifiedQuantity = BigDecimal.ZERO; // 没有不合格品

        // mock 行查询
        MesWmOutsourceReceiptLineDO line = randomPojo(MesWmOutsourceReceiptLineDO.class, o -> {
            o.setId(lineId);
            o.setReceiptId(receiptId);
        });
        when(outsourceReceiptLineService.getOutsourceReceiptLine(eq(lineId))).thenReturn(line);

        // 调用
        outsourceReceiptService.updateOutsourceReceiptWhenIqcFinish(
                receiptId, lineId, iqcId, qualifiedQuantity, unqualifiedQuantity);

        // 断言：合格品行更新
        verify(outsourceReceiptLineService).updateOutsourceReceiptLineDO(argThat(updateLine ->
                lineId.equals(updateLine.getId())));
        // 断言：不合格品不应该新增行（数量为 0）
        verify(outsourceReceiptLineService, never()).createOutsourceReceiptLineDO(any());
        // 断言：主表状态更新为待上架
        MesWmOutsourceReceiptDO updatedReceipt = outsourceReceiptMapper.selectById(receiptId);
        assertEquals(MesWmOutsourceReceiptStatusEnum.APPROVING.getStatus(), updatedReceipt.getStatus());
    }

    @Test
    public void testUpdateOutsourceReceiptWhenIqcFinish_receiptNotExists() {
        // 准备参数
        Long receiptId = randomLongId();
        Long lineId = randomLongId();
        Long iqcId = randomLongId();
        BigDecimal qualifiedQuantity = BigDecimal.valueOf(100);
        BigDecimal unqualifiedQuantity = BigDecimal.ZERO;

        // 调用，并断言异常
        assertServiceException(
                () -> outsourceReceiptService.updateOutsourceReceiptWhenIqcFinish(
                        receiptId, lineId, iqcId, qualifiedQuantity, unqualifiedQuantity),
                WM_OUTSOURCE_RECEIPT_NOT_EXISTS);
    }

    @Test
    public void testUpdateOutsourceReceiptWhenIqcFinish_statusNotConfirmed() {
        // mock 数据：插入一条草稿状态的外协入库单（非待检验状态）
        MesWmOutsourceReceiptDO receipt = randomPojo(MesWmOutsourceReceiptDO.class, o -> {
            o.setStatus(MesWmOutsourceReceiptStatusEnum.PREPARE.getStatus());
        });
        outsourceReceiptMapper.insert(receipt);
        // 准备参数
        Long receiptId = receipt.getId();
        Long lineId = randomLongId();
        Long iqcId = randomLongId();
        BigDecimal qualifiedQuantity = BigDecimal.valueOf(100);
        BigDecimal unqualifiedQuantity = BigDecimal.ZERO;

        // 调用，并断言异常
        assertServiceException(
                () -> outsourceReceiptService.updateOutsourceReceiptWhenIqcFinish(
                        receiptId, lineId, iqcId, qualifiedQuantity, unqualifiedQuantity),
                WM_OUTSOURCE_RECEIPT_STATUS_ERROR);
    }

    @Test
    public void testUpdateOutsourceReceiptWhenIqcFinish_lineNotExists() {
        // mock 数据：插入一条待检验状态的外协入库单
        MesWmOutsourceReceiptDO receipt = randomPojo(MesWmOutsourceReceiptDO.class, o -> {
            o.setStatus(MesWmOutsourceReceiptStatusEnum.CONFIRMED.getStatus());
        });
        outsourceReceiptMapper.insert(receipt);
        // 准备参数
        Long receiptId = receipt.getId();
        Long lineId = randomLongId();
        Long iqcId = randomLongId();
        BigDecimal qualifiedQuantity = BigDecimal.valueOf(100);
        BigDecimal unqualifiedQuantity = BigDecimal.ZERO;

        // mock 行查询返回 null
        when(outsourceReceiptLineService.getOutsourceReceiptLine(eq(lineId))).thenReturn(null);

        // 调用，并断言异常
        assertServiceException(
                () -> outsourceReceiptService.updateOutsourceReceiptWhenIqcFinish(
                        receiptId, lineId, iqcId, qualifiedQuantity, unqualifiedQuantity),
                WM_OUTSOURCE_RECEIPT_LINE_NOT_EXISTS);
    }

}
