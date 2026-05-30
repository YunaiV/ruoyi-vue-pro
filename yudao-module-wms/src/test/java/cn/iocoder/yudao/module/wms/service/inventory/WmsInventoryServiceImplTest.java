package cn.iocoder.yudao.module.wms.service.inventory;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.vo.WmsInventoryPageReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.WmsInventoryHistoryDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.WmsInventoryDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.item.WmsItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.item.WmsItemSkuDO;
import cn.iocoder.yudao.module.wms.dal.mysql.inventory.WmsInventoryHistoryMapper;
import cn.iocoder.yudao.module.wms.dal.mysql.inventory.WmsInventoryMapper;
import cn.iocoder.yudao.module.wms.dal.mysql.md.item.WmsItemMapper;
import cn.iocoder.yudao.module.wms.dal.mysql.md.item.WmsItemSkuMapper;
import cn.iocoder.yudao.module.wms.enums.order.WmsOrderTypeEnum;
import cn.iocoder.yudao.module.wms.service.inventory.dto.WmsInventoryChangeReqDTO;
import cn.iocoder.yudao.module.wms.service.inventory.dto.WmsInventoryCheckReqDTO;
import cn.iocoder.yudao.module.wms.service.md.item.WmsItemService;
import cn.iocoder.yudao.module.wms.service.md.item.WmsItemSkuService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.CHECK_ORDER_INVENTORY_CHANGED;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.INVENTORY_QUANTITY_NOT_ENOUGH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@Import({WmsInventoryServiceImpl.class, WmsInventoryHistoryServiceImpl.class})
public class WmsInventoryServiceImplTest extends BaseDbUnitTest {

    @Resource
    private WmsInventoryServiceImpl inventoryService;

    @Resource
    private WmsInventoryMapper inventoryMapper;
    @Resource
    private WmsInventoryHistoryMapper inventoryHistoryMapper;
    @Resource
    private WmsItemMapper itemMapper;
    @Resource
    private WmsItemSkuMapper skuMapper;

    @MockitoBean
    private WmsItemSkuService itemSkuService;
    @MockitoBean
    private WmsItemService itemService;

    @Test
    public void testChangeInventory_createInventory() {
        // mock 数据
        WmsItemDO item = createItem("ITEM-001", "红富士苹果");
        WmsItemSkuDO sku = createSku(item.getId(), "SKU-001", "10kg 箱装");
        WmsInventoryChangeReqDTO reqDTO = createChangeReq(sku.getId(), 100L, "5.00");

        // 调用
        inventoryService.changeInventory(reqDTO);

        // 断言：库存余额
        WmsInventoryDO inventory = inventoryMapper.selectBySkuIdAndWarehouseId(sku.getId(), 100L);
        assertNotNull(inventory);
        assertEquals(0, new BigDecimal("5.00").compareTo(inventory.getQuantity()));
        // 断言：库存流水
        List<WmsInventoryHistoryDO> histories = inventoryHistoryMapper.selectList();
        assertEquals(1, histories.size());
        WmsInventoryHistoryDO history = histories.get(0);
        assertEquals(sku.getId(), history.getSkuId());
        assertEquals(100L, history.getWarehouseId());
        assertEquals(0, BigDecimal.ZERO.compareTo(history.getBeforeQuantity()));
        assertEquals(0, new BigDecimal("5.00").compareTo(history.getAfterQuantity()));
        assertEquals(0, new BigDecimal("500.00").compareTo(history.getTotalPrice()));
        assertEquals(reqDTO.getOrderId(), history.getOrderId());
        assertEquals(reqDTO.getOrderNo(), history.getOrderNo());
        assertEquals(reqDTO.getOrderType(), history.getOrderType());
    }

