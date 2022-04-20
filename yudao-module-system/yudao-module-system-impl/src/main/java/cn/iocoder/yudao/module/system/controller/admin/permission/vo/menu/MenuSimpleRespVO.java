package cn.iocoder.yudao.module.system.controller.admin.permission.vo.menu;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@ApiModel("管理后台 - 菜单精简信息 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuSimpleRespVO {

    @ApiModelProperty(value = "菜单编号", required = true, example = "1024")
    private Long id;

    @ApiModelProperty(value = "菜单名称", required = true, example = "芋道")
    private String name;

    @ApiModelProperty(value = "父菜单 ID", required = true, example = "1024")
    private Long parentId;

    @ApiModelProperty(value = "类型", required = true, example = "1", notes = "参见 MenuTypeEnum 枚举类")
    private Integer type;

}
