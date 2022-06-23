package cn.iocoder.yudao.module.mp.controller.admin.fanstag.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;

/**
 * @author fengdan
 */
@ApiModel("管理后台 - 粉丝标签分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FansTagPageReqVO extends PageParam {

    @NotEmpty(message = "公众号appId不能为空")
    @ApiModelProperty("微信公众号appId")
    private String appId;

}
