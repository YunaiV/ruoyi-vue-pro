package cn.iocoder.yudao.module.mes.service.qc.ipqc;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.mes.controller.admin.qc.ipqc.vo.MesQcIpqcSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.workstation.MesMdWorkstationDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.feedback.MesProFeedbackDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.route.MesProRouteProcessDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.route.MesProRouteProductDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.task.MesProTaskDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.workorder.MesProWorkOrderDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.ipqc.MesQcIpqcDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.template.MesQcTemplateItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.productproduce.MesWmProductProduceLineDO;
import cn.iocoder.yudao.module.mes.dal.mysql.qc.ipqc.MesQcIpqcMapper;
import cn.iocoder.yudao.module.mes.enums.MesBizTypeConstants;
import cn.iocoder.yudao.module.mes.enums.qc.MesQcStatusEnum;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmQualityStatusEnum;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.md.workstation.MesMdWorkstationService;
import cn.iocoder.yudao.module.mes.service.pro.feedback.MesProFeedbackService;
import cn.iocoder.yudao.module.mes.service.pro.route.MesProRouteProcessService;
import cn.iocoder.yudao.module.mes.service.pro.route.MesProRouteProductService;
import cn.iocoder.yudao.module.mes.service.pro.task.MesProTaskService;
import cn.iocoder.yudao.module.mes.service.pro.workorder.MesProWorkOrderService;
import cn.iocoder.yudao.module.mes.service.qc.defectrecord.MesQcDefectRecordService;
import cn.iocoder.yudao.module.mes.service.qc.indicatorresult.MesQcIndicatorResultService;
import cn.iocoder.yudao.module.mes.service.qc.template.MesQcTemplateItemService;
import cn.iocoder.yudao.module.mes.service.wm.productproduce.MesWmProductProduceLineService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link MesQcIpqcServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
@Import(MesQcIpqcServiceImpl.class)
public class MesQcIpqcServiceImplTest extends BaseDbUnitTest {

    @Resource
    private MesQcIpqcServiceImpl ipqcService;

    @Resource
    private MesQcIpqcMapper ipqcMapper;

    @MockitoBean
    private MesQcTemplateItemService templateItemService;
    @MockitoBean
    private MesQcIpqcLineService ipqcLineService;
    @MockitoBean
    private MesProWorkOrderService workOrderService;
    @MockitoBean
    private MesMdWorkstationService workstationService;
    @MockitoBean
    private MesMdItemService itemService;
    @MockitoBean
    private MesQcDefectRecordService defectRecordService;
    @MockitoBean
    private AdminUserApi adminUserApi;
    @MockitoBean
    private MesProFeedbackService feedbackService;
    @MockitoBean
    private MesProRouteProductService routeProductService;
    @MockitoBean
    private MesProRouteProcessService routeProcessService;
    @MockitoBean
    private MesProTaskService taskService;
    @MockitoBean
    private MesQcIndicatorResultService indicatorResultService;
    @MockitoBean
    private MesWmProductProduceLineService productProduceLineService;

