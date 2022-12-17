package cn.iocoder.yudao.module.system.controller.admin.notify.vo.log;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 站内信 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class NotifyLogBaseVO {

    @ApiModelProperty(value = "模版编码")
    private String templateCode;

    @ApiModelProperty(value = "标题")
    private String title;

    @ApiModelProperty(value = "内容", required = true)
    private String content;

    @ApiModelProperty(value = "发送时间", required = true)
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Date sendTime;

    @ApiModelProperty(value = "芋艿", required = true)
    private String receiveUserName;

    @ApiModelProperty(value = "1", required = true)
    private Long userId;

    @ApiModelProperty(value = "是否已读 false-未读  true-已读")
    private Boolean readStatus;

    @ApiModelProperty(value = "阅读时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Date readTime;

}
