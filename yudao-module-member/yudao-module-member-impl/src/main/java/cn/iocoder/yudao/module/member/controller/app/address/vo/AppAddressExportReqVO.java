package cn.iocoder.yudao.module.member.controller.app.address.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@ApiModel(value = "用户 APP - 用户收件地址 Excel 导出 Request VO", description = "参数和 AddressPageReqVO 是一致的")
@Data
public class AppAddressExportReqVO {

    @ApiModelProperty(value = "用户编号")
    private Long userId;

    @ApiModelProperty(value = "收件人名称")
    private String name;

    @ApiModelProperty(value = "手机号")
    private String mobile;

    @ApiModelProperty(value = "地区编码")
    private Integer areaCode;

    @ApiModelProperty(value = "收件详细地址")
    private String detailAddress;

    @ApiModelProperty(value = "地址类型")
    private Integer type;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "开始创建时间")
    private Date beginCreateTime;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "结束创建时间")
    private Date endCreateTime;

}
