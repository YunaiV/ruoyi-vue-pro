package cn.iocoder.yudao.module.iot.controller.admin.product.vo.script;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT 产品脚本信息 Response VO")
@Data
@ExcelIgnoreUnannotated
public class IotProductScriptRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "26565")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "28277")
    @ExcelProperty("产品ID")
    private Long productId;

    @Schema(description = "产品唯一标识符", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("产品唯一标识符")
    private String productKey;

    @Schema(description = "脚本类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("脚本类型")
    private Integer scriptType;

    @Schema(description = "脚本内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("脚本内容")
    private String scriptContent;

    @Schema(description = "脚本语言", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("脚本语言")
    private String scriptLanguage;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("状态")
    private Integer status;

    @Schema(description = "备注说明", example = "你说的对")
    @ExcelProperty("备注说明")
    private String remark;

    @Schema(description = "最后测试时间")
    @ExcelProperty("最后测试时间")
    private LocalDateTime lastTestTime;

    @Schema(description = "最后测试结果(0=失败 1=成功)")
    @ExcelProperty("最后测试结果(0=失败 1=成功)")
    private Integer lastTestResult;

    @Schema(description = "脚本版本号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("脚本版本号")
    private Integer version;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}