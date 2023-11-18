package cn.iocoder.yudao.module.crm.controller.admin.contact.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;

// TODO @zyna：参考新的 VO 结构，把 ContactExcelVO 融合到 ContactRespVO 中
/**
 * crm联系人 Excel VO
 *
 * @author 芋道源码
 */
@Data
@Deprecated
public class ContactExcelVO {

    @ExcelProperty("下次联系时间")
    private LocalDateTime nextTime;

    @ExcelProperty("手机号")
    private String mobile;

    @ExcelProperty("电话")
    private String telephone;

    @ExcelProperty("电子邮箱")
    private String email;

    @ExcelProperty("客户编号")
    private Long customerId;

    @ExcelProperty("地址")
    private String address;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("最后跟进时间")
    private LocalDateTime lastTime;

    @ExcelProperty("主键")
    private Long id;

    @ExcelProperty("直属上级")
    private Long parentId;

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("职位")
    private String post;

    @ExcelProperty("QQ")
    private Long qq;

    @ExcelProperty("微信")
    private String webchat;

    @ExcelProperty("性别")
    private Integer sex;

    @ExcelProperty("是否关键决策人")
    private Boolean policyMakers;

    @ExcelProperty("负责人用户编号")
    private String ownerUserId;

}
