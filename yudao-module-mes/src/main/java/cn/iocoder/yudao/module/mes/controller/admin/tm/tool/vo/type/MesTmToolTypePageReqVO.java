package cn.iocoder.yudao.module.mes.controller.admin.tm.tool.vo.type;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - MES 工具类型分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesTmToolTypePageReqVO extends PageParam {

    @Schema(description = "类型编码", example = "TT-001")
    private String code;

    @Schema(description = "类型名称", example = "铣刀")
    private String name;

    @Schema(description = "保养维护类型", example = "1")
    private Integer maintenType;

}
