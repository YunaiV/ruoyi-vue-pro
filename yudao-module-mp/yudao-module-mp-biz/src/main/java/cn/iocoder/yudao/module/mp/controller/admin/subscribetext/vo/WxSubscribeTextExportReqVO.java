package cn.iocoder.yudao.module.mp.controller.admin.subscribetext.vo;

import lombok.*;

import java.util.*;

import io.swagger.annotations.*;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@ApiModel(value = "管理后台 - 关注欢迎语 Excel 导出 Request VO", description = "参数和 WxSubscribeTextPageReqVO 是一致的")
@Data
public class WxSubscribeTextExportReqVO {

    @ApiModelProperty(value = "消息类型 1文本消息；2图文消息；")
    private String msgType;

    @ApiModelProperty(value = "模板ID")
    private String tplId;

    @ApiModelProperty(value = "微信账号ID")
    private String wxAccountId;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "开始创建时间")
    private Date beginCreateTime;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "结束创建时间")
    private Date endCreateTime;

}