    @Test
    public void testFinishIpqc_writeBack_feedback() {
        // 准备数据：插入一条草稿状态的 IPQC 单，来源为报工单（PRO_FEEDBACK=304）
        Long sourceDocId = randomLongId();
        Long sourceLineId = randomLongId();
        BigDecimal qualifiedQty = BigDecimal.valueOf(80);
        BigDecimal unqualifiedQty = BigDecimal.valueOf(20);
        BigDecimal laborScrapQty = BigDecimal.valueOf(5);
        BigDecimal materialScrapQty = BigDecimal.valueOf(10);
        BigDecimal otherScrapQty = BigDecimal.valueOf(5);

        MesQcIpqcDO ipqc = randomPojo(MesQcIpqcDO.class, o -> {
            o.setStatus(MesQcStatusEnum.DRAFT.getStatus());
            o.setCheckResult(1); // 非空，满足 finishIpqc 的校验
            o.setSourceDocType(MesBizTypeConstants.PRO_FEEDBACK);
            o.setSourceDocId(sourceDocId);
            o.setSourceLineId(sourceLineId);
            o.setQualifiedQuantity(qualifiedQty);
            o.setUnqualifiedQuantity(unqualifiedQty);
            o.setLaborScrapQuantity(laborScrapQty);
            o.setMaterialScrapQuantity(materialScrapQty);
            o.setOtherScrapQuantity(otherScrapQty);
        });
        ipqcMapper.insert(ipqc);

        // 调用
        ipqcService.finishIpqc(ipqc.getId());

        // 断言 1：状态更新为已完成
        MesQcIpqcDO updatedIpqc = ipqcMapper.selectById(ipqc.getId());
        assertEquals(MesQcStatusEnum.FINISHED.getStatus(), updatedIpqc.getStatus());

        // 断言 2：调用了 feedbackService，传递 sourceLineId
        // 注意：数量经过 DB 存取后 scale 可能变化（例如 80 → 80.00），所以用 any() 匹配
        verify(feedbackService).updateProFeedbackWhenIpqcFinish(
                eq(sourceDocId),
                eq(sourceLineId),
                any(BigDecimal.class),
                any(BigDecimal.class),
                any(BigDecimal.class),
                any(BigDecimal.class),
                any(BigDecimal.class));
    }

    @Test
    public void testFinishIpqc_writeBack_feedback_withNullQuantities() {
        // 准备数据：合格品/不合格品/废品数量为 null 的场景，应该 defaultIfNull 为 0
        Long sourceDocId = randomLongId();
        Long sourceLineId = randomLongId();
        MesQcIpqcDO ipqc = randomPojo(MesQcIpqcDO.class, o -> {
            o.setStatus(MesQcStatusEnum.DRAFT.getStatus());
            o.setCheckResult(1);
            o.setSourceDocType(MesBizTypeConstants.PRO_FEEDBACK);
            o.setSourceDocId(sourceDocId);
            o.setSourceLineId(sourceLineId);
            o.setQualifiedQuantity(null);
            o.setUnqualifiedQuantity(null);
            o.setLaborScrapQuantity(null);
            o.setMaterialScrapQuantity(null);
            o.setOtherScrapQuantity(null);
        });
        ipqcMapper.insert(ipqc);

        // 调用
        ipqcService.finishIpqc(ipqc.getId());

        // 断言：所有数量参数都是 BigDecimal.ZERO（defaultIfNull 处理）
        verify(feedbackService).updateProFeedbackWhenIpqcFinish(
                eq(sourceDocId),
                eq(sourceLineId),
                eq(BigDecimal.ZERO),
                eq(BigDecimal.ZERO),
                eq(BigDecimal.ZERO),
                eq(BigDecimal.ZERO),
                eq(BigDecimal.ZERO));
    }

    @Test
    public void testFinishIpqc_writeBack_noSourceDoc() {
        // 准备数据：sourceDocType 为空，不需要回写
        MesQcIpqcDO ipqc = randomPojo(MesQcIpqcDO.class, o -> {
            o.setStatus(MesQcStatusEnum.DRAFT.getStatus());
            o.setCheckResult(1);
            o.setSourceDocType(null);
            o.setSourceDocId(null);
            o.setSourceLineId(null);
        });
        ipqcMapper.insert(ipqc);

        // 调用
        ipqcService.finishIpqc(ipqc.getId());

        // 断言 1：状态更新成功
        MesQcIpqcDO updatedIpqc = ipqcMapper.selectById(ipqc.getId());
        assertEquals(MesQcStatusEnum.FINISHED.getStatus(), updatedIpqc.getStatus());

        // 断言 2：不应该调用 feedbackService
        verify(feedbackService, never()).updateProFeedbackWhenIpqcFinish(
                anyLong(), anyLong(), any(), any(), any(), any(), any());
    }

