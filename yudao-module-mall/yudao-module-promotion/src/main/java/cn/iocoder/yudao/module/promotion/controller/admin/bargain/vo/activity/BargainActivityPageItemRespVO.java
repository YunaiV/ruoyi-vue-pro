package cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 砍价活动的分页项 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BargainActivityPageItemRespVO extends BargainActivityBaseVO {

    @Schema(description = "活动编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "22901")
    private Long id;

    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "618大促")
    private String spuName;
    @Schema(description = "商品主图", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn/xx.png")
    private String picUrl;

    @Schema(description = "活动状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "活动状态不能为空")
    private Integer status;

    @Schema(description = "活动总库存", requiredMode = Schema.RequiredMode.REQUIRED, example = "23")
    private Integer totalStock;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2022-07-01 23:59:59")
    private LocalDateTime createTime;

    // ========== 统计字段 ==========

    @Schema(description = "总砍价的用户数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "999")
    private Integer recordUserCount;

    @Schema(description = "成功砍价的用户数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "500")
    private Integer recordSuccessUserCount;

    @Schema(description = "帮助砍价的用户数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "888")
    private Integer helpUserCount;

}
