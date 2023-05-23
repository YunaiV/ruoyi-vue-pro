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
 * 客户 Excel VO
 *
 * @author 惟象科技
 */
@Data
public class CustomerExcelVO {

    @ExcelProperty("ID")
    private Long id;

    @ExcelProperty("创建者")
    private String creator;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty(value = "客户来源", converter = DictConvert.class)
    @DictFormat("customer_source") // TODO 代码优化：建议设置到对应的 XXXDictTypeConstants 枚举类中
    private String source;

    @ExcelProperty("手机号")
    private String phone;

    @ExcelProperty("邮箱")
    private String email;

    @ExcelProperty("备注")
    private String mark;

    @ExcelProperty("微信号")
    private String wechat;

    @ExcelProperty("医生职业级别")
    private String doctorProfessionalRank;

    @ExcelProperty("医院科室")
    private String hospitalDepartment;

    @ExcelProperty("学校职称")
    private String academicTitle;

    @ExcelProperty("学历")
    private String academicCredential;

    @ExcelProperty("医院")
    private Long hospitalId;

    @ExcelProperty("学校机构")
    private Long universityId;

    @ExcelProperty("公司")
    private Long companyId;

}
