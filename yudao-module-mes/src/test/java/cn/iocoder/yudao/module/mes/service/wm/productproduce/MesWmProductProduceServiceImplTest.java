package cn.iocoder.yudao.module.mes.service.wm.productproduce;

import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.productproduce.MesWmProductProduceDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.productproduce.MesWmProductProduceDetailDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.productproduce.MesWmProductProduceLineDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseAreaDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseLocationDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.productproduce.MesWmProductProduceMapper;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmProductProduceStatusEnum;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmQualityStatusEnum;
import cn.iocoder.yudao.module.mes.service.pro.workorder.MesProWorkOrderService;
import cn.iocoder.yudao.module.mes.service.wm.batch.MesWmBatchService;
import cn.iocoder.yudao.module.mes.service.wm.transaction.MesWmTransactionService;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseAreaService;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseLocationService;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link MesWmProductProduceServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
@Import(MesWmProductProduceServiceImpl.class)
public class MesWmProductProduceServiceImplTest extends BaseDbUnitTest {

    @Resource
    private MesWmProductProduceServiceImpl productProduceService;

    @Resource
    private MesWmProductProduceMapper productProduceMapper;

    @MockitoBean
    private MesWmProductProduceLineService productProduceLineService;
    @MockitoBean
    private MesWmProductProduceDetailService productProduceDetailService;
    @MockitoBean
    private MesProWorkOrderService workOrderService;
    @MockitoBean
    private MesWmBatchService batchService;
    @MockitoBean
    private MesWmTransactionService wmTransactionService;
    @MockitoBean
    private MesWmWarehouseService warehouseService;
    @MockitoBean
    private MesWmWarehouseLocationService locationService;
    @MockitoBean
    private MesWmWarehouseAreaService areaService;

