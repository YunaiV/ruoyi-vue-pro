package cn.iocoder.yudao.module.jl.controller.admin.crm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 机构/公司 Excel VO
 *
 * @author 北京惟象科技
 */
@Data
public class InstitutionExcelVO {

    @ExcelProperty("岗位ID")
    private Long id;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("省份")
    private String province;

    @ExcelProperty("城市")
    private String city;

    @ExcelProperty("名字")
    private String name;

    @ExcelProperty("详细地址")
    private String address;

    @ExcelProperty("备注信息")
    private String mark;

    @ExcelProperty("机构类型枚举值")
    private String type;

}
