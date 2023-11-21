package cn.iocoder.yudao.module.crm.controller.admin.contact.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

// TODO @zyna：参考新的 VO 结构，使用 ContactPageReqVO 查询导出的数据
@Schema(description = "管理后台 - crm联系人 Excel 导出 Request VO，参数和 ContactPageReqVO 是一致的")
@Data
@Deprecated
public class ContactExportReqVO {

    @Schema(description = "下次联系时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] nextTime;

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

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "最后跟进时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] lastTime;

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
