package cn.iocoder.yudao.module.oa.controller.admin.vehicle.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 车辆 Response VO")
@Data
public class OaVehicleRespVO {

    @Schema(description = "编号", example = "1024", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "车牌号", example = "沪A12345", requiredMode = Schema.RequiredMode.REQUIRED)
    private String no;

    @Schema(description = "车辆名称", example = "商务接待车", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "所属部门编号", example = "103")
    private Long deptId;

    @Schema(description = "所属部门名称", example = "研发部门")
    private String deptName;

    @Schema(description = "车型", example = "商务车", requiredMode = Schema.RequiredMode.REQUIRED)
    private String type;

    @Schema(description = "车辆分类", example = "公务用车")
    private String category;

    @Schema(description = "品牌型号", example = "别克 GL8")
    private String brandModel;

    @Schema(description = "座位数", example = "7", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer seatCount;

    @Schema(description = "裸车价格（元）", example = "250000.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal barePrice;

    @Schema(description = "交强险到期时间", example = "1789002000000", type = "integer", format = "int64")
    private LocalDateTime compulsoryInsuranceExpireTime;

    @Schema(description = "商业险到期时间", example = "1789002000000", type = "integer", format = "int64")
    private LocalDateTime commercialInsuranceExpireTime;

    @Schema(description = "年检到期时间", example = "1789002000000", type = "integer", format = "int64")
    private LocalDateTime inspectionExpireTime;

    @Schema(description = "车辆照片 URL", example = "https://example.com/vehicle.png")
    private String picUrl;

    @Schema(description = "车辆台账状态", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;

    @Schema(description = "显示顺序", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer sort;

    @Schema(description = "备注", example = "部门公务用车")
    private String remark;

    @Schema(description = "创建时间", example = "1789002000000", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64")
    private LocalDateTime createTime;

}