    @Test
    public void testChangeInventory_updateInventory() {
        // mock 数据
        WmsItemDO item = createItem("ITEM-001", "红富士苹果");
        WmsItemSkuDO sku = createSku(item.getId(), "SKU-001", "10kg 箱装");
        inventoryMapper.insert(createInventory(sku.getId(), 100L, "2.00"));
        WmsInventoryChangeReqDTO reqDTO = createChangeReq(sku.getId(), 100L, "3.00");

        // 调用
        inventoryService.changeInventory(reqDTO);

        // 断言：库存余额
        WmsInventoryDO inventory = inventoryMapper.selectBySkuIdAndWarehouseId(sku.getId(), 100L);
        assertNotNull(inventory);
        assertEquals(0, new BigDecimal("5.00").compareTo(inventory.getQuantity()));
        // 断言：库存流水
        List<WmsInventoryHistoryDO> histories = inventoryHistoryMapper.selectList();
        assertEquals(1, histories.size());
        WmsInventoryHistoryDO history = histories.get(0);
        assertEquals(0, new BigDecimal("2.00").compareTo(history.getBeforeQuantity()));
        assertEquals(0, new BigDecimal("5.00").compareTo(history.getAfterQuantity()));
        assertEquals(0, new BigDecimal("3.00").compareTo(history.getQuantity()));
        assertEquals(0, new BigDecimal("300.00").compareTo(history.getTotalPrice()));
    }

    @Test
    public void testChangeInventory_sameInventoryKeyKeepItemGranularity() {
        // mock 数据
        WmsItemDO item = createItem("ITEM-001", "红富士苹果");
        WmsItemSkuDO sku = createSku(item.getId(), "SKU-001", "10kg 箱装");
        WmsInventoryChangeReqDTO reqDTO = createChangeReq(sku.getId(), 100L, "5.00");
        List<WmsInventoryChangeReqDTO.Item> items = new ArrayList<>(reqDTO.getItems());
        items.add(new WmsInventoryChangeReqDTO.Item()
                .setSkuId(sku.getId())
                .setWarehouseId(100L)
                .setQuantity(new BigDecimal("7.00"))
                .setPrice(new BigDecimal("140.00"))
                .setTotalPrice(new BigDecimal("980.00"))
                .setRemark("测试入库 2"));
        reqDTO.setItems(items);

        // 调用
        inventoryService.changeInventory(reqDTO);

        // 断言：库存余额只保留一条，数量逐条累加
        WmsInventoryDO inventory = inventoryMapper.selectBySkuIdAndWarehouseId(sku.getId(), 100L);
        assertNotNull(inventory);
        assertEquals(0, new BigDecimal("12.00").compareTo(inventory.getQuantity()));
        // 断言：库存流水保持用户明细颗粒度
        List<WmsInventoryHistoryDO> histories = inventoryHistoryMapper.selectList();
        histories.sort(Comparator.comparing(WmsInventoryHistoryDO::getId));
        assertEquals(2, histories.size());
        assertEquals(0, BigDecimal.ZERO.compareTo(histories.get(0).getBeforeQuantity()));
        assertEquals(0, new BigDecimal("5.00").compareTo(histories.get(0).getAfterQuantity()));
        assertEquals(0, new BigDecimal("5.00").compareTo(histories.get(0).getQuantity()));
        assertEquals(0, new BigDecimal("500.00").compareTo(histories.get(0).getTotalPrice()));
        assertEquals(0, new BigDecimal("5.00").compareTo(histories.get(1).getBeforeQuantity()));
        assertEquals(0, new BigDecimal("12.00").compareTo(histories.get(1).getAfterQuantity()));
        assertEquals(0, new BigDecimal("7.00").compareTo(histories.get(1).getQuantity()));
        assertEquals(0, new BigDecimal("980.00").compareTo(histories.get(1).getTotalPrice()));
    }

