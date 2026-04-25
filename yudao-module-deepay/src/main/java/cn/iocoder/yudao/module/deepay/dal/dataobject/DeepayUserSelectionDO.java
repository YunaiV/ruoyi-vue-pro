package cn.iocoder.yudao.module.deepay.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户选择记录表 deepay_user_selection（Phase 6 — 越用越准）
 *
 * <p>用户每次选定一张设计图时写入一条记录。
 * {@link cn.iocoder.yudao.module.deepay.agent.TrendAgent} 在下次查询时优先返回
 * 与历史选择品类 + 风格一致的款式，使系统随使用次数增加而更精准。</p>
 */
@TableName("deepay_user_selection")
@Data
public class DeepayUserSelectionDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 外部用户 ID（与 deepay_user_profile.user_id 对应）。
     */
    private String userId;

    /**
     * 关联链码（来自 Context.chainCode，可追溯到完整生产链路）。
     */
    private String chainCode;

    /**
     * 用户选中的设计图 URL（CDN 地址）。
     */
    private String selectedImage;

    /**
     * 本次选择的品类（如 外套 / 内裤）。
     * 下次查询时作为 TrendAgent WHERE category=? 的权重依据。
     */
    private String category;

    /**
     * 本次选择的风格标签（如 MINIMAL / SEXY）。
     * PreferenceLearningAgent 据此更新 style_weights。
     */
    private String style;

    /**
     * 本次选择的目标市场（CN / EU / US / ME）。
     */
    private String market;

    /**
     * 本次设计图的评分（来自 JudgeAgent）。
     */
    private Integer score;

    private LocalDateTime createdAt;

}
