package cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.activity;

import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.product.CombinationProductRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 拼团活动的分页项 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CombinationActivityPageItemRespVO extends CombinationActivityBaseVO {

    @Schema(description = "活动编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "22901")
    private Long id;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "活动状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "拼团商品", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<CombinationProductRespVO> products;

    // ========== 商品字段 ==========

    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED, // 从 SPU 的 name 读取
            example = "618大促")
    private String spuName;
    @Schema(description = "商品主图", requiredMode = Schema.RequiredMode.REQUIRED, // 从 SPU 的 picUrl 读取
            example = "https://www.iocoder.cn/xx.png")
    private String picUrl;
    @Schema(description = "商品市场价，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, // 从 SPU 的 marketPrice 读取
            example = "50")
    private Integer marketPrice;

    // ========== 统计字段 ==========

    @Schema(description = "开团组数", requiredMode = Schema.RequiredMode.REQUIRED, example = "33")
    private Integer groupCount;

    @Schema(description = "成团组数", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    private Integer groupSuccessCount;

    @Schema(description = "购买次数", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer recordCount;

}
