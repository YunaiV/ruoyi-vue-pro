package cn.iocoder.yudao.module.ai.dal.dataobject.image;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * AI 服装设计素材采集源配置 DO
 *
 * <p>对应 Python {@code collection_sources} 表，预置了 10 大时装秀媒体、
 * 10 大奢侈品牌及 5 大模特机构。</p>
 *
 * @author deepay
 */
@TableName("ai_fashion_collection_source")
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class AiFashionCollectionSourceDO extends BaseDO {

    /** 来源标识（唯一 key，如 vogue_runway / gucci） */
    @TableId
    private String id;

    /** 来源名称 */
    private String name;

    /** 来源 URL */
    private String url;

    /**
     * 来源类型
     * fashion_show / brand / model_agency / street_style
     */
    private String sourceType;

    /** 分类说明 */
    private String category;

    /** 优先级（1 最高） */
    private Integer priority;

    /** 状态（active / paused / disabled） */
    private String status;

    /** 最近一次采集时间 */
    private LocalDateTime lastCollected;

    /** 累计采集图片数 */
    private Integer collectCount;

    /** 采集配置 JSON（interval_hours / max_pages / selectors 等） */
    private String config;

}
