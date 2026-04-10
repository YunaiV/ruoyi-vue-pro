package cn.iocoder.yudao.module.mes.controller.admin.wm.barcode.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - MES 条码清单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesWmBarcodePageReqVO extends PageParam {

    @Schema(description = "条码配置编号", example = "1")
    private Long configId;

    @Schema(description = "条码格式", example = "1")
    private Integer format;

    @Schema(description = "业务类型", example = "102")
    private Integer bizType;

    @Schema(description = "条码内容", example = "WH-WH001")
    private String content;

    @Schema(description = "业务编号", example = "1024")
    private Long bizId;

    @Schema(description = "业务编码", example = "WH001")
    private String bizCode;

    @Schema(description = "业务名称", example = "原料仓")
    private String bizName;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
