package cn.iocoder.yudao.module.ai.service.billing;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.budget.AiBudgetLogPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.billing.AiBudgetLogDO;
import cn.iocoder.yudao.module.ai.dal.mysql.billing.AiBudgetLogMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * AI 预算事件日志 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class AiBudgetLogServiceImpl implements AiBudgetLogService {

    @Resource
    private AiBudgetLogMapper budgetLogMapper;

    @Override
    public void createBudgetLog(AiBudgetLogDO budgetLog) {
        budgetLogMapper.insert(budgetLog);
    }

    @Override
    public PageResult<AiBudgetLogDO> getBudgetLogPage(AiBudgetLogPageReqVO pageReqVO) {
        return budgetLogMapper.selectPage(pageReqVO,
                pageReqVO.getUserId(), pageReqVO.getEventType(),
                pageReqVO.getPeriodStartTime());
    }

}
