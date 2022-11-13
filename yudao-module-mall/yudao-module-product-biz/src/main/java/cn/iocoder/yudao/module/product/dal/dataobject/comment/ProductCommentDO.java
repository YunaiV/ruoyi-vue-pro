package cn.iocoder.yudao.module.product.dal.dataobject.comment;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.product.enums.comment.ProductCommentAuditStatusEnum;
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
@TableName("product_comment")
@KeySequence("product_comment_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCommentDO extends BaseDO {

    /**
     * 评论编号，主键自增
     */
    @TableId
    private Long id;
    /**
     * 商品 SPU 编号
     *
     * 关联 {@link ProductSpuDO#getId()}
     */
    private Long spuId;
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
     * 审核状态
     *
     * 枚举 {@link ProductCommentAuditStatusEnum}
     */
    private Integer auditStatus;

    /**
     * 用户编号
     *
     * 关联 MemberUserDO 的 id 编号
     */
    private Long userId;
    /**
     * 用户 IP
     */
    private String userIp;
    /**
     * 是否匿名
     */
    private Boolean anonymous;
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
     * 描述相符星级
     *
     * 1-5 星
     */
    private Integer descriptionScore;
    /**
     * 商品评论星级
     *
     * 1-5 星
     */
    private Integer productScore;
    /**
     * 服务评论星级
     *
     * 1-5 星
     */
    private Integer serviceScore;
    /**
     * 物流评论星级
     *
     * 1-5 星
     */
    private Integer expressComment;

    /**
     * 商家是否回复
     */
    private Boolean replied;
    /**
     * 商家回复内容
     */
    private String replyContent;
    /**
     * 商家回复时间
     */
    private LocalDateTime replyTime;

    /**
     * 有用的计数
     *
     * 其他用户看到评论时，可点击「有用」按钮
     */
    private Integer usefulCount;

}
