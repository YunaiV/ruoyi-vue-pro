package cn.iocoder.yudao.module.ai.framework.ai.core.video;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI 视频异步任务的统一状态
 *
 * 各服务商的状态先映射到这里；无法识别的状态一律为 {@link #UNKNOWN}，不能当作成功，也不能当作结束
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum AiVideoTaskStatus {

    /**
     * 已提交、排队中
     */
    QUEUED(false),
    /**
     * 生成中
     */
    RUNNING(false),
    /**
     * 成功：至少有一个真实的视频结果（地址、GCS 地址或字节）
     */
    SUCCEEDED(true),
    /**
     * 失败：包括内容审核未通过、服务商声称成功但没有结果
     */
    FAILED(true),
    /**
     * 已取消
     */
    CANCELLED(true),
    /**
     * 已过期：任务编号或结果超过服务商的保留期
     */
    EXPIRED(true),
    /**
     * 未知：服务商返回了未识别的状态，或任务不存在。调用方应继续查询或人工处理，不能视为成功
     */
    UNKNOWN(false);

    /**
     * 是否为终态
     */
    private final boolean finished;

}
