package cn.iocoder.yudao.module.mes.controller.admin.tm.tool.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - MES 工具台账分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesTmToolPageReqVO extends PageParam {

    @Schema(description = "工具编码", example = "T-001")
    private String code;

    @Schema(description = "工具名称", example = "铣刀")
    private String name;

    @Schema(description = "工具类型编号", example = "100")
    private Long toolTypeId;

    @Schema(description = "品牌", example = "三菱")
    private String brand;

    @Schema(description = "型号规格", example = "M5-100")
    private String specification;

    @Schema(description = "状态", example = "1")
    private Integer status;

}
