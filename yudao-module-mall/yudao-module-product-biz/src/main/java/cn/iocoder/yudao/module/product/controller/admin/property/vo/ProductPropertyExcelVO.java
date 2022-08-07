package cn.iocoder.yudao.module.product.controller.admin.property.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 规格名称 Excel VO
 *
 * @author 芋道源码
 */
@Data
public class ProductPropertyExcelVO {

    @ExcelProperty("主键")
    private Long id;

    @ExcelProperty("规格名称")
    private String name;

    @ExcelProperty("状态： 0 开启 ，1 禁用")
    private Integer status;

    @ExcelProperty("创建时间")
    private Date createTime;

}
