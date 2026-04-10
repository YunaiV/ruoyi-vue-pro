package cn.iocoder.yudao.module.mes.service.wm.itemconsume;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.mes.controller.admin.wm.materialstock.vo.MesWmMaterialStockListReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.feedback.MesProFeedbackDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.route.MesProRouteDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.route.MesProRouteProductBomDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.itemconsume.MesWmItemConsumeDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.itemconsume.MesWmItemConsumeDetailDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.itemconsume.MesWmItemConsumeLineDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.materialstock.MesWmMaterialStockDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseAreaDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseLocationDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.itemconsume.MesWmItemConsumeMapper;
import cn.iocoder.yudao.module.mes.enums.MesBizTypeConstants;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmItemConsumeStatusEnum;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmTransactionTypeEnum;
import cn.iocoder.yudao.module.mes.service.pro.route.MesProRouteProductBomService;
import cn.iocoder.yudao.module.mes.service.pro.route.MesProRouteService;
import cn.iocoder.yudao.module.mes.service.wm.materialstock.MesWmMaterialStockService;
import cn.iocoder.yudao.module.mes.service.wm.transaction.MesWmTransactionService;
import cn.iocoder.yudao.module.mes.service.wm.transaction.dto.MesWmTransactionSaveReqDTO;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseAreaService;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseLocationService;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.PRO_FEEDBACK_ROUTE_PROCESS_INVALID;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.PRO_ROUTE_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link MesWmItemConsumeServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
@Import(MesWmItemConsumeServiceImpl.class)
public class MesWmItemConsumeServiceImplTest extends BaseDbUnitTest {

    @Resource
    private MesWmItemConsumeServiceImpl itemConsumeService;

    @Resource
    private MesWmItemConsumeMapper itemConsumeMapper;

    @MockitoBean
    private MesWmItemConsumeLineService itemConsumeLineService;
    @MockitoBean
    private MesWmItemConsumeDetailService itemConsumeDetailService;
    @MockitoBean
    private MesProRouteProductBomService routeProductBomService;
    @MockitoBean
    private MesProRouteService routeService;
    @MockitoBean
    private MesWmTransactionService wmTransactionService;
    @MockitoBean
    private MesWmWarehouseService warehouseService;
    @MockitoBean
    private MesWmWarehouseLocationService locationService;
    @MockitoBean
    private MesWmWarehouseAreaService areaService;
    @MockitoBean
    private MesWmMaterialStockService materialStockService;

    // ========== 公共 mock 数据 ==========

    private static final Long WAREHOUSE_ID = 100L;
    private static final Long LOCATION_ID = 200L;
    private static final Long AREA_ID = 300L;

    private MesWmWarehouseDO virtualWarehouse;
    private MesWmWarehouseLocationDO virtualLocation;
    private MesWmWarehouseAreaDO virtualArea;

    @BeforeEach
    public void setUp() {
        virtualWarehouse = new MesWmWarehouseDO();
        virtualWarehouse.setId(WAREHOUSE_ID);
        virtualWarehouse.setCode(MesWmWarehouseDO.WIP_VIRTUAL_WAREHOUSE);

        virtualLocation = new MesWmWarehouseLocationDO();
        virtualLocation.setId(LOCATION_ID);
        virtualLocation.setCode(MesWmWarehouseLocationDO.WIP_VIRTUAL_LOCATION);

        virtualArea = new MesWmWarehouseAreaDO();
        virtualArea.setId(AREA_ID);
        virtualArea.setCode(MesWmWarehouseAreaDO.WIP_VIRTUAL_AREA);

        when(warehouseService.getWarehouseByCode(MesWmWarehouseDO.WIP_VIRTUAL_WAREHOUSE))
                .thenReturn(virtualWarehouse);
        when(locationService.getWarehouseLocationByCode(MesWmWarehouseLocationDO.WIP_VIRTUAL_LOCATION))
                .thenReturn(virtualLocation);
        when(areaService.getWarehouseAreaByCode(MesWmWarehouseAreaDO.WIP_VIRTUAL_AREA))
                .thenReturn(virtualArea);

        lenient().when(routeService.validateRouteExists(anyLong())).thenAnswer(invocation -> {
            Long routeId = invocation.getArgument(0);
            return new MesProRouteDO().setId(routeId);
        });

        // mock createItemConsumeLineBatch：模拟 insertBatch 后填充 ID
        AtomicLong lineIdSeq = new AtomicLong(1000L);
        doAnswer(invocation -> {
            List<MesWmItemConsumeLineDO> lines = invocation.getArgument(0);
            for (MesWmItemConsumeLineDO line : lines) {
                line.setId(lineIdSeq.getAndIncrement());
            }
            return null;
        }).when(itemConsumeLineService).createItemConsumeLineBatch(any());
    }

