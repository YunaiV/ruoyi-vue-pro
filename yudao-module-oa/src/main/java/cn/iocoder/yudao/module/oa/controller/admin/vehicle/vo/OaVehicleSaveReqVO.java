package cn.iocoder.yudao.module.oa.controller.admin.vehicle.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.oa.enums.vehicle.OaVehicleStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 车辆新增/修改 Request VO")
@Data
public class OaVehicleSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "车牌号", requiredMode = Schema.RequiredMode.REQUIRED, example = "沪A12345")
    @NotBlank(message = "车牌号不能为空")
    @Size(max = 32, message = "车牌号长度不能超过 32 个字符")
    private String no;

    @Schema(description = "车辆名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "商务接待车")
    @NotBlank(message = "车辆名称不能为空")
    @Size(max = 128, message = "车辆名称长度不能超过 128 个字符")
    private String name;

    @Schema(description = "所属部门编号", example = "103")
    private Long deptId;

    @Schema(description = "车型", requiredMode = Schema.RequiredMode.REQUIRED, example = "商务车")
    @NotBlank(message = "车型不能为空")
    @Size(max = 64, message = "车型长度不能超过 64 个字符")
    private String type;

    @Schema(description = "车辆分类", example = "公务用车")
    @Size(max = 64, message = "车辆分类长度不能超过 64 个字符")
    private String category;

    @Schema(description = "品牌型号", example = "别克 GL8")
    @Size(max = 128, message = "品牌型号长度不能超过 128 个字符")
    private String brandModel;

    @Schema(description = "座位数", requiredMode = Schema.RequiredMode.REQUIRED, example = "7")
    @NotNull(message = "座位数不能为空")
    @DecimalMin(value = "1", message = "座位数不能小于 1")
    private Integer seatCount;

    @Schema(description = "裸车价格（元）", requiredMode = Schema.RequiredMode.REQUIRED, example = "250000.00")
    @NotNull(message = "裸车价格（元）不能为空")
    @DecimalMin(value = "0", message = "裸车价格（元）不能小于 0")
    @Digits(integer = 16, fraction = 2, message = "裸车价格整数位不能超过 {integer} 位，小数位不能超过 {fraction} 位")
    private BigDecimal barePrice;

    @Schema(description = "交强险到期时间", example = "1789002000000", type = "integer", format = "int64")
    private LocalDateTime compulsoryInsuranceExpireTime;

    @Schema(description = "商业险到期时间", example = "1789002000000", type = "integer", format = "int64")
    private LocalDateTime commercialInsuranceExpireTime;

    @Schema(description = "年检到期时间", example = "1789002000000", type = "integer", format = "int64")
    private LocalDateTime inspectionExpireTime;

    @Schema(description = "车辆照片 URL", example = "https://example.com/vehicle.png")
    @Size(max = 2048, message = "照片地址长度不能超过 2048 个字符")
    private String picUrl;

    @Schema(description = "显示顺序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "显示顺序不能为空")
    private Integer sort;

    @Schema(description = "车辆状态", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "车辆状态不能为空")
    @InEnum(value = OaVehicleStatusEnum.class, message = "车辆状态必须是 {value}")
    private Integer status;

    @Schema(description = "备注", example = "部门公务用车")
    @Size(max = 500, message = "备注长度不能超过 500 个字符")
    private String remark;

}
