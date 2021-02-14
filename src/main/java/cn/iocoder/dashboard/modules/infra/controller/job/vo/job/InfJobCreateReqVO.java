package cn.iocoder.dashboard.modules.infra.controller.job.vo.job;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ApiModel("定时任务创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class InfJobCreateReqVO extends InfJobBaseVO {

}
