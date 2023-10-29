package cn.iocoder.yudao.module.crm.controller.admin.business.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.Set;

@Schema(description = "管理后台 - 商机更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CrmBusinessUpdateReqVO extends CrmBusinessBaseVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "32129")
    @NotNull(message = "主键不能为空")
    private Long id;

    @Schema(description = "只读权限的用户编号数组")
    private Set<Long> roUserIds;

    @Schema(description = "读写权限的用户编号数组")
    private Set<Long> rwUserIds;


    // TODO @ljileo：修改的时候，应该可以传递添加的产品；

}
