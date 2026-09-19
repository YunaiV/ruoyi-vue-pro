package cn.iocoder.yudao.module.oa.controller.admin.contact.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - OA 联系人共享处理 Request VO")
@Data
public class OaContactHandleReqVO {

    @Schema(description = "联系人编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "联系人编号不能为空")
    private Long contactId;

    @Schema(description = "接收人分类编号", example = "1024")
    private Long categoryId;

}
