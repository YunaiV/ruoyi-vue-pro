package cn.iocoder.yudao.module.member.controller.admin.address.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;
import jakarta.validation.constraints.*;

/**
 * 用户收件地址 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class AddressBaseVO {

    @Schema(description = "收件人名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotNull(message = "收件人名称不能为空")
    private String name;

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "手机号不能为空")
    private String mobile;

    @Schema(description = "地区编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "15716")
    @NotNull(message = "地区编码不能为空")
    private Long areaId;

    @Schema(description = "收件详细地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "收件详细地址不能为空")
    private String detailAddress;

    @Schema(description = "是否默认", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "是否默认不能为空")
    private Boolean defaultStatus;

}
