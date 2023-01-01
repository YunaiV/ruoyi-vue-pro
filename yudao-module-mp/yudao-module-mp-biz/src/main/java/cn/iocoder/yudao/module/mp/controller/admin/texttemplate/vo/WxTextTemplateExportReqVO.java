package cn.iocoder.yudao.module.mp.controller.admin.texttemplate.vo;

import lombok.*;

import java.util.*;

import io.swagger.annotations.*;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@ApiModel(value = "管理后台 - 文本模板 Excel 导出 Request VO", description = "参数和 WxTextTemplatePageReqVO 是一致的")
@Data
public class WxTextTemplateExportReqVO {

    @ApiModelProperty(value = "模板名字")
    private String tplName;

    @ApiModelProperty(value = "模板内容")
    private String content;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "开始创建时间")
    private Date beginCreateTime;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "结束创建时间")
    private Date endCreateTime;

}
