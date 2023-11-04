package cn.iocoder.yudao.module.crm.controller.admin.productcategory.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 产品分类 List VO
 *
 * @author ZanGe丶
 */
@Schema(description = "管理后台 - 产品分类列表 Request VO")
@Data
public class ProductCategoryListReqVO {

    @ExcelProperty("名称")
    private String name;

    @ExcelProperty("父级id")
    private Long parentId;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
