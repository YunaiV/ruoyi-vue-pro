package cn.iocoder.yudao.module.oa.service.supply;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.oa.controller.admin.supply.vo.apply.*;
import cn.iocoder.yudao.module.oa.controller.admin.supply.vo.issue.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.supply.*;
import cn.iocoder.yudao.module.oa.dal.mysql.supply.*;
import cn.iocoder.yudao.module.oa.dal.redis.no.OaNoRedisDAO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link OaSupplyApplyServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import({OaSupplyApplyServiceImpl.class, ValidationAutoConfiguration.class})
public class OaSupplyApplyServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OaSupplyApplyService supplyApplyService;

    @Resource
    private OaSupplyApplyMapper supplyApplyMapper;
    @Resource
    private OaSupplyApplyItemMapper supplyApplyItemMapper;
    @Resource
    private PlatformTransactionManager transactionManager;

    @MockBean
    private OaSupplyItemService supplyItemService;
    @MockBean
    private AdminUserApi adminUserApi;
    @MockBean
    private OaNoRedisDAO noRedisDAO;
    @MockBean
    private BpmProcessInstanceApi processInstanceApi;

    @Test
    public void testGetPendingSupplyItemCount_statusAndDeletion() {
        // mock 数据
        OaSupplyApplyItemDO pending = randomSupplyApplyItemDO().setItemId(10L).setStatus(0);
        supplyApplyItemMapper.insert(pending);
        supplyApplyItemMapper.insert(randomSupplyApplyItemDO().setItemId(10L).setStatus(1));
        supplyApplyItemMapper.insert(randomSupplyApplyItemDO().setItemId(11L).setStatus(0));

        // 调用，并断言
        assertEquals(1L, supplyApplyService.getPendingSupplyItemCount(10L));
        supplyApplyItemMapper.deleteById(pending.getId());
        assertEquals(0L, supplyApplyService.getPendingSupplyItemCount(10L));
    }

    @Test
    public void testHasUnreturnedSupplyItem_quantityAndType() {
        // mock 数据：借用品已发放 3 件，已归还 1 件
        OaSupplyApplyItemDO item = randomSupplyApplyItemDO().setItemId(10L).setManageType(2)
                .setIssuedQuantity(3).setReturnedQuantity(1);
        supplyApplyItemMapper.insert(item);

        // 调用，并断言
        assertEquals(1L, supplyApplyService.getUnreturnedSupplyItemCount(10L));
        assertEquals(0L, supplyApplyService.getUnreturnedSupplyItemCount(11L));
        supplyApplyItemMapper.updateById(new OaSupplyApplyItemDO().setId(item.getId()).setReturnedQuantity(3));
        assertEquals(0L, supplyApplyService.getUnreturnedSupplyItemCount(10L));
        // 消耗品发放后不要求归还
        supplyApplyItemMapper.insert(randomSupplyApplyItemDO().setItemId(10L).setManageType(1)
                .setIssuedQuantity(3).setReturnedQuantity(0));
        assertEquals(0L, supplyApplyService.getUnreturnedSupplyItemCount(10L));
    }

    @Test
    public void testGetSupplyApply_notExists() {

        // 调用，并断言
        assertNull(supplyApplyService.getSupplyApply(-1L));
    }

    @Test
    public void testGetSupplyApplyMap_empty() {

        // 调用，并断言
        assertTrue(supplyApplyService.getSupplyApplyMap(Collections.emptyList()).isEmpty());
    }

    @Test
    public void testGetSupplyApplyMap_success() {
        // mock 数据
        OaSupplyApplyDO apply = randomSupplyApplyDO();
        supplyApplyMapper.insert(apply);
        supplyApplyMapper.insert(randomSupplyApplyDO());

        // 调用
        Map<Long, OaSupplyApplyDO> applies = supplyApplyService.getSupplyApplyMap(Collections.singletonList(apply.getId()));

        // 断言
        assertEquals(1, applies.size());
        assertEquals(apply.getNo(), applies.get(apply.getId()).getNo());
    }

    @Test
    public void testCreateSupplyApply_success() {
        // 准备参数
        OaSupplyApplySaveReqVO reqVO = new OaSupplyApplySaveReqVO().setApplyTime(LocalDateTime.now())
                .setUseType(1).setPickupMethod(1).setReason("办公领用").setFileUrls(Collections.emptyList())
                .setItems(Collections.singletonList(new OaSupplyApplySaveReqVO.Item().setItemId(10L).setApplyQuantity(2)));
        // mock 方法
        when(adminUserApi.validateUser(1L)).thenReturn(new AdminUserRespDTO().setId(1L).setDeptId(100L));
        when(noRedisDAO.generate(anyString())).thenReturn("YP-TEST");
        when(supplyItemService.getSupplyItemMap(anyCollection())).thenReturn(Collections.singletonMap(10L,
                new OaSupplyItemDO().setId(10L).setName("白板笔").setUnit("支").setManageType(1).setStatus(0)));

        // 调用
        Long id = supplyApplyService.createSupplyApply(reqVO, 1L);

        // 断言
        assertEquals(-1, supplyApplyMapper.selectById(id).getStatus());
        OaSupplyApplyItemDO item = CollUtil.getFirst(supplyApplyItemMapper.selectListByApplyId(id));
        assertEquals("白板笔", item.getItemName());
        assertEquals(2, item.getApplyQuantity());
        verify(supplyItemService, never()).updateSupplyItemStockQuantity(anyLong(), anyInt());
    }

    @Test
    public void testCreateSupplyApply_duplicateNo() {
        // mock 数据
        supplyApplyMapper.insert(randomSupplyApplyDO().setNo("YP-TEST"));
        // 准备参数
        OaSupplyApplySaveReqVO reqVO = new OaSupplyApplySaveReqVO().setApplyTime(LocalDateTime.now())
                .setUseType(1).setPickupMethod(1).setReason("办公领用").setFileUrls(Collections.emptyList())
                .setItems(Collections.singletonList(new OaSupplyApplySaveReqVO.Item().setItemId(10L).setApplyQuantity(2)));
        // mock 方法
        when(adminUserApi.validateUser(1L)).thenReturn(new AdminUserRespDTO().setId(1L).setDeptId(100L));
        when(noRedisDAO.generate(anyString())).thenReturn("YP-TEST");
        when(supplyItemService.getSupplyItemMap(anyCollection())).thenReturn(Collections.singletonMap(10L,
                new OaSupplyItemDO().setId(10L).setName("白板笔").setUnit("支").setManageType(1).setStatus(0)));

        // 调用，并断言：申请单号冲突不能提示为物品编号冲突
        assertServiceException(() -> supplyApplyService.createSupplyApply(reqVO, 1L), SUPPLY_APPLY_NO_DUPLICATE);
    }

    @Test
    public void testCreateSupplyApply_emptyDraft() {
        // 准备参数
        OaSupplyApplySaveReqVO reqVO = new OaSupplyApplySaveReqVO();

        // 调用，并断言
        assertThrows(ConstraintViolationException.class, () -> supplyApplyService.createSupplyApply(reqVO, 1L));
        verifyNoInteractions(supplyItemService, adminUserApi, noRedisDAO, processInstanceApi);
    }

    @Test
    public void testCreateSupplyApply_quantityMissing() {
        // 准备参数
        OaSupplyApplySaveReqVO reqVO = new OaSupplyApplySaveReqVO().setApplyTime(LocalDateTime.now())
                .setUseType(1).setPickupMethod(1).setReason("办公领用").setItems(Collections.singletonList(
                new OaSupplyApplySaveReqVO.Item().setItemId(10L)));

        // 调用，并断言
        assertThrows(ConstraintViolationException.class, () -> supplyApplyService.createSupplyApply(reqVO, 1L));
        verifyNoInteractions(supplyItemService, adminUserApi, noRedisDAO, processInstanceApi);
    }

    @Test
    public void testUpdateSupplyApply_missingInformation() {
        // mock 数据
        OaSupplyApplyDO apply = randomSupplyApplyDO().setStatus(3).setProcessInstanceId("old-process");
        supplyApplyMapper.insert(apply);
        supplyApplyItemMapper.insert(randomSupplyApplyItemDO().setApplyId(apply.getId()));
        // 准备参数
        OaSupplyApplySaveReqVO reqVO = new OaSupplyApplySaveReqVO().setId(apply.getId());

        // 调用，并断言
        assertThrows(ConstraintViolationException.class, () -> supplyApplyService.updateSupplyApply(reqVO, 1L));

        // 断言
        OaSupplyApplyDO actual = supplyApplyMapper.selectById(apply.getId());
        assertEquals(apply.getApplyTime(), actual.getApplyTime());
        assertEquals(apply.getUseType(), actual.getUseType());
        assertEquals(apply.getPickupMethod(), actual.getPickupMethod());
        assertEquals(apply.getReason(), actual.getReason());
        assertEquals(3, actual.getStatus());
        assertEquals("old-process", actual.getProcessInstanceId());
        assertEquals(apply.getDeptId(), actual.getDeptId());
        assertEquals(apply.getNo(), actual.getNo());
        assertEquals(1, supplyApplyItemMapper.selectListByApplyId(apply.getId()).size());
    }

    @Test
    public void testUpdateSupplyApply_nullFieldsIgnored() {
        // mock 数据
        OaSupplyApplyDO apply = randomSupplyApplyDO().setStatus(3).setProcessInstanceId("old-process")
                .setRemark("原备注").setFileUrls(Collections.singletonList("https://example.com/test.pdf"));
        supplyApplyMapper.insert(apply);
        // 准备参数
        OaSupplyApplySaveReqVO reqVO = new OaSupplyApplySaveReqVO().setId(apply.getId())
                .setApplyTime(LocalDateTime.of(2026, 9, 15, 0, 0)).setUseType(1).setPickupMethod(1)
                .setReason("更新事由").setItems(Collections.singletonList(
                        new OaSupplyApplySaveReqVO.Item().setItemId(10L).setApplyQuantity(2)));
        // mock 方法
        when(supplyItemService.getSupplyItemMap(anyCollection())).thenReturn(Collections.singletonMap(10L,
                new OaSupplyItemDO().setId(10L).setName("白板笔").setManageType(1).setStatus(0)));

        // 调用
        supplyApplyService.updateSupplyApply(reqVO, 1L);

        // 断言
        OaSupplyApplyDO actual = supplyApplyMapper.selectById(apply.getId());
        assertEquals(reqVO.getReason(), actual.getReason());
        assertEquals(reqVO.getApplyTime(), actual.getApplyTime());
        assertEquals(apply.getRemark(), actual.getRemark());
        assertEquals(apply.getFileUrls(), actual.getFileUrls());
        assertEquals(apply.getStatus(), actual.getStatus());
        assertEquals(apply.getProcessInstanceId(), actual.getProcessInstanceId());
    }

    @Test
    public void testUpdateSupplyApply_completeDraft() {
        // mock 数据
        OaSupplyApplyDO apply = randomSupplyApplyDO().setStatus(-1).setApplyTime(null).setReason(null);
        supplyApplyMapper.insert(apply);
        // 准备参数
        OaSupplyApplySaveReqVO reqVO = new OaSupplyApplySaveReqVO().setId(apply.getId())
                .setApplyTime(LocalDateTime.of(2026, 9, 15, 0, 0)).setUseType(1).setPickupMethod(1)
                .setReason("补全草稿").setFileUrls(Collections.singletonList("https://example.com/test.pdf"))
                .setItems(Collections.singletonList(new OaSupplyApplySaveReqVO.Item().setItemId(10L).setApplyQuantity(2)));
        // mock 方法
        when(supplyItemService.getSupplyItemMap(anyCollection())).thenReturn(Collections.singletonMap(10L,
                new OaSupplyItemDO().setId(10L).setName("白板笔").setManageType(1).setStatus(0)));
        when(processInstanceApi.createProcessInstance(eq(1L), any())).thenReturn("process-complete");

        // 调用
        supplyApplyService.updateSupplyApply(reqVO, 1L);
        String processId = supplyApplyService.submitSupplyApply(apply.getId(), 1L);

        // 断言
        OaSupplyApplyDO actual = supplyApplyMapper.selectById(apply.getId());
        assertEquals(reqVO.getApplyTime(), actual.getApplyTime());
        assertEquals(reqVO.getReason(), actual.getReason());
        assertEquals(reqVO.getFileUrls(), actual.getFileUrls());
        assertEquals("process-complete", processId);
        assertEquals(1, actual.getStatus());
    }

    @ParameterizedTest
    @ValueSource(strings = {"applyTime", "useType", "pickupMethod", "reason"})
    public void testCreateSupplyApply_missingInformation(String field) {
        // mock 数据
        OaSupplyApplySaveReqVO apply = new OaSupplyApplySaveReqVO().setApplyTime(LocalDateTime.now())
                .setUseType(1).setPickupMethod(1).setReason("办公领用")
                .setItems(Collections.singletonList(new OaSupplyApplySaveReqVO.Item().setItemId(10L).setApplyQuantity(2)));
        switch (field) {
            case "applyTime":
                apply.setApplyTime(null);
                break;
            case "useType":
                apply.setUseType(null);
                break;
            case "pickupMethod":
                apply.setPickupMethod(null);
                break;
            case "reason":
                apply.setReason(" ");
                break;
        }

        // 调用，并断言
        assertThrows(ConstraintViolationException.class, () -> supplyApplyService.createSupplyApply(apply, 1L));
        verifyNoInteractions(processInstanceApi, noRedisDAO, supplyItemService);
    }

    @Test
    public void testCreateSupplyApply_emptyItems() {
        // 准备参数
        OaSupplyApplySaveReqVO reqVO = new OaSupplyApplySaveReqVO().setApplyTime(LocalDateTime.now())
                .setUseType(1).setPickupMethod(1).setReason("办公领用").setItems(Collections.emptyList());

        // 调用，并断言
        assertThrows(ConstraintViolationException.class, () -> supplyApplyService.createSupplyApply(reqVO, 1L));
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testCreateSupplyApply_invalidQuantity() {
        // 准备参数
        OaSupplyApplySaveReqVO reqVO = new OaSupplyApplySaveReqVO().setApplyTime(LocalDateTime.now())
                .setUseType(1).setPickupMethod(1).setReason("办公领用")
                .setItems(Collections.singletonList(new OaSupplyApplySaveReqVO.Item().setItemId(10L).setApplyQuantity(0)));

        // 调用，并断言
        assertThrows(ConstraintViolationException.class, () -> supplyApplyService.createSupplyApply(reqVO, 1L));
        verifyNoInteractions(processInstanceApi);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void testSubmitSupplyApply_itemUnavailable(boolean deleted) {
        // mock 数据
        OaSupplyApplyDO apply = randomSupplyApplyDO().setStatus(-1);
        supplyApplyMapper.insert(apply);
        supplyApplyItemMapper.insert(randomSupplyApplyItemDO().setApplyId(apply.getId()));
        // mock 方法
        when(supplyItemService.getSupplyItemMap(anyCollection())).thenReturn(deleted ? Collections.emptyMap()
                : Collections.singletonMap(10L, new OaSupplyItemDO().setId(10L).setStatus(1)));

        // 调用，并断言异常
        assertServiceException(() -> supplyApplyService.submitSupplyApply(apply.getId(), 1L), SUPPLY_APPLY_ITEM_INVALID);
        assertEquals(-1, supplyApplyMapper.selectById(apply.getId()).getStatus());
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testSubmitSupplyApply_success() {
        // mock 数据
        OaSupplyApplyDO apply = randomSupplyApplyDO().setStatus(-1);
        supplyApplyMapper.insert(apply);
        supplyApplyItemMapper.insert(randomSupplyApplyItemDO().setApplyId(apply.getId()));
        // mock 方法
        when(supplyItemService.getSupplyItemMap(anyCollection())).thenReturn(Collections.singletonMap(10L,
                new OaSupplyItemDO().setId(10L).setStatus(0)));
        when(processInstanceApi.createProcessInstance(eq(1L), any())).thenReturn("process-1");

        // 调用
        String processId = supplyApplyService.submitSupplyApply(apply.getId(), 1L);

        // 断言
        assertEquals("process-1", processId);
        assertEquals(1, supplyApplyMapper.selectById(apply.getId()).getStatus());
        verify(supplyItemService, never()).updateSupplyItemStockQuantity(anyLong(), anyInt());
    }

    @Test
    public void testSubmitSupplyApply_failureRollback() {
        // mock 数据
        OaSupplyApplyDO apply = randomSupplyApplyDO().setStatus(-1);
        supplyApplyMapper.insert(apply);
        supplyApplyItemMapper.insert(randomSupplyApplyItemDO().setApplyId(apply.getId()));
        // mock 方法
        when(supplyItemService.getSupplyItemMap(anyCollection())).thenReturn(Collections.singletonMap(10L,
                new OaSupplyItemDO().setId(10L).setStatus(0)));
        when(processInstanceApi.createProcessInstance(eq(1L), any())).thenThrow(new IllegalStateException("流程未部署"));

        // 调用，并断言
        assertThrows(IllegalStateException.class, () -> new TransactionTemplate(transactionManager)
                .executeWithoutResult(status -> supplyApplyService.submitSupplyApply(apply.getId(), 1L)));
        assertEquals(-1, supplyApplyMapper.selectById(apply.getId()).getStatus());
    }

    @Test
    public void testUpdateSupplyApplyStatus_approve() {
        // mock 数据
        OaSupplyApplyDO apply = randomSupplyApplyDO().setStatus(1).setProcessInstanceId("process-1");
        supplyApplyMapper.insert(apply);
        OaSupplyApplyItemDO item = randomSupplyApplyItemDO().setApplyId(apply.getId()).setStatus(-1);
        supplyApplyItemMapper.insert(item);

        // 调用
        supplyApplyService.updateSupplyApplyStatus(apply.getId(), "process-1", 2);

        // 断言
        assertEquals(2, supplyApplyMapper.selectById(apply.getId()).getStatus());
        assertEquals(0, supplyApplyItemMapper.selectById(item.getId()).getStatus());
    }

    @Test
    public void testUpdateSupplyApplyStatus_oldProcessIgnored() {
        // mock 数据
        OaSupplyApplyDO apply = randomSupplyApplyDO().setStatus(1).setProcessInstanceId("new-process");
        supplyApplyMapper.insert(apply);

        // 调用
        supplyApplyService.updateSupplyApplyStatus(apply.getId(), "old-process", 2);

        // 断言
        assertEquals(1, supplyApplyMapper.selectById(apply.getId()).getStatus());
    }

    @Test
    public void testUpdateSupplyApplyStatus_repeatApprove() {
        // mock 数据
        OaSupplyApplyDO apply = randomSupplyApplyDO().setStatus(2).setProcessInstanceId("process-1");
        supplyApplyMapper.insert(apply);
        OaSupplyApplyItemDO item = randomSupplyApplyItemDO().setApplyId(apply.getId()).setStatus(2).setIssuedQuantity(3);
        supplyApplyItemMapper.insert(item);

        // 调用
        supplyApplyService.updateSupplyApplyStatus(apply.getId(), "process-1", 2);

        // 断言：同一流程重复通过，不重置已发放明细
        assertEquals(2, supplyApplyMapper.selectById(apply.getId()).getStatus());
        OaSupplyApplyItemDO dbItem = supplyApplyItemMapper.selectById(item.getId());
        assertEquals(2, dbItem.getStatus());
        assertEquals(3, dbItem.getIssuedQuantity());
    }


    @Test
    public void testDeleteSupplyApply_notOwner() {
        // mock 数据
        OaSupplyApplyDO apply = randomSupplyApplyDO().setStatus(-1);
        supplyApplyMapper.insert(apply);

        // 调用，并断言异常
        assertServiceException(() -> supplyApplyService.deleteSupplyApply(apply.getId(), 2L), SUPPLY_APPLY_ACCESS_DENIED);
        assertNotNull(supplyApplyMapper.selectById(apply.getId()));
    }

    // ========== 随机对象 ==========

    /**
     * 构造测试数据，固定业务字段的合法取值。
     *
     * @return 未入库的测试对象
     */
    private static OaSupplyApplyDO randomSupplyApplyDO() {
        return randomPojo(OaSupplyApplyDO.class, apply -> apply.setId(null).setStatus(2)
                .setDeptId(100L).setUseType(1).setPickupMethod(1).setFileUrls(Collections.emptyList())
                .setProcessInstanceId(null).setCreator("1").setDeleted(false));
    }

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
