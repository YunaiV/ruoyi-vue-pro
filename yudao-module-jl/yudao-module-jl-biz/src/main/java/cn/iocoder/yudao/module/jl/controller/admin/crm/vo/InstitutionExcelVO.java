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
 * CRM 模块的机构/公司 Excel VO
 *
 * @author 芋道源码
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

    @ExcelProperty(value = "机构类型", converter = DictConvert.class)
    @DictFormat("institution_type") // TODO 代码优化：建议设置到对应的 XXXDictTypeConstants 枚举类中
    private String type;

}
