package cn.iocoder.yudao.module.jl.controller.admin.crm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 销售线索中的客户方案 Excel VO
 *
 * @author 惟象科技
 */
@Data
public class SalesleadCustomerPlanExcelVO {

    @ExcelProperty("岗位ID")
    private Long id;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("线索 id")
    private Long salesleadId;

    @ExcelProperty("文件地址")
    private String fileUrl;

    @ExcelProperty("文件名字")
    private String fileName;

}
