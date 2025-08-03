package cn.iocoder.yudao.module.product.controller.app.comment.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Schema(description = "用户 App - 商品评价分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppCommentPageReqVO extends PageParam {

    /**
     * 好评
     */
    public static final Integer GOOD_COMMENT = 1;
    /**
     * 中评
     */
    public static final Integer MEDIOCRE_COMMENT = 2;
    /**
     * 差评
     */
    public static final Integer NEGATIVE_COMMENT = 3;

    @Schema(description = "商品 SPU 编号", example = "29502")
    @NotNull(message = "商品 SPU 编号不能为空")
    private Long spuId;

    @Schema(description = "app 评论页 tab 类型 (0 全部、1 好评、2 中评、3 差评)", example = "0")
    @NotNull(message = "商品 SPU 编号不能为空")
    private Integer type;

}
