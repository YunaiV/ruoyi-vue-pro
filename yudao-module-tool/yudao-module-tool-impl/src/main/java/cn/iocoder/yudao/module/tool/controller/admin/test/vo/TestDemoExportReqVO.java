package cn.iocoder.yudao.module.tool.controller.admin.test.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@ApiModel(value = "管理后台 - 字典类型 Excel 导出 Request VO", description = "参数和 TestDemoPageReqVO 是一致的")
@Data
public class TestDemoExportReqVO {

    @ApiModelProperty(value = "名字", example = "芋道")
    private String name;

    @ApiModelProperty(value = "状态", example = "1")
    private Integer status;

    @ApiModelProperty(value = "类型", example = "2")
    private Integer type;

    @ApiModelProperty(value = "分类", example = "3")
    private Integer category;

    @ApiModelProperty(value = "备注", example = "我是备注")
    private String remark;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "开始创建时间")
    private Date beginCreateTime;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "结束创建时间")
    private Date endCreateTime;

}
