package cn.iocoder.yudao.module.product.controller.admin.propertyvalue.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@ApiModel(value = "管理后台 - 规格值 Excel 导出 Request VO", description = "参数和 PropertyValuePageReqVO 是一致的")
@Data
public class PropertyValueExportReqVO {

    @ApiModelProperty(value = "规格键id")
    private Long propertyId;

    @ApiModelProperty(value = "规格值名字")
    private String name;

    @ApiModelProperty(value = "状态： 1 开启 ，2 禁用")
    private Integer status;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "开始创建时间")
    private Date beginCreateTime;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "结束创建时间")
    private Date endCreateTime;

}
