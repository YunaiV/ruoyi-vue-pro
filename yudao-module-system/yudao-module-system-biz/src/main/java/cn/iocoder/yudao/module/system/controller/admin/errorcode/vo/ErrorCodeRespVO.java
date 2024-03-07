package cn.iocoder.yudao.module.system.controller.admin.errorcode.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.system.enums.DictTypeConstants;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 错误码 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErrorCodeRespVO {

    @Schema(description = "错误码编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("错误码编号")
    private Long id;

    @Schema(description = "错误码类型，参见 ErrorCodeTypeEnum 枚举类", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty(value = "错误码类型", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.ERROR_CODE_TYPE)
    private Integer type;

    @Schema(description = "应用名", requiredMode = Schema.RequiredMode.REQUIRED, example = "dashboard")
    @ExcelProperty("应用名")
    private String applicationName;

    @Schema(description = "错误码编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "1234")
    @ExcelProperty("错误码编码")
    private Integer code;

    @Schema(description = "错误码错误提示", requiredMode = Schema.RequiredMode.REQUIRED, example = "帅气")
    @ExcelProperty("错误码错误提示")
    private String message;

    @Schema(description = "备注", example = "哈哈哈")
    @ExcelProperty("备注")
    private String memo;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
