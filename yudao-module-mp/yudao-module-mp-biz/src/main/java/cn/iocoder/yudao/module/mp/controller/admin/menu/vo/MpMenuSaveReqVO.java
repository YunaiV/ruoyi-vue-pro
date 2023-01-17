package cn.iocoder.yudao.module.mp.controller.admin.menu.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@ApiModel("管理后台 - 公众号菜单保存 Request VO")
@Data
public class MpMenuSaveReqVO {

    @ApiModelProperty(value = "公众号账号的编号", required = true, example = "2048")
    @NotNull(message = "公众号账号的编号不能为空")
    private Long accountId;

    @NotEmpty(message = "菜单不能为空")
    @Valid
    private List<Menu> menus;

    @ApiModel("管理后台 - 公众号菜单保存时的每个菜单")
    @Data
    public static class Menu extends MpMenuBaseVO {

        /**
         * 子菜单数组
         */
        private List<Menu> children;

    }

}