    private MesProFeedbackDO buildFeedback() {
        return MesProFeedbackDO.builder()
                .id(randomLongId()).workOrderId(randomLongId()).taskId(randomLongId())
                .workstationId(randomLongId()).processId(randomLongId()).routeId(randomLongId())
                .itemId(randomLongId()).feedbackQuantity(BigDecimal.TEN)
                .build();
    }

    // ========== generateItemConsume ==========

    @Test
    public void testGenerateItemConsume_routeContextIncomplete() {
        MesProFeedbackDO feedback = buildFeedback();
        feedback.setRouteId(null);
        assertServiceException(() -> itemConsumeService.generateItemConsume(feedback), PRO_FEEDBACK_ROUTE_PROCESS_INVALID);
        verify(routeProductBomService, never()).getRouteProductBomList(any(), any(), any());
    }

    @Test
    public void testGenerateItemConsume_routeNotExists() {
        MesProFeedbackDO feedback = buildFeedback();
        doThrow(ServiceExceptionUtil.exception(PRO_ROUTE_NOT_EXISTS)).when(routeService)
                .validateRouteExists(feedback.getRouteId());
        assertServiceException(() -> itemConsumeService.generateItemConsume(feedback), PRO_ROUTE_NOT_EXISTS);
        verify(routeProductBomService, never()).getRouteProductBomList(any(), any(), any());
    }

    @Test
    public void testGenerateItemConsume_noBom() {
        // 准备
        MesProFeedbackDO feedback = buildFeedback();
        when(routeProductBomService.getRouteProductBomList(
                feedback.getRouteId(), feedback.getProcessId(), feedback.getItemId()))
                .thenReturn(Collections.emptyList());

        // 调用
        MesWmItemConsumeDO result = itemConsumeService.generateItemConsume(feedback);

        // 断言
        assertNull(result);
        verify(itemConsumeLineService, never()).createItemConsumeLineBatch(any());
        verify(itemConsumeDetailService, never()).createItemConsumeDetailBatch(any());
    }

    @Test
    public void testGenerateItemConsume_noStock() {
        // 准备：1 个 BOM 物料，线边库无库存
        MesProFeedbackDO feedback = buildFeedback();
        Long bomItemId = randomLongId();
        MesProRouteProductBomDO bom = new MesProRouteProductBomDO();
        bom.setItemId(bomItemId);
        bom.setQuantity(new BigDecimal("2")); // 用料比例 = 2
        when(routeProductBomService.getRouteProductBomList(any(), any(), any()))
                .thenReturn(ListUtil.of(bom));
        when(materialStockService.getMaterialStockList(any(MesWmMaterialStockListReqVO.class)))
                .thenReturn(Collections.emptyList());

        // 调用
        MesWmItemConsumeDO result = itemConsumeService.generateItemConsume(feedback);

        // 断言 1：消耗单头已入库
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(MesWmItemConsumeStatusEnum.PREPARE.getStatus(), result.getStatus());
        assertEquals(feedback.getId(), result.getFeedbackId());

        // 断言 2：消耗行正确
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<MesWmItemConsumeLineDO>> lineCaptor = ArgumentCaptor.forClass(List.class);
        verify(itemConsumeLineService).createItemConsumeLineBatch(lineCaptor.capture());
        List<MesWmItemConsumeLineDO> lines = lineCaptor.getValue();
        assertEquals(1, lines.size());
        assertEquals(bomItemId, lines.get(0).getItemId());
        // 消耗数量 = 2 × 10 = 20
        assertEquals(0, new BigDecimal("20").compareTo(lines.get(0).getQuantity()));

        // 断言 3：生成 1 条无批次的 detail
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<MesWmItemConsumeDetailDO>> detailCaptor = ArgumentCaptor.forClass(List.class);
        verify(itemConsumeDetailService).createItemConsumeDetailBatch(detailCaptor.capture());
        List<MesWmItemConsumeDetailDO> details = detailCaptor.getValue();
        assertEquals(1, details.size());
        MesWmItemConsumeDetailDO detail = details.get(0);
        assertEquals(bomItemId, detail.getItemId());
        assertEquals(0, new BigDecimal("20").compareTo(detail.getQuantity()));
        assertNull(detail.getMaterialStockId());
        assertNull(detail.getBatchId());
        assertEquals(WAREHOUSE_ID, detail.getWarehouseId());
        assertEquals(LOCATION_ID, detail.getLocationId());
        assertEquals(AREA_ID, detail.getAreaId());
    }

