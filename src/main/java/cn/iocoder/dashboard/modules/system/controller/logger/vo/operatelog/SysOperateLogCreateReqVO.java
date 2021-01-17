package cn.iocoder.dashboard.modules.system.controller.logger.vo.operatelog;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ApiModel(value = "操作日志创建 Request VO", description = "暂时提供给前端，仅仅后端切面记录操作日志时，进行使用")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SysOperateLogCreateReqVO extends SysOperateLogBaseVO {
}
