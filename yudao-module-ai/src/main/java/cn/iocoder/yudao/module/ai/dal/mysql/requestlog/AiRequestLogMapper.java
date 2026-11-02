package cn.iocoder.yudao.module.ai.dal.mysql.requestlog;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.ai.controller.admin.requestlog.vo.AiRequestLogPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.requestlog.vo.AiRequestLogStatisticsRespVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.requestlog.AiRequestLogDO;
import cn.iocoder.yudao.module.ai.enums.requestlog.AiRequestLogStatusEnum;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

/**
 * AI 请求日志 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface AiRequestLogMapper extends BaseMapperX<AiRequestLogDO> {

    String USAGE_KNOWN_CONDITION = "input_tokens IS NOT NULL AND output_tokens IS NOT NULL AND total_tokens IS NOT NULL";

    default PageResult<AiRequestLogDO> selectPage(AiRequestLogPageReqVO reqVO) {
        return selectPage(reqVO, buildQueryWrapper(reqVO).orderByDesc("id"));
    }

    default AiRequestLogStatisticsRespVO selectStatistics(AiRequestLogPageReqVO reqVO) {
        // SQL 聚合查询：与分页使用相同的筛选条件
        QueryWrapperX<AiRequestLogDO> queryWrapper = buildQueryWrapper(reqVO);
        queryWrapper.select("COUNT(*) AS request_count",
                "SUM(CASE WHEN status = " + AiRequestLogStatusEnum.SUCCESS.getStatus() + " THEN 1 ELSE 0 END) AS success_count",
                "SUM(CASE WHEN status = " + AiRequestLogStatusEnum.FAIL.getStatus() + " THEN 1 ELSE 0 END) AS fail_count",
                "SUM(CASE WHEN status = " + AiRequestLogStatusEnum.CANCEL.getStatus() + " THEN 1 ELSE 0 END) AS cancel_count",
                "SUM(input_tokens) AS input_tokens",
                "SUM(output_tokens) AS output_tokens",
                "SUM(CASE WHEN " + USAGE_KNOWN_CONDITION + " THEN total_tokens END) AS total_tokens",
                "SUM(CASE WHEN " + USAGE_KNOWN_CONDITION + " THEN 1 ELSE 0 END) AS known_usage_count");
        // 转换数据：计数没有记录时为 0；SUM 没有已知值时为 null
        Map<String, Object> result = CollUtil.getFirst(selectMaps(queryWrapper));
        Long requestCount = MapUtil.getLong(result, "request_count", 0L);
        Long knownUsageCount = MapUtil.getLong(result, "known_usage_count", 0L);
        return new AiRequestLogStatisticsRespVO()
                .setRequestCount(requestCount)
                .setSuccessCount(MapUtil.getLong(result, "success_count", 0L))
                .setFailCount(MapUtil.getLong(result, "fail_count", 0L))
                .setCancelCount(MapUtil.getLong(result, "cancel_count", 0L))
                .setInputTokens(MapUtil.getLong(result, "input_tokens"))
                .setOutputTokens(MapUtil.getLong(result, "output_tokens"))
                .setTotalTokens(MapUtil.getLong(result, "total_tokens"))
                .setKnownUsageCount(knownUsageCount)
                .setUnknownUsageCount(requestCount - knownUsageCount);
    }

    default QueryWrapperX<AiRequestLogDO> buildQueryWrapper(AiRequestLogPageReqVO reqVO) {
        return new QueryWrapperX<AiRequestLogDO>()
                .eqIfPresent("request_id", reqVO.getRequestId())
                .eqIfPresent("user_id", reqVO.getUserId())
                .eqIfPresent("biz_type", reqVO.getBizType())
                .eqIfPresent("platform", reqVO.getPlatform())
                .eqIfPresent("model_id", reqVO.getModelId())
                .likeIfPresent("model", reqVO.getModel())
                .eqIfPresent("status", reqVO.getStatus())
                .betweenIfPresent("begin_time", reqVO.getBeginTime());
    }

}
