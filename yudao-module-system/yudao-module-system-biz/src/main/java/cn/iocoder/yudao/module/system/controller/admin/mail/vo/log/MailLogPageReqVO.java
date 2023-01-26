package cn.iocoder.yudao.module.system.controller.admin.mail.vo.log;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@ApiModel("管理后台 - 邮箱日志分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MailLogPageReqVO extends PageParam {

    @ApiModelProperty(value = "用户编号", example = "30883")
    private Long userId;

    @ApiModelProperty(value = "用户类型", example = "2", notes = "参见 UserTypeEnum 枚举")
    private Byte userType;

    @ApiModelProperty(value = "接收邮箱地址", example = "76854@qq.com", notes = "模糊匹配")
    private String toMail;

    @ApiModelProperty(value = "邮箱账号编号", example = "18107")
    private Long accountId;

    @ApiModelProperty(value = "模板编号", example = "5678")
    private Long templateId;

    @ApiModelProperty(value = "发送状态", example = "1", notes = "参见 MailSendStatusEnum 枚举")
    private Integer sendStatus;

    @ApiModelProperty(value = "发送时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] sendTime;

}
