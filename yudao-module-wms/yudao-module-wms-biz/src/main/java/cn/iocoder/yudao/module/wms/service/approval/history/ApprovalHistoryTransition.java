package cn.iocoder.yudao.module.wms.service.approval.history;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.statemachine.ColaTransition;
import cn.iocoder.yudao.module.wms.statemachine.ColaContext;
import cn.iocoder.yudao.module.wms.controller.admin.approval.history.vo.WmsApprovalHistorySaveReqVO;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;

import java.util.function.Function;

/**
 * @author: LeeFJ
 * @date: 2025/3/19 10:10
 * @description:
 */
public abstract class ApprovalHistoryTransition<E,D> extends ColaTransition<Integer, E,D> {

    @Resource
    @Lazy
    protected WmsApprovalHistoryService approvalHistoryService;

    public ApprovalHistoryTransition(Integer[] from, Integer to, Function<D, Integer> getter, E event) {
        super(from, to, getter, event);
    }
    /**
     * 变更状态
     **/
    @Override
    public void perform(Integer from, Integer to, E event, ColaContext<D> context) {
        // 保存审批历史
        WmsApprovalHistorySaveReqVO historySaveReqVO = BeanUtils.toBean(context.approvalReqVO(), WmsApprovalHistorySaveReqVO.class);
        historySaveReqVO.setStatusBefore(from).setStatusAfter(to);
        approvalHistoryService.createApprovalHistory(historySaveReqVO);
    }
}
