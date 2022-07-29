package cn.iocoder.yudao.module.pay.controller.admin.merchant.vo.merchant;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@ApiModel("管理后台 - 支付商户信息分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayMerchantPageReqVO extends PageParam {

    @ApiModelProperty(value = "商户号")
    private String no;

    @ApiModelProperty(value = "商户全称")
    private String name;

    @ApiModelProperty(value = "商户简称")
    private String shortName;

    @ApiModelProperty(value = "开启状态")
    private Integer status;

    @ApiModelProperty(value = "备注")
    private String remark;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "创建时间")
    private Date[] createTime;

}
