package cn.iocoder.yudao.module.deepay.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 趋势款池 deepay_trend_pool（Phase 6-7 爆款层 + 设计层）
 *
 * <p>数据来源分级：
 * <ul>
 *   <li>🟢 爆款层（主用）：1688热卖、TikTok热门、SHEIN趋势</li>
 *   <li>🔴 设计层（灵感）：时装秀(runway)、品牌(brand)：ZARA/H&M/Nike/Gucci等</li>
 * </ul>
 * </p>
 */
@TableName("deepay_trend_pool")
@Data
public class DeepayTrendPoolDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 数据来源：1688 / tiktok / shein / brand / runway
     */
    private String source;

    /**
     * 品类：外套 / 裤子 / 裙子 / 上衣 / 内裤 / 连衣裙 …
     */
    private String category;

    /**
     * 风格标签：性感 / 工装 / 极简 / 休闲 / 轻奢 / 运动 …
     */
    private String style;

    /**
     * 目标客群：男装 / 少女 / 中老年 / 运动
     */
    private String crowd;

    /**
     * 参考图 URL（设计师看到的是参考款，不是商品）
     */
    private String imageUrl;

    /**
     * 综合评分（0~100+）：由来源加权 + 用户反馈动态更新
     * <ul>
     *   <li>runway  +15</li>
     *   <li>brand   +12</li>
     *   <li>shein   +10</li>
     *   <li>tiktok  +8</li>
     *   <li>1688    +6</li>
     * </ul>
     * 用户选中 +5，用户跳过 -3
     */
    private Integer score;

    private LocalDateTime createdAt;

}