    @Test
    public void testChangeInventory_sameExistingInventoryKeyKeepItemGranularity() {
        // mock 数据
        WmsItemDO item = createItem("ITEM-001", "红富士苹果");
        WmsItemSkuDO sku = createSku(item.getId(), "SKU-001", "10kg 箱装");
        inventoryMapper.insert(createInventory(sku.getId(), 100L, "2.00"));
        WmsInventoryChangeReqDTO reqDTO = createChangeReq(sku.getId(), 100L, "3.00");
        List<WmsInventoryChangeReqDTO.Item> items = new ArrayList<>(reqDTO.getItems());
        items.add(new WmsInventoryChangeReqDTO.Item()
                .setSkuId(sku.getId())
                .setWarehouseId(100L)
                .setQuantity(new BigDecimal("4.00"))
                .setPrice(new BigDecimal("80.00"))
                .setTotalPrice(new BigDecimal("320.00"))
                .setRemark("测试入库 2"));
        reqDTO.setItems(items);

        // 调用
        inventoryService.changeInventory(reqDTO);

        // 断言：库存余额只保留一条，数量逐条累加
        WmsInventoryDO inventory = inventoryMapper.selectBySkuIdAndWarehouseId(sku.getId(), 100L);
        assertNotNull(inventory);
        assertEquals(0, new BigDecimal("9.00").compareTo(inventory.getQuantity()));
        // 断言：库存流水保持用户明细颗粒度
        List<WmsInventoryHistoryDO> histories = inventoryHistoryMapper.selectList();
        histories.sort(Comparator.comparing(WmsInventoryHistoryDO::getId));
        assertEquals(2, histories.size());
        assertEquals(0, new BigDecimal("2.00").compareTo(histories.get(0).getBeforeQuantity()));
        assertEquals(0, new BigDecimal("5.00").compareTo(histories.get(0).getAfterQuantity()));
        assertEquals(0, new BigDecimal("3.00").compareTo(histories.get(0).getQuantity()));
        assertEquals(0, new BigDecimal("300.00").compareTo(histories.get(0).getTotalPrice()));
        assertEquals(0, new BigDecimal("5.00").compareTo(histories.get(1).getBeforeQuantity()));
        assertEquals(0, new BigDecimal("9.00").compareTo(histories.get(1).getAfterQuantity()));
        assertEquals(0, new BigDecimal("4.00").compareTo(histories.get(1).getQuantity()));
        assertEquals(0, new BigDecimal("320.00").compareTo(histories.get(1).getTotalPrice()));
    }

    @Test
    public void testChangeInventory_multipleInventoryKeysKeepItemGranularity() {
        // mock 数据
        WmsItemDO item = createItem("ITEM-001", "红富士苹果");
        WmsItemSkuDO sku = createSku(item.getId(), "SKU-001", "10kg 箱装");
        WmsInventoryChangeReqDTO reqDTO = createChangeReq(sku.getId(), 100L, "5.00");
        List<WmsInventoryChangeReqDTO.Item> items = new ArrayList<>(reqDTO.getItems());
        items.add(new WmsInventoryChangeReqDTO.Item()
                .setSkuId(sku.getId())
                .setWarehouseId(200L)
                .setQuantity(new BigDecimal("3.00"))
                .setPrice(new BigDecimal("60.00"))
                .setTotalPrice(new BigDecimal("180.00"))
                .setRemark("测试入库 2"));
        items.add(new WmsInventoryChangeReqDTO.Item()
                .setSkuId(sku.getId())
                .setWarehouseId(100L)
                .setQuantity(new BigDecimal("-2.00"))
                .setPrice(new BigDecimal("40.00"))
                .setTotalPrice(new BigDecimal("-80.00"))
                .setRemark("测试出库"));
        reqDTO.setItems(items);

        // 调用
        inventoryService.changeInventory(reqDTO);

        // 断言：不同库存余额分别更新
        WmsInventoryDO inventory1 = inventoryMapper.selectBySkuIdAndWarehouseId(sku.getId(), 100L);
        assertNotNull(inventory1);
        assertEquals(0, new BigDecimal("3.00").compareTo(inventory1.getQuantity()));
        WmsInventoryDO inventory2 = inventoryMapper.selectBySkuIdAndWarehouseId(sku.getId(), 200L);
        assertNotNull(inventory2);
        assertEquals(0, new BigDecimal("3.00").compareTo(inventory2.getQuantity()));
        // 断言：批量加锁后仍按明细颗粒度记录 before/after
        List<WmsInventoryHistoryDO> histories = inventoryHistoryMapper.selectList();
        histories.sort(Comparator.comparing(WmsInventoryHistoryDO::getId));
        assertEquals(3, histories.size());
        assertEquals(100L, histories.get(0).getWarehouseId());
        assertEquals(0, BigDecimal.ZERO.compareTo(histories.get(0).getBeforeQuantity()));
        assertEquals(0, new BigDecimal("5.00").compareTo(histories.get(0).getAfterQuantity()));
        assertEquals(0, new BigDecimal("500.00").compareTo(histories.get(0).getTotalPrice()));
        assertEquals(200L, histories.get(1).getWarehouseId());
        assertEquals(0, BigDecimal.ZERO.compareTo(histories.get(1).getBeforeQuantity()));
        assertEquals(0, new BigDecimal("3.00").compareTo(histories.get(1).getAfterQuantity()));
        assertEquals(0, new BigDecimal("180.00").compareTo(histories.get(1).getTotalPrice()));
        assertEquals(100L, histories.get(2).getWarehouseId());
        assertEquals(0, new BigDecimal("5.00").compareTo(histories.get(2).getBeforeQuantity()));
        assertEquals(0, new BigDecimal("3.00").compareTo(histories.get(2).getAfterQuantity()));
        assertEquals(0, new BigDecimal("-80.00").compareTo(histories.get(2).getTotalPrice()));
    }

