package cn.iocoder.yudao.module.iot.controller.admin.rule.vo.data.rule;

import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotDataRuleDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - IoT 数据流转规则 Response VO")
@Data
public class IotDataRuleRespVO {

    @Schema(description = "数据流转规则编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "8540")
    private Long id;

    @Schema(description = "数据流转规则名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    private String name;

    @Schema(description = "数据流转规则描述", example = "你猜")
    private String description;

    @Schema(description = "数据流转规则状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "数据源配置数组", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<IotDataRuleDO.SourceConfig> sourceConfigs;

    @Schema(description = "数据目的编号数组", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> sinkIds;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}