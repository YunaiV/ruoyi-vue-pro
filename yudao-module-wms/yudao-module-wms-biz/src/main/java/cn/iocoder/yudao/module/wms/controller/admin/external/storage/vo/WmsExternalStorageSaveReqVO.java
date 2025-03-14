package cn.iocoder.yudao.module.wms.controller.admin.external.storage.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.wms.enums.ExternalStorageType;
import cn.iocoder.yudao.module.wms.enums.ValidStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

/**
 * @table-fields : api_parameters,code,name,id,type,status
 */
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

    @Schema(description = "外部仓类型 ; ExternalStorageType : 1-三方仓 , 2-平台仓", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "外部仓类型不能为空")
    @InEnum(ExternalStorageType.class)
    private Integer type;

    @Schema(description = "对接参数，JSON格式的对接需要的参数")
    private String apiParameters;

    @Schema(description = "状态，WMS通用的对象有效状态 ; ValidStatus : 0-不可用 , 1-可用", example = "")
    @InEnum(ValidStatus.class)
    private Integer status;
}
