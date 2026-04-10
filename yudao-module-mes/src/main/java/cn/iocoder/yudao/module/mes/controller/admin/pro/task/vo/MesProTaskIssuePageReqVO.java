package cn.iocoder.yudao.module.mes.controller.admin.pro.task.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - MES 生产任务投料分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesProTaskIssuePageReqVO extends PageParam {

    @Schema(description = "生产任务编号", example = "1")
    private Long taskId;

    @Schema(description = "生产工单编号", example = "1")
    private Long workOrderId;

    @Schema(description = "产品物料编号", example = "100")
    private Long itemId;

    @Schema(description = "来源单据类型", example = "MATERIAL_ISSUE")
    private String sourceDocType;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
