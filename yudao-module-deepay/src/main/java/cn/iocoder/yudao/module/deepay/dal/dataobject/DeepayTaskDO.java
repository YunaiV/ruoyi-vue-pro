package cn.iocoder.yudao.module.deepay.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 异步出图任务表 deepay_task（STEP 21）。
 *
 * <pre>
 * CREATE TABLE deepay_task (
 *     id         BIGINT PRIMARY KEY AUTO_INCREMENT,
 *     user_id    VARCHAR(64),
 *     status     VARCHAR(20),   -- pending / running / success / failed
 *     result     TEXT,          -- JSON：safeImages 列表
 *     error_msg  VARCHAR(1024),
 *     created_at DATETIME
 * );
 * </pre>
 */
@TableName("deepay_task")
@Data
public class DeepayTaskDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发起任务的用户 ID */
    private String userId;

    /**
     * 任务状态：
     * pending — 已创建，等待执行；
     * running — 正在执行；
     * success — 执行成功；
     * failed  — 执行失败（见 errorMsg）。
     */
    private String status;
    /** 执行结果，JSON 序列化的 safeImages 列表 */
    private String result;

    /** 失败原因 */
    private String errorMsg;

    private LocalDateTime createdAt;

}
