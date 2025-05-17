package cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.activity;

import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.product.CombinationProductRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 拼团活动 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CombinationActivityRespVO extends CombinationActivityBaseVO {

    @Schema(description = "活动编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "22901")
    private Long id;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "开团人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "666")
    private Integer userSize;

    @Schema(description = "拼团商品", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<CombinationProductRespVO> products;

    @Schema(description = "商品 SPU 名字", requiredMode = Schema.RequiredMode.REQUIRED, example = "一个白菜")
    private String spuName; // 从 SPU 的 name 读取
    @Schema(description = "商品图片", requiredMode = Schema.RequiredMode.REQUIRED, example = "4096")
    private String picUrl; // 从 SPU 的 picUrl 读取
    @Schema(description = "商品市场价，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
    private Integer marketPrice; // 从 SPU 的 marketPrice 读取

    @Schema(description = "拼团金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer combinationPrice; // 从 products 获取最小 price 读取

}
