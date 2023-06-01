package cn.iocoder.yudao.module.jl.controller.admin.crm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;


/**
 * 销售线索 Excel VO
 *
 * @author 惟象科技
 */
@Data
public class SalesleadExcelVO {

    @ExcelProperty("ID")
    private Long id;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty(value = "销售线索来源", converter = DictConvert.class)
    @DictFormat("sales_lead_source") // TODO 代码优化：建议设置到对应的 XXXDictTypeConstants 枚举类中
    private String source;

    @ExcelProperty("关键需求")
    private String requirement;

    @ExcelProperty("预算(元)")
    private Long budget;

    @ExcelProperty("报价")
    private Long quotation;

    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat("sales_lead_status") // TODO 代码优化：建议设置到对应的 XXXDictTypeConstants 枚举类中
    private Integer status;

    @ExcelProperty("客户id")
    private Long customerId;

    @ExcelProperty("项目id")
    private Long projectId;

    @ExcelProperty("业务类型")
    private String businessType;

    @ExcelProperty("丢单的说明")
    private String lostNote;

    @ExcelProperty("绑定的销售报价人员")
    private Long managerId;

}
