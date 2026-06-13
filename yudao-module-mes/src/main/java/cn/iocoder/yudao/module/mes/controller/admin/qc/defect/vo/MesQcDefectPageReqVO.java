package cn.iocoder.yudao.module.mes.controller.admin.qc.defect.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - MES 缺陷类型分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesQcDefectPageReqVO extends PageParam {

    @Schema(description = "缺陷编码", example = "DF001")
    private String code;

    @Schema(description = "缺陷描述", example = "外观缺陷")
    private String name;

    @Schema(description = "检测项类型", example = "2")
    private Integer type;

    @Schema(description = "缺陷等级", example = "1")
    private Integer level;

}
