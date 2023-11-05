package cn.iocoder.yudao.module.trade.controller.app.delivery.vo.pickup;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 App - 自提门店 Response VO")
@Data
public class AppDeliveryPickUpStoreRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "23128")
    private Long id;

    @Schema(description = "门店名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    private String name;

    @Schema(description = "门店 logo", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn/1.png")
    private String logo;

    @Schema(description = "门店手机", requiredMode = Schema.RequiredMode.REQUIRED, example = "15601892312")
    private String phone;

    @Schema(description = "区域编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "18733")
    private Integer areaId;

    @Schema(description = "地区名字", requiredMode = Schema.RequiredMode.REQUIRED, example = "上海上海市普陀区")
    private String areaName;

    @Schema(description = "门店详细地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "复旦大学路 188 号")
    private String detailAddress;

    @Schema(description = "纬度", requiredMode = Schema.RequiredMode.REQUIRED, example = "5.88")
    private Double latitude;

    @Schema(description = "经度", requiredMode = Schema.RequiredMode.REQUIRED, example = "6.99")
    private Double longitude;

    @Schema(description = "距离，单位：千米", example = "100") // 只有在用户传递了经纬度时，才进行计算
    private Double distance;

}
