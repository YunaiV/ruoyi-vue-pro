package cn.iocoder.yudao.module.system.controller.admin.permission.vo.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(title = "管理后台 - 菜单列表 Request VO")
@Data
public class MenuListReqVO {

    @Schema(title = "菜单名称", example = "芋道", description = "模糊匹配")
    private String name;

    @Schema(title = "展示状态", example = "1", description = "参见 CommonStatusEnum 枚举类")
    private Integer status;

}
