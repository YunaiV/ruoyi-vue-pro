package cn.iocoder.yudao.module.statistics.dal.dataobject.product;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDate;

/**
 * 商品统计 DO
 *
 * @author owen
 */
@TableName("product_statistics")
@KeySequence("product_statistics_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductStatisticsDO extends BaseDO {

    /**
     * 编号，主键自增
     */
    @TableId
    private Long id;
    /**
     * 统计日期
     */
    private LocalDate time;
    /**
     * 商品 SPU 编号
     */
    private Long spuId;
    /**
     * 浏览量
     */
    private Integer browseCount;
    /**
     * 访客量
     */
    private Integer browseUserCount;
    /**
     * 收藏数量
     */
    private Integer favoriteCount;
    /**
     * 加购数量
     */
    private Integer cartCount;
    /**
     * 下单件数
     */
    private Integer orderCount;
    /**
     * 支付件数
     */
    private Integer orderPayCount;
    /**
     * 支付金额，单位：分
     */
    private Integer orderPayPrice;
    /**
     * 退款件数
     */
    private Integer afterSaleCount;
    /**
     * 退款金额，单位：分
     */
    private Integer afterSaleRefundPrice;
    /**
     * 访客支付转化率（百分比）
     */
    private Integer browseConvertPercent;

}