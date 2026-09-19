package cn.iocoder.yudao.module.oa.service.supply;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.oa.controller.admin.supply.vo.apply.*;
import cn.iocoder.yudao.module.oa.controller.admin.supply.vo.issue.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.supply.*;
import cn.iocoder.yudao.module.oa.dal.mysql.supply.OaSupplyApplyItemMapper;
import cn.iocoder.yudao.module.oa.dal.mysql.supply.OaSupplyApplyMapper;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collections;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link OaSupplyIssueServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(OaSupplyIssueServiceImpl.class)
public class OaSupplyIssueServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OaSupplyIssueService supplyIssueService;

    @Resource
    private OaSupplyApplyItemMapper supplyApplyItemMapper;
    @Resource
    private OaSupplyApplyMapper supplyApplyMapper; // 验证分页关联查询
    @Resource
    private PlatformTransactionManager transactionManager;

    @MockitoBean
    private OaSupplyApplyService supplyApplyService;
    @MockitoBean
    private OaSupplyItemService supplyItemService;
    @MockitoBean
    private AdminUserApi adminUserApi;

    @Test
    public void testIssueSupply_success() {
        // mock 数据
        OaSupplyApplyDO apply = new OaSupplyApplyDO().setId(1L).setStatus(2);
        OaSupplyApplyItemDO item = randomSupplyApplyItemDO().setApplyId(apply.getId());
        supplyApplyItemMapper.insert(item);
        // mock 方法
        when(supplyApplyService.getSupplyApply(apply.getId())).thenReturn(apply);
        // 准备参数
        OaSupplyIssueReqVO reqVO = new OaSupplyIssueReqVO().setId(item.getId()).setIssuedQuantity(3);

        // 调用
        supplyIssueService.issueSupply(reqVO, 1L);

        // 断言
        OaSupplyApplyItemDO dbItem = supplyApplyItemMapper.selectById(item.getId());
        assertEquals(3, dbItem.getIssuedQuantity());
        assertEquals(2, dbItem.getStatus());
        verify(supplyItemService).updateSupplyItemStockQuantity(10L, -3);
        assertServiceException(() -> supplyIssueService.issueSupply(reqVO, 1L), SUPPLY_APPLY_STATUS_INVALID);
    }

    @Test
    public void testIssueSupply_consumable() {
        // mock 数据
        OaSupplyApplyDO apply = new OaSupplyApplyDO().setId(1L).setStatus(2);
        OaSupplyApplyItemDO item = randomSupplyApplyItemDO().setApplyId(apply.getId()).setManageType(1);
        supplyApplyItemMapper.insert(item);
        // mock 方法
        when(supplyApplyService.getSupplyApply(apply.getId())).thenReturn(apply);

        // 调用
        supplyIssueService.issueSupply(new OaSupplyIssueReqVO().setId(item.getId()).setIssuedQuantity(2), 1L);

        // 断言
        assertEquals(1, supplyApplyItemMapper.selectById(item.getId()).getStatus());
    }

    @Test
    public void testIssueSupply_notApproved() {
        // mock 数据
        OaSupplyApplyDO apply = new OaSupplyApplyDO().setId(1L).setStatus(1);
        OaSupplyApplyItemDO item = randomSupplyApplyItemDO().setApplyId(apply.getId());
        supplyApplyItemMapper.insert(item);
        // mock 方法
        when(supplyApplyService.getSupplyApply(apply.getId())).thenReturn(apply);

        // 调用，并断言异常
        assertServiceException(() -> supplyIssueService.issueSupply(new OaSupplyIssueReqVO()
                .setId(item.getId()).setIssuedQuantity(2), 1L), SUPPLY_APPLY_STATUS_INVALID);
        verifyNoInteractions(supplyItemService);
    }

    @Test
    public void testReturnSupply_partialThenFull() {
        // mock 数据
        OaSupplyApplyItemDO item = randomSupplyApplyItemDO().setApplyId(1L).setStatus(2).setIssuedQuantity(3);
        supplyApplyItemMapper.insert(item);

        // 调用
        supplyIssueService.returnSupply(new OaSupplyReturnReqVO().setId(item.getId()).setQuantity(1));

        // 断言
        assertEquals(1, supplyApplyItemMapper.selectById(item.getId()).getReturnedQuantity());
        assertEquals(2, supplyApplyItemMapper.selectById(item.getId()).getStatus());

        // 调用剩余归还
        supplyIssueService.returnSupply(new OaSupplyReturnReqVO().setId(item.getId()).setQuantity(2));
        assertEquals(3, supplyApplyItemMapper.selectById(item.getId()).getReturnedQuantity());
        assertEquals(3, supplyApplyItemMapper.selectById(item.getId()).getStatus());
        verify(supplyItemService).updateSupplyItemStockQuantity(10L, 1);
        verify(supplyItemService).updateSupplyItemStockQuantity(10L, 2);
    }

    @Test
    public void testReturnSupply_excessive() {
        // mock 数据
        OaSupplyApplyItemDO item = randomSupplyApplyItemDO().setApplyId(1L).setStatus(2).setIssuedQuantity(3)
                .setReturnedQuantity(2);
        supplyApplyItemMapper.insert(item);

        // 调用，并断言异常
        assertServiceException(() -> supplyIssueService.returnSupply(new OaSupplyReturnReqVO()
                .setId(item.getId()).setQuantity(2)), SUPPLY_APPLY_QTY_INVALID);
        verifyNoInteractions(supplyItemService);
    }

    @Test
    public void testGetSupplyIssuePage_creatorNotFound() {
        // 准备参数
        OaSupplyIssuePageReqVO reqVO = new OaSupplyIssuePageReqVO().setCreatorName("不存在的用户");
        // mock 方法
        when(adminUserApi.getUserListByNickname(reqVO.getCreatorName())).thenReturn(Collections.emptyList());

        // 调用
        PageResult<OaSupplyApplyItemDO> page = supplyIssueService.getSupplyIssuePage(reqVO);

        // 断言
        assertEquals(0L, page.getTotal());
        assertTrue(page.getList().isEmpty());
    }

    @Test
    public void testGetSupplyIssuePage_creator() {
        // mock 数据
        OaSupplyApplyDO apply = randomPojo(OaSupplyApplyDO.class, o -> o.setId(null).setStatus(2)
                .setDeptId(100L).setUseType(1).setPickupMethod(1).setFileUrls(Collections.emptyList())
                .setProcessInstanceId(null).setCreator("1").setDeleted(false));
        supplyApplyMapper.insert(apply);
        OaSupplyApplyItemDO item = randomSupplyApplyItemDO().setApplyId(apply.getId());
        supplyApplyItemMapper.insert(item);
        OaSupplyApplyDO otherApply = randomPojo(OaSupplyApplyDO.class, o -> o.setId(null).setStatus(2)
                .setDeptId(100L).setUseType(1).setPickupMethod(1).setFileUrls(Collections.emptyList())
                .setProcessInstanceId(null).setCreator("2").setDeleted(false));
        supplyApplyMapper.insert(otherApply);
        supplyApplyItemMapper.insert(randomSupplyApplyItemDO().setApplyId(otherApply.getId()));
        // 准备参数
        OaSupplyIssuePageReqVO reqVO = new OaSupplyIssuePageReqVO().setCreatorName("芋道");
        // mock 方法
        when(adminUserApi.getUserListByNickname(reqVO.getCreatorName()))
                .thenReturn(Collections.singletonList(new AdminUserRespDTO().setId(1L)));

        // 调用
        PageResult<OaSupplyApplyItemDO> page = supplyIssueService.getSupplyIssuePage(reqVO);

        // 断言
        assertEquals(1L, page.getTotal());
        assertEquals(item.getId(), CollUtil.getFirst(page.getList()).getId());
    }

    @Test
    public void testIssueSupply_applyNotExists() {
        // mock 数据
        OaSupplyApplyItemDO item = randomSupplyApplyItemDO().setApplyId(1L);
        supplyApplyItemMapper.insert(item);
        // 准备参数
        OaSupplyIssueReqVO reqVO = new OaSupplyIssueReqVO().setId(item.getId()).setIssuedQuantity(1);

        // 调用，并断言异常
        assertServiceException(() -> supplyIssueService.issueSupply(reqVO, 1L), SUPPLY_APPLY_NOT_EXISTS);
        assertEquals(0, supplyApplyItemMapper.selectById(item.getId()).getIssuedQuantity());
        verifyNoInteractions(supplyItemService);
    }

    @Test
    public void testIssueSupply_stockFailureRollback() {
        // mock 数据
        OaSupplyApplyItemDO item = randomSupplyApplyItemDO().setApplyId(1L);
        supplyApplyItemMapper.insert(item);
        // 准备参数
        OaSupplyIssueReqVO reqVO = new OaSupplyIssueReqVO().setId(item.getId()).setIssuedQuantity(3);
        // mock 方法
        when(supplyApplyService.getSupplyApply(1L)).thenReturn(new OaSupplyApplyDO().setId(1L).setStatus(2));
        doThrow(new IllegalStateException("库存不足")).when(supplyItemService).updateSupplyItemStockQuantity(10L, -3);

        // 调用，并断言
        assertThrows(IllegalStateException.class, () -> new TransactionTemplate(transactionManager)
                .executeWithoutResult(status -> supplyIssueService.issueSupply(reqVO, 1L)));
        OaSupplyApplyItemDO actual = supplyApplyItemMapper.selectById(item.getId());
        assertEquals(0, actual.getStatus());
        assertEquals(0, actual.getIssuedQuantity());
    }

    // ========== 随机对象 ==========

    /**
     * 构造测试数据，固定业务字段的合法取值。
     *
     * @return 未入库的测试对象
     */
    private static OaSupplyApplyItemDO randomSupplyApplyItemDO() {
        return randomPojo(OaSupplyApplyItemDO.class, item -> item.setId(null).setItemId(10L)
                .setStatus(0).setManageType(2).setApplyQuantity(3).setIssuedQuantity(0)
                .setReturnedQuantity(0).setDeleted(false));
    }

}
