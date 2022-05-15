package cn.iocoder.yudao.module.product.controller.admin.attrvalue.vo;

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
public class AttrValueExcelVO {

    @ExcelProperty("主键")
    private Integer id;

    @ExcelProperty("创建时间")
    private Date createTime;

    @ExcelProperty("规格键id")
    private String attrKeyId;

    @ExcelProperty("规格值名字")
    private String attrValueName;

    @ExcelProperty("状态： 1 开启 ，2 禁用")
    private Integer status;

}
