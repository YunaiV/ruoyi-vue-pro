package cn.iocoder.yudao.module.wms.service.approval.history;

import cn.iocoder.yudao.framework.cola.statemachine.TransitionContext;
import cn.iocoder.yudao.framework.cola.statemachine.TransitionHandler;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.controller.admin.approval.history.vo.WmsApprovalHistorySaveReqVO;
import cn.iocoder.yudao.module.wms.enums.WmsConstants;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;

/**
 * @author: LeeFJ
 * @date: 2025/3/19 10:10
 * @description:
 */
public abstract class ApprovalHistoryTransitionHandler<E,D> extends TransitionHandler<Integer, E,D> {

    @Resource
    @Lazy
    protected WmsApprovalHistoryService approvalHistoryService;

    /**
     * 变更状态
     **/
    @Override
    public void perform(Integer from, Integer to, E event, TransitionContext<D> context) {
        // 保存审批历史
        WmsApprovalHistorySaveReqVO historySaveReqVO = BeanUtils.toBean(context.getExtra(WmsConstants.APPROVAL_REQ_VO_KEY), WmsApprovalHistorySaveReqVO.class);
        historySaveReqVO.setStatusBefore(from).setStatusAfter(to);
        approvalHistoryService.createApprovalHistory(historySaveReqVO);
    }
}
