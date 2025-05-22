package cn.iocoder.yudao.module.crm.controller.admin.product.vo.category;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - CRM 产品分类列表 Request VO")
@Data
public class CrmProductCategoryListReqVO {

    @ExcelProperty("名称")
    private String name;

    @ExcelProperty("父级 id")
    private Long parentId;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
