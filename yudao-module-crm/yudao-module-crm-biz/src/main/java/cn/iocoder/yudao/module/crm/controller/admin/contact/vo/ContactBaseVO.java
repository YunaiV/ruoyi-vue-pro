package cn.iocoder.yudao.module.crm.controller.admin.contact.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import javax.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * crm联系人 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class ContactBaseVO {

    @Schema(description = "下次联系时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDateTime nextTime;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "电话")
    private String telephone;

    @Schema(description = "电子邮箱")
    private String email;

    @Schema(description = "客户编号", example = "10795")
    private Long customerId;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

    @Schema(description = "最后跟进时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime lastTime;

    @Schema(description = "直属上级", example = "23457")
    private Long parentId;

    @Schema(description = "姓名", example = "芋艿")
    private String name;

    @Schema(description = "职位")
    private String post;

    @Schema(description = "QQ")
    private Long qq;

    @Schema(description = "微信")
    private String webchat;

    @Schema(description = "性别")
    private Integer sex;

    @Schema(description = "是否关键决策人")
    private Boolean policyMakers;

    @Schema(description = "负责人用户编号", example = "14334")
    private String ownerUserId;

}
