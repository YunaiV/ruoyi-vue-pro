package cn.iocoder.yudao.module.mp.controller.admin.newstemplate.vo;

import lombok.*;

import java.util.*;

import io.swagger.annotations.*;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@ApiModel(value = "管理后台 - 图文消息模板 Excel 导出 Request VO", description = "参数和 WxNewsTemplatePageReqVO 是一致的")
@Data
public class WxNewsTemplateExportReqVO {

    @ApiModelProperty(value = "模板名称")
    private String tplName;

    @ApiModelProperty(value = "是否已上传微信")
    private String isUpload;

    @ApiModelProperty(value = "素材ID")
    private String mediaId;

    @ApiModelProperty(value = "微信账号ID")
    private String wxAccountId;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "开始创建时间")
    private Date beginCreateTime;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "结束创建时间")
    private Date endCreateTime;

}
