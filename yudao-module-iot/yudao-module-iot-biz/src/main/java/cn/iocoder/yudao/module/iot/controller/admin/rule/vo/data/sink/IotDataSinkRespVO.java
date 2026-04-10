package cn.iocoder.yudao.module.iot.controller.admin.rule.vo.data.sink;

import cn.iocoder.yudao.module.iot.dal.dataobject.rule.config.IotAbstractDataSinkConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT 数据流转目的 Response VO")
@Data
public class IotDataSinkRespVO {

    @Schema(description = "数据目的编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "18564")
    private Long id;

    @Schema(description = "数据目的名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    private String name;

    @Schema(description = "数据目的描述", example = "随便")
    private String description;

    @Schema(description = "数据目的状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "数据目的类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer type;

    @Schema(description = "数据目的配置")
    private IotAbstractDataSinkConfig config;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}