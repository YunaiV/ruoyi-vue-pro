package cn.iocoder.yudao.module.crm.controller.admin.businessstatustype.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

// TODO @lilleo：这个暂时不需要；嘿嘿~不是每个模块都需要导出哈
@Schema(description = "管理后台 - 商机状态类型 Excel 导出 Request VO，参数和 CrmBusinessStatusTypePageReqVO 是一致的")
@Data
public class CrmBusinessStatusTypeExportReqVO {

    @Schema(description = "状态类型名", example = "芋艿")
    private String name;

    @Schema(description = "使用的部门编号")
    private String deptIds;

    @Schema(description = "开启状态", example = "1")
    private Boolean status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
