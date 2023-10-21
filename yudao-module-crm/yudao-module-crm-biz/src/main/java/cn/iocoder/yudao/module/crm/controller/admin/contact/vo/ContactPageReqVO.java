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

    // TODO @芋艿：需要查询的字段；

    @Schema(description = "联系人名称", example = "张三")
    private String name;

    @Schema(description = "下次联系时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] nextTime;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "电话")
    private String telephone;

    @Schema(description = "电子邮箱")
    private String email;

    @Schema(description = "职务")
    private String post;

    @Schema(description = "客户编号", example = "10795")
    private Long customerId;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

    @Schema(description = "负责人用户编号", example = "7648")
    private Long ownerUserId;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "最后跟进时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] lastTime;

}
