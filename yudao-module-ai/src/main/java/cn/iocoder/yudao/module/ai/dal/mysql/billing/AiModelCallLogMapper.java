package cn.iocoder.yudao.module.ai.dal.mysql.billing;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.ai.dal.dataobject.billing.AiModelCallLogDO;
import cn.iocoder.yudao.module.ai.enums.billing.AiCallStatusEnum;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AI 模型调用日志 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface AiModelCallLogMapper extends BaseMapperX<AiModelCallLogDO> {

    default PageResult<AiModelCallLogDO> selectPage(PageParam pageParam,
                                                    Long userId, String bizType,
                                                    String platform, Long modelId,
                                                    String status, Boolean blocked,
                                                    LocalDateTime[] requestTime) {
        return selectPage(pageParam, new LambdaQueryWrapperX<AiModelCallLogDO>()
                .eqIfPresent(AiModelCallLogDO::getUserId, userId)
                .eqIfPresent(AiModelCallLogDO::getBizType, bizType)
                .eqIfPresent(AiModelCallLogDO::getPlatform, platform)
                .eqIfPresent(AiModelCallLogDO::getModelId, modelId)
                .eqIfPresent(AiModelCallLogDO::getStatus, status)
                .eqIfPresent(AiModelCallLogDO::getBlocked, blocked)
                .betweenIfPresent(AiModelCallLogDO::getRequestTime, requestTime)
                .orderByDesc(AiModelCallLogDO::getId));
    }

    /**
     * 聚合统计调用日志
     */
    default Map<String, Object> selectStat(Long userId, String platform, Long modelId,
                                           String bizType, LocalDateTime[] requestTime) {
        QueryWrapperX<AiModelCallLogDO> wrapper = new QueryWrapperX<>();
        wrapper.select(
                "COUNT(*) AS totalCount",
                "SUM(CASE WHEN status = '" + AiCallStatusEnum.SUCCESS.getStatus() + "' THEN 1 ELSE 0 END) AS successCount",
                "SUM(CASE WHEN status = '" + AiCallStatusEnum.FAIL.getStatus() + "' THEN 1 ELSE 0 END) AS failCount",
                "IFNULL(SUM(prompt_tokens), 0) AS totalPromptTokens",
                "IFNULL(SUM(completion_tokens), 0) AS totalCompletionTokens",
                "IFNULL(SUM(total_tokens), 0) AS totalTokens",
                "IFNULL(SUM(cost_amount), 0) AS totalCostAmount",
                "IFNULL(AVG(duration_ms), 0) AS avgDurationMs"
        );
        if (userId != null) {
            wrapper.eq("user_id", userId);
        }
        if (platform != null) {
            wrapper.eq("platform", platform);
        }
        if (modelId != null) {
            wrapper.eq("model_id", modelId);
        }
        if (bizType != null) {
            wrapper.eq("biz_type", bizType);
        }
        if (requestTime != null) {
            if (requestTime.length >= 1 && requestTime[0] != null) {
                wrapper.ge("request_time", requestTime[0]);
            }
            if (requestTime.length >= 2 && requestTime[1] != null) {
                wrapper.le("request_time", requestTime[1]);
            }
        }
        wrapper.eq("deleted", false);
        List<Map<String, Object>> result = selectMaps(wrapper);
        return result.isEmpty() ? Map.of() : result.get(0);
    }

    /**
     * 查询全部（用于导出），复用分页查询的条件
     */
    default List<AiModelCallLogDO> selectList(Long userId, String bizType,
                                              String platform, Long modelId,
                                              String status, Boolean blocked,
                                              LocalDateTime[] requestTime) {
        return selectList(new LambdaQueryWrapperX<AiModelCallLogDO>()
                .eqIfPresent(AiModelCallLogDO::getUserId, userId)
                .eqIfPresent(AiModelCallLogDO::getBizType, bizType)
                .eqIfPresent(AiModelCallLogDO::getPlatform, platform)
                .eqIfPresent(AiModelCallLogDO::getModelId, modelId)
                .eqIfPresent(AiModelCallLogDO::getStatus, status)
                .eqIfPresent(AiModelCallLogDO::getBlocked, blocked)
                .betweenIfPresent(AiModelCallLogDO::getRequestTime, requestTime)
                .orderByDesc(AiModelCallLogDO::getId));
    }

}
