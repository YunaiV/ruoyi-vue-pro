package cn.iocoder.yudao.module.system.controller.admin.auth.vo.client;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ApiModel("管理后台 - OAuth2 客户端更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OAuth2ClientUpdateReqVO extends OAuth2ClientBaseVO {

}
