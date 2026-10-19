package cn.iocoder.yudao.module.oa.service.supply;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.oa.controller.admin.supply.vo.item.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.supply.OaSupplyItemDO;
import cn.iocoder.yudao.module.oa.dal.mysql.supply.OaSupplyItemMapper;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link OaSupplyItemServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(OaSupplyItemServiceImpl.class)
public class OaSupplyItemServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OaSupplyItemService supplyItemService;

    @Resource
    private OaSupplyItemMapper supplyItemMapper;

    @MockBean
    private OaSupplyApplyService supplyApplyService;
    @MockBean
    private DeptApi deptApi;

    @Test
    public void testDeleteSupplyItem_pendingIssue() {
        // mock 数据
        OaSupplyItemDO item = randomSupplyItemDO();
        supplyItemMapper.insert(item);
        // mock 方法
        Mockito.when(supplyApplyService.getPendingSupplyItemCount(item.getId())).thenReturn(1L);

        // 调用，并断言异常
        assertServiceException(() -> supplyItemService.deleteSupplyItem(item.getId()), SUPPLY_ITEM_PENDING_ISSUE);
        assertNotNull(supplyItemMapper.selectById(item.getId()));
    }

    @Test
    public void testDeleteSupplyItem_noOutstandingItems() {
        // mock 数据
        OaSupplyItemDO item = randomSupplyItemDO();
        supplyItemMapper.insert(item);

        // 调用
        supplyItemService.deleteSupplyItem(item.getId());
        // 断言
        assertNull(supplyItemMapper.selectById(item.getId()));
    }

    @Test
    public void testDeleteSupplyItem_unreturned() {
        // mock 数据
        OaSupplyItemDO item = randomSupplyItemDO();
        supplyItemMapper.insert(item);
        // mock 方法
        Mockito.when(supplyApplyService.getUnreturnedSupplyItemCount(item.getId())).thenReturn(1L);

        // 调用，并断言异常
        assertServiceException(() -> supplyItemService.deleteSupplyItem(item.getId()), SUPPLY_ITEM_NOT_RETURNED);
        assertNotNull(supplyItemMapper.selectById(item.getId()));
    }

    @Test
    public void testCreateSupplyItem_success() {
        // 准备参数
        OaSupplyItemSaveReqVO reqVO = BeanUtils.toBean(randomSupplyItemDO(), OaSupplyItemSaveReqVO.class);

        // 调用
        Long id = supplyItemService.createSupplyItem(reqVO);

        // 断言
        OaSupplyItemDO item = supplyItemMapper.selectById(id);
        assertEquals(reqVO.getCategory(), item.getCategory());
        assertEquals(reqVO.getPicUrl(), item.getPicUrl());
        assertEquals(reqVO.getStockQuantity(), item.getStockQuantity());
    }

    @Test
    public void testGetSupplyItem_notExists() {

        // 调用，并断言
        assertNull(supplyItemService.getSupplyItem(-1L));
    }

    @Test
    public void testUpdateSupplyItem_success() {
        // mock 数据
        OaSupplyItemDO item = randomSupplyItemDO();
        supplyItemMapper.insert(item);
        // 准备参数
        OaSupplyItemSaveReqVO reqVO = BeanUtils.toBean(item, OaSupplyItemSaveReqVO.class)
                .setNo("NEW-CODE").setStockQuantity(8).setStatus(1);

        // 调用
        supplyItemService.updateSupplyItem(reqVO);

        // 断言
        OaSupplyItemDO dbItem = supplyItemMapper.selectById(item.getId());
        assertEquals("NEW-CODE", dbItem.getNo());
        assertEquals(8, dbItem.getStockQuantity());
        assertEquals(1, dbItem.getStatus());
    }

    @Test
    public void testCreateSupplyItem_noDuplicate() {
        // mock 数据
        OaSupplyItemDO item = randomSupplyItemDO();
        supplyItemMapper.insert(item);
        // 准备参数
        OaSupplyItemSaveReqVO reqVO = BeanUtils.toBean(item, OaSupplyItemSaveReqVO.class).setId(null);

        // 调用，并断言异常
        assertServiceException(() -> supplyItemService.createSupplyItem(reqVO), SUPPLY_ITEM_NO_DUPLICATE);
    }

    @Test
    public void testGetSupplyItemPage_categoryAndSort() {
        // mock 数据
        OaSupplyItemDO first = randomSupplyItemDO().setSort(1);
        supplyItemMapper.insert(first);
        supplyItemMapper.insert(randomSupplyItemDO().setSort(2));
        supplyItemMapper.insert(randomSupplyItemDO().setCategory(2).setSort(0));
        // 准备参数
        OaSupplyItemPageReqVO reqVO = new OaSupplyItemPageReqVO().setCategory(1);

        // 调用
        PageResult<OaSupplyItemDO> page = supplyItemService.getSupplyItemPage(reqVO);

        // 断言
        assertEquals(2L, page.getTotal());
        assertEquals(first.getId(), CollUtil.getFirst(page.getList()).getId());
    }

    @Test
    public void testStockInSupplyItem_success() {
        // mock 数据
        OaSupplyItemDO item = randomSupplyItemDO();
        supplyItemMapper.insert(item);
        // 准备参数
        OaSupplyItemStockInReqVO reqVO = new OaSupplyItemStockInReqVO().setId(item.getId()).setQuantity(3);

        // 调用
        supplyItemService.stockInSupplyItem(reqVO);

        // 断言
        assertEquals(8, supplyItemMapper.selectById(item.getId()).getStockQuantity());
    }

    @Test
    public void testUpdateSupplyItemStockQuantity_insufficient() {
        // mock 数据
        OaSupplyItemDO item = randomSupplyItemDO();
        supplyItemMapper.insert(item);

        // 调用，并断言异常
        assertServiceException(() -> supplyItemService.updateSupplyItemStockQuantity(item.getId(), -6),
                SUPPLY_STOCK_INSUFFICIENT);
        assertEquals(5, supplyItemMapper.selectById(item.getId()).getStockQuantity());
    }

    @Test
    public void testDeleteSupplyItem_success() {
        // mock 数据
        OaSupplyItemDO item = randomSupplyItemDO();
        supplyItemMapper.insert(item);

        // 调用
        supplyItemService.deleteSupplyItem(item.getId());

        // 断言
        assertNull(supplyItemMapper.selectById(item.getId()));
    }

    @Test
    public void testGetSupplyItemMap_success() {
        // mock 数据
        OaSupplyItemDO item = randomSupplyItemDO();
        supplyItemMapper.insert(item);
        supplyItemMapper.insert(randomSupplyItemDO());

        // 调用
        Map<Long, OaSupplyItemDO> items = supplyItemService.getSupplyItemMap(Collections.singletonList(item.getId()));

        // 断言
        assertEquals(1, items.size());
        assertEquals(item.getName(), items.get(item.getId()).getName());
    }

    @Test
    public void testGetSupplyItemMap_empty() {

        // 调用，并断言
        assertTrue(supplyItemService.getSupplyItemMap(Collections.emptyList()).isEmpty());
    }

    @Test
    public void testUpdateSupplyItemStockQuantity_deductToZero() {
        // mock 数据
        OaSupplyItemDO item = randomSupplyItemDO();
        supplyItemMapper.insert(item);

        // 调用
        supplyItemService.updateSupplyItemStockQuantity(item.getId(), -5);

        // 断言
        assertEquals(0, supplyItemMapper.selectById(item.getId()).getStockQuantity());
    }

    // ========== 随机对象 ==========

    /**
     * 构造测试数据，固定业务字段的合法取值。
     *
     * @return 未入库的测试对象
     */
    private static OaSupplyItemDO randomSupplyItemDO() {
        return randomPojo(OaSupplyItemDO.class, item -> item.setId(null).setDeptId(100L).setCategory(1)
                .setManageType(2).setStatus(0).setSort(0).setStockQuantity(5).setMinStockQuantity(1)
                .setReferencePrice(BigDecimal.ONE).setDeleted(false));
    }

}
