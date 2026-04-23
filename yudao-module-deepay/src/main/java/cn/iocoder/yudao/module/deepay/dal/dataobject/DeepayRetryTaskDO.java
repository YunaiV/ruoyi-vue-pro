package cn.iocoder.yudao.module.deepay.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 失败重试任务 deepay_retry_task
 *
 * <p>凡是 AI 出图、支付回调、打版等关键步骤失败，均在此表登记，
 * 由 {@link cn.iocoder.yudao.module.deepay.scheduler.DeepayRetryScheduler} 定时扫描并重跑。</p>
 */
@TableName("deepay_retry_task")
@Data
public class DeepayRetryTaskDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联链码（可为 null，如全局 AI 失败） */
    private String chainCode;

    /**
     * 任务类型：
     * AI_DESIGN / PAYMENT / PATTERN / PUBLISH
     */
    private String taskType;

    /**
     * 任务状态：
     * PENDING / RETRYING / DONE / FAILED
     */
    private String status;

    /** 失败原因或异常信息摘要（最多 2048 字符） */
    private String errorMsg;

    /** 已重试次数 */
    private Integer retryCount;

    /** 最大重试次数（默认 3） */
    private Integer maxRetry;

    /** 下次重试时间（退避策略用） */
    private LocalDateTime nextRetryAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
