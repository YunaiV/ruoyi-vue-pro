package cn.iocoder.yudao.adminserver.modules.system.controller.logger.vo.loginlog;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ApiModel(value = "登陆日志创建 Request VO",
        description = "暂时提供给前端，仅仅后端记录登陆日志时，进行使用")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SysLoginLogCreateReqVO extends SysLoginLogBaseVO {
}
