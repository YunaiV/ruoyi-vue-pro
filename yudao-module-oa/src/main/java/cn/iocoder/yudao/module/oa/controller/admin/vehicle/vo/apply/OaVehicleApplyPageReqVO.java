package cn.iocoder.yudao.module.oa.controller.admin.vehicle.vo.apply;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.oa.enums.vehicle.OaVehicleReturnStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 用车申请分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class OaVehicleApplyPageReqVO extends PageParam {

    @Schema(description = "申请单号", example = "YC202609140001")
    private String no;

    @Schema(description = "车牌号", example = "沪A12345")
    private String vehicleNo;

    @Schema(description = "审批状态", example = "-1")
    @InEnum(value = BpmProcessInstanceStatusEnum.class, message = "审批状态必须是 {value}")
    private Integer status;

    @Schema(description = "还车状态", example = "0")
    @InEnum(value = OaVehicleReturnStatusEnum.class, message = "还车状态必须是 {value}")
    private Integer returnStatus;

    @Schema(description = "申请部门编号", example = "1024")
    private Long deptId;

    @Schema(description = "创建时间", example = "[\"2026-09-14 00:00:00\", \"2026-09-14 23:59:59\"]")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
