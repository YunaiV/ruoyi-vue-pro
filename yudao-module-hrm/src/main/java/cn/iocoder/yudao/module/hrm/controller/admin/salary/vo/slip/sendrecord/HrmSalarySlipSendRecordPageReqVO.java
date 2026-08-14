package cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.slip.sendrecord;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - HRM 工资条发放记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class HrmSalarySlipSendRecordPageReqVO extends PageParam {

    @Schema(description = "年份", example = "2026")
    private Integer year;

    @Schema(description = "月份", example = "7")
    private Integer month;

}