    @Test
    public void testSplitPendingAndFinishProduce_withUnqualified() {
        // 准备数据：插入一个 PREPARE 状态的产出单
        Long feedbackId = randomLongId();
        Long sourceLineId = 100L;
        Long itemId = randomLongId();
        Long batchId = randomLongId();
        String batchCode = "BATCH-001";

        MesWmProductProduceDO produce = randomPojo(MesWmProductProduceDO.class, o -> {
            o.setFeedbackId(feedbackId);
            o.setStatus(MesWmProductProduceStatusEnum.PREPARE.getStatus());
        });
        productProduceMapper.insert(produce);

        // mock: 虚拟线边库
        Long whId = randomLongId(), locId = randomLongId(), areaId = randomLongId();
        MesWmWarehouseDO wh = new MesWmWarehouseDO(); wh.setId(whId);
        MesWmWarehouseLocationDO loc = new MesWmWarehouseLocationDO(); loc.setId(locId);
        MesWmWarehouseAreaDO area = new MesWmWarehouseAreaDO(); area.setId(areaId);
        when(warehouseService.getWarehouseByCode(MesWmWarehouseDO.WIP_VIRTUAL_WAREHOUSE)).thenReturn(wh);
        when(locationService.getWarehouseLocationByCode(MesWmWarehouseLocationDO.WIP_VIRTUAL_LOCATION)).thenReturn(loc);
        when(areaService.getWarehouseAreaByCode(MesWmWarehouseAreaDO.WIP_VIRTUAL_AREA)).thenReturn(area);

        // mock: 通过 sourceLineId 直接定位 PENDING 产出行
        MesWmProductProduceLineDO pendingLine = MesWmProductProduceLineDO.builder()
                .id(sourceLineId).produceId(produce.getId()).feedbackId(feedbackId)
                .itemId(itemId).quantity(BigDecimal.valueOf(100))
                .batchId(batchId).batchCode(batchCode)
                .qualityStatus(MesWmQualityStatusEnum.PENDING.getStatus())
                .build();
        when(productProduceLineService.validateProductProduceLineExists(sourceLineId))
                .thenReturn(pendingLine);

        // mock: finishProductProduce 内部查行和明细（用于创建库存事务）
        MesWmProductProduceLineDO qualifiedLine = MesWmProductProduceLineDO.builder()
                .id(100L).produceId(produce.getId()).itemId(itemId)
                .quantity(BigDecimal.valueOf(80))
                .qualityStatus(MesWmQualityStatusEnum.PASS.getStatus())
                .build();
        MesWmProductProduceLineDO unqualifiedLine = MesWmProductProduceLineDO.builder()
                .id(101L).produceId(produce.getId()).itemId(itemId)
                .quantity(BigDecimal.valueOf(20))
                .qualityStatus(MesWmQualityStatusEnum.FAIL.getStatus())
                .build();
        when(productProduceLineService.getProductProduceLineListByProduceId(produce.getId()))
                .thenReturn(ListUtil.of(qualifiedLine, unqualifiedLine));

        // mock: finishProductProduce 中按行查明细
        MesWmProductProduceDetailDO qualifiedDetail = MesWmProductProduceDetailDO.builder()
                .lineId(100L).quantity(BigDecimal.valueOf(80)).build();
        MesWmProductProduceDetailDO unqualifiedDetail = MesWmProductProduceDetailDO.builder()
                .lineId(101L).quantity(BigDecimal.valueOf(20)).build();
        when(productProduceDetailService.getProductProduceDetailListByLineId(100L))
                .thenReturn(ListUtil.of(qualifiedDetail));
        when(productProduceDetailService.getProductProduceDetailListByLineId(101L))
                .thenReturn(ListUtil.of(unqualifiedDetail));

        // 调用
        productProduceService.splitPendingAndFinishProduce(feedbackId, sourceLineId,
                BigDecimal.valueOf(80), BigDecimal.valueOf(20));

        // 断言 1：不合格品行 - 新建了一行
        ArgumentCaptor<MesWmProductProduceLineDO> lineCaptor = ArgumentCaptor.forClass(MesWmProductProduceLineDO.class);
        verify(productProduceLineService).createProductProduceLine(lineCaptor.capture());
        MesWmProductProduceLineDO createdLine = lineCaptor.getValue();
        assertEquals(MesWmQualityStatusEnum.FAIL.getStatus(), createdLine.getQualityStatus());
        assertEquals(0, BigDecimal.valueOf(20).compareTo(createdLine.getQuantity()));
        assertEquals(itemId, createdLine.getItemId());

        // 断言 2：原待检行 - 更新为合格品
        ArgumentCaptor<MesWmProductProduceLineDO> updateCaptor = ArgumentCaptor.forClass(MesWmProductProduceLineDO.class);
        verify(productProduceLineService).updateProductProduceLine(updateCaptor.capture());
        MesWmProductProduceLineDO updatedLine = updateCaptor.getValue();
        assertEquals(MesWmQualityStatusEnum.PASS.getStatus(), updatedLine.getQualityStatus());
        assertEquals(0, BigDecimal.valueOf(80).compareTo(updatedLine.getQuantity()));

        // 断言 3：生成了 2 条明细（不合格品明细 + 合格品明细）
        verify(productProduceDetailService, times(2)).createProductProduceDetail(any());

        // 断言 4：产出单状态更新为已完成
        MesWmProductProduceDO updatedProduce = productProduceMapper.selectById(produce.getId());
        assertEquals(MesWmProductProduceStatusEnum.FINISHED.getStatus(), updatedProduce.getStatus());

        // 断言 5：创建了库存事务
        verify(wmTransactionService, times(2)).createTransaction(any());
    }

