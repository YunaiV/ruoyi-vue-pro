package cn.iocoder.yudao.module.deepay.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 爆款变体表 deepay_variant
 *
 * <p>每条记录表示一个由爆款衍生的变体款式（Phase 8 爆款复制引擎产出）。
 * 变体编码格式：{@code {parent_chain_code}-V{三位序号}}，例如 {@code ABC123-V001}。</p>
 *
 * <p>变体图片（{@link #imageUrl}）由 FluxService 生成，
 * 使用 {@link #designPrompt} 作为输入 Prompt。</p>
 */
@TableName("deepay_variant")
@Data
public class DeepayVariantDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 爆款父链码（关联 deepay_hot_product.chain_code） */
    private String parentChainCode;

    /** 变体唯一编码（格式：{父链码}-V{001}） */
    private String variantCode;

    /** 品类（与父款相同，冗余存储便于查询） */
    private String category;

    /** 颜色变体（黑 / 白 / 灰 / 米白 / 深蓝 / 卡其等） */
    private String color;

    /** 面料变体（棉 / 牛仔 / 针织 / 羊毛 / 雪纺等） */
    private String fabric;

    /** 版型变体（宽松 / 修身 / 直筒等） */
    private String fit;

    /** 风格标签（与父款相同） */
    private String style;

    /** 变体设计图 CDN 地址（FluxService 生成后写入） */
    private String imageUrl;

    /** 生成本变体所用的 Flux Prompt */
    private String designPrompt;

    private LocalDateTime createdAt;

}
