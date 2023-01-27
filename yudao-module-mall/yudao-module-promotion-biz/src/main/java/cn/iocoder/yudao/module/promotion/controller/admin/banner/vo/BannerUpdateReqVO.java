package cn.iocoder.yudao.module.promotion.controller.admin.banner.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

/**
 * @author xia
 */
@ApiModel("管理后台 - Banner更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BannerUpdateReqVO extends BannerBaseVO {

    @ApiModelProperty(value = "banner 编号", required = true)
    @NotNull(message = "banner 编号不能为空")
    private Long id;

}
