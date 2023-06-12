package cn.iocoder.yudao.module.product.api.comment.dto;

import lombok.Data;

import java.util.List;

/**
 * 评论创建请求 DTO
 *
 * @author HUIHUI
 */
@Data
public class CommentCreateReqDTO {

    /**
     * 是否匿名
     */
    private Boolean anonymous;

    /**
     * 交易订单项编号
     */
    private Long orderItemId;

    /**
     * 商品 SPU 编号
     */
    private Long spuId;

    /**
     * 商品 SPU 名称
     */
    private String spuName;

    /**
     * 商品 SKU 编号
     */
    private Long skuId;

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
     * 评论图片地址数组，以逗号分隔最多上传9张
     */
    private List<String> picUrls;

    /**
     * 评价人名称
     */
    private String userNickname;

    /**
     * 评价人头像
     */
    private String userAvatar;

    /**
     * 评价人
     */
    private Long userId;

}
