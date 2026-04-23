package cn.iocoder.yudao.module.deepay.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户配额表 deepay_user_quota（STEP 26）。
 *
 * <p>与 deepay_user_profile（偏好）分离，独立管理计费逻辑，保证强一致性。</p>
 *
 * <pre>
 * CREATE TABLE deepay_user_quota (
 *     id          BIGINT PRIMARY KEY AUTO_INCREMENT,
 *     user_id     VARCHAR(64) NOT NULL UNIQUE,
 *     free_quota  INT DEFAULT 3,   -- 剩余免费次数（新用户 3 次）
 *     paid_quota  INT DEFAULT 0,   -- 剩余付费次数
 *     used_quota  INT DEFAULT 0,   -- 累计已使用次数
 *     updated_at  DATETIME,
 *     created_at  DATETIME
 * );
 * </pre>
 */
@TableName("deepay_user_quota")
@Data
public class DeepayUserQuotaDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID（唯一索引） */
    private String userId;

    /** 剩余免费次数（新用户初始化为 3） */
    private Integer freeQuota;

    /** 剩余付费次数 */
    private Integer paidQuota;

    /** 累计已使用次数 */
    private Integer usedQuota;

    private LocalDateTime updatedAt;

    private LocalDateTime createdAt;

}
