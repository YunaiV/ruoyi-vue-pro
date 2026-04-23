package cn.iocoder.yudao.module.deepay.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设计图评分结果表 deepay_design_image
 *
 * <p>由 {@link cn.iocoder.yudao.module.deepay.agent.ImageScoringAgent} 写入。</p>
 */
@TableName("deepay_design_image")
@Data
public class DeepayDesignImageDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 图片 CDN 地址 */
    private String url;

    /** 品类（外套 / 连衣裙 等） */
    private String category;

    /** 风格标签 */
    private String style;

    /** 综合分 */
    private Double score;

    /** 趋势分 */
    private Double trendScore;

    /** 客户匹配分 */
    private Double matchScore;

    /** 被展示次数（每次出现在 generate 结果中 +1），用于计算点击率 */
    private Integer viewCount;

    /** 用户点击/选中次数，用于计算点击率 = clickCount / viewCount */
    private Integer clickCount;

    /** 该图片关联的成交订单数，用于计算成交率 = orderCount / clickCount */
    private Integer orderCount;

    private LocalDateTime createdAt;

}
