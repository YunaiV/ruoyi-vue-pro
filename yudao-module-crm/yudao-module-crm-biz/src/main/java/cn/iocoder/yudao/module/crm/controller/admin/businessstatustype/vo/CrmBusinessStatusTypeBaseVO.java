package cn.iocoder.yudao.module.crm.controller.admin.businessstatustype.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 商机状态类型 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class CrmBusinessStatusTypeBaseVO {

    @Schema(description = "状态类型名", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @NotNull(message = "状态类型名不能为空")
    private String name;

    @Schema(description = "使用的部门编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "使用的部门编号不能为空")
    private String deptIds;

    @Schema(description = "开启状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "开启状态不能为空")
    private Boolean status;

}
