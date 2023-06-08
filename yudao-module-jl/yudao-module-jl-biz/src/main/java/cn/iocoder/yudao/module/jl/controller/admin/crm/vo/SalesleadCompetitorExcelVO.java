package cn.iocoder.yudao.module.jl.controller.admin.crm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 销售线索中竞争对手的报价 Excel VO
 *
 * @author 惟象科技
 */
@Data
public class SalesleadCompetitorExcelVO {

    @ExcelProperty("岗位ID")
    private Long id;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("销售线索 id")
    private Long salesleadId;

    @ExcelProperty("竞争对手")
    private Long competitorId;

    @ExcelProperty("竞争对手的报价")
    private Long competitorQuotation;

}
