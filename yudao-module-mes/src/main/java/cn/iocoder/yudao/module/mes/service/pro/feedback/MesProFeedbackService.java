package cn.iocoder.yudao.module.mes.service.pro.feedback;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.pro.feedback.vo.MesProFeedbackPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.pro.feedback.vo.MesProFeedbackSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.feedback.MesProFeedbackDO;
import jakarta.validation.Valid;

import java.math.BigDecimal;

/**
 * MES 生产报工 Service 接口
 *
 * @author 芋道源码
 */
public interface MesProFeedbackService {

    /**
     * 创建生产报工（草稿）
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createFeedback(@Valid MesProFeedbackSaveReqVO createReqVO);

    /**
     * 修改生产报工
     *
     * @param updateReqVO 更新信息
     */
    void updateFeedback(@Valid MesProFeedbackSaveReqVO updateReqVO);

    /**
     * 删除生产报工（仅草稿）
     *
     * @param id 编号
     */
    void deleteFeedback(Long id);

    /**
     * 获得生产报工
     *
     * @param id 编号
     * @return 生产报工
     */
    MesProFeedbackDO getFeedback(Long id);

    /**
     * 校验生产报工存在
     *
     * @param id 编号
     * @return 生产报工
     */
    MesProFeedbackDO validateFeedbackExists(Long id);

    /**
     * 获得生产报工分页
     *
     * @param pageReqVO 分页查询
     * @return 生产报工分页
     */
    PageResult<MesProFeedbackDO> getFeedbackPage(MesProFeedbackPageReqVO pageReqVO);

    /**
     * 提交报工（草稿 → 审批中）
     *
     * @param id 编号
     */
    void submitFeedback(Long id);

    /**
     * 驳回报工（审批中 → 草稿）
     *
     * @param id 编号
     */
    void rejectFeedback(Long id);

    /**
     * 审批通过报工单，执行生产入库流程
     *
     * <p>整体流程如下：
     * <ol>
     *   <li>校验报工单状态和数量</li>
     *   <li>生成物料消耗单，扣减线边库物料</li>
     *   <li>根据工序配置（关键工序 & 是否需要检验）分情况处理：
     *     <ul>
     *       <li><b>关键工序 + 需要检验</b>：生成产出单（质量状态=待检验），报工单状态 → 待检验，等待质检完成回调</li>
     *       <li><b>关键工序 + 无需检验</b>：生成产出单（按合格/不合格拆行），执行产品入库，更新任务/工单已生产数量，报工单状态 → 已完成</li>
     *       <li><b>非关键工序</b>：不生成产出单，不更新任务/工单数量，报工单状态 → 已完成</li>
     *     </ul>
     *   </li>
     * </ol>
     *
     * @param id 报工单编号
     * @return true=已完成, false=待检验（需等质检回调）
     */
    boolean approveFeedback(Long id);

    /**
     * IPQC 完成后回调：完成报工单并更新任务/工单进度
     *
     * <p>由 IPQC 检验完成时调用，将报工单状态从待检验改为已完成，
     * 并根据检验结果回写合格/不合格/废品数量，同时更新任务/工单的已生产数量。
     *
     * @param feedbackId          报工记录 ID
     * @param sourceLineId        来源产出行 ID（用于直接定位待检行）
     * @param qualifiedQty        合格品数量
     * @param unqualifiedQty      不合格品数量
     * @param laborScrapQty       工废数量
     * @param materialScrapQty    料废数量
     * @param otherScrapQty       其他废品数量
     */
    void updateProFeedbackWhenIpqcFinish(Long feedbackId, Long sourceLineId,
                                         BigDecimal qualifiedQty, BigDecimal unqualifiedQty,
                                         BigDecimal laborScrapQty, BigDecimal materialScrapQty, BigDecimal otherScrapQty);

}

