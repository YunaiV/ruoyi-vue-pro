package cn.iocoder.yudao.module.crm.controller.admin.contact.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "管理后台 - CRM 联系人商机 Request VO") // 【商机关联联系人】用于关联，取消关联的操作
@Data
public class CrmContactBusiness2ReqVO {

    @Schema(description = "商机编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "7638")
    @NotNull(message="商机不能为空")
    private Long businessId;

    @Schema(description = "联系人编号数组", requiredMode = Schema.RequiredMode.REQUIRED, example = "20878")
    @NotEmpty(message="联系人数组不能为空")
    private List<Long> contactIds;

}