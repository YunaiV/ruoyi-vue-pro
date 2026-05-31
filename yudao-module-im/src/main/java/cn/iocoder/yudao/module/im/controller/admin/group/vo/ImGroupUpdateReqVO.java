package cn.iocoder.yudao.module.im.controller.admin.group.vo;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Schema(description = "管理后台 - 群更新 Request VO")
@Data
public class ImGroupUpdateReqVO {

    @Schema(description = "群编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1003")
    @NotNull(message = "群编号不能为空")
    private Long id;

    @Schema(description = "群名称", example = "芋道技术交流群")
    @Size(max = 64, message = "群名称长度不能超过 64")
    private String name;

    @Schema(description = "群头像")
    @Size(max = 512, message = "群头像长度不能超过 512")
    private String avatar;

    @Schema(description = "群公告")
    @Size(max = 2048, message = "群公告长度不能超过 2048")
    private String notice;

    @Schema(description = "进群是否需群主 / 管理员审批", example = "true")
    private Boolean joinApproval;

    @AssertTrue(message = "群名称不能为空")
    @JsonIgnore
    public boolean isNameValid() {
        return name == null || StrUtil.isNotBlank(name);
    }

    @AssertTrue(message = "群头像不能为空")
    @JsonIgnore
    public boolean isAvatarValid() {
        return avatar == null || StrUtil.isNotBlank(avatar);
    }

}
