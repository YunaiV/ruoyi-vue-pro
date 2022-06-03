package cn.iocoder.yudao.module.mp.controller.admin.mediaupload.vo;

import lombok.*;

import java.util.*;

import io.swagger.annotations.*;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@ApiModel(value = "管理后台 - 微信素材上传表  Excel 导出 Request VO", description = "参数和 WxMediaUploadPageReqVO 是一致的")
@Data
public class WxMediaUploadExportReqVO {

    @ApiModelProperty(value = "类型")
    private String type;

    @ApiModelProperty(value = "图片URL")
    private String url;

    @ApiModelProperty(value = "素材ID")
    private String mediaId;

    @ApiModelProperty(value = "缩略图素材ID")
    private String thumbMediaId;

    @ApiModelProperty(value = "微信账号ID")
    private String wxAccountId;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "开始创建时间")
    private Date beginCreateTime;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "结束创建时间")
    private Date endCreateTime;

}
