package cn.iocoder.yudao.module.mp.controller.admin.menu.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import me.chanjar.weixin.common.bean.menu.WxMenuButton;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 微信菜单 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class MpMenuBaseVO {

    @ApiModelProperty(value = "公众号账号的编号", required = true, example = "2048")
    @NotNull(message = "公众号账号的编号不能为空")
    private Long accountId;

}
