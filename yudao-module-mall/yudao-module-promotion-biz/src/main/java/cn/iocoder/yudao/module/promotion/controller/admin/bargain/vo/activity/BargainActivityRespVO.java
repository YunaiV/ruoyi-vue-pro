package cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.activity;

import cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.product.BargainProductRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 砍价活动 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BargainActivityRespVO extends BargainActivityBaseVO {

    // TODO @puhui999：example 补全

    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "618大促")
    private String spuName;

    @Schema(description = "商品主图", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn/xx.png")
    private String picUrl;

    @Schema(description = "活动编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "22901")
    private Long id;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "砍价成功数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer successCount;

    @Schema(description = "活动状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "活动状态不能为空")
    private Integer status;

    @Schema(description = "砍价商品", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    private List<BargainProductRespVO> products;

}
