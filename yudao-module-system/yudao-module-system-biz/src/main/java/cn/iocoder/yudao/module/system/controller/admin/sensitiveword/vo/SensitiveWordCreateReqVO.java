package cn.iocoder.yudao.module.system.controller.admin.sensitiveword.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ApiModel("管理后台 - 敏感词创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SensitiveWordCreateReqVO extends SensitiveWordBaseVO {

}
