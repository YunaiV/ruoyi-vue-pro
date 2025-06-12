package cn.iocoder.yudao.module.tms.controller.admin.port.info.vo;

import cn.iocoder.yudao.module.system.api.utils.Validation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;

@Schema(description = "管理后台 - TMS港口信息新增/修改 Request VO")
@Data
public class TmsPortInfoSaveReqVO {
    @Schema(description = "编号")
    @NotNull(groups = {Validation.OnUpdate.class}, message = "更新时，港口信息id不能为空")
    @Null(groups = {Validation.OnCreate.class}, message = "创建时，港口信息id需为空")
    private Long id;

    @Schema(description = "港口编码")
    private String code;

    @Schema(description = "港口中文名")
    @NotBlank(message = "港口中文名不能为空")
    private String name;

    @Schema(description = "港口英文名")
    @NotBlank(message = "港口英文名不能为空")
    private String nameEn;

    @Schema(description = "国家代码(字典)")
    private Integer countryCode;

    @Schema(description = "国家描述")
    private String countryName;

    @Schema(description = "城市中文名")
    private String cityName;

    @Schema(description = "城市英文名")
    private String cityNameEn;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "启用/禁用状态")
    private Boolean status;

}