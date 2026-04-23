package cn.iocoder.yudao.module.deepay.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 爆款识别结果表 deepay_hot_product
 *
 * <p>识别规则：
 * <ul>
 *   <li>sold_count &ge; 10  → HOT</li>
 *   <li>sold_count &ge; 50  → SUPER_HOT</li>
 * </ul>
 * </p>
 *
 * <p>由 {@link cn.iocoder.yudao.module.deepay.agent.HotCloneAgent} 写入，
 * {@link cn.iocoder.yudao.module.deepay.scheduler.DeepayHotCloneScheduler} 定时扫描。</p>
 */
@TableName("deepay_hot_product")
@Data
public class DeepayHotProductDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 原始商品链码（关联 deepay_product.chain_code） */
    private String chainCode;

    /** 品类（外套 / 内裤 / 连衣裙 / 裤子 / 上衣等） */
    private String category;

    /** 风格标签（SEXY / MINIMAL / CASUAL / SPORT / LUXURY） */
    private String style;

    /** 原款主图 CDN 地址（来自 deepay_product.cdn_image_url） */
    private String imageUrl;

    /** 累计销量（识别爆款依据） */
    private Integer soldCount;

    /**
     * 爆款等级。
     * <ul>
     *   <li>{@code HOT}       — soldCount &ge; 10</li>
     *   <li>{@code SUPER_HOT} — soldCount &ge; 50</li>
     * </ul>
     */
    private String hotLevel;

    /**
     * 爆款评分（soldCount / 50.0，便于排序；SUPER_HOT 时 &ge; 1.0）。
     */
    private Double score;

    private LocalDateTime createdAt;

}
