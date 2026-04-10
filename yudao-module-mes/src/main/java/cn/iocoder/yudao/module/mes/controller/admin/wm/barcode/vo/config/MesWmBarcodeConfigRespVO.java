package cn.iocoder.yudao.module.mes.controller.admin.wm.barcode.vo.config;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 条码配置 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MesWmBarcodeConfigRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "条码格式", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("条码格式")
    private Integer format;

    @Schema(description = "业务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "102")
    @ExcelProperty("业务类型")
    private Integer bizType;

    @Schema(description = "内容格式模板", requiredMode = Schema.RequiredMode.REQUIRED, example = "WH-{BUSINESSCODE}")
    @ExcelProperty("内容格式模板")
    private String contentFormat;

    @Schema(description = "内容样例", example = "WH-WH001")
    @ExcelProperty("内容样例")
    private String contentExample;

    @Schema(description = "是否自动生成", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @ExcelProperty("是否自动生成")
    private Boolean autoGenerateFlag;

    @Schema(description = "默认打印模板", example = "template1")
    @ExcelProperty("默认打印模板")
    private String defaultTemplate;

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
