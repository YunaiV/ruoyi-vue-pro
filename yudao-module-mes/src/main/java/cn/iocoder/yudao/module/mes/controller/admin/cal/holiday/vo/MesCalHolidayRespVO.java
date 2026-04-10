package cn.iocoder.yudao.module.mes.controller.admin.cal.holiday.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 假期设置 Response VO")
@Data
public class MesCalHolidayRespVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "日期")
    private LocalDateTime day;

    @Schema(description = "日期类型")
    private Integer type;

    @Schema(description = "备注")
    private String remark;

}