    @Test
    public void testFinishIpqc_writeBack_sourceDocTypeDirtyData() {
        // 准备数据：sourceDocType 非空但 sourceDocId 为空，应该抛异常
        MesQcIpqcDO ipqc = randomPojo(MesQcIpqcDO.class, o -> {
            o.setStatus(MesQcStatusEnum.DRAFT.getStatus());
            o.setCheckResult(1);
            o.setSourceDocType(MesBizTypeConstants.PRO_FEEDBACK);
            o.setSourceDocId(null); // 脏数据
            o.setSourceLineId(null);
        });
        ipqcMapper.insert(ipqc);

        // 调用，并断言异常
        assertThrows(IllegalArgumentException.class, () -> ipqcService.finishIpqc(ipqc.getId()));
    }

    @Test
    public void testFinishIpqc_writeBack_unknownSourceDocType() {
        // 准备数据：sourceDocType 无法识别
        MesQcIpqcDO ipqc = randomPojo(MesQcIpqcDO.class, o -> {
            o.setStatus(MesQcStatusEnum.DRAFT.getStatus());
            o.setCheckResult(1);
            o.setSourceDocType(99999); // 无法识别的类型
            o.setSourceDocId(randomLongId());
            o.setSourceLineId(null);
        });
        ipqcMapper.insert(ipqc);

        // 调用，并断言异常
        assertThrows(IllegalArgumentException.class, () -> ipqcService.finishIpqc(ipqc.getId()));
    }

    @Test
    public void testFinishIpqc_checkResultEmpty() {
        // 准备数据：checkResult 为空，应该报错
        MesQcIpqcDO ipqc = randomPojo(MesQcIpqcDO.class, o -> {
            o.setStatus(MesQcStatusEnum.DRAFT.getStatus());
            o.setCheckResult(null); // 检测结论为空
            o.setSourceDocType(null);
        });
        ipqcMapper.insert(ipqc);

        // 调用，应该抛异常（检测结论必填）
        assertThrows(Exception.class, () -> ipqcService.finishIpqc(ipqc.getId()));
    }

    @Test
    public void testFinishIpqc_notDraftStatus() {
        // 准备数据：已完成状态，不能再次完成
        MesQcIpqcDO ipqc = randomPojo(MesQcIpqcDO.class, o -> {
            o.setStatus(MesQcStatusEnum.FINISHED.getStatus());
            o.setCheckResult(1);
        });
        ipqcMapper.insert(ipqc);

        // 调用，应该抛异常（不是草稿状态）
        assertThrows(Exception.class, () -> ipqcService.finishIpqc(ipqc.getId()));
    }

    // ==================== processId 推导行为测试 ====================

    /**
     * 工位有工序 + 该工序在产品工艺路线中 → processId 应该被设置为工位工序
     */
    @Test
    public void testCreateIpqc_processId_workstationHasProcess() {
        // 准备参数
        Long workstationId = randomLongId();
        Long workOrderId = randomLongId();
        Long productId = randomLongId();
        Long processId = randomLongId();
        Long routeId = randomLongId();
        Long templateId = randomLongId();

        MesQcIpqcSaveReqVO reqVO = new MesQcIpqcSaveReqVO();
        reqVO.setCode("IPQC-TEST-001");
        reqVO.setWorkstationId(workstationId);
        reqVO.setWorkOrderId(workOrderId);
        reqVO.setInspectorUserId(randomLongId());

        // mock 工位返回有工序
        MesMdWorkstationDO workstation = new MesMdWorkstationDO();
        workstation.setId(workstationId);
        workstation.setProcessId(processId);
        when(workstationService.validateWorkstationExists(workstationId)).thenReturn(workstation);
        // mock 工单
        MesProWorkOrderDO workOrder = new MesProWorkOrderDO();
        workOrder.setId(workOrderId);
        workOrder.setProductId(productId);
        when(workOrderService.validateWorkOrderExists(workOrderId)).thenReturn(workOrder);
        when(workOrderService.validateWorkOrderConfirmed(workOrderId)).thenReturn(workOrder);
        // mock 模板匹配
        MesQcTemplateItemDO templateItem = new MesQcTemplateItemDO();
        templateItem.setTemplateId(templateId);
        when(templateItemService.getRequiredTemplateByItemIdAndType(eq(productId), anyInt())).thenReturn(templateItem);
        // mock 工艺路线
        MesProRouteProductDO routeProduct = new MesProRouteProductDO();
        routeProduct.setRouteId(routeId);
        when(routeProductService.getRouteProductByItemId(productId)).thenReturn(routeProduct);
        // mock 工序在路线中
        when(routeProcessService.getRouteProcessByRouteIdAndProcessId(routeId, processId))
                .thenReturn(new MesProRouteProcessDO());

        // 调用
        Long ipqcId = ipqcService.createIpqc(reqVO);

        // 断言：processId 应该是工位工序
        MesQcIpqcDO ipqc = ipqcMapper.selectById(ipqcId);
        assertEquals(processId, ipqc.getProcessId());
    }

