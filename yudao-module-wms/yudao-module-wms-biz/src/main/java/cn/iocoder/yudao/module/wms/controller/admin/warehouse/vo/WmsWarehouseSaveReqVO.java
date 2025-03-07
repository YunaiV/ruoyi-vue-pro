package cn.iocoder.yudao.module.wms.controller.admin.warehouse.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - 仓库新增/修改 Request VO")
@Data
public class WmsWarehouseSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "30946")
    private Long id;

    @Schema(description = "属性/模式 : 0-自营;1-三方;2-平台；", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "属性/模式 : 0-自营;1-三方;2-平台；不能为空")
    private Integer mode;

    @Schema(description = "代码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "代码不能为空")
    private String code;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @NotEmpty(message = "名称不能为空")
    private String name;

    @Schema(description = "外部存储ID", example = "22814")
    private Long externalStorageId;

    @Schema(description = "外部存储代码")
    private String externalStorageCode;

    @Schema(description = "公司名称", example = "张三")
    private String companyName;

    @Schema(description = "国家")
    private String country;

    @Schema(description = "省/州")
    private String province;

    @Schema(description = "市")
    private String city;

    @Schema(description = "详细地址1")
    private String addressLine1;

    @Schema(description = "详细地址2")
    private String addressLine2;

    @Schema(description = "邮编")
    private String postcode;

    @Schema(description = "联系人")
    private String contactPerson;

    @Schema(description = "联系的话")
    private String contactPhone;

    @Schema(description = "库存同步：0-关闭；1-开启；")
    private Integer isSync;

}