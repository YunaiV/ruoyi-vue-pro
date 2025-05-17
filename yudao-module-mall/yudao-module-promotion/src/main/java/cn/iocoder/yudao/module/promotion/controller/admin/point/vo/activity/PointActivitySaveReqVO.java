package cn.iocoder.yudao.module.promotion.controller.admin.point.vo.activity;

import cn.iocoder.yudao.module.promotion.controller.admin.point.vo.product.PointProductSaveReqVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "管理后台 - 积分商城活动新增/修改 Request VO")
@Data
public class PointActivitySaveReqVO {

    @Schema(description = "积分商城活动编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "11373")
    private Long id;

    @Schema(description = "积分商城活动商品", requiredMode = Schema.RequiredMode.REQUIRED, example = "19509")
    @NotNull(message = "积分商城活动商品不能为空")
    private Long spuId;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "排序不能为空")
    private Integer sort;

    @Schema(description = "积分商城商品", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<PointProductSaveReqVO> products;

}