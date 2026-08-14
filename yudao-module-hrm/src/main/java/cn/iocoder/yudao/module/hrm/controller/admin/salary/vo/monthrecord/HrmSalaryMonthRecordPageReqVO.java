package cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.monthrecord;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.hrm.enums.salary.monthrecord.HrmSalaryMonthRecordStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - HRM 月度工资表分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class HrmSalaryMonthRecordPageReqVO extends PageParam {

    @Schema(description = "年份", example = "2026")
    private Integer year;

    @Schema(description = "月份", example = "7")
    private Integer month;

    @Schema(description = "核算状态", example = "1")
    @InEnum(value = HrmSalaryMonthRecordStatusEnum.class, message = "月度工资表状态必须是 {value}")
    private Integer status;

}
