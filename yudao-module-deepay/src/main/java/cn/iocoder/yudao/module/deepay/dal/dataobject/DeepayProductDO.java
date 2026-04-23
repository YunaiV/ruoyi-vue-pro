package cn.iocoder.yudao.module.deepay.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Deepay 商品表。
 *
 * <p>对应数据库表 {@code deepay_product}。
 * 由 ProductAgent 生成，经 PricingAgent 定价，PublishAgent 写入。</p>
 */
@TableName("deepay_product")
@Data
public class DeepayProductDO {

    /** 自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联的样式链码 */
    private String chainCode;

    /** 商品标题 */
    private String title;

    /** 商品描述 */
    private String description;

    /** 商品封面图 URL */
    private String coverImage;

    /** 销售价格（元） */
    private BigDecimal price;

    /**
     * 商品状态。
     * <ul>
     *   <li>{@code SELLING} —— 在售</li>
     *   <li>{@code SOLD}    —— 已售</li>
     * </ul>
     */
    private String status;

    /** 记录创建时间 */
    private LocalDateTime createdAt;

    /** 记录更新时间 */
    private LocalDateTime updatedAt;

}
