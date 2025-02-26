package cn.iocoder.yudao.module.iot.controller.admin.ota.vo.upgrade.record;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Data
@Schema(description = "管理后台 - OTA升级记录分页 Request VO")
public class IotOtaUpgradeRecordPageReqVO extends PageParam {

    // TODO @li：使用 IotOtaUpgradeRecordStatusEnum 枚举哈
    /**
     * 待处理状态
     */
    public static final Integer PENDING = 0;
    /**
     * 已推送状态
     */
    public static final Integer PUSHED = 10;
    /**
     * 正在升级状态
     */
    public static final Integer UPGRADING = 20;
    /**
     * 升级成功状态
     */
    public static final Integer SUCCESS = 30;
    /**
     * 升级失败状态
     */
    public static final Integer FAILURE = 40;
    /**
     * 升级已取消状态
     */
    public static final Integer CANCELED = 50;

    /**
     * 升级任务编号字段。
     * <p>
     * 该字段用于标识升级任务的唯一编号，不能为空。
     */
    @NotNull(message = "升级任务编号不能为空")
    @Schema(description = "升级任务编号", requiredMode = REQUIRED, example = "1024")
    private Long taskId;

    /**
     * 设备标识字段。
     * <p>
     * 该字段用于标识设备的名称，通常用于区分不同的设备。
     */
    @Schema(description = "设备标识", requiredMode = REQUIRED, example = "摄像头A1-1")
    private String deviceName;

}
