package cn.iocoder.yudao.module.iot.controller.admin.ota.vo.task.record;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - IoT OTA 升级记录分页 Request VO")
@Data
public class IotOtaTaskRecordPageReqVO extends PageParam {

    // TODO @芋艿：分页条件字段梳理；
    @Schema(description = "升级任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "升级任务编号不能为空")
    private Long taskId;

    @Schema(description = "设备标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "摄像头A1-1")
    private String deviceName;

}
