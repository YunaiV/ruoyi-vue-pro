package cn.iocoder.yudao.module.mes.controller.admin.qc.indicatorresult.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.mes.enums.qc.MesQcTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - MES 检验结果分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesQcIndicatorResultPageReqVO extends PageParam {

    @Schema(description = "关联质检单ID", example = "1")
    private Long qcId;

    @Schema(description = "质检类型", example = "1")
    @InEnum(MesQcTypeEnum.class)
    private Integer qcType;

    @Schema(description = "样品编号", example = "SPL-001")
    private String code;

    @Schema(description = "产品物料ID", example = "1")
    private Long itemId;

}
