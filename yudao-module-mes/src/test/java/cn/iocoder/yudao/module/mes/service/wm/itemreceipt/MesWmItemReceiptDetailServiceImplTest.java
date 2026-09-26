package cn.iocoder.yudao.module.mes.service.wm.itemreceipt;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.mes.controller.admin.wm.itemreceipt.vo.detail.MesWmItemReceiptDetailSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.itemreceipt.MesWmItemReceiptLineDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.itemreceipt.MesWmItemReceiptDetailDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.itemreceipt.MesWmItemReceiptDetailMapper;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.wm.batch.MesWmBatchService;
import cn.iocoder.yudao.module.mes.service.wm.materialstock.MesWmMaterialStockService;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseAreaService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.Objects;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MesWmItemReceiptDetailServiceImplTest extends BaseMockitoUnitTest {
    @InjectMocks private MesWmItemReceiptDetailServiceImpl service;
    @Mock private MesWmItemReceiptDetailMapper itemReceiptDetailMapper;
    @Mock private MesWmItemReceiptService itemReceiptService;
    @Mock private MesWmItemReceiptLineService itemReceiptLineService;
    @Mock private MesWmWarehouseAreaService warehouseAreaService;
    @Mock private MesWmMaterialStockService materialStockService;
    @Mock private MesMdItemService itemService;
    @Mock private MesWmBatchService batchService;

    private MesWmItemReceiptDetailSaveReqVO prepare(Long batchId, boolean batchFlag) {
        when(itemReceiptLineService.getItemReceiptLine(11L)).thenReturn(
                new MesWmItemReceiptLineDO().setId(11L).setReceiptId(1L).setItemId(2L).setBatchId(batchId));
        when(itemService.validateItemExists(2L)).thenReturn(new MesMdItemDO().setId(2L).setBatchFlag(batchFlag));
        return new MesWmItemReceiptDetailSaveReqVO().setReceiptId(1L).setLineId(11L)
                .setItemId(2L).setAreaId(3L).setQuantity(BigDecimal.ONE);
    }

    @Test
    void createInheritsLineBatchBeforeMixingValidation() {
        MesWmItemReceiptDetailSaveReqVO request = prepare(22L, true).setBatchId(999L);
        service.createItemReceiptDetail(request);
        verify(batchService).validateBatchExists(22L, 2L);
        verify(materialStockService).checkAreaMixingRule(3L, 2L, 22L);
        verify(itemReceiptDetailMapper).insert(argThat((MesWmItemReceiptDetailDO row) -> Objects.equals(row.getBatchId(), 22L)));
    }

    @Test
    void missingLineBatchIsRejectedBeforeInsert() {
        MesWmItemReceiptDetailSaveReqVO request = prepare(null, true);
        assertServiceException(() -> service.createItemReceiptDetail(request), MD_ITEM_BATCH_REQUIRED);
        verify(itemReceiptDetailMapper, never()).insert(any(MesWmItemReceiptDetailDO.class));
    }

    @Test
    void unrelatedReceiptLineIsRejected() {
        MesWmItemReceiptDetailSaveReqVO request = prepare(22L, true).setReceiptId(99L);
        reset(itemService);
        assertServiceException(() -> service.createItemReceiptDetail(request), WM_ITEM_RECEIPT_DETAIL_LINE_MISMATCH);
        verifyNoInteractions(materialStockService);
    }

    @Test
    void nonBatchItemCanRemainWithoutBatch() {
        service.createItemReceiptDetail(prepare(null, false));
        verifyNoInteractions(batchService);
        verify(itemReceiptDetailMapper).insert(argThat((MesWmItemReceiptDetailDO row) -> row.getBatchId() == null));
    }
}
