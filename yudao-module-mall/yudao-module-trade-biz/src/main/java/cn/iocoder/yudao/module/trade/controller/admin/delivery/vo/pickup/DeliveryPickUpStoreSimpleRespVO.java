package cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.pickup;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 自提门店精简信息 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryPickUpStoreSimpleRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "23128")
    private Long id;

    @Schema(description = "门店名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    private String name;

    @Schema(description = "门店手机", requiredMode = Schema.RequiredMode.REQUIRED, example = "15601892312")
    private String phone;

    @Schema(description = "区域编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "18733")
    private Integer areaId;

    @Schema(description = "区域名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "xx市")
    private String areaName;

    @Schema(description = "门店详细地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "复旦大学路 188 号")
    private String detailAddress;

}