    @Test
    public void testGenerateItemConsume_singleBatchSufficient() {
        // 准备：1 个 BOM 物料，线边库有 1 个批次且库存充足
        MesProFeedbackDO feedback = buildFeedback();
        Long bomItemId = randomLongId();
        MesProRouteProductBomDO bom = new MesProRouteProductBomDO();
        bom.setItemId(bomItemId);
        bom.setQuantity(new BigDecimal("3")); // 消耗 = 3 × 10 = 30
        when(routeProductBomService.getRouteProductBomList(any(), any(), any()))
                .thenReturn(ListUtil.of(bom));

        Long stockId = randomLongId();
        Long batchId = randomLongId();
        MesWmMaterialStockDO stock = new MesWmMaterialStockDO();
        stock.setId(stockId);
        stock.setItemId(bomItemId);
        stock.setQuantity(new BigDecimal("50")); // 库存 50 > 需求 30
        stock.setBatchId(batchId);
        stock.setBatchCode("BATCH-001");
        when(materialStockService.getMaterialStockList(any(MesWmMaterialStockListReqVO.class)))
                .thenReturn(ListUtil.of(stock));

        // 调用
        MesWmItemConsumeDO result = itemConsumeService.generateItemConsume(feedback);

        // 断言：生成 1 条带批次的 detail
        assertNotNull(result);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<MesWmItemConsumeDetailDO>> detailCaptor = ArgumentCaptor.forClass(List.class);
        verify(itemConsumeDetailService).createItemConsumeDetailBatch(detailCaptor.capture());
        List<MesWmItemConsumeDetailDO> details = detailCaptor.getValue();
        assertEquals(1, details.size());
        MesWmItemConsumeDetailDO detail = details.get(0);
        assertEquals(0, new BigDecimal("30").compareTo(detail.getQuantity()));
        assertEquals(stockId, detail.getMaterialStockId());
        assertEquals(batchId, detail.getBatchId());
        assertEquals("BATCH-001", detail.getBatchCode());
    }

