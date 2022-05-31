package cn.iocoder.yudao.module.market.controller.admin.banner.vo;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Copyright: 南京淘点网络科技有限公司
 * @author: wangxia
 */
@Data
public class BannerUpdateStatusReqVO {

    @ApiModelProperty(value = "banner编号", required = true)
    @NotNull(message = "banner编号不能为空")
    private Long id;

    @ApiModelProperty(value = "状态", required = true)
    @NotNull(message = "状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;
}
