package cn.iocoder.yudao.module.product.controller.admin.attrkey.vo;

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
public class AttrKeyExcelVO {

    @ExcelProperty("主键")
    private Integer id;

    @ExcelProperty("创建时间")
    private Date createTime;

    @ExcelProperty("规格名称")
    private String attrName;

    @ExcelProperty("状态： 1 开启 ，2 禁用")
    private Integer status;

}
