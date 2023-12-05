package cn.iocoder.yudao.module.crm.controller.admin.product.vo.productcategory;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

// TODO 芋艿：这个导出最后搞；命名应该是按照 ProductExportReqVO 风格
@Schema(description = "管理后台 - 产品分类列表 Request VO")
@Data
public class CrmProductCategoryListReqVO {

    @ExcelProperty("名称")
    private String name;

    @ExcelProperty("父级 id")
    private Long parentId;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
