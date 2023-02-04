package cn.iocoder.yudao.module.system.controller.admin.permission.vo.role;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 角色更新状态 Request VO")
@Data
public class RoleUpdateStatusReqVO {

    @Schema(description = "角色编号", required = true, example = "1024")
    @NotNull(message = "角色编号不能为空")
    private Long id;

    @Schema(description = "状态,见 CommonStatusEnum 枚举", required = true, example = "1")
    @NotNull(message = "状态不能为空")
    @InEnum(value = CommonStatusEnum.class, message = "修改状态必须是 {value}")
    private Integer status;

}
