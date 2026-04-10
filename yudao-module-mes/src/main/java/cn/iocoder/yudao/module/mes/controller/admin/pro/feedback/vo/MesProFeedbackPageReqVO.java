package cn.iocoder.yudao.module.mes.controller.admin.pro.feedback.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - MES 生产报工分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesProFeedbackPageReqVO extends PageParam {

    @Schema(description = "报工单编号", example = "FB202503")
    private String code;

    @Schema(description = "报工类型", example = "1")
    private Integer type;

    @Schema(description = "生产工单编号", example = "1")
    private Long workOrderId;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "报工时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] feedbackTime;

    @Schema(description = "产品物料编号", example = "100")
    private Long itemId;

    @Schema(description = "报工人编号", example = "1")
    private Long feedbackUserId;

    @Schema(description = "记录人编号（创建人）", example = "1")
    private String creator;

}
