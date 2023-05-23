package cn.iocoder.yudao.module.jl.controller.admin.join.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 客户所属的销售人员 Excel VO
 *
 * @author 惟象科技
 */
@Data
public class JoinCustomer2saleExcelVO {

    @ExcelProperty("岗位ID")
    private Long id;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("客户id")
    private Long customerId;

    @ExcelProperty("销售 id")
    private Long salesId;

}
