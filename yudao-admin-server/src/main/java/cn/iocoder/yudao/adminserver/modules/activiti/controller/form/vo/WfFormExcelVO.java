package cn.iocoder.yudao.adminserver.modules.activiti.controller.form.vo;

import lombok.*;
import java.util.*;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 动态表单 Excel VO
 *
 * @author 芋艿
 */
@Data
public class WfFormExcelVO {

    @ExcelProperty("表单编号")
    private Long id;

    @ExcelProperty("表单名称")
    private String name;

    @ExcelProperty("商户状态")
    private Integer status;

    @ExcelProperty("表单JSON")
    private String formJson;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("创建时间")
    private Date createTime;

}
