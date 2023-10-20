package cn.iocoder.yudao.module.crm.controller.admin.contact.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * crm联系人 Excel VO
 *
 * @author 芋道源码
 */
@Data
public class ContactExcelVO {

    @ExcelProperty("主键")
    private Long id;

    @ExcelProperty("联系人名称")
    private String name;

    @ExcelProperty("下次联系时间")
    private LocalDateTime nextTime;

    @ExcelProperty("手机号")
    private String mobile;

    @ExcelProperty("电话")
    private String telephone;

    @ExcelProperty("电子邮箱")
    private String email;

    @ExcelProperty("职务")
    private String post;

    @ExcelProperty("客户编号")
    private Long customerId;

    @ExcelProperty("地址")
    private String address;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("负责人用户编号")
    private Long ownerUserId;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("最后跟进时间")
    private LocalDateTime lastTime;

}
