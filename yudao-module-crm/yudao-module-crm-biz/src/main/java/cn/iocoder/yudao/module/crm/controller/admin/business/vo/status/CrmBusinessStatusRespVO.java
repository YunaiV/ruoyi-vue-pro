package cn.iocoder.yudao.module.crm.controller.admin.business.vo.status;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 商机状态 Response VO")
@Data
@ExcelIgnoreUnannotated
public class CrmBusinessStatusRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "23899")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "状态类型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "7139")
    @ExcelProperty("状态类型编号")
    private Long typeId;

    @Schema(description = "状态名", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @ExcelProperty("状态名")
    private String name;

    @Schema(description = "赢单率")
    @ExcelProperty("赢单率")
    private String percent;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("排序")
    private Integer sort;

}
