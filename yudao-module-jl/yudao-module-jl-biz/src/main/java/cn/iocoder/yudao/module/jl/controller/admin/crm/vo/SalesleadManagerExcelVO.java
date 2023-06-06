package cn.iocoder.yudao.module.jl.controller.admin.crm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 销售线索中的项目售前支持人员 Excel VO
 *
 * @author 惟象科技
 */
@Data
public class SalesleadManagerExcelVO {

    @ExcelProperty("岗位ID")
    private Long id;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("线索id")
    private Long salesleadId;

    @ExcelProperty("销售售中人员")
    private Long managerId;

}
