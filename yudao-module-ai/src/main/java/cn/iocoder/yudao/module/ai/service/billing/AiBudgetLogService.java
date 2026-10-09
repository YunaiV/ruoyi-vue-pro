package cn.iocoder.yudao.module.ai.service.billing;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.budget.AiBudgetLogPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.billing.AiBudgetLogDO;

/**
 * AI 预算事件日志 Service 接口
 *
 * @author 芋道源码
 */
public interface AiBudgetLogService {

    /**
     * 创建预算事件日志
     *
     * @param budgetLog 事件日志
     */
    void createBudgetLog(AiBudgetLogDO budgetLog);

    /**
     * 获得预算事件日志分页
     *
     * @param pageReqVO 分页查询
     * @return 事件日志分页
     */
    PageResult<AiBudgetLogDO> getBudgetLogPage(AiBudgetLogPageReqVO pageReqVO);

}
