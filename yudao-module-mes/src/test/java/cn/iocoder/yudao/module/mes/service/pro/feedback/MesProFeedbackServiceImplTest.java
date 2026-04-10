package cn.iocoder.yudao.module.mes.service.pro.feedback;

import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.feedback.MesProFeedbackDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.productproduce.MesWmProductProduceLineDO;
import cn.iocoder.yudao.module.mes.dal.mysql.pro.feedback.MesProFeedbackMapper;
import cn.iocoder.yudao.module.mes.enums.pro.MesProFeedbackStatusEnum;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmQualityStatusEnum;
import cn.iocoder.yudao.module.mes.service.md.workstation.MesMdWorkstationService;
import cn.iocoder.yudao.module.mes.service.pro.route.MesProRouteProcessService;
import cn.iocoder.yudao.module.mes.service.pro.task.MesProTaskService;
import cn.iocoder.yudao.module.mes.service.pro.workorder.MesProWorkOrderService;
import cn.iocoder.yudao.module.mes.service.wm.itemconsume.MesWmItemConsumeService;
import cn.iocoder.yudao.module.mes.service.wm.productproduce.MesWmProductProduceLineService;
import cn.iocoder.yudao.module.mes.service.wm.productproduce.MesWmProductProduceService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * {@link MesProFeedbackServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
@Import(MesProFeedbackServiceImpl.class)
public class MesProFeedbackServiceImplTest extends BaseDbUnitTest {

    @Resource
    private MesProFeedbackServiceImpl feedbackService;

    @Resource
    private MesProFeedbackMapper feedbackMapper;

    @MockitoBean
    private MesProWorkOrderService workOrderService;
    @MockitoBean
    private MesProRouteProcessService routeProcessService;
    @MockitoBean
    private MesMdWorkstationService workstationService;
    @MockitoBean
    private MesProTaskService taskService;
    @MockitoBean
    private MesWmItemConsumeService itemConsumeService;
    @MockitoBean
    private MesWmProductProduceService productProduceService;
    @MockitoBean
    private MesWmProductProduceLineService produceLineService;

    @Test
    public void testUpdateProFeedbackWhenIpqcFinish_success_withUnqualified() {
        // 准备数据：插入一条待检验状态的报工单
        Long taskId = randomLongId();
        Long workOrderId = randomLongId();
        MesProFeedbackDO feedback = randomPojo(MesProFeedbackDO.class, o -> {
            o.setStatus(MesProFeedbackStatusEnum.UNCHECK.getStatus());
            o.setTaskId(taskId);
            o.setWorkOrderId(workOrderId);
            o.setFeedbackQuantity(BigDecimal.valueOf(100));
        });
        feedbackMapper.insert(feedback);

        // mock: 产出行（用于 updateTaskAndWorkOrderByFeedback 内的数量聚合）
        MesWmProductProduceLineDO qualifiedLine = MesWmProductProduceLineDO.builder()
                .quantity(BigDecimal.valueOf(80))
                .qualityStatus(MesWmQualityStatusEnum.PASS.getStatus())
                .build();
        MesWmProductProduceLineDO unqualifiedLine = MesWmProductProduceLineDO.builder()
                .quantity(BigDecimal.valueOf(20))
                .qualityStatus(MesWmQualityStatusEnum.FAIL.getStatus())
                .build();
        when(produceLineService.getProductProduceLineListByFeedbackId(feedback.getId()))
                .thenReturn(ListUtil.of(qualifiedLine, unqualifiedLine));

        // 调用
        BigDecimal qualifiedQty = BigDecimal.valueOf(80);
        BigDecimal unqualifiedQty = BigDecimal.valueOf(20);
        BigDecimal laborScrapQty = BigDecimal.valueOf(5);
        BigDecimal materialScrapQty = BigDecimal.valueOf(10);
        BigDecimal otherScrapQty = BigDecimal.valueOf(5);

        feedbackService.updateProFeedbackWhenIpqcFinish(feedback.getId(),
                qualifiedQty, unqualifiedQty, laborScrapQty, materialScrapQty, otherScrapQty);

        // 断言 1：调用了 splitPendingAndFinishProduce
        verify(productProduceService).splitPendingAndFinishProduce(
                eq(feedback.getId()), eq(qualifiedQty), eq(unqualifiedQty));

        // 断言 2：报工单状态更新为已完成
        MesProFeedbackDO updatedFeedback = feedbackMapper.selectById(feedback.getId());
        assertEquals(MesProFeedbackStatusEnum.FINISHED.getStatus(), updatedFeedback.getStatus());

        // 断言 3：数量回写正确
        assertEquals(0, qualifiedQty.compareTo(updatedFeedback.getQualifiedQuantity()));
        assertEquals(0, unqualifiedQty.compareTo(updatedFeedback.getUnqualifiedQuantity()));
        assertEquals(0, BigDecimal.ZERO.compareTo(updatedFeedback.getUncheckQuantity()));
        assertEquals(0, laborScrapQty.compareTo(updatedFeedback.getLaborScrapQuantity()));
        assertEquals(0, materialScrapQty.compareTo(updatedFeedback.getMaterialScrapQuantity()));
        assertEquals(0, otherScrapQty.compareTo(updatedFeedback.getOtherScrapQuantity()));

        // 断言 4：更新了任务和工单的已生产数量
        // 注意：数量经过 DB 存取后 scale 可能变化，用 any() 匹配
        verify(taskService).updateProducedQuantity(eq(taskId),
                any(BigDecimal.class), any(BigDecimal.class), any(BigDecimal.class));
        verify(workOrderService).updateProducedQuantity(eq(workOrderId),
                any(BigDecimal.class));
    }

    @Test
    public void testUpdateProFeedbackWhenIpqcFinish_success_allQualified() {
        // 准备数据：全部合格
        Long taskId = randomLongId();
        Long workOrderId = randomLongId();
        MesProFeedbackDO feedback = randomPojo(MesProFeedbackDO.class, o -> {
            o.setStatus(MesProFeedbackStatusEnum.UNCHECK.getStatus());
            o.setTaskId(taskId);
            o.setWorkOrderId(workOrderId);
            o.setFeedbackQuantity(BigDecimal.valueOf(50));
        });
        feedbackMapper.insert(feedback);

        // mock: 产出行（全部合格）
        MesWmProductProduceLineDO qualifiedLine = MesWmProductProduceLineDO.builder()
                .quantity(BigDecimal.valueOf(50))
                .qualityStatus(MesWmQualityStatusEnum.PASS.getStatus())
                .build();
        when(produceLineService.getProductProduceLineListByFeedbackId(feedback.getId()))
                .thenReturn(ListUtil.of(qualifiedLine));

        // 调用
        feedbackService.updateProFeedbackWhenIpqcFinish(feedback.getId(),
                BigDecimal.valueOf(50), BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

        // 断言 1：报工单状态为已完成
        MesProFeedbackDO updatedFeedback = feedbackMapper.selectById(feedback.getId());
        assertEquals(MesProFeedbackStatusEnum.FINISHED.getStatus(), updatedFeedback.getStatus());

        // 断言 2：合格品数量正确，不合格品和废品为 0
        assertEquals(0, BigDecimal.valueOf(50).compareTo(updatedFeedback.getQualifiedQuantity()));
        assertEquals(0, BigDecimal.ZERO.compareTo(updatedFeedback.getUnqualifiedQuantity()));
        assertEquals(0, BigDecimal.ZERO.compareTo(updatedFeedback.getLaborScrapQuantity()));
    }

    @Test
    public void testUpdateProFeedbackWhenIpqcFinish_feedbackNotExists() {
        // 调用不存在的 feedbackId，应该抛异常
        Long feedbackId = randomLongId();
        assertThrows(Exception.class, () ->
                feedbackService.updateProFeedbackWhenIpqcFinish(feedbackId,
                        BigDecimal.TEN, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
    }

    @Test
    public void testUpdateProFeedbackWhenIpqcFinish_feedbackNotUncheck() {
        // 准备数据：草稿状态（不是 UNCHECK），应该报错
        MesProFeedbackDO feedback = randomPojo(MesProFeedbackDO.class, o -> {
            o.setStatus(MesProFeedbackStatusEnum.PREPARE.getStatus());
        });
        feedbackMapper.insert(feedback);

        // 调用，应该抛异常
        assertThrows(Exception.class, () ->
                feedbackService.updateProFeedbackWhenIpqcFinish(feedback.getId(),
                        BigDecimal.TEN, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));

        // 断言：不应该调用任何产出单方法
        verify(productProduceService, never()).splitPendingAndFinishProduce(anyLong(), any(), any());
    }

    @Test
    public void testUpdateProFeedbackWhenIpqcFinish_feedbackAlreadyFinished() {
        // 准备数据：已完成状态，不能再次完成
        MesProFeedbackDO feedback = randomPojo(MesProFeedbackDO.class, o -> {
            o.setStatus(MesProFeedbackStatusEnum.FINISHED.getStatus());
        });
        feedbackMapper.insert(feedback);

        // 调用，应该抛异常
        assertThrows(Exception.class, () ->
                feedbackService.updateProFeedbackWhenIpqcFinish(feedback.getId(),
                        BigDecimal.TEN, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
    }

}
