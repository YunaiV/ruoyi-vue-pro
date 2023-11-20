package cn.iocoder.yudao.module.report.controller.admin.ureport.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;
import java.util.*;

@Schema(description = "管理后台 - Ureport2报表新增/修改 Request VO")
@Data
public class UreportFileSaveReqVO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "9948")
    private Long id;

    @Schema(description = "文件名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @NotEmpty(message = "文件名称不能为空")
    private String fileName;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "文件内容")
    private String fileContent;

    @Schema(description = "备注", example = "随便")
    private String remark;

}