    @Test
    public void testChangeInventory_concurrentCreateSameInventoryOnlyOneBalance() throws Exception {
        // mock 数据
        WmsItemDO item = createItem("ITEM-001", "红富士苹果");
        WmsItemSkuDO sku = createSku(item.getId(), "SKU-001", "10kg 箱装");
        int threadCount = 4;
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        List<Future<?>> futures = new ArrayList<>(threadCount);
        for (int i = 0; i < threadCount; i++) {
            futures.add(executorService.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(ex);
                }
                inventoryService.changeInventory(createChangeReq(sku.getId(), 100L, "1.00"));
            }));
        }

        try {
            // 调用
            assertTrue(readyLatch.await(5, TimeUnit.SECONDS));
            startLatch.countDown();
            for (Future<?> future : futures) {
                future.get(10, TimeUnit.SECONDS);
            }
        } finally {
            executorService.shutdownNow();
        }

        // 断言：并发补行后仍只有一条库存余额，数量累计正确
        List<WmsInventoryDO> inventories = inventoryMapper.selectListByKeys(Collections.singletonList(
                new WmsInventoryDO().setSkuId(sku.getId()).setWarehouseId(100L)));
        assertEquals(1, inventories.size());
        assertEquals(0, new BigDecimal("4.00").compareTo(inventories.get(0).getQuantity()));
        assertEquals(threadCount, inventoryHistoryMapper.selectCount());
    }

    @Test
    public void testChangeInventory_quantityNotEnough() {
        // mock 数据
        WmsItemDO item = createItem("ITEM-001", "红富士苹果");
        WmsItemSkuDO sku = createSku(item.getId(), "SKU-001", "10kg 箱装");
        inventoryMapper.insert(createInventory(sku.getId(), 100L, "2.00"));
        WmsInventoryChangeReqDTO reqDTO = createChangeReq(sku.getId(), 100L, "-3.00");
        mockItemSkuAndItem(item, sku);

        // 调用，并断言
        assertServiceException(() -> inventoryService.changeInventory(reqDTO), INVENTORY_QUANTITY_NOT_ENOUGH,
                item.getName(), sku.getName(), 100L, new BigDecimal("2.00"), new BigDecimal("-3.00"));
        WmsInventoryDO inventory = inventoryMapper.selectBySkuIdAndWarehouseId(sku.getId(), 100L);
        assertNotNull(inventory);
        assertEquals(0, new BigDecimal("2.00").compareTo(inventory.getQuantity()));
        assertEquals(0L, inventoryHistoryMapper.selectCount());
    }

    @Test
    public void testChangeInventory_quantityNotEnoughWithoutInventory() {
        // mock 数据
        WmsItemDO item = createItem("ITEM-001", "红富士苹果");
        WmsItemSkuDO sku = createSku(item.getId(), "SKU-001", "10kg 箱装");
        WmsInventoryChangeReqDTO reqDTO = createChangeReq(sku.getId(), 100L, "-3.00");
        mockItemSkuAndItem(item, sku);

        // 调用，并断言
        assertServiceException(() -> inventoryService.changeInventory(reqDTO), INVENTORY_QUANTITY_NOT_ENOUGH,
                item.getName(), sku.getName(), 100L, new BigDecimal("0.00"), new BigDecimal("-3.00"));
        assertEquals(0L, inventoryMapper.selectCount());
        assertEquals(0L, inventoryHistoryMapper.selectCount());
    }

    @Test
    public void testChangeInventory_quantityNotEnoughRollbackCreatedInventories() {
        // mock 数据
        WmsItemDO item = createItem("ITEM-001", "红富士苹果");
        WmsItemSkuDO sku = createSku(item.getId(), "SKU-001", "10kg 箱装");
        WmsInventoryChangeReqDTO reqDTO = createChangeReq(sku.getId(), 100L, "5.00");
        List<WmsInventoryChangeReqDTO.Item> items = new ArrayList<>(reqDTO.getItems());
        items.add(new WmsInventoryChangeReqDTO.Item()
                .setSkuId(sku.getId())
                .setWarehouseId(200L)
                .setQuantity(new BigDecimal("-1.00"))
                .setPrice(new BigDecimal("20.00"))
                .setTotalPrice(new BigDecimal("-20.00"))
                .setRemark("测试出库"));
        reqDTO.setItems(items);
        mockItemSkuAndItem(item, sku);

        // 调用，并断言：第二条明细库存不足，事务回滚前面补齐的库存行
        assertServiceException(() -> inventoryService.changeInventory(reqDTO), INVENTORY_QUANTITY_NOT_ENOUGH,
                item.getName(), sku.getName(), 200L, new BigDecimal("0.00"), new BigDecimal("-1.00"));
        assertNull(inventoryMapper.selectBySkuIdAndWarehouseId(sku.getId(), 100L));
        assertNull(inventoryMapper.selectBySkuIdAndWarehouseId(sku.getId(), 200L));
        assertEquals(0L, inventoryHistoryMapper.selectCount());
    }

    @Test
    public void testCheckInventory_updateExistingInventory() {
        // mock 数据
        WmsItemDO item = createItem("ITEM-001", "红富士苹果");
        WmsItemSkuDO sku = createSku(item.getId(), "SKU-001", "10kg 箱装");
        WmsInventoryDO inventory = createInventory(sku.getId(), 100L, "10.00");
        inventoryMapper.insert(inventory);
        WmsInventoryCheckReqDTO reqDTO = createCheckReq(inventory.getId(), sku.getId(), 100L,
                "10.00", "7.00");

        // 调用
        inventoryService.checkInventory(reqDTO);

        // 断言：库存余额直接调整为实盘数量
        WmsInventoryDO dbInventory = inventoryMapper.selectById(inventory.getId());
        assertNotNull(dbInventory);
        assertEquals(0, new BigDecimal("7.00").compareTo(dbInventory.getQuantity()));
        // 断言：库存流水记录盘库差异
        List<WmsInventoryHistoryDO> histories = inventoryHistoryMapper.selectList();
        assertEquals(1, histories.size());
        WmsInventoryHistoryDO history = histories.get(0);
        assertEquals(sku.getId(), history.getSkuId());
        assertEquals(100L, history.getWarehouseId());
        assertEquals(0, new BigDecimal("10.00").compareTo(history.getBeforeQuantity()));
        assertEquals(0, new BigDecimal("7.00").compareTo(history.getAfterQuantity()));
        assertEquals(0, new BigDecimal("-3.00").compareTo(history.getQuantity()));
        assertEquals(0, new BigDecimal("-300.00").compareTo(history.getTotalPrice()));
        assertEquals(reqDTO.getOrderId(), history.getOrderId());
        assertEquals(reqDTO.getOrderNo(), history.getOrderNo());
        assertEquals(reqDTO.getOrderType(), history.getOrderType());
    }

    @Test
    public void testCheckInventory_existingInventoryChanged() {
        // mock 数据
        WmsItemDO item = createItem("ITEM-001", "红富士苹果");
        WmsItemSkuDO sku = createSku(item.getId(), "SKU-001", "10kg 箱装");
        WmsInventoryDO inventory = createInventory(sku.getId(), 100L, "12.00");
        inventoryMapper.insert(inventory);
        WmsInventoryCheckReqDTO reqDTO = createCheckReq(inventory.getId(), sku.getId(), 100L,
                "10.00", "7.00");

        // 调用，并断言
        assertServiceException(() -> inventoryService.checkInventory(reqDTO), CHECK_ORDER_INVENTORY_CHANGED);
        WmsInventoryDO dbInventory = inventoryMapper.selectById(inventory.getId());
        assertNotNull(dbInventory);
        assertEquals(0, new BigDecimal("12.00").compareTo(dbInventory.getQuantity()));
        assertEquals(0L, inventoryHistoryMapper.selectCount());
    }

    @Test
    public void testCheckInventory_createNewInventory() {
        // mock 数据
        WmsItemDO item = createItem("ITEM-001", "红富士苹果");
        WmsItemSkuDO sku = createSku(item.getId(), "SKU-001", "10kg 箱装");
        WmsInventoryCheckReqDTO reqDTO = createCheckReq(null, sku.getId(), 100L,
                "0.00", "5.00");

        // 调用
        inventoryService.checkInventory(reqDTO);

        // 断言：新增库存直接落为实盘数量
        WmsInventoryDO inventory = inventoryMapper.selectBySkuIdAndWarehouseId(sku.getId(), 100L);
        assertNotNull(inventory);
        assertEquals(0, new BigDecimal("5.00").compareTo(inventory.getQuantity()));
        // 断言：库存流水记录从 0 到实盘数量
        List<WmsInventoryHistoryDO> histories = inventoryHistoryMapper.selectList();
        assertEquals(1, histories.size());
        assertEquals(0, BigDecimal.ZERO.compareTo(histories.get(0).getBeforeQuantity()));
        assertEquals(0, new BigDecimal("5.00").compareTo(histories.get(0).getAfterQuantity()));
        assertEquals(0, new BigDecimal("5.00").compareTo(histories.get(0).getQuantity()));
        assertEquals(0, new BigDecimal("500.00").compareTo(histories.get(0).getTotalPrice()));
    }

    @Test
    public void testCheckInventory_createNewZeroInventory() {
        // mock 数据
        WmsItemDO item = createItem("ITEM-001", "红富士苹果");
        WmsItemSkuDO sku = createSku(item.getId(), "SKU-001", "10kg 箱装");
        WmsInventoryCheckReqDTO reqDTO = createCheckReq(null, sku.getId(), 100L,
                "0.00", "0.00");

        // 调用
        inventoryService.checkInventory(reqDTO);

        // 断言：新增零库存商品也会生成库存余额行
        WmsInventoryDO inventory = inventoryMapper.selectBySkuIdAndWarehouseId(sku.getId(), 100L);
        assertNotNull(inventory);
        assertEquals(0, BigDecimal.ZERO.compareTo(inventory.getQuantity()));
        assertEquals(0L, inventoryHistoryMapper.selectCount());
    }

    @Test
    public void testCheckInventory_newInventoryAlreadyExists() {
        // mock 数据
        WmsItemDO item = createItem("ITEM-001", "红富士苹果");
        WmsItemSkuDO sku = createSku(item.getId(), "SKU-001", "10kg 箱装");
        inventoryMapper.insert(createInventory(sku.getId(), 100L, "3.00"));
        WmsInventoryCheckReqDTO reqDTO = createCheckReq(null, sku.getId(), 100L,
                "0.00", "5.00");

        // 调用，并断言
        assertServiceException(() -> inventoryService.checkInventory(reqDTO), CHECK_ORDER_INVENTORY_CHANGED);
        WmsInventoryDO inventory = inventoryMapper.selectBySkuIdAndWarehouseId(sku.getId(), 100L);
        assertNotNull(inventory);
        assertEquals(0, new BigDecimal("3.00").compareTo(inventory.getQuantity()));
        assertEquals(0L, inventoryHistoryMapper.selectCount());
    }

    @Test
    public void testGetInventoryPage_filterByWarehouse() {
        // mock 数据
        WmsItemDO item = createItem("ITEM-001", "红富士苹果");
        WmsItemSkuDO sku = createSku(item.getId(), "SKU-001", "10kg 箱装");
        inventoryMapper.insert(createInventory(sku.getId(), 100L, "2.00"));
        inventoryMapper.insert(createInventory(sku.getId(), 200L, "5.00"));
        // 准备参数
        WmsInventoryPageReqVO reqVO = new WmsInventoryPageReqVO();
        reqVO.setType(WmsInventoryPageReqVO.TYPE_WAREHOUSE);
        reqVO.setWarehouseId(100L);

        // 调用
        PageResult<WmsInventoryDO> pageResult = inventoryService.getInventoryPage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        WmsInventoryDO inventory = pageResult.getList().get(0);
        assertEquals(100L, inventory.getWarehouseId());
        assertEquals(sku.getId(), inventory.getSkuId());
        assertEquals(0, new BigDecimal("2.00").compareTo(inventory.getQuantity()));
    }

    @Test
    public void testGetInventoryPage_groupByWarehouse_minQuantity() {
        // mock 数据
        WmsItemDO item = createItem("ITEM-001", "红富士苹果");
        WmsItemSkuDO sku = createSku(item.getId(), "SKU-001", "10kg 箱装");
        inventoryMapper.insert(createInventory(sku.getId(), 100L, "2.00"));
        inventoryMapper.insert(createInventory(sku.getId(), 200L, "7.00"));
        // 准备参数
        WmsInventoryPageReqVO reqVO = new WmsInventoryPageReqVO();
        reqVO.setType(WmsInventoryPageReqVO.TYPE_WAREHOUSE);
        reqVO.setMinQuantity(new BigDecimal("6.00"));

        // 调用
        PageResult<WmsInventoryDO> pageResult = inventoryService.getInventoryPage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertEquals(200L, pageResult.getList().get(0).getWarehouseId());
    }

    @Test
    public void testGetInventoryPage_onlyPositiveQuantity() {
        // mock 数据
        WmsItemDO item = createItem("ITEM-001", "红富士苹果");
        WmsItemSkuDO sku = createSku(item.getId(), "SKU-001", "10kg 箱装");
        inventoryMapper.insert(createInventory(sku.getId(), 100L, "0.00"));
        inventoryMapper.insert(createInventory(sku.getId(), 200L, "0.01"));
        inventoryMapper.insert(createInventory(sku.getId(), 300L, "1.00"));
        // 准备参数
        WmsInventoryPageReqVO reqVO = new WmsInventoryPageReqVO();
        reqVO.setType(WmsInventoryPageReqVO.TYPE_WAREHOUSE);
        reqVO.setOnlyPositiveQuantity(true);

        // 调用
        PageResult<WmsInventoryDO> pageResult = inventoryService.getInventoryPage(reqVO);
        // 断言
        assertEquals(2, pageResult.getTotal());
        assertEquals(2, pageResult.getList().size());
        assertEquals(200L, pageResult.getList().get(0).getWarehouseId());
        assertEquals(0, new BigDecimal("0.01").compareTo(pageResult.getList().get(0).getQuantity()));
        assertEquals(300L, pageResult.getList().get(1).getWarehouseId());
        assertEquals(0, new BigDecimal("1.00").compareTo(pageResult.getList().get(1).getQuantity()));
    }

    @Test
    public void testGetInventoryPage_sku() {
        // mock 数据
        WmsItemDO item = createItem("ITEM-001", "红富士苹果");
        WmsItemSkuDO sku = createSku(item.getId(), "SKU-001", "10kg 箱装");
        WmsItemSkuDO sku2 = createSku(item.getId(), "SKU-002", "5kg 箱装");
        inventoryMapper.insert(createInventory(sku.getId(), 100L, "2.00"));
        inventoryMapper.insert(createInventory(sku2.getId(), 100L, "3.00"));
        // 准备参数
        WmsInventoryPageReqVO reqVO = new WmsInventoryPageReqVO();
        reqVO.setType(WmsInventoryPageReqVO.TYPE_ITEM);
        reqVO.setWarehouseId(100L);
        reqVO.setSkuId(sku.getId());

        // 调用
        PageResult<WmsInventoryDO> pageResult = inventoryService.getInventoryPage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertEquals(sku.getId(), pageResult.getList().get(0).getSkuId());
    }

    @Test
    public void testGetInventoryPage_orderByItemDimension() {
        // mock 数据
        WmsItemDO item1 = createItem("ITEM-001", "红富士苹果");
        WmsItemSkuDO sku1 = createSku(item1.getId(), "SKU-001", "10kg 箱装");
        WmsItemDO item2 = createItem("ITEM-002", "香蕉");
        WmsItemSkuDO sku2 = createSku(item2.getId(), "SKU-002", "5kg 箱装");
        inventoryMapper.insert(createInventory(sku2.getId(), 100L, "3.00"));
        inventoryMapper.insert(createInventory(sku1.getId(), 200L, "2.00"));
        inventoryMapper.insert(createInventory(sku1.getId(), 100L, "1.00"));
        // 准备参数
        WmsInventoryPageReqVO reqVO = new WmsInventoryPageReqVO();
        reqVO.setType(WmsInventoryPageReqVO.TYPE_ITEM);

        // 调用
        PageResult<WmsInventoryDO> pageResult = inventoryService.getInventoryPage(reqVO);
        // 断言
        assertEquals(3, pageResult.getTotal());
        assertEquals(sku1.getId(), pageResult.getList().get(0).getSkuId());
        assertEquals(100L, pageResult.getList().get(0).getWarehouseId());
        assertEquals(sku1.getId(), pageResult.getList().get(1).getSkuId());
        assertEquals(200L, pageResult.getList().get(1).getWarehouseId());
        assertEquals(sku2.getId(), pageResult.getList().get(2).getSkuId());
        assertEquals(100L, pageResult.getList().get(2).getWarehouseId());
    }

    private WmsItemDO createItem(String code, String name) {
        WmsItemDO item = WmsItemDO.builder()
                .code(code)
                .name(name)
                .unit("箱")
                .categoryId(1L)
                .build();
        itemMapper.insert(item);
        return item;
    }

    private WmsItemSkuDO createSku(Long itemId, String code, String name) {
        WmsItemSkuDO sku = WmsItemSkuDO.builder()
                .itemId(itemId)
                .code(code)
                .name(name)
                .barCode("69010001")
                .build();
        skuMapper.insert(sku);
        return sku;
    }

    private void mockItemSkuAndItem(WmsItemDO item, WmsItemSkuDO sku) {
        when(itemSkuService.validateItemSkuExists(sku.getId())).thenReturn(sku);
        when(itemService.validateItemExists(item.getId())).thenReturn(item);
    }

    private static WmsInventoryDO createInventory(Long skuId, Long warehouseId, String quantity) {
        return WmsInventoryDO.builder()
                .skuId(skuId)
                .warehouseId(warehouseId)
                .quantity(new BigDecimal(quantity))
                .build();
    }

    private static WmsInventoryChangeReqDTO createChangeReq(Long skuId, Long warehouseId, String quantity) {
        return new WmsInventoryChangeReqDTO()
                .setOrderId(1L)
                .setOrderNo("RK202605120001")
                .setOrderType(WmsOrderTypeEnum.RECEIPT.getType())
                .setItems(Collections.singletonList(new WmsInventoryChangeReqDTO.Item()
                        .setSkuId(skuId)
                        .setWarehouseId(warehouseId)
                        .setQuantity(new BigDecimal(quantity))
                        .setPrice(new BigDecimal("100.00"))
                        .setTotalPrice(new BigDecimal(quantity).multiply(new BigDecimal("100.00")))
                        .setRemark("测试入库")));
    }

    private static WmsInventoryCheckReqDTO createCheckReq(Long inventoryId, Long skuId, Long warehouseId,
                                                          String quantity, String checkQuantity) {
        return new WmsInventoryCheckReqDTO()
                .setOrderId(2L)
                .setOrderNo("PK202605120001")
                .setOrderType(WmsOrderTypeEnum.CHECK.getType())
                .setItems(Collections.singletonList(new WmsInventoryCheckReqDTO.Item()
                        .setInventoryId(inventoryId)
                        .setSkuId(skuId)
                        .setWarehouseId(warehouseId)
                        .setQuantity(new BigDecimal(quantity))
                        .setCheckQuantity(new BigDecimal(checkQuantity))
                        .setPrice(new BigDecimal("100.00"))
                        .setRemark("测试盘库")));
    }

}
