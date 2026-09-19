package cn.iocoder.yudao.module.oa.controller.admin.contact.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - OA 联系人共享 Request VO")
@Data
public class OaContactShareReqVO {

    @Schema(description = "联系人编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "联系人编号不能为空")
    private Long contactId;

    @Schema(description = "共享接收人用户编号列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1, 2]")
    @NotEmpty(message = "共享接收人不能为空")
    private List<@NotNull(message = "共享接收人编号不能为空") Long> userIds;

}
