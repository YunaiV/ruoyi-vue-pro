package cn.iocoder.yudao.module.product.controller.admin.propertyvalue.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 规格值 Excel VO
 *
 * @author 芋道源码
 */
@Data
public class PropertyValueExcelVO {

    @ExcelProperty("主键")
    private Integer id;

    @ExcelProperty("规格键id")
    private Long propertyId;

    @ExcelProperty("规格值名字")
    private String name;

    @ExcelProperty("状态： 1 开启 ，2 禁用")
    private Integer status;

    @ExcelProperty("创建时间")
    private Date createTime;

}
