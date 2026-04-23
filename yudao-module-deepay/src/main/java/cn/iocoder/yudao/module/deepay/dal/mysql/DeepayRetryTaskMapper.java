package cn.iocoder.yudao.module.deepay.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayRetryTaskDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 失败重试任务 Mapper
 */
@Mapper
public interface DeepayRetryTaskMapper extends BaseMapperX<DeepayRetryTaskDO> {

    /** 查询所有待重试任务（PENDING 或 RETRYING 且已到重试时间，且未超过最大重试次数） */
    default List<DeepayRetryTaskDO> selectPendingTasks() {
        return selectList(new LambdaQueryWrapper<DeepayRetryTaskDO>()
                .in(DeepayRetryTaskDO::getStatus, "PENDING", "RETRYING")
                .le(DeepayRetryTaskDO::getNextRetryAt, LocalDateTime.now())
                .apply("retry_count < max_retry")
                .orderByAsc(DeepayRetryTaskDO::getNextRetryAt));
    }

    /** 将任务标记为 DONE */
    default void markDone(Long id) {
        update(null, new LambdaUpdateWrapper<DeepayRetryTaskDO>()
                .eq(DeepayRetryTaskDO::getId, id)
                .set(DeepayRetryTaskDO::getStatus, "DONE")
                .set(DeepayRetryTaskDO::getUpdatedAt, LocalDateTime.now()));
    }

    /** 记录一次失败，递增 retry_count，更新下次重试时间（指数退避） */
    default void markRetryFailed(Long id, String errorMsg, int retryCount, int maxRetry) {
        String newStatus = retryCount >= maxRetry ? "FAILED" : "RETRYING";
        // 指数退避：2^retryCount 分钟，最多 60 分钟
        long backoffMinutes = Math.min((long) Math.pow(2, retryCount), 60L);
        update(null, new LambdaUpdateWrapper<DeepayRetryTaskDO>()
                .eq(DeepayRetryTaskDO::getId, id)
                .set(DeepayRetryTaskDO::getStatus, newStatus)
                .set(DeepayRetryTaskDO::getErrorMsg, errorMsg)
                .set(DeepayRetryTaskDO::getRetryCount, retryCount + 1)
                .set(DeepayRetryTaskDO::getNextRetryAt,
                        LocalDateTime.now().plusMinutes(backoffMinutes))
                .set(DeepayRetryTaskDO::getUpdatedAt, LocalDateTime.now()));
    }

}
