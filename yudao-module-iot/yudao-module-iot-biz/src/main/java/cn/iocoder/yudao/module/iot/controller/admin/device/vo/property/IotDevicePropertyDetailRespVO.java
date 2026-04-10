package cn.iocoder.yudao.module.iot.controller.admin.device.vo.property;

import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.model.dataType.ThingModelDataSpecs;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - IoT 设备属性详细 Response VO") // 额外增加 来自 ThingModelProperty 的变量 属性
@Data
public class IotDevicePropertyDetailRespVO extends IotDevicePropertyRespVO {

    @Schema(description = "属性名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "数据类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "int")
    private String dataType;

    @Schema(description = "数据定义")
    private ThingModelDataSpecs dataSpecs;

    @Schema(description = "数据定义列表")
    private List<ThingModelDataSpecs> dataSpecsList;

}