package cn.iocoder.yudao.module.iot.controller.admin.ota.vo.task;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - IoT OTA 升级任务分页 Request VO")
@Data
public class IotOtaTaskPageReqVO extends PageParam {

    @Schema(description = "任务名称", example = "升级任务")
    private String name;

    @Schema(description = "固件编号", example = "1024")
    private Long firmwareId;

}