    @Test
    public void testGenerateItemConsume_multiBatchFifo() {
        // 准备：1 个 BOM 物料，需消耗 30，线边库有 2 个批次（20 + 15）
        MesProFeedbackDO feedback = buildFeedback();
        Long bomItemId = randomLongId();
        MesProRouteProductBomDO bom = new MesProRouteProductBomDO();
        bom.setItemId(bomItemId);
        bom.setQuantity(new BigDecimal("3")); // 消耗 = 3 × 10 = 30
        when(routeProductBomService.getRouteProductBomList(any(), any(), any()))
                .thenReturn(ListUtil.of(bom));

        Long stockId1 = randomLongId();
        Long stockId2 = randomLongId();
        MesWmMaterialStockDO stock1 = new MesWmMaterialStockDO();
        stock1.setId(stockId1);
        stock1.setItemId(bomItemId);
        stock1.setQuantity(new BigDecimal("20")); // 先进先出：先消耗这批
        stock1.setBatchId(1L);
        stock1.setBatchCode("BATCH-001");

        MesWmMaterialStockDO stock2 = new MesWmMaterialStockDO();
        stock2.setId(stockId2);
        stock2.setItemId(bomItemId);
        stock2.setQuantity(new BigDecimal("15"));
        stock2.setBatchId(2L);
        stock2.setBatchCode("BATCH-002");

        // FIFO 顺序返回
        when(materialStockService.getMaterialStockList(any(MesWmMaterialStockListReqVO.class)))
                .thenReturn(Arrays.asList(stock1, stock2));

        // 调用
        MesWmItemConsumeDO result = itemConsumeService.generateItemConsume(feedback);

        // 断言：生成 2 条带批次的 detail（20 + 10）
        assertNotNull(result);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<MesWmItemConsumeDetailDO>> detailCaptor = ArgumentCaptor.forClass(List.class);
        verify(itemConsumeDetailService).createItemConsumeDetailBatch(detailCaptor.capture());
        List<MesWmItemConsumeDetailDO> details = detailCaptor.getValue();
        assertEquals(2, details.size());

        // 第 1 条：批次 1 全部消耗 20
        assertEquals(stockId1, details.get(0).getMaterialStockId());
        assertEquals(0, new BigDecimal("20").compareTo(details.get(0).getQuantity()));
        assertEquals("BATCH-001", details.get(0).getBatchCode());

        // 第 2 条：批次 2 消耗 10（30 - 20 = 10）
        assertEquals(stockId2, details.get(1).getMaterialStockId());
        assertEquals(0, new BigDecimal("10").compareTo(details.get(1).getQuantity()));
        assertEquals("BATCH-002", details.get(1).getBatchCode());
    }

    @Test
    public void testGenerateItemConsume_insufficientStock() {
        // 准备：需消耗 30，线边库只有 1 个批次（库存 18），不足部分生成无批次 detail
        MesProFeedbackDO feedback = buildFeedback();
        Long bomItemId = randomLongId();
        MesProRouteProductBomDO bom = new MesProRouteProductBomDO();
        bom.setItemId(bomItemId);
        bom.setQuantity(new BigDecimal("3")); // 消耗 = 3 × 10 = 30
        when(routeProductBomService.getRouteProductBomList(any(), any(), any()))
                .thenReturn(ListUtil.of(bom));

        Long stockId = randomLongId();
        MesWmMaterialStockDO stock = new MesWmMaterialStockDO();
        stock.setId(stockId);
        stock.setItemId(bomItemId);
        stock.setQuantity(new BigDecimal("18")); // 不够 30
        stock.setBatchId(1L);
        stock.setBatchCode("BATCH-001");
        when(materialStockService.getMaterialStockList(any(MesWmMaterialStockListReqVO.class)))
                .thenReturn(ListUtil.of(stock));

        // 调用
        MesWmItemConsumeDO result = itemConsumeService.generateItemConsume(feedback);

        // 断言：2 条 detail（18 带批次 + 12 无批次）
        assertNotNull(result);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<MesWmItemConsumeDetailDO>> detailCaptor = ArgumentCaptor.forClass(List.class);
        verify(itemConsumeDetailService).createItemConsumeDetailBatch(detailCaptor.capture());
        List<MesWmItemConsumeDetailDO> details = detailCaptor.getValue();
        assertEquals(2, details.size());

        // 第 1 条：批次全部消耗 18
        assertEquals(stockId, details.get(0).getMaterialStockId());
        assertEquals(0, new BigDecimal("18").compareTo(details.get(0).getQuantity()));
        assertEquals("BATCH-001", details.get(0).getBatchCode());

        // 第 2 条：剩余 12 无批次
        assertNull(details.get(1).getMaterialStockId());
        assertEquals(0, new BigDecimal("12").compareTo(details.get(1).getQuantity()));
        assertNull(details.get(1).getBatchId());
    }

