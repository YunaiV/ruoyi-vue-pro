package cn.iocoder.yudao.module.crm.controller.admin.contact.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - CRM 联系人商机 Request VO") // 【联系人关联商机】用于关联，取消关联的操作
@Data
public class CrmContactBusinessReqVO {

    @Schema(description = "联系人编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "20878")
    @NotNull(message="联系人不能为空")
    private Long contactId;

    @Schema(description = "商机编号数组", requiredMode = Schema.RequiredMode.REQUIRED, example = "7638")
    @NotEmpty(message="商机不能为空")
    private List<Long> businessIds;

}