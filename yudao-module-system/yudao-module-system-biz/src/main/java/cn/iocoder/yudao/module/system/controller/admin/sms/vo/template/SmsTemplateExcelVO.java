package cn.iocoder.yudao.module.system.controller.admin.sms.vo.template;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.system.enums.DictTypeConstants;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 短信模板 Excel VO
 *
 * @author 芋道源码
 */
@Data
public class SmsTemplateExcelVO {

    @ExcelProperty("编号")
    private Long id;

    @ExcelProperty(value = "短信签名", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.SMS_TEMPLATE_TYPE)
    private Integer type;

    @ExcelProperty(value = "开启状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.COMMON_STATUS)
    private Integer status;

    @ExcelProperty("模板编码")
    private String code;

    @ExcelProperty("模板名称")
    private String name;

    @ExcelProperty("模板内容")
    private String content;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("短信 API 的模板编号")
    private String apiTemplateId;

    @ExcelProperty("短信渠道编号")
    private Long channelId;

    @ExcelProperty(value = "短信渠道编码", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.SMS_CHANNEL_CODE)
    private String channelCode;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
