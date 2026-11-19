package cn.iocoder.yudao.module.ai.controller.admin.skill.vo.file;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - AI 技能文件保存内容 Request VO")
@Data
public class AiSkillFileContentReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "编号不能为空")
    private Long id;

    @Schema(description = "文件内容，允许为空字符串；大小由服务端按 UTF-8 字节校验", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "# 订单统计")
    @NotNull(message = "文件内容不能为空")
    private String content;

}
