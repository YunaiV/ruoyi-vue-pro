package cn.iocoder.yudao.module.deepay.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设计变体表 — DesignVariantAgent 生成的 3+ 个风格变体，selected=1 表示最终选定款。
 */
@Data
@TableName("deepay_design_variant")
public class DeepayDesignVariantDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联链码 */
    private String chainCode;

    /** 变体图片 URL */
    private String imageUrl;

    /** 风格标签（偏欧美 / 偏基础 / 偏运动 / 风格强化 / 简化版） */
    private String styleTag;

    /** 综合评分 0~100（ScoreUtil.computeDesignScore） */
    private Integer score;

    /** 1=被选为最优变体，0=未被选中 */
    private Integer selected;

    private LocalDateTime createdAt;
}
