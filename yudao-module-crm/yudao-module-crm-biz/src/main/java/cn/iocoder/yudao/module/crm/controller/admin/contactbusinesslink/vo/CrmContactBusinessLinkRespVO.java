package cn.iocoder.yudao.module.crm.controller.admin.contactbusinesslink.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - CRM 联系人商机关联 Response VO")
@Data
public class CrmContactBusinessLinkRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "17220")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "联系人编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "20878")
    private Long contactId;

    @Schema(description = "商机编号", requiredMode =  Schema.RequiredMode.REQUIRED, example = "7638")
    private Long businessId;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}