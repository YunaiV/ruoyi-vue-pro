package cn.iocoder.yudao.module.market.controller.admin.banner.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author xia
 */
@ApiModel("管理后台 - Banner Response VO")
@Data
@ToString(callSuper = true)
public class BannerRespVO  extends BannerBaseVO {

    @ApiModelProperty(value = "banner编号", required = true)
    @NotNull(message = "banner编号不能为空")
    private Long id;

    @ApiModelProperty(value = "创建时间", required = true)
    private Date createTime;

}
