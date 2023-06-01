package cn.iocoder.yudao.module.jl.controller.admin.project.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 项目款项 Excel VO
 *
 * @author 惟象科技
 */
@Data
public class ProjectFundExcelVO {

    @ExcelProperty("岗位ID")
    private Long id;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("名称")
    private String name;

    @ExcelProperty("资金额度")
    private Long price;

    @ExcelProperty("项目 id")
    private Long projectId;

    @ExcelProperty("支付状态(未支付，部分支付，完全支付)")
    private String status;

    @ExcelProperty("支付时间")
    private LocalDateTime paidTime;

    @ExcelProperty("支付的截止时间")
    private LocalDate deadline;

    @ExcelProperty("支付凭证上传地址")
    private String receiptUrl;

    @ExcelProperty("支付凭证文件名称")
    private String receiptName;

}
