package cn.iocoder.yudao.module.crm.controller.admin.customer.vo.limitconfig;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 客户限制配置 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class CrmCustomerLimitConfigBaseVO {

    @Schema(description = "规则类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "规则类型不能为空")
    private Integer type;

    @Schema(description = "规则适用人群")
    private List<Long> userIds;

    @Schema(description = "规则适用部门")
    private List<Long> deptIds;

    @Schema(description = "数量上限", requiredMode = Schema.RequiredMode.REQUIRED, example = "28384")
    @NotNull(message = "数量上限不能为空")
    private Integer maxCount;

    @Schema(description = "成交客户是否占有拥有客户数(当 type = 1 时)")
    private Boolean dealCountEnabled;

}
