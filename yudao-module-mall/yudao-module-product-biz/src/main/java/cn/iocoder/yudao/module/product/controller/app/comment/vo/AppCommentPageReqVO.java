package cn.iocoder.yudao.module.product.controller.app.comment.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Schema(description = "用户APP - 商品评价分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppCommentPageReqVO extends PageParam {

    /**
     * 所有
     */
    public static final Integer ALL = 0;

    /**
     * 所有数量 key
     */
    public static final String ALL_COUNT = "allCount";

    /**
     * 好评
     */
    public static final Integer FAVOURABLE_COMMENT = 1;

    /**
     * 好评数量 key
     */
    public static final String FAVOURABLE_COMMENT_COUNT = "favourableCommentCount";

    /**
     * 中评
     */
    public static final Integer MEDIOCRE_COMMENT = 2;

    /**
     * 中评数量 key
     */
    public static final String MEDIOCRE_COMMENT_COUNT = "mediocreCommentCount";

    /**
     * 差评
     */
    public static final Integer NEGATIVE_COMMENT = 3;

    /**
     * 差评数量 key
     */
    public static final String NEGATIVE_COMMENT_COUNT = "negativeCommentCount";

    /**
     * 默认匿名昵称
     */
    public static final String ANONYMOUS_NICKNAME = "匿名用户";

    @Schema(description = "商品SPU编号", example = "29502")
    @NotNull(message = "商品SPU编号不能为空")
    private Long spuId;

    @Schema(description = "app 评论页 tab 类型 (0 全部、1 好评、2 中评、3 差评)", example = "0")
    @NotNull(message = "商品SPU编号不能为空")
    private Integer type;

}
