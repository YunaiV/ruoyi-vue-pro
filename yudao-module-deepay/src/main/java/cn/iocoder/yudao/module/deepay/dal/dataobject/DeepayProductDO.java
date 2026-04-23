package cn.iocoder.yudao.module.deepay.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品表 deepay_product
 */
@TableName("deepay_product")
@Data
public class DeepayProductDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联链码 */
    private String chainCode;

    /**
     * 关联的设计图 ID（deepay_design_image.id）。
     * 绑定设计来源，保证学习系统链路不断。
     */
    private Long designId;

    /** 商品标题 */
    private String title;

    /** 商品描述 */
    private String description;

    /** 售价 */
    private BigDecimal price;

    /** 状态：SELLING / STOPPED / REDESIGNING */
    private String status;

    /** 销量 */
    private Integer soldCount;

    /** 可用库存 */
    private Integer stock;

    /** 生产成本（元），用于计算利润和 ROI */
    private java.math.BigDecimal costPrice;

    /** CDN 图片地址（FLUX 生成后同步至 CDN） */
    private String cdnImageUrl;

    /**
     * 商品品类（外套 / 内裤 / 裤子 / 上衣 / 连衣裙 …）。
     * 由 ProductAgent 落库时写入（来自 Context.category）；
     * TrendAgent 用 WHERE category=? 做精准过滤。
     */
    private String category;

    /** 风格标签（SEXY / CASUAL / SPORT / MINIMAL / LUXURY），由 ProductAgent 落库时写入。 */
    private String style;

    /**
     * 主图（便捷字段，等价于 cdnImageUrl）。
     * TrendAgent 通过 {@link #getMainImage()} 读取，避免字段名不一致问题。
     */
    public String getMainImage() {
        return this.cdnImageUrl;
    }

    private LocalDateTime createdAt;

}