    /**
     * 工位没有工序（processId=null）→ processId 应该留 null（不回退到任务工序）
     */
    @Test
    public void testCreateIpqc_processId_workstationNoProcess_shouldBeNull() {
        // 准备参数
        Long workstationId = randomLongId();
        Long workOrderId = randomLongId();
        Long productId = randomLongId();
        Long taskId = randomLongId();
        Long templateId = randomLongId();

        MesQcIpqcSaveReqVO reqVO = new MesQcIpqcSaveReqVO();
        reqVO.setCode("IPQC-TEST-002");
        reqVO.setWorkstationId(workstationId);
        reqVO.setWorkOrderId(workOrderId);
        reqVO.setTaskId(taskId); // 故意传入 taskId，验证不会回退
        reqVO.setInspectorUserId(randomLongId());

        // mock 工位没有工序
        MesMdWorkstationDO workstation = new MesMdWorkstationDO();
        workstation.setId(workstationId);
        workstation.setProcessId(null); // 无工序
        when(workstationService.validateWorkstationExists(workstationId)).thenReturn(workstation);
        // mock 工单
        MesProWorkOrderDO workOrder = new MesProWorkOrderDO();
        workOrder.setId(workOrderId);
        workOrder.setProductId(productId);
        when(workOrderService.validateWorkOrderExists(workOrderId)).thenReturn(workOrder);
        when(workOrderService.validateWorkOrderConfirmed(workOrderId)).thenReturn(workOrder);
        // mock 任务
        MesProTaskDO task = new MesProTaskDO();
        task.setId(taskId);
        task.setWorkOrderId(workOrderId);
        task.setWorkstationId(workstationId);
        task.setItemId(productId);
        when(taskService.validateTaskNotFinished(taskId)).thenReturn(task);
        // mock 模板匹配
        MesQcTemplateItemDO templateItem = new MesQcTemplateItemDO();
        templateItem.setTemplateId(templateId);
        when(templateItemService.getRequiredTemplateByItemIdAndType(eq(productId), anyInt())).thenReturn(templateItem);

        // 调用
        Long ipqcId = ipqcService.createIpqc(reqVO);

        // 断言：processId 应该是 null，即使传入了 taskId 也不应该回退
        MesQcIpqcDO ipqc = ipqcMapper.selectById(ipqcId);
        assertNull(ipqc.getProcessId(),
                "工位无工序时 processId 应为 null，不应回退到任务工序");
        // 断言：不应该读取任务的 processId
        verify(routeProductService, never()).getRouteProductByItemId(any());
    }

