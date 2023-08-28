package cn.iocoder.yudao.module.product.controller.admin.comment.vo;

import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuBaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 商品评价 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductCommentRespVO extends ProductCommentBaseVO {

    @Schema(description = "订单项编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "24965")
    private Long id;

    @Schema(description = "是否匿名", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean anonymous;

    @Schema(description = "交易订单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "24428")
    private Long orderId;

    @Schema(description = "是否可见", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean visible;

    @Schema(description = "商家是否回复", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean replyStatus;

    @Schema(description = "回复管理员编号", example = "9527")
    private Long replyUserId;

    @Schema(description = "商家回复内容", example = "感谢好评哦亲(づ￣3￣)づ╭❤～")
    private String replyContent;

    @Schema(description = "商家回复时间", example = "2023-08-08 12:20:55")
    private LocalDateTime replyTime;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "评分星级 1-5 分", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Integer scores;

    @Schema(description = "商品 SPU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "清凉丝滑透气小短袖")
    @NotNull(message = "商品 SPU 编号不能为空")
    private Long spuId;

    @Schema(description = "商品 SPU 名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @NotNull(message = "商品 SPU 名称不能为空")
    private String spuName;

    @Schema(description = "商品 SKU 图片地址", example = "https://www.iocoder.cn/yudao.jpg")
    private String skuPicUrl;

    @Schema(description = "商品 SKU 规格值数组")
    private List<ProductSkuBaseVO.Property> skuProperties;

}
