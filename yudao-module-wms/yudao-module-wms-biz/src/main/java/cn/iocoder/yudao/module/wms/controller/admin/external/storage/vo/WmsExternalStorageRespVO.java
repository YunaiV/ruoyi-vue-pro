package cn.iocoder.yudao.module.wms.controller.admin.external.storage.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

@Schema(description = "管理后台 - 外部存储库 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsExternalStorageRespVO {

    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED, example = "21397")
    @ExcelProperty("id")
    private Long id;

    @Schema(description = "代码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("代码")
    private String code;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @ExcelProperty("名称")
    private String name;

    @Schema(description = "外部仓类型:1-三方,2-平台；", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("外部仓类型:1-三方,2-平台；")
    private Integer type;

    @Schema(description = "JSON格式的对接需要的参数")
    @ExcelProperty("JSON格式的对接需要的参数")
    private String apiParameters;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}