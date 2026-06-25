package cn.iocoder.yudao.module.im.controller.admin.group.vo.request;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.im.enums.group.ImGroupAddSourceEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - IM 加群申请发起 Request VO")
@Data
public class ImGroupRequestApplyReqVO {

    @Schema(description = "群编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "群编号不能为空")
    private Long groupId;

    @Schema(description = "申请理由", example = "我是芋艿（一种食材）")
    @Size(max = 255, message = "申请理由最多 255 个字符")
    private String applyContent;

    @Schema(description = "加入来源", example = "1")
    @InEnum(ImGroupAddSourceEnum.class)
    private Integer addSource;

}