    @Test
    public void testGenerateItemConsume_multipleBomItems() {
        // 准备：2 个 BOM 物料
        MesProFeedbackDO feedback = buildFeedback();
        Long itemA = randomLongId();
        Long itemB = randomLongId();

        MesProRouteProductBomDO bomA = new MesProRouteProductBomDO();
        bomA.setItemId(itemA);
        bomA.setQuantity(new BigDecimal("1")); // 消耗 = 1 × 10 = 10

        MesProRouteProductBomDO bomB = new MesProRouteProductBomDO();
        bomB.setItemId(itemB);
        bomB.setQuantity(new BigDecimal("2")); // 消耗 = 2 × 10 = 20

        when(routeProductBomService.getRouteProductBomList(any(), any(), any()))
                .thenReturn(Arrays.asList(bomA, bomB));

        // 物料 A 有库存，物料 B 无库存
        MesWmMaterialStockDO stockA = new MesWmMaterialStockDO();
        stockA.setId(randomLongId());
        stockA.setItemId(itemA);
        stockA.setQuantity(new BigDecimal("100"));
        stockA.setBatchId(1L);
        stockA.setBatchCode("BATCH-A");

        when(materialStockService.getMaterialStockList(argThat((MesWmMaterialStockListReqVO req) ->
                req != null && itemA.equals(req.getItemId()))))
                .thenReturn(ListUtil.of(stockA));
        when(materialStockService.getMaterialStockList(argThat((MesWmMaterialStockListReqVO req) ->
                req != null && itemB.equals(req.getItemId()))))
                .thenReturn(Collections.emptyList());

        // 调用
        MesWmItemConsumeDO result = itemConsumeService.generateItemConsume(feedback);

        // 断言：2 条消耗行，2 条 detail
        assertNotNull(result);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<MesWmItemConsumeLineDO>> lineCaptor = ArgumentCaptor.forClass(List.class);
        verify(itemConsumeLineService).createItemConsumeLineBatch(lineCaptor.capture());
        assertEquals(2, lineCaptor.getValue().size());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<MesWmItemConsumeDetailDO>> detailCaptor = ArgumentCaptor.forClass(List.class);
        verify(itemConsumeDetailService).createItemConsumeDetailBatch(detailCaptor.capture());
        List<MesWmItemConsumeDetailDO> details = detailCaptor.getValue();
        assertEquals(2, details.size());

        // 物料 A：有批次
        MesWmItemConsumeDetailDO detailA = CollUtil.findOne(details,
                d -> itemA.equals(d.getItemId()));
        assertEquals(0, new BigDecimal("10").compareTo(detailA.getQuantity()));
        assertNotNull(detailA.getMaterialStockId());
        assertEquals("BATCH-A", detailA.getBatchCode());

        // 物料 B：无批次
        MesWmItemConsumeDetailDO detailB = CollUtil.findOne(details,
                d -> itemB.equals(d.getItemId()));
        assertEquals(0, new BigDecimal("20").compareTo(detailB.getQuantity()));
        assertNull(detailB.getMaterialStockId());
    }

    // ========== finishItemConsume ==========

