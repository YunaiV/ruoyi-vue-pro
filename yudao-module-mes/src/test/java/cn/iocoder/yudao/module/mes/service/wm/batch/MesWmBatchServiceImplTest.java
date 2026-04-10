package cn.iocoder.yudao.module.mes.service.wm.batch;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.feedback.MesProFeedbackDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.batch.MesWmBatchDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.itemconsume.MesWmItemConsumeDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.itemconsume.MesWmItemConsumeDetailDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.productproduce.MesWmProductProduceDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.productproduce.MesWmProductProduceDetailDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.productproduce.MesWmProductProduceLineDO;
import cn.iocoder.yudao.module.mes.dal.mysql.pro.feedback.MesProFeedbackMapper;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.batch.MesWmBatchMapper;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.itemconsume.MesWmItemConsumeDetailMapper;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.itemconsume.MesWmItemConsumeMapper;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.productproduce.MesWmProductProduceDetailMapper;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.productproduce.MesWmProductProduceLineMapper;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.productproduce.MesWmProductProduceMapper;
import cn.iocoder.yudao.module.mes.service.md.autocode.MesMdAutoCodeRecordService;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemBatchConfigService;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.wm.barcode.MesWmBarcodeService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link MesWmBatchServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
@Import(MesWmBatchServiceImpl.class)
public class MesWmBatchServiceImplTest extends BaseDbUnitTest {

    @Resource
    private MesWmBatchServiceImpl batchService;

    @Resource
    private MesWmBatchMapper batchMapper;

    @Resource
    private MesWmItemConsumeMapper consumeMapper;
    @Resource
    private MesWmItemConsumeDetailMapper consumeDetailMapper;
    @Resource
    private MesProFeedbackMapper feedbackMapper;
    @Resource
    private MesWmProductProduceMapper produceMapper;
    @Resource
    private MesWmProductProduceLineMapper produceLineMapper;
    @Resource
    private MesWmProductProduceDetailMapper produceDetailMapper;

    @MockitoBean
    private MesMdItemService itemService;
    @MockitoBean
    private MesMdItemBatchConfigService itemBatchConfigService;
    @MockitoBean
    private MesMdAutoCodeRecordService autoCodeRecordService;
    @MockitoBean
    private MesWmBarcodeService barcodeService;

    // ==================== 向前追溯 ====================

