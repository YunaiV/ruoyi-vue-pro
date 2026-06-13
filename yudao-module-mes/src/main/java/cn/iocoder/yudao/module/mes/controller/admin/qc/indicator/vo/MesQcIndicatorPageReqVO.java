package cn.iocoder.yudao.module.mes.controller.admin.qc.indicator.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - MES 质检指标分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesQcIndicatorPageReqVO extends PageParam {

    @Schema(description = "检测项编码", example = "QI001")
    private String code;

    @Schema(description = "检测项名称", example = "长度")
    private String name;

    @Schema(description = "检测项类型", example = "1")
    private Integer type;

    @Schema(description = "结果值类型", example = "1")
    private Integer resultType;

}