    @Test
    public void testSplitPendingAndFinishProduce_allQualified() {
        // 准备数据：全部合格品场景
        Long feedbackId = randomLongId();
        Long sourceLineId = 200L;
        Long itemId = randomLongId();

        MesWmProductProduceDO produce = randomPojo(MesWmProductProduceDO.class, o -> {
            o.setFeedbackId(feedbackId);
            o.setStatus(MesWmProductProduceStatusEnum.PREPARE.getStatus());
        });
        productProduceMapper.insert(produce);

        // mock: 虚拟线边库
        Long whId = randomLongId(), locId = randomLongId(), areaId = randomLongId();
        MesWmWarehouseDO wh = new MesWmWarehouseDO(); wh.setId(whId);
        MesWmWarehouseLocationDO loc = new MesWmWarehouseLocationDO(); loc.setId(locId);
        MesWmWarehouseAreaDO area = new MesWmWarehouseAreaDO(); area.setId(areaId);
        when(warehouseService.getWarehouseByCode(any())).thenReturn(wh);
        when(locationService.getWarehouseLocationByCode(any())).thenReturn(loc);
        when(areaService.getWarehouseAreaByCode(any())).thenReturn(area);

        // mock: 通过 sourceLineId 直接定位 PENDING 产出行
        MesWmProductProduceLineDO pendingLine = MesWmProductProduceLineDO.builder()
                .id(sourceLineId).produceId(produce.getId()).feedbackId(feedbackId)
                .itemId(itemId).quantity(BigDecimal.valueOf(50))
                .qualityStatus(MesWmQualityStatusEnum.PENDING.getStatus())
                .build();
        when(productProduceLineService.validateProductProduceLineExists(sourceLineId))
                .thenReturn(pendingLine);

        // mock: finishProductProduce 查行和明细
        MesWmProductProduceLineDO qualifiedLine = MesWmProductProduceLineDO.builder()
                .id(200L).produceId(produce.getId()).itemId(itemId)
                .quantity(BigDecimal.valueOf(50))
                .qualityStatus(MesWmQualityStatusEnum.PASS.getStatus())
                .build();
        when(productProduceLineService.getProductProduceLineListByProduceId(produce.getId()))
                .thenReturn(ListUtil.of(qualifiedLine));
        MesWmProductProduceDetailDO detail = MesWmProductProduceDetailDO.builder()
                .lineId(200L).quantity(BigDecimal.valueOf(50)).build();
        when(productProduceDetailService.getProductProduceDetailListByLineId(200L))
                .thenReturn(ListUtil.of(detail));

        // 调用：不合格品数量 = 0
        productProduceService.splitPendingAndFinishProduce(feedbackId, sourceLineId,
                BigDecimal.valueOf(50), BigDecimal.ZERO);

        // 断言 1：没有新建行（不合格品数量为 0）
        verify(productProduceLineService, never()).createProductProduceLine(any());

        // 断言 2：更新了原待检行为合格
        ArgumentCaptor<MesWmProductProduceLineDO> updateCaptor = ArgumentCaptor.forClass(MesWmProductProduceLineDO.class);
        verify(productProduceLineService).updateProductProduceLine(updateCaptor.capture());
        assertEquals(MesWmQualityStatusEnum.PASS.getStatus(), updateCaptor.getValue().getQualityStatus());

        // 断言 3：只生成了 1 条明细（全部合格品）
        verify(productProduceDetailService, times(1)).createProductProduceDetail(any());

        // 断言 4：产出单状态更新为已完成
        MesWmProductProduceDO updatedProduce = productProduceMapper.selectById(produce.getId());
        assertEquals(MesWmProductProduceStatusEnum.FINISHED.getStatus(), updatedProduce.getStatus());
    }

    @Test
    public void testSplitPendingAndFinishProduce_produceNotExists() {
        // 调用不存在的 feedbackId，应该抛异常
        Long feedbackId = randomLongId();
        Long sourceLineId = randomLongId();
        ServiceException ex = assertThrows(ServiceException.class, () ->
                productProduceService.splitPendingAndFinishProduce(feedbackId, sourceLineId,
                        BigDecimal.TEN, BigDecimal.ZERO));
        assertEquals(WM_PRODUCT_PRODUCE_NOT_EXISTS.getCode(), ex.getCode());
    }

    @Test
    public void testSplitPendingAndFinishProduce_lineNotExists() {
        // 准备数据：产出单存在，但 sourceLineId 对应的行不存在
        Long feedbackId = randomLongId();
        Long sourceLineId = randomLongId();
        MesWmProductProduceDO produce = randomPojo(MesWmProductProduceDO.class, o -> {
            o.setFeedbackId(feedbackId);
            o.setStatus(MesWmProductProduceStatusEnum.PREPARE.getStatus());
        });
        productProduceMapper.insert(produce);

        // mock: 虚拟线边库
        MesWmWarehouseDO wh = new MesWmWarehouseDO(); wh.setId(1L);
        MesWmWarehouseLocationDO loc = new MesWmWarehouseLocationDO(); loc.setId(1L);
        MesWmWarehouseAreaDO area = new MesWmWarehouseAreaDO(); area.setId(1L);
        when(warehouseService.getWarehouseByCode(any())).thenReturn(wh);
        when(locationService.getWarehouseLocationByCode(any())).thenReturn(loc);
        when(areaService.getWarehouseAreaByCode(any())).thenReturn(area);

        // mock: validateProductProduceLineExists 抛异常
        when(productProduceLineService.validateProductProduceLineExists(sourceLineId))
                .thenThrow(new ServiceException(WM_PRODUCT_PRODUCE_LINE_NOT_EXISTS));

        // 调用，应该抛异常
        ServiceException ex = assertThrows(ServiceException.class, () ->
                productProduceService.splitPendingAndFinishProduce(feedbackId, sourceLineId,
                        BigDecimal.TEN, BigDecimal.ZERO));
        assertEquals(WM_PRODUCT_PRODUCE_LINE_NOT_EXISTS.getCode(), ex.getCode());
    }

}
