package cn.iocoder.yudao.module.ai.service.billing;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.calllog.AiModelCallLogPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.calllog.AiModelCallLogStatReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.calllog.AiModelCallLogStatRespVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.billing.AiModelCallLogDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.billing.AiModelPricingDO;
import cn.iocoder.yudao.module.ai.dal.mysql.billing.AiModelCallLogMapper;
import cn.iocoder.yudao.module.ai.enums.billing.AiCallStatusEnum;
import cn.iocoder.yudao.module.ai.enums.billing.AiTokenSourceEnum;
import cn.iocoder.yudao.module.ai.service.billing.pricing.AiPricingContext;
import cn.iocoder.yudao.module.ai.service.billing.pricing.AiPricingStrategyManager;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

/**
 * AI 模型调用日志 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class AiModelCallLogServiceImpl implements AiModelCallLogService {

    @Resource
    private AiModelCallLogMapper callLogMapper;

    @Resource
    private AiModelPricingService modelPricingService;

    @Resource
    private AiPricingStrategyManager pricingStrategyManager;

    @Override
    public Long createCallLog(AiModelCallLogDO callLog) {
        // 1. 查询计费配置，填充价格快照 + 计算费用
        fillPricingAndCost(callLog);

        // 2. 插入
        callLogMapper.insert(callLog);
        return callLog.getId();
    }

    @Override
    public AiModelCallLogDO getCallLog(Long id) {
        return callLogMapper.selectById(id);
    }

    @Override
    public PageResult<AiModelCallLogDO> getCallLogPage(AiModelCallLogPageReqVO pageReqVO) {
        return callLogMapper.selectPage(pageReqVO,
                pageReqVO.getUserId(), pageReqVO.getBizType(),
                pageReqVO.getPlatform(), pageReqVO.getModelId(),
                pageReqVO.getStatus(), pageReqVO.getBlocked(),
                pageReqVO.getRequestTime());
    }

    @Override
    public List<AiModelCallLogDO> getCallLogList(AiModelCallLogPageReqVO pageReqVO) {
        return callLogMapper.selectList(
                pageReqVO.getUserId(), pageReqVO.getBizType(),
                pageReqVO.getPlatform(), pageReqVO.getModelId(),
                pageReqVO.getStatus(), pageReqVO.getBlocked(),
                pageReqVO.getRequestTime());
    }

    @Override
    public AiModelCallLogStatRespVO getCallLogStat(AiModelCallLogStatReqVO statReqVO) {
        Map<String, Object> statMap = callLogMapper.selectStat(
                statReqVO.getUserId(), statReqVO.getPlatform(),
                statReqVO.getModelId(), statReqVO.getBizType(),
                statReqVO.getRequestTime());
        if (statMap == null || statMap.isEmpty()) {
            return AiModelCallLogStatRespVO.builder()
                    .totalCount(0L).successCount(0L).failCount(0L)
                    .totalPromptTokens(0L).totalCompletionTokens(0L).totalTokens(0L)
                    .totalCostAmount(0L).totalCostAmountYuan(0.0).avgDurationMs(0L)
                    .build();
        }
        long totalCostAmount = toLong(statMap.get("totalCostAmount"));
        return AiModelCallLogStatRespVO.builder()
                .totalCount(toLong(statMap.get("totalCount")))
                .successCount(toLong(statMap.get("successCount")))
                .failCount(toLong(statMap.get("failCount")))
                .totalPromptTokens(toLong(statMap.get("totalPromptTokens")))
                .totalCompletionTokens(toLong(statMap.get("totalCompletionTokens")))
                .totalTokens(toLong(statMap.get("totalTokens")))
                .totalCostAmount(totalCostAmount)
                .totalCostAmountYuan(totalCostAmount / 1_000_000.0)
                .avgDurationMs(toLong(statMap.get("avgDurationMs")))
                .build();
    }

    private static long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0L;
    }

    /**
     * 填充价格快照并计算费用
     *
     * 规则：
     * - 查询模型最新启用的计费配置，快照四档单价到日志
     * - 失败且无 token 时，费用为 0
     * - 无计费配置时，单价和费用均为 0
     */
    private void fillPricingAndCost(AiModelCallLogDO callLog) {
        // 默认值
        if (callLog.getCurrency() == null) {
            callLog.setCurrency("CNY");
        }
        if (callLog.getBlocked() == null) {
            callLog.setBlocked(false);
        }

        // 查询计费配置
        AiModelPricingDO pricing = null;
        if (callLog.getModelId() != null) {
            pricing = modelPricingService.getLatestModelPricing(callLog.getModelId());
        }

        // 快照单价
        if (pricing != null) {
            callLog.setPriceInPer1m(pricing.getPriceInPer1m());
            callLog.setPriceCachedPer1m(pricing.getPriceCachedPer1m());
            callLog.setPriceOutPer1m(pricing.getPriceOutPer1m());
            callLog.setPriceReasoningPer1m(pricing.getPriceReasoningPer1m());
        } else {
            callLog.setPriceInPer1m(0L);
            callLog.setPriceCachedPer1m(0L);
            callLog.setPriceOutPer1m(0L);
            callLog.setPriceReasoningPer1m(0L);
        }

        // 预估费用（缺少厂商 usage）沿用调用方传入的金额，不再重算覆盖
        if (AiTokenSourceEnum.ESTIMATED.getSource().equals(callLog.getTokenSource())
                && callLog.getCostAmount() != null) {
            return;
        }

        // 失败且无 token 时不计费
        if (AiCallStatusEnum.FAIL.getStatus().equals(callLog.getStatus())
                && isTokenEmpty(callLog)) {
            callLog.setCostAmount(0L);
            return;
        }

        // 计算费用
        AiPricingContext context = AiPricingContext.builder()
                .promptTokens(callLog.getPromptTokens())
                .completionTokens(callLog.getCompletionTokens())
                .cachedTokens(callLog.getCachedTokens())
                .reasoningTokens(callLog.getReasoningTokens())
                .pricing(pricing)
                .platform(callLog.getPlatform())
                .model(callLog.getModel())
                .build();
        String strategyType = pricing != null ? pricing.getStrategyType() : null;
        callLog.setCostAmount(pricingStrategyManager.getStrategy(strategyType).calculateCost(context));
    }

    private boolean isTokenEmpty(AiModelCallLogDO callLog) {
        return (callLog.getPromptTokens() == null || callLog.getPromptTokens() == 0)
                && (callLog.getCompletionTokens() == null || callLog.getCompletionTokens() == 0);
    }

}
