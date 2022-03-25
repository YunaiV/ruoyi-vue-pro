package cn.iocoder.yudao.module.system.controller.admin.mail.vo.log;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.sql.Timestamp;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Data
public class MailLogExcelVO {

    @ExcelProperty(value = "邮箱" )
    private String from;

    @ExcelProperty(value = "模版编号" )
    private String templeCode;

    @ExcelProperty(value = "标题")
    private String title;

    @ExcelProperty(value = "内容")
    private String content;

    @ExcelProperty(value = "收件人" )
    private String to;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ExcelProperty(value = "发送时间" )
    private Timestamp sendTime;

    @ExcelProperty(value = "发送状态")
    private Boolean sendStatus;

    @ExcelProperty(value = "发送结果")
    private String sendResult;

}
