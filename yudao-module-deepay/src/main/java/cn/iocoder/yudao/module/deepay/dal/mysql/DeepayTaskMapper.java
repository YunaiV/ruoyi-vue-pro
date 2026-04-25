package cn.iocoder.yudao.module.deepay.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayTaskDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 异步出图任务 Mapper（STEP 21）。
 */
@Mapper
public interface DeepayTaskMapper extends BaseMapperX<DeepayTaskDO> {

    /** 将任务标记为 running */
    default void markRunning(Long id) {
        update(null, new LambdaUpdateWrapper<DeepayTaskDO>()
                .eq(DeepayTaskDO::getId, id)
                .set(DeepayTaskDO::getStatus, "running"));
    }

    /** 将任务标记为 success 并保存结果 JSON */
    default void markSuccess(Long id, String result) {
        update(null, new LambdaUpdateWrapper<DeepayTaskDO>()
                .eq(DeepayTaskDO::getId, id)
                .set(DeepayTaskDO::getStatus, "success")
                .set(DeepayTaskDO::getResult, result));
    }

    /** 将任务标记为 failed 并保存错误信息 */
    default void markFailed(Long id, String errorMsg) {
        String truncated = errorMsg != null
                ? errorMsg.substring(0, Math.min(errorMsg.length(), 1024)) : "unknown error";
        update(null, new LambdaUpdateWrapper<DeepayTaskDO>()
                .eq(DeepayTaskDO::getId, id)
                .set(DeepayTaskDO::getStatus, "failed")
                .set(DeepayTaskDO::getErrorMsg, truncated));
    }

}
