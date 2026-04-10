package cn.iocoder.yudao.module.mes.controller.admin.md.unitmeasure.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - MES 计量单位分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesMdUnitMeasurePageReqVO extends PageParam {

    @Schema(description = "单位编码", example = "KG")
    private String code;

    @Schema(description = "单位名称", example = "公斤")
    private String name;

    @Schema(description = "状态", example = "0")
    private Integer status;

}
