package cn.iocoder.yudao.module.system.controller.admin.mail.vo.account;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ApiModel("管理后台 - 邮箱账号创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MailAccountCreateReqVO extends MailAccountBaseVO {

}
