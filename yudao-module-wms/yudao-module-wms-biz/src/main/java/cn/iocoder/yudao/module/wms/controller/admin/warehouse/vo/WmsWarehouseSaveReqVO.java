package cn.iocoder.yudao.module.wms.controller.admin.warehouse.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.wms.enums.common.ValidStatus;
import cn.iocoder.yudao.module.wms.enums.warehouse.WarehouseMode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import jakarta.validation.constraints.*;

/**
 * @table-fields : country,code,contact_phone,city,contact_person,postcode,is_sync,mode,external_storage_id,address_line2,province,address_line1,company_name,name,id,status
 */
@Schema(description = "管理后台 - 仓库新增/修改 Request VO")
@Data
public class WmsWarehouseSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "30946")
    private Long id;

    @Schema(description = "仓库经营方式 ; WarehouseMode : 0-自营 , 1-三方仓 , 2-平台仓", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "仓库经营方式不能为空")
    @InEnum(WarehouseMode.class)
    private Integer mode;

    @Schema(description = "代码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "代码不能为空")
    private String code;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @NotEmpty(message = "名称不能为空")
    private String name;

    @Schema(description = "外部存储ID", example = "22814")
    private Long externalStorageId;

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

    @Schema(description = "状态，WMS通用的对象有效状态 ; ValidStatus : 0-不可用 , 1-可用", example = "")
    @InEnum(ValidStatus.class)
    private Integer status;
}
