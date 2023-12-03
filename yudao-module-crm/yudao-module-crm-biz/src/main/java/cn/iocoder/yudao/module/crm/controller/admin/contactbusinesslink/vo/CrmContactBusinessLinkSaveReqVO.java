package cn.iocoder.yudao.module.crm.controller.admin.contactbusinesslink.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;
import java.util.*;

@Schema(description = "管理后台 - 联系人商机关联新增/修改 Request VO")
@Data
public class CrmContactBusinessLinkSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "17220")
    private Long id;

    @Schema(description = "联系人id", example = "20878")
    @NotNull(message="联系人不能为空")
    private Long contactId;

    @Schema(description = "商机id", example = "7638")
    @NotNull(message="商机不能为空")
    private Long businessId;

}