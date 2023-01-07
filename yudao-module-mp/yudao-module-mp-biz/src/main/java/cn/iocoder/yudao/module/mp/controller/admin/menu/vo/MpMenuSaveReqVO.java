package cn.iocoder.yudao.module.mp.controller.admin.menu.vo;

import lombok.*;
import io.swagger.annotations.*;
import me.chanjar.weixin.common.bean.menu.WxMenuButton;

import javax.validation.constraints.NotNull;
import java.util.List;

@ApiModel("管理后台 - 微信菜单保存 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MpMenuSaveReqVO extends MpMenuBaseVO {

    @NotNull(message = "按钮不能为空")
    private List<WxMenuButton> buttons;

}