    /**
     * 工位有工序但该工序不在产品工艺路线中 → processId 应该留 null
     */
    @Test
    public void testCreateIpqc_processId_processNotInRoute() {
        // 准备参数
        Long workstationId = randomLongId();
        Long workOrderId = randomLongId();
        Long productId = randomLongId();
        Long processId = randomLongId();
        Long routeId = randomLongId();
        Long templateId = randomLongId();

        MesQcIpqcSaveReqVO reqVO = new MesQcIpqcSaveReqVO();
        reqVO.setCode("IPQC-TEST-003");
        reqVO.setWorkstationId(workstationId);
        reqVO.setWorkOrderId(workOrderId);
        reqVO.setInspectorUserId(randomLongId());

        // mock 工位有工序
        MesMdWorkstationDO workstation = new MesMdWorkstationDO();
        workstation.setId(workstationId);
        workstation.setProcessId(processId);
        when(workstationService.validateWorkstationExists(workstationId)).thenReturn(workstation);
        // mock 工单
        MesProWorkOrderDO workOrder = new MesProWorkOrderDO();
        workOrder.setId(workOrderId);
        workOrder.setProductId(productId);
        when(workOrderService.validateWorkOrderExists(workOrderId)).thenReturn(workOrder);
        when(workOrderService.validateWorkOrderConfirmed(workOrderId)).thenReturn(workOrder);
        // mock 模板匹配
        MesQcTemplateItemDO templateItem = new MesQcTemplateItemDO();
        templateItem.setTemplateId(templateId);
        when(templateItemService.getRequiredTemplateByItemIdAndType(eq(productId), anyInt())).thenReturn(templateItem);
        // mock 工艺路线存在
        MesProRouteProductDO routeProduct = new MesProRouteProductDO();
        routeProduct.setRouteId(routeId);
        when(routeProductService.getRouteProductByItemId(productId)).thenReturn(routeProduct);
        // mock 工序不在路线中
        when(routeProcessService.getRouteProcessByRouteIdAndProcessId(routeId, processId))
                .thenReturn(null);

        // 调用
        Long ipqcId = ipqcService.createIpqc(reqVO);

        // 断言：processId 应该是 null
        MesQcIpqcDO ipqc = ipqcMapper.selectById(ipqcId);
        assertNull(ipqc.getProcessId());
    }

    // ==================== sourceLineId 校验测试 ====================

    /**
     * sourceDocType=PRO_FEEDBACK 时 sourceLineId 为空 → 创建失败
     */
    @Test
    public void testCreateIpqc_sourceLineId_required() {
        // 准备参数
        Long workstationId = randomLongId();
        Long workOrderId = randomLongId();
        Long productId = randomLongId();
        Long feedbackId = randomLongId();
        Long templateId = randomLongId();

        MesQcIpqcSaveReqVO reqVO = new MesQcIpqcSaveReqVO();
        reqVO.setCode("IPQC-SL-001");
        reqVO.setWorkstationId(workstationId);
        reqVO.setWorkOrderId(workOrderId);
        reqVO.setInspectorUserId(randomLongId());
        reqVO.setSourceDocType(MesBizTypeConstants.PRO_FEEDBACK);
        reqVO.setSourceDocId(feedbackId);
        reqVO.setSourceLineId(null); // 不传 sourceLineId

        // mock
        MesMdWorkstationDO workstation = new MesMdWorkstationDO();
        workstation.setId(workstationId);
        workstation.setProcessId(null);
        when(workstationService.validateWorkstationExists(workstationId)).thenReturn(workstation);
        MesProWorkOrderDO workOrder = new MesProWorkOrderDO();
        workOrder.setId(workOrderId);
        workOrder.setProductId(productId);
        when(workOrderService.validateWorkOrderExists(workOrderId)).thenReturn(workOrder);
        when(workOrderService.validateWorkOrderConfirmed(workOrderId)).thenReturn(workOrder);
        MesQcTemplateItemDO templateItem = new MesQcTemplateItemDO();
        templateItem.setTemplateId(templateId);
        when(templateItemService.getRequiredTemplateByItemIdAndType(eq(productId), anyInt())).thenReturn(templateItem);
        when(feedbackService.validateFeedbackExists(feedbackId)).thenReturn(new MesProFeedbackDO());

        // 调用，断言异常
        ServiceException ex = assertThrows(ServiceException.class, () -> ipqcService.createIpqc(reqVO));
        assertEquals(QC_IPQC_SOURCE_LINE_REQUIRED.getCode(), ex.getCode());
    }

