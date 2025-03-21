package cn.iocoder.yudao.module.srm.controller.admin.finance.subject.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - Erp财务主体 精简列表")
@Data
@ExcelIgnoreUnannotated
public class ErpFinanceSubjectSimpleRespVO {
    @Schema(description = "id")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "主体名称")
    @ExcelProperty("主体名称")
    private String name;
}
