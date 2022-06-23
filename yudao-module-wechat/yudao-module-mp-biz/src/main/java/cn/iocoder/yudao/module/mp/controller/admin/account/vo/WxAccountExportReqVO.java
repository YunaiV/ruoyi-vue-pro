package cn.iocoder.yudao.module.mp.controller.admin.account.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @author fengdan
 */
@ApiModel(value = "管理后台 - 公众号账户 Excel 导出 Request VO", description = "参数和 WxAccountPageReqVO 是一致的")
@Data
public class WxAccountExportReqVO {

    @ApiModelProperty(value = "公众号名称")
    private String name;

    @ApiModelProperty(value = "公众号账户")
    private String account;

    @ApiModelProperty(value = "公众号appid")
    private String appId;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "开始创建时间")
    private Date beginCreateTime;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "结束创建时间")
    private Date endCreateTime;

}
