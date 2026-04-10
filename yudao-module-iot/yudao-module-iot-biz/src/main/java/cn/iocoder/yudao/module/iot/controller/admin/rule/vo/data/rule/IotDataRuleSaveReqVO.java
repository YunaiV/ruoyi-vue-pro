package cn.iocoder.yudao.module.iot.controller.admin.rule.vo.data.rule;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotDataRuleDO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - IoT 数据流转规则新增/修改 Request VO")
@Data
public class IotDataRuleSaveReqVO {

    @Schema(description = "数据流转规则编号", example = "8540")
    private Long id;

    @Schema(description = "数据流转规则名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @NotEmpty(message = "数据流转规则名称不能为空")
    private String name;

    @Schema(description = "数据流转规则描述", example = "你猜")
    private String description;

    @Schema(description = "数据流转规则状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "数据流转规则状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

    @Schema(description = "数据源配置数组", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "数据源配置数组不能为空")
    private List<IotDataRuleDO.SourceConfig> sourceConfigs;

    @Schema(description = "数据目的编号数组", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "数据目的编号数组不能为空")
    private List<Long> sinkIds;

}