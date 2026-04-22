package cn.iocoder.yudao.module.deepay.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设计要素拆解表 — DesignSplitAgent 输出，供 PatternPrepareAgent / CostEstimateAgent 使用。
 */
@Data
@TableName("deepay_design_features")
public class DeepayDesignFeaturesDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联链码 */
    private String chainCode;

    /** 品类（外套 / 裤子 / 裙子 / 上衣 / 内裤…） */
    private String category;

    /** 推荐面料（棉 / 牛仔 / 化纤 / 混纺…） */
    private String fabric;

    /** 主风格（工装 / 极简 / 性感 / 休闲…） */
    private String style;

    /** 颜色 JSON 数组，例如 ["黑","灰","白"] */
    private String color;

    /** 版型结构（宽松 / 修身 / 贴体 / 廓形） */
    private String structure;

    /** 结构复杂度 0~100（>80 拒绝生产） */
    private Integer complexity;

    private LocalDateTime createdAt;
}
