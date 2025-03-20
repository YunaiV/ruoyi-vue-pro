package cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 简化的 ProductRespVO
 **/
@Schema(description = "管理后台 - ERP 产品 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpProductRespSimpleVO extends BaseDO {

    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "731")
    @ExcelProperty("产品编号")
    private Long id;


    @Schema(description = "部门id", requiredMode = Schema.RequiredMode.REQUIRED, example = "50003")
    @ExcelProperty("部门id")
    private Long deptId;

    @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @ExcelProperty("产品名称")
    private String name;

    @Schema(description = "SKU（编码）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("SKU（编码）")
    private String barCode;


    @Schema(description = "单位名称")
    @ExcelProperty("单位名称")
    private String unitName;

    @Schema(description = "品牌", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("品牌")
    private String brand;

    @Schema(description = "材料（中文）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("材料（中文）")
    private String material;

    @Schema(description = "主图", example = "https://www.iocoder.cn")
    @ExcelProperty("主图")
    private String primaryImageUrl;


}