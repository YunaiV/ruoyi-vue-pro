package cn.iocoder.yudao.module.crm.controller.admin.contact.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - crm联系人分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ContactPageReqVO extends PageParam {

    // TODO @zyna：筛选条件
    // ●客户：
    // ●姓名：
    // ●手机、电话、座机、QQ、微信、邮箱

    @Schema(description = "下次联系时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] nextTime;

    @Schema(description = "手机号",example = "13898273941")
    private String mobile;

    @Schema(description = "电话",example = "021-383773")
    private String telephone;

    @Schema(description = "电子邮箱",example = "111@22.com")
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

    @Schema(description = "QQ",example = "3882872")
    private Long qq;

    @Schema(description = "微信",example = "zzZ98373")
    private String wechat;

    @Schema(description = "性别")
    private Integer sex;

    @Schema(description = "是否关键决策人")
    private Boolean master;

    @Schema(description = "负责人用户编号", example = "14334")
    private Long ownerUserId;

}
