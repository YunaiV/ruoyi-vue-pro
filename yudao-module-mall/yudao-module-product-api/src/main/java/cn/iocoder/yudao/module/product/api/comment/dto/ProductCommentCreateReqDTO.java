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
     * 交易订单项编号
     */
    private Long orderItemId;

    // TODO @huihui：spuId、spuName 去查询哇？通过 skuId
    /**
     * 商品 SPU 编号
     */
    private Long spuId;
    /**
     * 商品 SPU 名称
     */
    private String spuName;

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
    // TODO @puhui999：是不是 userNickname、userAvatar 去掉？通过 userId 查询
    /**
     * 评价人名称
     */
    private String userNickname;
    /**
     * 评价人头像
     */
    private String userAvatar;

}
