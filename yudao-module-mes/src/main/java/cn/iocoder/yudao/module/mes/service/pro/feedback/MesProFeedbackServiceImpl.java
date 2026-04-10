package cn.iocoder.yudao.module.mes.service.pro.feedback;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.pro.feedback.vo.MesProFeedbackPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.pro.feedback.vo.MesProFeedbackSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.feedback.MesProFeedbackDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.route.MesProRouteProcessDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.task.MesProTaskDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.itemconsume.MesWmItemConsumeDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.productproduce.MesWmProductProduceDO;
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
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 生产报工 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesProFeedbackServiceImpl implements MesProFeedbackService {

    @Resource
    private MesProFeedbackMapper feedbackMapper;

    @Resource
    private MesProWorkOrderService workOrderService;
    @Resource
    private MesProRouteProcessService routeProcessService;
    @Resource
    private MesMdWorkstationService workstationService;
    @Resource
    @Lazy // 避免循环依赖
    private MesProTaskService taskService;
    @Resource
    private MesWmItemConsumeService itemConsumeService;
    @Resource
    private MesWmProductProduceService productProduceService;
    @Resource
    private MesWmProductProduceLineService produceLineService;

    @Override
    public Long createFeedback(MesProFeedbackSaveReqVO createReqVO) {
        // 1. 校验
        MesProTaskDO task = validateFeedbackData(createReqVO);

        // 2. 插入（自动填充 itemId）
        MesProFeedbackDO feedback = BeanUtils.toBean(createReqVO, MesProFeedbackDO.class)
                .setStatus(MesProFeedbackStatusEnum.PREPARE.getStatus())
                .setItemId(task.getItemId());
        feedbackMapper.insert(feedback);
        return feedback.getId();
    }

    @Override
    public void updateFeedback(MesProFeedbackSaveReqVO updateReqVO) {
        // 1.1 校验存在 + 草稿状态
        validateFeedbackStatusPrepare(updateReqVO.getId());
        // 1.2 校验业务数据
        MesProTaskDO task = validateFeedbackData(updateReqVO);

        // 2. 更新（自动填充 itemId）
        MesProFeedbackDO updateObj = BeanUtils.toBean(updateReqVO, MesProFeedbackDO.class)
                .setItemId(task.getItemId());
        feedbackMapper.updateById(updateObj);
    }

    @Override
    public void deleteFeedback(Long id) {
        // 1. 校验存在 + 草稿状态
        validateFeedbackStatusPrepare(id);

        // 2. 删除
        feedbackMapper.deleteById(id);
    }

    @Override
    public MesProFeedbackDO getFeedback(Long id) {
        return feedbackMapper.selectById(id);
    }

    @Override
    public PageResult<MesProFeedbackDO> getFeedbackPage(MesProFeedbackPageReqVO pageReqVO) {
        return feedbackMapper.selectPage(pageReqVO);
    }

    @Override
    public void submitFeedback(Long id) {
        // 1. 校验存在 + 草稿状态
        validateFeedbackStatusPrepare(id);

        // 2. 更新状态为审批中，记录报工人和报工时间
        feedbackMapper.updateById(new MesProFeedbackDO().setId(id)
                .setStatus(MesProFeedbackStatusEnum.APPROVING.getStatus())
                .setFeedbackUserId(getLoginUserId())
                .setFeedbackTime(LocalDateTime.now()));
    }

    @Override
    public void rejectFeedback(Long id) {
        // 1. 校验存在 + 审批中状态
        validateFeedbackStatusApproving(id);

        // 2. 更新状态为草稿
        feedbackMapper.updateById(new MesProFeedbackDO().setId(id)
                .setStatus(MesProFeedbackStatusEnum.PREPARE.getStatus()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approveFeedback(Long id, Long userId) {
        // 1.1.a 校验存在 + 审批中状态
        MesProFeedbackDO feedback = validateFeedbackStatusApproving(id);
        // 1.1.b 校验报工数量 > 0
        if (feedback.getFeedbackQuantity() == null
                || feedback.getFeedbackQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw exception(PRO_FEEDBACK_QUANTITY_MUST_POSITIVE);
        }
        // 1.2.a 校验任务未完成
        taskService.validateTaskNotFinished(feedback.getTaskId());
        // 1.2.b 仍有待检数量时不能执行
        if (feedback.getUncheckQuantity() != null
                && feedback.getUncheckQuantity().compareTo(BigDecimal.ZERO) > 0) {
            throw exception(PRO_FEEDBACK_UNCHECK_QUANTITY_EXISTS, feedback.getUncheckQuantity());
        }

        // 2. 物料消耗：根据工序 BOM 生成消耗记录并执行扣减
        MesWmItemConsumeDO itemConsume = itemConsumeService.generateItemConsume(feedback);
        if (itemConsume != null) {
            itemConsumeService.finishItemConsume(itemConsume.getId());
        }

        // 3. 查询工序的关键工序标识 + 检验标识
        MesProRouteProcessDO routeProcess = routeProcessService.getRouteProcessByRouteIdAndProcessId(
                feedback.getRouteId(), feedback.getProcessId());
        boolean keyFlag = routeProcess != null && Boolean.TRUE.equals(routeProcess.getKeyFlag());
        boolean checkFlag = routeProcess != null && Boolean.TRUE.equals(routeProcess.getCheckFlag());

        // 4. 关键工序：生成产出单，并根据是否需要检验决定入库方式
        if (keyFlag) {
            // 4.1 需要检验：生成产出单（质量状态=待检验），更新报工状态为待检验，等质检完成回调后再入库
            if (checkFlag) {
                // 完成时回调见：MesQcIpqcServiceImpl#finishIpqc → splitPendingAndFinishProduce + completeFeedbackFromIpqc
                productProduceService.generateProductProduce(feedback, true);
                feedbackMapper.updateById(new MesProFeedbackDO().setId(id)
                        .setStatus(MesProFeedbackStatusEnum.UNCHECK.getStatus()));
                return false;
            }
            // 4.2 无需检验：生成产出单（按合格/不合格拆行），直接完成入库，并更新任务/工单的已生产数量
            MesWmProductProduceDO produce = productProduceService.generateProductProduce(feedback, false);
            productProduceService.finishProductProduce(produce.getId());
            updateTaskAndWorkOrderByFeedback(feedback);
        }

        // 5. 非关键工序：不生成产出单，不更新任务/工单数量，直接完成
        feedbackMapper.updateById(new MesProFeedbackDO().setId(id)
                .setStatus(MesProFeedbackStatusEnum.FINISHED.getStatus())
                .setApproveUserId(userId));
        return true; // 已完成
    }

    /**
     * 根据当前报工单的最终结果，更新生产任务和生产工单的进度
     *
     * <ul>
     *   <li>使用报工数量（feedbackQuantity），累加任务和工单的已生产数量</li>
     *   <li>使用产出单行按 qualityStatus 聚合的合格品/不合格品数量，累加任务的合格品/不合格品数量
     *       （不直接用 feedback 上的数量，确保质检回调场景下数量来源正确）</li>
     * </ul>
     *
     * @param feedback 报工单
     */
    private void updateTaskAndWorkOrderByFeedback(MesProFeedbackDO feedback) {
        // 1. 查询该报工单关联的所有产出单行，按质量状态聚合数量
        BigDecimal qualifiedQty = BigDecimal.ZERO;
        BigDecimal unqualifiedQty = BigDecimal.ZERO;
        List<MesWmProductProduceLineDO> lines = produceLineService.getProductProduceLineListByFeedbackId(feedback.getId());
        for (MesWmProductProduceLineDO line : lines) {
            if (ObjUtil.equal(line.getQualityStatus(), MesWmQualityStatusEnum.FAIL.getStatus())) {
                unqualifiedQty = unqualifiedQty.add(line.getQuantity());
            }
            if (ObjUtil.equal(line.getQualityStatus(), MesWmQualityStatusEnum.PASS.getStatus())) {
                qualifiedQty = qualifiedQty.add(line.getQuantity());
            }
        }

        // 2. 更新任务的已生产/合格/不合格数量
        taskService.updateProducedQuantity(feedback.getTaskId(),
                feedback.getFeedbackQuantity(), qualifiedQty, unqualifiedQty);
        // 3. 更新工单的已生产数量
        workOrderService.updateProducedQuantity(feedback.getWorkOrderId(),
                feedback.getFeedbackQuantity());
    }

    // ==================== 校验方法 ====================

    private MesProFeedbackDO validateFeedbackExists(Long id) {
        MesProFeedbackDO feedback = feedbackMapper.selectById(id);
        if (feedback == null) {
            throw exception(PRO_FEEDBACK_NOT_EXISTS);
        }
        return feedback;
    }

    private MesProFeedbackDO validateFeedbackStatusPrepare(Long id) {
        MesProFeedbackDO feedback = validateFeedbackExists(id);
        if (ObjUtil.notEqual(feedback.getStatus(), MesProFeedbackStatusEnum.PREPARE.getStatus())) {
            throw exception(PRO_FEEDBACK_NOT_PREPARE);
        }
        return feedback;
    }

    private MesProFeedbackDO validateFeedbackStatusApproving(Long id) {
        MesProFeedbackDO feedback = validateFeedbackExists(id);
        if (ObjUtil.notEqual(feedback.getStatus(), MesProFeedbackStatusEnum.APPROVING.getStatus())) {
            throw exception(PRO_FEEDBACK_NOT_APPROVING);
        }
        return feedback;
    }

    /**
     * 校验报工单的业务数据（创建 & 修改共用）
     *
     * @param reqVO 报工请求
     * @return 关联的生产任务
     */
    private MesProTaskDO validateFeedbackData(MesProFeedbackSaveReqVO reqVO) {
        // 1. 校验工作站存在
        workstationService.validateWorkstationExists(reqVO.getWorkstationId());

        // 2.1 校验工艺路线 + 工序配置有效
        MesProRouteProcessDO routeProcess = routeProcessService.getRouteProcessByRouteIdAndProcessId(
                reqVO.getRouteId(), reqVO.getProcessId());
        if (routeProcess == null) {
            throw exception(PRO_FEEDBACK_ROUTE_PROCESS_INVALID);
        }
        // 2.2 校验数量
        boolean checkFlag = Boolean.TRUE.equals(routeProcess.getCheckFlag());
        if (checkFlag) {
            // 需要检验：只需填报工数量，且必须 > 0
            if (reqVO.getFeedbackQuantity() == null
                    || reqVO.getFeedbackQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw exception(PRO_FEEDBACK_QUANTITY_MUST_POSITIVE);
            }
        } else {
            // 不需检验：需填合格品 + 不良品数量，合计 > 0
            BigDecimal qualified = ObjectUtil.defaultIfNull(reqVO.getQualifiedQuantity(), BigDecimal.ZERO);
            BigDecimal unqualified = ObjectUtil.defaultIfNull(reqVO.getUnqualifiedQuantity(), BigDecimal.ZERO);
            if (qualified.add(unqualified).compareTo(BigDecimal.ZERO) <= 0) {
                throw exception(PRO_FEEDBACK_QUALIFIED_UNQUALIFIED_REQUIRED);
            }
        }

        // 3. 校验工单已确认
        workOrderService.validateWorkOrderConfirmed(reqVO.getWorkOrderId());

        // 4. 校验任务存在且未终态（已完成/已取消），并返回任务用于冗余 itemId
        return taskService.validateTaskNotFinished(reqVO.getTaskId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProFeedbackWhenIpqcFinish(Long feedbackId, BigDecimal qualifiedQty, BigDecimal unqualifiedQty,
                                                BigDecimal laborScrapQty, BigDecimal materialScrapQty, BigDecimal otherScrapQty) {
        // 1. 校验报工单存在且为待检验状态
        MesProFeedbackDO feedback = validateFeedbackExists(feedbackId);
        if (ObjUtil.notEqual(feedback.getStatus(), MesProFeedbackStatusEnum.UNCHECK.getStatus())) {
            throw exception(PRO_FEEDBACK_NOT_UNCHECK);
        }

        // 2. 拆分待检产出行（合格/不合格），生成明细，完成产出入库
        productProduceService.splitPendingAndFinishProduce(feedbackId, qualifiedQty, unqualifiedQty);

        // 3. 回写合格/不合格/废品数量，更新状态为已完成
        feedbackMapper.updateById(new MesProFeedbackDO().setId(feedbackId)
                .setQualifiedQuantity(qualifiedQty)
                .setUnqualifiedQuantity(unqualifiedQty)
                .setUncheckQuantity(BigDecimal.ZERO)
                .setLaborScrapQuantity(laborScrapQty)
                .setMaterialScrapQuantity(materialScrapQty)
                .setOtherScrapQuantity(otherScrapQty)
                .setStatus(MesProFeedbackStatusEnum.FINISHED.getStatus()));

        // 4. 更新任务/工单的已生产数量
        feedback.setQualifiedQuantity(qualifiedQty).setUnqualifiedQuantity(unqualifiedQty);
        updateTaskAndWorkOrderByFeedback(feedback);
    }

}
