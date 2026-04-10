package cn.iocoder.yudao.module.mes.controller.admin.qc.rqc.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - MES 退货检验单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesQcRqcPageReqVO extends PageParam {

    @Schema(description = "检验单编号", example = "RQC2025")
    private String code;

    @Schema(description = "来源单据类型", example = "1")
    private Integer sourceDocType;

    @Schema(description = "来源单据编码", example = "RT2025")
    private String sourceDocCode;

    @Schema(description = "产品物料 ID", example = "20")
    private Long itemId;

    @Schema(description = "批次号", example = "BATCH001")
    private String batchCode;

    @Schema(description = "检测结果", example = "1")
    private Integer checkResult;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "检测人员用户 ID", example = "1")
    private Long inspectorUserId;

}