    @Test
    public void testGetForwardBatchList_nullCode() {
        // 传入 null code，应返回空列表（不抛异常）
        List<MesWmBatchDO> result = batchService.getForwardBatchList(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetForwardBatchList_noResults() {
        // 传入一个不存在的批次号，XML 查询返回空（因为关联表无数据）
        List<MesWmBatchDO> result = batchService.getForwardBatchList("NOT_EXIST_CODE");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== 向后追溯 ====================

    @Test
    public void testGetBackwardBatchList_nullCode() {
        // 传入 null code，应返回空列表（不抛异常）
        List<MesWmBatchDO> result = batchService.getBackwardBatchList(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetBackwardBatchList_noResults() {
        // 传入一个不存在的批次号，XML 查询返回空
        List<MesWmBatchDO> result = batchService.getBackwardBatchList("NOT_EXIST_CODE");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== 循环检测 ====================

    @Test
    public void testForwardBatchList_sameCodeDoesNotRecurse() {
        // 验证：传入相同 code 多次调用不会 StackOverflow
        // 由于数据库中没有环路数据，这里主要验证 visited set 的 null + 空数据安全
        List<MesWmBatchDO> result = batchService.getForwardBatchList("SAME_CODE");
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // 二次调用同一个 code，不应有任何异常
        List<MesWmBatchDO> result2 = batchService.getForwardBatchList("SAME_CODE");
        assertNotNull(result2);
        assertTrue(result2.isEmpty());
    }

    @Test
    public void testBackwardBatchList_sameCodeDoesNotRecurse() {
        // 验证向后追溯的 visited set 安全
        List<MesWmBatchDO> result = batchService.getBackwardBatchList("SAME_CODE");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== selectFirst ====================

    @Test
    public void testSelectFirst_noMatch() {
        // 查询不存在的 itemId，应返回 null
        MesWmBatchDO query = MesWmBatchDO.builder().itemId(randomLongId()).build();
        MesWmBatchDO result = batchMapper.selectFirst(query);
        assertNull(result);
    }

    @Test
    public void testSelectFirst_returnsSmallestId() {
        // 准备数据：插入两条相同条件的批次
        Long itemId = randomLongId();
        MesWmBatchDO batch1 = MesWmBatchDO.builder()
                .itemId(itemId).code("BATCH_001").build();
        MesWmBatchDO batch2 = MesWmBatchDO.builder()
                .itemId(itemId).code("BATCH_002").build();
        batchMapper.insert(batch1);
        batchMapper.insert(batch2);

        // 查询
        MesWmBatchDO query = MesWmBatchDO.builder().itemId(itemId).build();
        MesWmBatchDO result = batchMapper.selectFirst(query);

        // 断言：返回 ID 最小的
        assertNotNull(result);
        assertEquals(batch1.getId(), result.getId());
        assertEquals("BATCH_001", result.getCode());
    }

    @Test
    public void testSelectFirst_nullFieldMatching() {
        // 准备数据：一条有 vendorId，一条没有 vendorId
        Long itemId = randomLongId();
        Long vendorId = randomLongId();

        MesWmBatchDO batchWithVendor = MesWmBatchDO.builder()
                .itemId(itemId).code("BATCH_WITH_V").vendorId(vendorId).build();
        MesWmBatchDO batchWithoutVendor = MesWmBatchDO.builder()
                .itemId(itemId).code("BATCH_NO_V").build();
        batchMapper.insert(batchWithVendor);
        batchMapper.insert(batchWithoutVendor);

        // 查询：vendorId 为 null -> 应该只匹配 vendorId IS NULL 的记录
        MesWmBatchDO query = MesWmBatchDO.builder().itemId(itemId).build();
        MesWmBatchDO result = batchMapper.selectFirst(query);

        assertNotNull(result);
        assertEquals("BATCH_NO_V", result.getCode());
        assertNull(result.getVendorId());
    }

    @Test
    public void testSelectFirst_withVendorId() {
        // 准备数据
        Long itemId = randomLongId();
        Long vendorId = randomLongId();

        MesWmBatchDO batchWithVendor = MesWmBatchDO.builder()
                .itemId(itemId).code("BATCH_V1").vendorId(vendorId).build();
        MesWmBatchDO batchWithoutVendor = MesWmBatchDO.builder()
                .itemId(itemId).code("BATCH_NV").build();
        batchMapper.insert(batchWithVendor);
        batchMapper.insert(batchWithoutVendor);

        // 查询：指定 vendorId -> 只匹配有 vendorId 的
        MesWmBatchDO query = MesWmBatchDO.builder().itemId(itemId).vendorId(vendorId).build();
        MesWmBatchDO result = batchMapper.selectFirst(query);

        assertNotNull(result);
        assertEquals("BATCH_V1", result.getCode());
        assertEquals(vendorId, result.getVendorId());
    }

    // ==================== selectByCode ====================

    @Test
    public void testSelectByCode() {
        // 准备数据
        MesWmBatchDO batch = MesWmBatchDO.builder()
                .itemId(randomLongId()).code("TEST_CODE_001").build();
        batchMapper.insert(batch);

        // 查询
        MesWmBatchDO result = batchMapper.selectByCode("TEST_CODE_001");
        assertNotNull(result);
        assertEquals(batch.getId(), result.getId());

        // 查询不存在的
        MesWmBatchDO notFound = batchMapper.selectByCode("NOT_EXIST");
        assertNull(notFound);
    }

    // ==================== 向前追溯（集成测试）====================

    /**
     * 向前追溯集成测试：构造完整链路数据
     * 链路：消耗明细(icd, batchCode=RAW_BATCH) → 消耗单(ic) → 报工(pf) → 入库单(pp) → 入库行(ppl, batchCode=PRODUCT_BATCH)
     * 预期：查 RAW_BATCH 能找到 PRODUCT_BATCH
     */
    @Test
    public void testSelectListByForward_withData() {
        // 准备：公共 ID
        Long workOrderId = randomLongId();
        Long rawItemId = randomLongId();
        Long productItemId = randomLongId();
        Long productBatchId = randomLongId();
        LocalDateTime now = LocalDateTime.now();

        // 1. 报工记录
        MesProFeedbackDO feedback = MesProFeedbackDO.builder()
                .workOrderId(workOrderId).itemId(productItemId).build();
        feedbackMapper.insert(feedback);

        // 2. 消耗单
        MesWmItemConsumeDO consume = MesWmItemConsumeDO.builder()
                .workOrderId(workOrderId).taskId(randomLongId()).workstationId(randomLongId())
                .processId(randomLongId()).feedbackId(feedback.getId()).consumeDate(now).build();
        consumeMapper.insert(consume);

        // 3. 消耗明细（关联原材料批次 RAW_BATCH）
        MesWmItemConsumeDetailDO consumeDetail = MesWmItemConsumeDetailDO.builder()
                .consumeId(consume.getId()).lineId(randomLongId()).itemId(rawItemId)
                .quantity(BigDecimal.TEN).batchCode("RAW_BATCH")
                .warehouseId(randomLongId()).locationId(randomLongId()).areaId(randomLongId()).build();
        consumeDetailMapper.insert(consumeDetail);

        // 4. 生产入库单
        MesWmProductProduceDO produce = MesWmProductProduceDO.builder()
                .workOrderId(workOrderId).build();
        produceMapper.insert(produce);

        // 5. 入库行（关联成品批次 PRODUCT_BATCH）
        MesWmProductProduceLineDO produceLine = MesWmProductProduceLineDO.builder()
                .produceId(produce.getId()).itemId(productItemId)
                .batchId(productBatchId).batchCode("PRODUCT_BATCH").build();
        produceLineMapper.insert(produceLine);

        // 执行向前追溯
        List<MesWmBatchDO> result = batchMapper.selectListByForward("RAW_BATCH");

        // 断言
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("PRODUCT_BATCH", result.get(0).getCode());
        assertEquals(productBatchId, result.get(0).getId());
        assertEquals(workOrderId, result.get(0).getWorkOrderId());
        assertEquals(productItemId, result.get(0).getItemId());
    }

    /**
     * 向前追溯：已删除的消耗明细应被过滤（icd.deleted = 1）
     */
    @Test
    public void testSelectListByForward_deletedDataFiltered() {
        Long workOrderId = randomLongId();
        LocalDateTime now = LocalDateTime.now();

        // 1. 报工记录
        MesProFeedbackDO feedback = MesProFeedbackDO.builder()
                .workOrderId(workOrderId).itemId(randomLongId()).build();
        feedbackMapper.insert(feedback);

        // 2. 消耗单
        MesWmItemConsumeDO consume = MesWmItemConsumeDO.builder()
                .workOrderId(workOrderId).taskId(randomLongId()).workstationId(randomLongId())
                .processId(randomLongId()).feedbackId(feedback.getId()).consumeDate(now).build();
        consumeMapper.insert(consume);

        // 3. 消耗明细（逻辑删除）
        MesWmItemConsumeDetailDO consumeDetail = MesWmItemConsumeDetailDO.builder()
                .consumeId(consume.getId()).lineId(randomLongId()).itemId(randomLongId())
                .quantity(BigDecimal.TEN).batchCode("RAW_DEL_TEST")
                .warehouseId(randomLongId()).locationId(randomLongId()).areaId(randomLongId()).build();
        consumeDetailMapper.insert(consumeDetail);
        consumeDetailMapper.deleteById(consumeDetail.getId()); // 逻辑删除消耗明细

        // 4. 生产入库单 + 入库行
        MesWmProductProduceDO produce = MesWmProductProduceDO.builder()
                .workOrderId(workOrderId).build();
        produceMapper.insert(produce);
        MesWmProductProduceLineDO produceLine = MesWmProductProduceLineDO.builder()
                .produceId(produce.getId()).itemId(randomLongId())
                .batchId(randomLongId()).batchCode("PRODUCT_DEL_TEST").build();
        produceLineMapper.insert(produceLine);

        // 执行：消耗明细已删除，应该追溯不到
        List<MesWmBatchDO> result = batchMapper.selectListByForward("RAW_DEL_TEST");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== 向后追溯（集成测试）====================

    /**
     * 向后追溯集成测试：构造完整链路数据
     * 链路：入库明细(ppd, batchCode=PRODUCT_BATCH) → 入库单(pp) → 消耗单(ic) → 消耗明细(icd, batchCode=RAW_BATCH)
     * 预期：查 PRODUCT_BATCH 能找到 RAW_BATCH
     */
    @Test
    public void testSelectListByBackward_withData() {
        Long workOrderId = randomLongId();
        Long rawItemId = randomLongId();
        Long rawBatchId = randomLongId();
        LocalDateTime now = LocalDateTime.now();

        // 1. 生产入库单
        MesWmProductProduceDO produce = MesWmProductProduceDO.builder()
                .workOrderId(workOrderId).build();
        produceMapper.insert(produce);

        // 2. 入库明细（关联成品批次 PRODUCT_BACK_BATCH）
        MesWmProductProduceDetailDO produceDetail = MesWmProductProduceDetailDO.builder()
                .produceId(produce.getId()).itemId(randomLongId())
                .batchCode("PRODUCT_BACK_BATCH").build();
        produceDetailMapper.insert(produceDetail);

        // 3. 消耗单
        MesWmItemConsumeDO consume = MesWmItemConsumeDO.builder()
                .workOrderId(workOrderId).taskId(randomLongId()).workstationId(randomLongId())
                .processId(randomLongId()).feedbackId(randomLongId()).consumeDate(now).build();
        consumeMapper.insert(consume);

        // 4. 消耗明细（关联原材料批次 RAW_BACK_BATCH）
        MesWmItemConsumeDetailDO consumeDetail = MesWmItemConsumeDetailDO.builder()
                .consumeId(consume.getId()).lineId(randomLongId()).itemId(rawItemId)
                .quantity(BigDecimal.ONE).batchId(rawBatchId).batchCode("RAW_BACK_BATCH")
                .warehouseId(randomLongId()).locationId(randomLongId()).areaId(randomLongId()).build();
        consumeDetailMapper.insert(consumeDetail);

        // 执行向后追溯
        List<MesWmBatchDO> result = batchMapper.selectListByBackward("PRODUCT_BACK_BATCH");

        // 断言
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("RAW_BACK_BATCH", result.get(0).getCode());
        assertEquals(rawBatchId, result.get(0).getId());
        assertEquals(workOrderId, result.get(0).getWorkOrderId());
        assertEquals(rawItemId, result.get(0).getItemId());
    }

    /**
     * 向后追溯：消耗明细 batchCode 为 NULL 的应被过滤
     */
    @Test
    public void testSelectListByBackward_nullBatchCodeFiltered() {
        Long workOrderId = randomLongId();
        LocalDateTime now = LocalDateTime.now();

        // 生产入库单 + 入库明细
        MesWmProductProduceDO produce = MesWmProductProduceDO.builder()
                .workOrderId(workOrderId).build();
        produceMapper.insert(produce);
        MesWmProductProduceDetailDO produceDetail = MesWmProductProduceDetailDO.builder()
                .produceId(produce.getId()).itemId(randomLongId())
                .batchCode("PRODUCT_NULL_TEST").build();
        produceDetailMapper.insert(produceDetail);

        // 消耗单 + 消耗明细（batchCode 为 null）
        MesWmItemConsumeDO consume = MesWmItemConsumeDO.builder()
                .workOrderId(workOrderId).taskId(randomLongId()).workstationId(randomLongId())
                .processId(randomLongId()).feedbackId(randomLongId()).consumeDate(now).build();
        consumeMapper.insert(consume);
        MesWmItemConsumeDetailDO consumeDetail = MesWmItemConsumeDetailDO.builder()
                .consumeId(consume.getId()).lineId(randomLongId()).itemId(randomLongId())
                .quantity(BigDecimal.ONE).batchCode(null) // 无批次
                .warehouseId(randomLongId()).locationId(randomLongId()).areaId(randomLongId()).build();
        consumeDetailMapper.insert(consumeDetail);

        // 执行：应过滤掉 batchCode IS NULL 的消耗明细
        List<MesWmBatchDO> result = batchMapper.selectListByBackward("PRODUCT_NULL_TEST");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

}
