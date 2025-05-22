package cn.iocoder.yudao.module.product.dal.dataobject.comment;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品评论 DO
 *
 * @author 芋道源码
 */
@TableName(value = "product_comment", autoResultMap = true)
@KeySequence("product_comment_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCommentDO extends BaseDO {

    /**
     * 默认匿名昵称
     */
    public static final String NICKNAME_ANONYMOUS = "匿名用户";

    /**
     * 评论编号，主键自增
     */
    @TableId
    private Long id;

    /**
     * 评价人的用户编号
     *
     * 关联 MemberUserDO 的 id 编号
     */
    private Long userId;
    /**
     * 评价人名称
     */
    private String userNickname;
    /**
     * 评价人头像
     */
    private String userAvatar;
    /**
     * 是否匿名
     */
    private Boolean anonymous;

    /**
     * 交易订单编号
     *
     * 关联 TradeOrderDO 的 id 编号
     */
    private Long orderId;
    /**
     * 交易订单项编号
     *
     * 关联 TradeOrderItemDO 的 id 编号
     */
    private Long orderItemId;

    /**
     * 商品 SPU 编号
     *
     * 关联 {@link ProductSpuDO#getId()}
     */
    private Long spuId;
    /**
     * 商品 SPU 名称
     *
     * 关联 {@link ProductSpuDO#getName()}
     */
    private String spuName;
    /**
     * 商品 SKU 编号
     *
     * 关联 {@link ProductSkuDO#getId()}
     */
    private Long skuId;
    /**
     * 商品 SKU 图片地址
     *
     * 关联 {@link ProductSkuDO#getPicUrl()}
     */
    private String skuPicUrl;
    /**
     * 属性数组，JSON 格式
     *
     * 关联 {@link ProductSkuDO#getProperties()}
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<ProductSkuDO.Property> skuProperties;

    /**
     * 是否可见
     *
     * true:显示
     * false:隐藏
     */
    private Boolean visible;
    /**
     * 评分星级
     *
     * 1-5 分
     */
    private Integer scores;
    /**
     * 描述星级
     *
     * 1-5 星
     */
    private Integer descriptionScores;
    /**
     * 服务星级
     *
     * 1-5 星
     */
    private Integer benefitScores;
    /**
     * 评论内容
     */
    private String content;
    /**
     * 评论图片地址数组
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> picUrls;

    /**
     * 商家是否回复
     */
    private Boolean replyStatus;
    /**
     * 回复管理员编号
     * 关联 AdminUserDO 的 id 编号
     */
    private Long replyUserId;
    /**
     * 商家回复内容
     */
    private String replyContent;
    /**
     * 商家回复时间
     */
    private LocalDateTime replyTime;

}
