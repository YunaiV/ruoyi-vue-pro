package cn.iocoder.yudao.module.system.controller.admin.sms.vo.template;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ApiModel("管理后台 - 短信模板创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SmsTemplateCreateReqVO extends SmsTemplateBaseVO {

}
