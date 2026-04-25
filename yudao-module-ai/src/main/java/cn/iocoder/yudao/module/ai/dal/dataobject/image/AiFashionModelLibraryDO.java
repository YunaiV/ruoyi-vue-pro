package cn.iocoder.yudao.module.ai.dal.dataobject.image;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * AI 服装设计素材库 DO
 *
 * <p>对应 Python {@code fashion_images} 表，存储时装秀、品牌、模特机构的图片元数据。</p>
 *
 * @author deepay
 */
@TableName("ai_fashion_model_library")
@KeySequence("ai_fashion_model_library_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class AiFashionModelLibraryDO extends BaseDO {

    /** 编号 */
    @TableId
    private Long id;

    /** 原始来源 URL */
    private String sourceUrl;

    /** 服务器本地路径（或对象存储 key） */
    private String localPath;

    /** 关联的文件 ID（FileApi） */
    private Long fileId;

    /** 标题 */
    private String title;

    /** 描述 */
    private String description;

    /**
     * 品类
     * dress / suit / shirt / pants / jacket / skirt / accessories
     */
    private String category;

    /** 风格标签（JSON 数组字符串） */
    private String styleTags;

    /** 颜色标签（JSON 数组字符串） */
    private String colorTags;

    /** 品牌名称 */
    private String brand;

    /** 季节（spring_summer_2024 / autumn_winter_2024 等） */
    private String season;

    /**
     * 来源类型
     * fashion_show / brand / model_agency / street_style
     */
    private String sourceType;

    /** 关联采集源 ID（对应 {@link AiFashionCollectionSourceDO#getId()}） */
    private String collectionSourceId;

    /** 图片 MD5 哈希（去重用） */
    private String imageHash;

    /** 图片宽度（像素） */
    private Integer width;

    /** 图片高度（像素） */
    private Integer height;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 文件格式（jpg/png/webp） */
    private String fileFormat;

    /** 是否含模特 */
    private Boolean isModel;

    /** 模特姿势（walking/standing/sitting/front/side/back） */
    private String modelPose;

    /** 模特体型（slim/athletic/curvy/plus_size） */
    private String modelBodyType;

    /** 索引时间 */
    private java.time.LocalDateTime indexedAt;

}
