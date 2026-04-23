package cn.iocoder.yudao.module.deepay.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 选款参考池 deepay_selection_pool
 *
 * <p>外部选品数据（1688 / TikTok / SHEIN / 品牌）供 SelectionFeedAgent 读取。</p>
 */
@TableName("deepay_selection_pool")
@Data
public class DeepaySelectionPoolDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 款式图片 URL */
    private String imageUrl;

    /** 品类 */
    private String category;

    /** 风格标签 */
    private String style;

    /** 来源（1688 / tiktok / shein / brand） */
    private String source;

    /** 热度评分 */
    private Double score;

    private LocalDateTime createdAt;

}
