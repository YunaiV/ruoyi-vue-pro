package cn.iocoder.yudao.module.pay.dal.dataobject.blockchain;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 区块链存证任务 DO
 *
 * @author deepay
 */
@TableName("pay_blockchain_task")
@KeySequence("pay_blockchain_task_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockchainTaskDO extends BaseDO {

    /**
     * 自增主键
     */
    @TableId
    private Long id;

    /**
     * 任务唯一 ID（业务去重用）
     */
    private String taskId;

    /**
     * 关联订单号
     */
    private String orderId;

    /**
     * 用户编号
     */
    private String userId;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 货币代码，如 CNY / EUR
     */
    private String currency;

    /**
     * 数据哈希（SHA-256）
     */
    private String dataHash;

    /**
     * 链上交易哈希
     */
    private String txHash;

    /**
     * 公链批量上传交易哈希
     */
    private String publicChainTxHash;

    /**
     * 链类型：JD_CHAIN / LOCAL_HASH
     */
    private String chainType;

    /**
     * 任务状态：PROCESSING / SUCCESS / FAILED / DEAD_LETTER
     */
    private String status;

    /**
     * 错误信息（失败时记录）
     */
    private String errorMsg;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 是否已批量上传到公链
     */
    private Boolean batchUploaded;

    /**
     * 批量上传时间
     */
    private LocalDateTime batchUploadTime;

    /**
     * 任务创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 任务完成时间
     */
    private LocalDateTime completedAt;

}
