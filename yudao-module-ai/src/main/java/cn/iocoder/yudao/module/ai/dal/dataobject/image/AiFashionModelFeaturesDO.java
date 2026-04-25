package cn.iocoder.yudao.module.ai.dal.dataobject.image;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * AI 服装设计模特身体特征 DO
 *
 * <p>对应 Python {@code model_features} 表，存储模特的身体量体数据。</p>
 *
 * @author deepay
 */
@TableName("ai_fashion_model_features")
@KeySequence("ai_fashion_model_features_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class AiFashionModelFeaturesDO extends BaseDO {

    /** 编号 */
    @TableId
    private Long id;

    /** 关联素材库图片编号 */
    private Long libraryImageId;

    /** 身高（厘米） */
    private Integer heightCm;

    /** 体重（公斤） */
    private BigDecimal weightKg;

    /** 胸围（厘米） */
    private Integer bustCm;

    /** 腰围（厘米） */
    private Integer waistCm;

    /** 臀围（厘米） */
    private Integer hipsCm;

    /** 肤色（fair/medium/tan/dark） */
    private String skinTone;

    /** 发色（black/brown/blonde/red/white） */
    private String hairColor;

    /** 发长（short/medium/long） */
    private String hairLength;

    /** 姿势类型（front/side/back/walking/sitting/dynamic） */
    private String poseType;

}
