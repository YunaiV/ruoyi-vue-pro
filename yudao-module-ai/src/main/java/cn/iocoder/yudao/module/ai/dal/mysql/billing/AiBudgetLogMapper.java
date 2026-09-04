package cn.iocoder.yudao.module.ai.dal.mysql.billing;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.dal.dataobject.billing.AiBudgetLogDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;

/**
 * AI 预算事件日志 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface AiBudgetLogMapper extends BaseMapperX<AiBudgetLogDO> {

    default PageResult<AiBudgetLogDO> selectPage(PageParam pageParam,
                                                 Long userId, String eventType,
                                                 LocalDateTime[] periodStartTime) {
        return selectPage(pageParam, new LambdaQueryWrapperX<AiBudgetLogDO>()
                .eqIfPresent(AiBudgetLogDO::getUserId, userId)
                .eqIfPresent(AiBudgetLogDO::getEventType, eventType)
                .betweenIfPresent(AiBudgetLogDO::getPeriodStartTime, periodStartTime)
                .orderByDesc(AiBudgetLogDO::getId));
    }

}
