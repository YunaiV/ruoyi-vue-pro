package cn.iocoder.yudao.module.mp.controller.admin.fanstag.vo;

import lombok.*;

import java.util.*;

import io.swagger.annotations.*;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@ApiModel(value = "管理后台 - 粉丝标签 Excel 导出 Request VO", description = "参数和 WxFansTagPageReqVO 是一致的")
@Data
public class FansTagExportReqVO {

    @ApiModelProperty(value = "标签名称")
    private String name;

    @ApiModelProperty(value = "粉丝数量")
    private Integer count;

    @ApiModelProperty(value = "微信账号ID")
    private String wxAccountId;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "开始创建时间")
    private Date beginCreateTime;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "结束创建时间")
    private Date endCreateTime;

}
