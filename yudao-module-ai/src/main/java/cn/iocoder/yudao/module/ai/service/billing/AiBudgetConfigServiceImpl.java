package cn.iocoder.yudao.module.ai.service.billing;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.budget.AiBudgetConfigPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.budget.AiBudgetConfigSaveReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.billing.AiBudgetConfigDO;
import cn.iocoder.yudao.module.ai.dal.mysql.billing.AiBudgetConfigMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.BUDGET_CONFIG_DUPLICATE;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.BUDGET_CONFIG_NOT_EXISTS;

/**
 * AI 预算配置 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class AiBudgetConfigServiceImpl implements AiBudgetConfigService {

    @Resource
    private AiBudgetConfigMapper budgetConfigMapper;

    @Override
    public Long createBudgetConfig(AiBudgetConfigSaveReqVO createReqVO) {
        // 校验唯一性：同租户同用户同周期类型只能有一条配置
        validateBudgetConfigUnique(null, createReqVO.getUserId(), createReqVO.getPeriodType());
        AiBudgetConfigDO config = new AiBudgetConfigDO();
        config.setUserId(createReqVO.getUserId());
        config.setPeriodType(createReqVO.getPeriodType());
        config.setCurrency("CNY");
        config.setBudgetAmount(yuanToMicro(createReqVO.getBudgetAmountYuan()));
        config.setAlertThresholds(createReqVO.getAlertThresholds());
        config.setStatus(createReqVO.getStatus());
        budgetConfigMapper.insert(config);
        return config.getId();
    }

    @Override
    public void updateBudgetConfig(AiBudgetConfigSaveReqVO updateReqVO) {
        validateBudgetConfigExists(updateReqVO.getId());
        // 校验唯一性：排除自身
        validateBudgetConfigUnique(updateReqVO.getId(), updateReqVO.getUserId(), updateReqVO.getPeriodType());
        AiBudgetConfigDO updateObj = new AiBudgetConfigDO();
        updateObj.setId(updateReqVO.getId());
        updateObj.setUserId(updateReqVO.getUserId());
        updateObj.setPeriodType(updateReqVO.getPeriodType());
        updateObj.setBudgetAmount(yuanToMicro(updateReqVO.getBudgetAmountYuan()));
        updateObj.setAlertThresholds(updateReqVO.getAlertThresholds());
        updateObj.setStatus(updateReqVO.getStatus());
        budgetConfigMapper.updateById(updateObj);
    }

    @Override
    public void deleteBudgetConfig(Long id) {
        validateBudgetConfigExists(id);
        budgetConfigMapper.deleteById(id);
    }

    private void validateBudgetConfigExists(Long id) {
        if (budgetConfigMapper.selectById(id) == null) {
            throw exception(BUDGET_CONFIG_NOT_EXISTS);
        }
    }

    private void validateBudgetConfigUnique(Long excludeId, Long userId, String periodType) {
        AiBudgetConfigDO existing = budgetConfigMapper.selectByUserAndPeriod(userId, periodType);
        if (existing != null && !existing.getId().equals(excludeId)) {
            throw exception(BUDGET_CONFIG_DUPLICATE);
        }
    }

    @Override
    public AiBudgetConfigDO getBudgetConfig(Long id) {
        return budgetConfigMapper.selectById(id);
    }

    @Override
    public PageResult<AiBudgetConfigDO> getBudgetConfigPage(AiBudgetConfigPageReqVO pageReqVO) {
        return budgetConfigMapper.selectPage(pageReqVO);
    }

    @Override
    public AiBudgetConfigDO getBudgetConfig(Long userId, String periodType) {
        return budgetConfigMapper.selectByUserAndPeriod(userId, periodType);
    }

    private Long yuanToMicro(Double yuan) {
        if (yuan == null) {
            return 0L;
        }
        return Math.round(yuan * 1_000_000);
    }

}
