package cn.iocoder.yudao.module.jl.controller.admin.crm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 友商 Excel VO
 *
 * @author 惟象科技
 */
@Data
public class CompetitorExcelVO {

    @ExcelProperty("岗位ID")
    private Long id;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("公司名")
    private String name;

    @ExcelProperty("联系人")
    private String contactName;

    @ExcelProperty("手机号")
    private String phone;

    @ExcelProperty("友商类型")
    private String type;

    @ExcelProperty("优势")
    private String advantage;

    @ExcelProperty("劣势")
    private String disadvantage;

    @ExcelProperty("备注")
    private String mark;

}
