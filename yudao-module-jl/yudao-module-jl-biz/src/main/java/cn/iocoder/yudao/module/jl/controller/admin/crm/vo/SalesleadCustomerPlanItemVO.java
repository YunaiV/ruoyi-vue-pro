package cn.iocoder.yudao.module.jl.controller.admin.crm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 销售线索中的客户方案 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class SalesleadCustomerPlanItemVO {

    @Schema(description = "线索 id", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "25040")
    @NotNull(message = "线索 id不能为空")
    private Long salesleadId;

    @Schema(description = "文件地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "")
    @NotNull(message = "文件地址不能为空")
    private String fileUrl;

    @Schema(description = "文件名字", requiredMode = Schema.RequiredMode.REQUIRED, example = "")
    @NotNull(message = "文件名字不能为空")
    private String fileName;

}
