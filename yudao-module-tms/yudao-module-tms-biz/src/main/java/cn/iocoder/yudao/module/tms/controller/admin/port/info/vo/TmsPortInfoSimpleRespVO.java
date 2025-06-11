package cn.iocoder.yudao.module.tms.controller.admin.port.info.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - TMS港口信息精简列表 Response VO")
@Data
public class TmsPortInfoSimpleRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "港口名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "上海港")
    private String name;

    @Schema(description = "港口代码", requiredMode = Schema.RequiredMode.REQUIRED, example = "SHG")
    private String code;

    @Schema(description = "国家", requiredMode = Schema.RequiredMode.REQUIRED, example = "CN")
    private String country;

    @Schema(description = "省份", requiredMode = Schema.RequiredMode.REQUIRED, example = "浙江省")
    private String province;

    @Schema(description = "城市", requiredMode = Schema.RequiredMode.REQUIRED, example = "上海")
    private String city;

} 