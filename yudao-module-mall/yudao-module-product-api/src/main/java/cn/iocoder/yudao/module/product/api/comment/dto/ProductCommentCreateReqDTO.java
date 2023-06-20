package cn.iocoder.yudao.module.product.api.comment.dto;

import lombok.Data;

import java.util.List;

/**
 * 评论创建请求 DTO
 *
 * @author HUIHUI
 */
@Data
public class ProductCommentCreateReqDTO {

    /**
     * 商品 SKU 编号
     */
    private Long skuId;
    /**
     * 订单编号
     */
    private Long orderId;
    /**
     * 交易订单项编号
     */
    private Long orderItemId;

    /**
     * 评分星级 1-5 分
     */
    private Integer scores;
    /**
     * 描述星级 1-5 分
     */
    private Integer descriptionScores;
    /**
     * 服务星级 1-5 分
     */
    private Integer benefitScores;
    /**
     * 评论内容
     */
    private String content;
    /**
     * 评论图片地址数组，以逗号分隔最多上传 9 张
     */
    private List<String> picUrls;

    /**
     * 是否匿名
     */
    private Boolean anonymous;
    /**
     * 评价人
     */
    private Long userId;


}
