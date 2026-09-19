package cn.iocoder.yudao.module.oa.controller.admin.vehicle.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.oa.enums.vehicle.OaVehicleStatusEnum;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 车辆分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class OaVehiclePageReqVO extends PageParam {

    @Schema(description = "车牌号", example = "沪A12345")
    private String no;

    @Schema(description = "车辆名称", example = "商务接待车")
    private String name;

    @Schema(description = "所属部门编号", example = "103")
    private Long deptId;

    @Schema(description = "分类", example = "公务用车")
    private String category;

    @Schema(description = "状态", example = "0")
    @InEnum(value = OaVehicleStatusEnum.class, message = "状态必须是 {value}")
    private Integer status;

    @Schema(description = "车型", example = "商务车")
    private String type;

    @Schema(description = "品牌型号", example = "别克 GL8")
    private String brandModel;

    @Schema(description = "交强险到期时间", example = "[\"2026-09-14 00:00:00\", \"2026-09-14 23:59:59\"]")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] compulsoryInsuranceExpireTime;

    @Schema(description = "商业险到期时间", example = "[\"2026-09-14 00:00:00\", \"2026-09-14 23:59:59\"]")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] commercialInsuranceExpireTime;

    @Schema(description = "年检到期时间", example = "[\"2026-09-14 00:00:00\", \"2026-09-14 23:59:59\"]")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] inspectionExpireTime;

}
