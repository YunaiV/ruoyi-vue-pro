package cn.iocoder.yudao.module.system.controller.admin.sms.vo.log;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.framework.excel.core.convert.JsonConvert;
import cn.iocoder.yudao.module.system.enums.DictTypeConstants;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * 短信日志 Excel VO
 *
 * @author 芋道源码
 */
@Data
public class SmsLogExcelVO {

    @ExcelProperty("编号")
    private Long id;

    @ExcelProperty("短信渠道编号")
    private Long channelId;

    @ExcelProperty("短信渠道编码")
    private String channelCode;

    @ExcelProperty("模板编号")
    private Long templateId;

    @ExcelProperty("模板编码")
    private String templateCode;

    @ExcelProperty(value = "短信类型", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.SMS_TEMPLATE_TYPE)
    private Integer templateType;

    @ExcelProperty("短信内容")
    private String templateContent;

    @ExcelProperty(value = "短信参数", converter = JsonConvert.class)
    private Map<String, Object> templateParams;

    @ExcelProperty("短信 API 的模板编号")
    private String apiTemplateId;

    @ExcelProperty("手机号")
    private String mobile;

    @ExcelProperty("用户编号")
    private Long userId;

    @ExcelProperty(value = "用户类型", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.USER_TYPE)
    private Integer userType;

    @ExcelProperty(value = "发送状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.SMS_SEND_STATUS)
    private Integer sendStatus;

    @ExcelProperty("发送时间")
    private Date sendTime;

    @ExcelProperty("发送结果的编码")
    private Integer sendCode;

    @ExcelProperty("发送结果的提示")
    private String sendMsg;

    @ExcelProperty("短信 API 发送结果的编码")
    private String apiSendCode;

    @ExcelProperty("短信 API 发送失败的提示")
    private String apiSendMsg;

    @ExcelProperty("短信 API 发送返回的唯一请求 ID")
    private String apiRequestId;

    @ExcelProperty("短信 API 发送返回的序号")
    private String apiSerialNo;

    @ExcelProperty(value = "接收状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.SMS_RECEIVE_STATUS)
    private Integer receiveStatus;

    @ExcelProperty("接收时间")
    private Date receiveTime;

    @ExcelProperty("API 接收结果的编码")
    private String apiReceiveCode;

    @ExcelProperty("API 接收结果的说明")
    private String apiReceiveMsg;

    @ExcelProperty("创建时间")
    private Date createTime;

}
