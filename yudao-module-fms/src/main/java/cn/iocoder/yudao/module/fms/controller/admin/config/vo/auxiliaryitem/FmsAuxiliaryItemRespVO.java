package cn.iocoder.yudao.module.fms.controller.admin.config.vo.auxiliaryitem;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * FMS 辅助核算项目 Response VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - FMS 辅助核算项目 Response VO")
@Data
@ExcelIgnoreUnannotated
public class FmsAuxiliaryItemRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "账套编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long accountSetId;

    @Schema(description = "辅助核算类别编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long auxiliaryTypeId;

    @Schema(description = "编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "KH001")
    @ExcelProperty("编码")
    private String code;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "上海测试数字科技有限公司")
    @ExcelProperty("名称")
    private String name;

    @Schema(description = "备注", example = "重点客户")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "规格", example = "标准版")
    @ExcelProperty("规格")
    private String specification;

    @Schema(description = "单位", example = "台")
    @ExcelProperty("单位")
    private String unit;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
