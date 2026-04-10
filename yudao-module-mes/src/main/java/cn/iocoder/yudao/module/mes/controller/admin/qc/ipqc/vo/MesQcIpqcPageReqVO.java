package cn.iocoder.yudao.module.mes.controller.admin.qc.ipqc.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - MES 过程检验单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesQcIpqcPageReqVO extends PageParam {

    @Schema(description = "检验单编号", example = "IPQC2025")
    private String code;

    @Schema(description = "IPQC 检验类型", example = "1")
    private Integer type;

    @Schema(description = "生产工单 ID", example = "10")
    private Long workOrderId;

    @Schema(description = "产品物料 ID", example = "50")
    private Long itemId;

    @Schema(description = "检测结果", example = "1")
    private Integer checkResult;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "检测人员用户 ID", example = "1")
    private Long inspectorUserId;

}
