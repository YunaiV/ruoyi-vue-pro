package cn.iocoder.yudao.module.system.controller.admin.errorcode.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ApiModel("管理后台 - 错误码创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErrorCodeCreateReqVO extends ErrorCodeBaseVO {

}