    /**
     * sourceLineId 不属于该报工的产出行 → 创建失败
     */
    @Test
    public void testCreateIpqc_sourceLineId_notBelong() {
        // 准备参数
        Long workstationId = randomLongId();
        Long workOrderId = randomLongId();
        Long productId = randomLongId();
        Long feedbackId = randomLongId();
        Long sourceLineId = randomLongId();
        Long templateId = randomLongId();

        MesQcIpqcSaveReqVO reqVO = new MesQcIpqcSaveReqVO();
        reqVO.setCode("IPQC-SL-002");
        reqVO.setWorkstationId(workstationId);
        reqVO.setWorkOrderId(workOrderId);
        reqVO.setInspectorUserId(randomLongId());
        reqVO.setSourceDocType(MesBizTypeConstants.PRO_FEEDBACK);
        reqVO.setSourceDocId(feedbackId);
        reqVO.setSourceLineId(sourceLineId);

        // mock
        MesMdWorkstationDO workstation = new MesMdWorkstationDO();
        workstation.setId(workstationId);
        workstation.setProcessId(null);
        when(workstationService.validateWorkstationExists(workstationId)).thenReturn(workstation);
        MesProWorkOrderDO workOrder = new MesProWorkOrderDO();
        workOrder.setId(workOrderId);
        workOrder.setProductId(productId);
        when(workOrderService.validateWorkOrderExists(workOrderId)).thenReturn(workOrder);
        when(workOrderService.validateWorkOrderConfirmed(workOrderId)).thenReturn(workOrder);
        MesQcTemplateItemDO templateItem = new MesQcTemplateItemDO();
        templateItem.setTemplateId(templateId);
        when(templateItemService.getRequiredTemplateByItemIdAndType(eq(productId), anyInt())).thenReturn(templateItem);
        when(feedbackService.validateFeedbackExists(feedbackId)).thenReturn(new MesProFeedbackDO());
        // mock：产出行存在但 feedbackId 不匹配
        MesWmProductProduceLineDO line = new MesWmProductProduceLineDO();
        line.setId(sourceLineId);
        line.setFeedbackId(randomLongId()); // 不同的 feedbackId
        line.setQualityStatus(MesWmQualityStatusEnum.PENDING.getStatus());
        when(productProduceLineService.validateProductProduceLineExists(sourceLineId)).thenReturn(line);

        // 调用，断言异常
        ServiceException ex = assertThrows(ServiceException.class, () -> ipqcService.createIpqc(reqVO));
        assertEquals(QC_IPQC_SOURCE_LINE_NOT_BELONG.getCode(), ex.getCode());
    }

    /**
     * sourceLineId 对应的产出行不是待检验状态 → 创建失败
     */
    @Test
    public void testCreateIpqc_sourceLineId_notPending() {
        // 准备参数
        Long workstationId = randomLongId();
        Long workOrderId = randomLongId();
        Long productId = randomLongId();
        Long feedbackId = randomLongId();
        Long sourceLineId = randomLongId();
        Long templateId = randomLongId();

        MesQcIpqcSaveReqVO reqVO = new MesQcIpqcSaveReqVO();
        reqVO.setCode("IPQC-SL-003");
        reqVO.setWorkstationId(workstationId);
        reqVO.setWorkOrderId(workOrderId);
        reqVO.setInspectorUserId(randomLongId());
        reqVO.setSourceDocType(MesBizTypeConstants.PRO_FEEDBACK);
        reqVO.setSourceDocId(feedbackId);
        reqVO.setSourceLineId(sourceLineId);

        // mock
        MesMdWorkstationDO workstation = new MesMdWorkstationDO();
        workstation.setId(workstationId);
        workstation.setProcessId(null);
        when(workstationService.validateWorkstationExists(workstationId)).thenReturn(workstation);
        MesProWorkOrderDO workOrder = new MesProWorkOrderDO();
        workOrder.setId(workOrderId);
        workOrder.setProductId(productId);
        when(workOrderService.validateWorkOrderExists(workOrderId)).thenReturn(workOrder);
        when(workOrderService.validateWorkOrderConfirmed(workOrderId)).thenReturn(workOrder);
        MesQcTemplateItemDO templateItem = new MesQcTemplateItemDO();
        templateItem.setTemplateId(templateId);
        when(templateItemService.getRequiredTemplateByItemIdAndType(eq(productId), anyInt())).thenReturn(templateItem);
        when(feedbackService.validateFeedbackExists(feedbackId)).thenReturn(new MesProFeedbackDO());
        // mock：产出行存在且归属正确，但不是 PENDING 状态
        MesWmProductProduceLineDO line = new MesWmProductProduceLineDO();
        line.setId(sourceLineId);
        line.setFeedbackId(feedbackId);
        line.setQualityStatus(MesWmQualityStatusEnum.PASS.getStatus()); // 已合格，非待检
        when(productProduceLineService.validateProductProduceLineExists(sourceLineId)).thenReturn(line);

        // 调用，断言异常
        ServiceException ex = assertThrows(ServiceException.class, () -> ipqcService.createIpqc(reqVO));
        assertEquals(QC_IPQC_SOURCE_LINE_NOT_PENDING.getCode(), ex.getCode());
    }