    @Test
    public void testFinishItemConsume_withDetails() {
        // 准备：插入一条消耗记录
        MesWmItemConsumeDO consume = MesWmItemConsumeDO.builder()
                .workOrderId(randomLongId()).taskId(randomLongId())
                .workstationId(randomLongId()).processId(randomLongId())
                .feedbackId(randomLongId()).consumeDate(java.time.LocalDateTime.now())
                .status(MesWmItemConsumeStatusEnum.PREPARE.getStatus()).build();
        itemConsumeMapper.insert(consume);

        // mock detail 列表
        Long lineId1 = randomLongId();
        Long lineId2 = randomLongId();
        MesWmItemConsumeDetailDO detail1 = MesWmItemConsumeDetailDO.builder()
                .consumeId(consume.getId()).lineId(lineId1).itemId(randomLongId())
                .quantity(new BigDecimal("20")).batchId(1L).batchCode("BATCH-001")
                .warehouseId(WAREHOUSE_ID).locationId(LOCATION_ID).areaId(AREA_ID).build();
        MesWmItemConsumeDetailDO detail2 = MesWmItemConsumeDetailDO.builder()
                .consumeId(consume.getId()).lineId(lineId2).itemId(randomLongId())
                .quantity(new BigDecimal("12"))
                .warehouseId(WAREHOUSE_ID).locationId(LOCATION_ID).areaId(AREA_ID).build();
        when(itemConsumeDetailService.getItemConsumeDetailListByConsumeId(consume.getId()))
                .thenReturn(Arrays.asList(detail1, detail2));

        // 调用
        itemConsumeService.finishItemConsume(consume.getId());

        // 断言 1：状态更新为已完成
        MesWmItemConsumeDO updated = itemConsumeMapper.selectById(consume.getId());
        assertEquals(MesWmItemConsumeStatusEnum.FINISHED.getStatus(), updated.getStatus());

        // 断言 2：创建了 2 次库存事务
        ArgumentCaptor<MesWmTransactionSaveReqDTO> txCaptor = ArgumentCaptor.forClass(MesWmTransactionSaveReqDTO.class);
        verify(wmTransactionService, times(2)).createTransaction(txCaptor.capture());
        List<MesWmTransactionSaveReqDTO> txList = txCaptor.getAllValues();

        // 验证第 1 条事务
        MesWmTransactionSaveReqDTO tx1 = txList.get(0);
        assertEquals(MesWmTransactionTypeEnum.OUT.getType(), tx1.getType());
        assertEquals(detail1.getItemId(), tx1.getItemId());
        assertEquals(0, new BigDecimal("-20").compareTo(tx1.getQuantity()));
        assertEquals(1L, tx1.getBatchId());
        assertEquals("BATCH-001", tx1.getBatchCode());
        assertEquals(WAREHOUSE_ID, tx1.getWarehouseId());
        assertFalse(tx1.getCheckFlag());
        assertEquals(MesBizTypeConstants.WM_ITEM_CONSUME, tx1.getBizType());
        assertEquals(consume.getId(), tx1.getBizId());
        assertEquals(lineId1, tx1.getBizLineId());

        // 验证第 2 条事务（无批次）
        MesWmTransactionSaveReqDTO tx2 = txList.get(1);
        assertEquals(detail2.getItemId(), tx2.getItemId());
        assertEquals(0, new BigDecimal("-12").compareTo(tx2.getQuantity()));
        assertNull(tx2.getBatchId());
    }

    @Test
    public void testFinishItemConsume_emptyDetails() {
        // 准备：插入一条消耗记录，但没有 detail
        MesWmItemConsumeDO consume = MesWmItemConsumeDO.builder()
                .workOrderId(randomLongId()).taskId(randomLongId())
                .workstationId(randomLongId()).processId(randomLongId())
                .feedbackId(randomLongId()).consumeDate(java.time.LocalDateTime.now())
                .status(MesWmItemConsumeStatusEnum.PREPARE.getStatus()).build();
        itemConsumeMapper.insert(consume);

        when(itemConsumeDetailService.getItemConsumeDetailListByConsumeId(consume.getId()))
                .thenReturn(Collections.emptyList());

        // 调用
        itemConsumeService.finishItemConsume(consume.getId());

        // 断言：状态更新为已完成，但无事务创建
        MesWmItemConsumeDO updated = itemConsumeMapper.selectById(consume.getId());
        assertEquals(MesWmItemConsumeStatusEnum.FINISHED.getStatus(), updated.getStatus());
        verify(wmTransactionService, never()).createTransaction(any());
    }

    // ========== getByFeedbackId ==========

    @Test
    public void testGetByFeedbackId_exists() {
        Long feedbackId = randomLongId();
        MesWmItemConsumeDO consume = MesWmItemConsumeDO.builder()
                .workOrderId(randomLongId()).taskId(randomLongId())
                .workstationId(randomLongId()).processId(randomLongId())
                .feedbackId(feedbackId).consumeDate(java.time.LocalDateTime.now())
                .status(MesWmItemConsumeStatusEnum.PREPARE.getStatus()).build();
        itemConsumeMapper.insert(consume);

        MesWmItemConsumeDO result = itemConsumeService.getByFeedbackId(feedbackId);
        assertNotNull(result);
        assertEquals(consume.getId(), result.getId());
    }

    @Test
    public void testGetByFeedbackId_notExists() {
        MesWmItemConsumeDO result = itemConsumeService.getByFeedbackId(randomLongId());
        assertNull(result);
    }

}
