package cn.iocoder.yudao.module.mes.controller.admin.md.workstation.vo.workshop;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 车间 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MesMdWorkshopRespVO {

    @Schema(description = "车间编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("车间编号")
    private Long id;

    @Schema(description = "车间编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "WS001")
    @ExcelProperty("车间编码")
    private String code;

    @Schema(description = "车间名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "一号车间")
    @ExcelProperty("车间名称")
    private String name;

    @Schema(description = "面积", example = "1000.00")
    @ExcelProperty("面积")
    private BigDecimal area;

    @Schema(description = "负责人用户编号", example = "1")
    private Long chargeUserId;

    @Schema(description = "负责人名称", example = "张三")
    @ExcelProperty("负责人")
    private String chargeUserName;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("状态")
    private Integer status;

    @Schema(description = "备注", example = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
