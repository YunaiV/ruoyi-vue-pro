package cn.iocoder.yudao.module.iot.controller.admin.ota.vo.upgrade.record;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(description = "管理后台 - IoT OTA 升级记录分页 Request VO")
public class IotOtaUpgradeRecordPageReqVO extends PageParam {

    // TODO @li：已经有注解，不用重复注释
    /**
     * 升级任务编号字段。
     * <p>
     * 该字段用于标识升级任务的唯一编号，不能为空。
     */
    @Schema(description = "升级任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "升级任务编号不能为空")
    private Long taskId;

    /**
     * 设备标识字段。
     * <p>
     * 该字段用于标识设备的名称，通常用于区分不同的设备。
     */
    @Schema(description = "设备标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "摄像头A1-1")
    private String deviceName;

}
