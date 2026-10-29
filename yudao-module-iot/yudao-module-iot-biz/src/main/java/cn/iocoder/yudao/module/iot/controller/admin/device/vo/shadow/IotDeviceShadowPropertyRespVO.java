package cn.iocoder.yudao.module.iot.controller.admin.device.vo.shadow;

import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.model.dataType.ThingModelDataSpecs;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 管理后台 - IoT 设备影子属性对比 Response VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - IoT 设备影子属性对比 Response VO")
@Data
public class IotDeviceShadowPropertyRespVO {

    @Schema(description = "标识符", example = "Temperature")
    private String identifier;

    @Schema(description = "属性名称", example = "温度")
    private String name;

    @Schema(description = "数据类型", example = "int")
    private String dataType;

    @Schema(description = "数据定义")
    private ThingModelDataSpecs dataSpecs;

    @Schema(description = "数据定义列表")
    private List<ThingModelDataSpecs> dataSpecsList;

    @Schema(description = "期望值")
    private Object desiredValue;

    @Schema(description = "上报值")
    private Object reportedValue;

    @Schema(description = "期望更新时间（epoch 秒）")
    private Long desiredTime;

    @Schema(description = "上报更新时间（epoch 秒）")
    private Long reportedTime;

}
