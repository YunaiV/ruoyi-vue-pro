package cn.iocoder.yudao.module.deepay.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作审计日志 deepay_log
 *
 * <p>记录所有关键业务操作：生成、上架、支付、改价、下架、退款等。
 * 每条记录不可变，永久保留，作为合规与追溯依据。</p>
 */
@TableName("deepay_log")
@Data
public class DeepayLogDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联链码 */
    private String chainCode;

    /**
     * 操作类型：
     * CREATE / PUBLISH / PAY / REPRICE / STOP / REDESIGN / REFUND / RESTOCK / RETRY
     */
    private String action;

    /** 操作前的状态或数值（JSON 或文本） */
    @TableField("before_val")
    private String before;

    /** 操作后的状态或数值（JSON 或文本） */
    @TableField("after_val")
    private String after;

    /** 操作时间 */
    private LocalDateTime time;

}
