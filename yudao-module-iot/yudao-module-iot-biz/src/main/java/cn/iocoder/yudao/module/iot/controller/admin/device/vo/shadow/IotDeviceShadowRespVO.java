package cn.iocoder.yudao.module.iot.controller.admin.device.vo.shadow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 管理后台 - IoT 设备影子 Response VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - IoT 设备影子 Response VO")
@Data
public class IotDeviceShadowRespVO {

    @Schema(description = "版本号", example = "3")
    private Long version;

    @Schema(description = "最后更新时间（epoch 秒）", example = "1469564576")
    private Long shadowTime;

    @Schema(description = "影子状态", example = "0")
    private Integer status;

    @Schema(description = "属性对比列表")
    private List<IotDeviceShadowPropertyRespVO> properties;

    @Schema(description = "期望状态")
    private Map<String, Object> desired;

    @Schema(description = "上报状态")
    private Map<String, Object> reported;

    @Schema(description = "元数据")
    private Map<String, Object> metadata;

}
