package cn.iocoder.yudao.module.deepay.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 客户选款记录 deepay_selection_log（Phase 6-7 — 驱动记忆进化）
 *
 * <p>每次用户对参考图执行"选中"或"跳过"操作时写入一条记录。
 * FeedbackAgent 据此更新 deepay_trend_pool.score，实现"越用越准"。</p>
 *
 * <p>验收：
 * <ul>
 *   <li>✔ 每次点选都落库</li>
 *   <li>✔ 能按 customer_id + category 查历史偏好</li>
 *   <li>✔ is_selected=1 驱动权重上升，0 驱动权重下降</li>
 * </ul>
 * </p>
 */
@TableName("deepay_selection_log")
@Data
public class DeepaySelectionLogDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 客户 ID（对应 deepay_customer_profile.customer_id）
     */
    private Long customerId;

    /**
     * 用户所看的参考图 URL
     */
    private String imageUrl;

    /**
     * 参考图品类（外套 / 内裤 / 连衣裙 …）
     */
    private String category;

    /**
     * 参考图风格（工装 / 极简 / 性感 …）
     */
    private String style;

    /**
     * 参考图客群（男装 / 少女 / 中老年 / 运动）
     */
    private String crowd;

    /**
     * 用户行为：1=选中（喜欢） / 0=跳过（不感兴趣）
     */
    private Integer isSelected;

    private LocalDateTime createdAt;

}
