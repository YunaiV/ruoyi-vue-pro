package cn.iocoder.yudao.module.mes.controller.admin.qc.oqc.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - MES 出货检验单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesQcOqcPageReqVO extends PageParam {

    @Schema(description = "检验单编号", example = "OQC2025")
    private String code;

    @Schema(description = "客户 ID", example = "10")
    private Long clientId;

    @Schema(description = "批次号", example = "BC2025")
    private String batchCode;

    @Schema(description = "产品物料 ID", example = "20")
    private Long itemId;

    @Schema(description = "检测结果", example = "1")
    private Integer checkResult;

}
