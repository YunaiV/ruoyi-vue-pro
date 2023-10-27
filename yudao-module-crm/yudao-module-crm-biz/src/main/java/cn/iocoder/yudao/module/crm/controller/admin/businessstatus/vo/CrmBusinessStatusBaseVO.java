package cn.iocoder.yudao.module.crm.controller.admin.businessstatus.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 商机状态 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class CrmBusinessStatusBaseVO {

    // TODO @lilleo：example 要写下

    @Schema(description = "状态类型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "22882")
    @NotNull(message = "状态类型编号不能为空")
    private Long typeId;

    @Schema(description = "状态名", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @NotNull(message = "状态名不能为空")
    private String name;

    // TODO @lilleo：percent 应该是 Integer；
    @Schema(description = "赢单率")
    private String percent;

    // TODO @lilleo：这个是不是不用前端新增和修改的时候传递，交给顺序计算出来，存储起来就好了；
    @Schema(description = "排序")
    private Integer sort;

}
