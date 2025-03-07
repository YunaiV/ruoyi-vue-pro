package cn.iocoder.yudao.module.wms.controller.admin.external.storage.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - 外部存储库新增/修改 Request VO")
@Data
public class WmsExternalStorageSaveReqVO {

    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED, example = "21397")
    private Long id;

    @Schema(description = "代码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "代码不能为空")
    private String code;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @NotEmpty(message = "名称不能为空")
    private String name;

    @Schema(description = "外部仓类型:1-三方,2-平台；", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "外部仓类型:1-三方,2-平台；不能为空")
    private Integer type;

    @Schema(description = "JSON格式的对接需要的参数")
    private String apiParameters;

}