    /**
     * sourceLineId 校验全部通过 → 创建成功
     */
    @Test
    public void testCreateIpqc_sourceLineId_success() {
        // 准备参数
        Long workstationId = randomLongId();
        Long workOrderId = randomLongId();
        Long productId = randomLongId();
        Long feedbackId = randomLongId();
        Long sourceLineId = randomLongId();
        Long templateId = randomLongId();

        MesQcIpqcSaveReqVO reqVO = new MesQcIpqcSaveReqVO();
        reqVO.setCode("IPQC-SL-004");
        reqVO.setWorkstationId(workstationId);
        reqVO.setWorkOrderId(workOrderId);
        reqVO.setInspectorUserId(randomLongId());
        reqVO.setSourceDocType(MesBizTypeConstants.PRO_FEEDBACK);
        reqVO.setSourceDocId(feedbackId);
        reqVO.setSourceLineId(sourceLineId);

        // mock
        MesMdWorkstationDO workstation = new MesMdWorkstationDO();
        workstation.setId(workstationId);
        workstation.setProcessId(null);
        when(workstationService.validateWorkstationExists(workstationId)).thenReturn(workstation);
        MesProWorkOrderDO workOrder = new MesProWorkOrderDO();
        workOrder.setId(workOrderId);
        workOrder.setProductId(productId);
        when(workOrderService.validateWorkOrderExists(workOrderId)).thenReturn(workOrder);
        when(workOrderService.validateWorkOrderConfirmed(workOrderId)).thenReturn(workOrder);
        MesQcTemplateItemDO templateItem = new MesQcTemplateItemDO();
        templateItem.setTemplateId(templateId);
        when(templateItemService.getRequiredTemplateByItemIdAndType(eq(productId), anyInt())).thenReturn(templateItem);
        MesProFeedbackDO feedback = new MesProFeedbackDO();
        feedback.setId(feedbackId);
        feedback.setCode("FB-001");
        when(feedbackService.validateFeedbackExists(feedbackId)).thenReturn(feedback);
        // mock：产出行存在、归属正确、PENDING 状态
        MesWmProductProduceLineDO line = new MesWmProductProduceLineDO();
        line.setId(sourceLineId);
        line.setFeedbackId(feedbackId);
        line.setQualityStatus(MesWmQualityStatusEnum.PENDING.getStatus());
        when(productProduceLineService.validateProductProduceLineExists(sourceLineId)).thenReturn(line);

        // 调用
        Long ipqcId = ipqcService.createIpqc(reqVO);

        // 断言：创建成功，sourceDocCode 回写
        MesQcIpqcDO ipqc = ipqcMapper.selectById(ipqcId);
        assertNotNull(ipqc);
        assertEquals(feedbackId, ipqc.getSourceDocId());
        assertEquals(sourceLineId, ipqc.getSourceLineId());
        assertEquals("FB-001", ipqc.getSourceDocCode());
    }

}
