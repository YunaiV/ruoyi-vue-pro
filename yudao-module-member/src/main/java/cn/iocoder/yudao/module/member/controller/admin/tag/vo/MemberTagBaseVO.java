package cn.iocoder.yudao.module.member.controller.admin.tag.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 会员标签 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class MemberTagBaseVO {

    @Schema(description = "标签名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @NotNull(message = "标签名称不能为空")
    private String name;

}
