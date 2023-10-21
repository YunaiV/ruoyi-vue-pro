package cn.iocoder.yudao.module.crm.controller.admin.contact.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * crm联系人 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class ContactBaseVO {

    // TODO @zyna：部分字段，缺少 example，需要补充；

    @Schema(description = "联系人名称", example = "张三")
    @NotNull(message = "姓名不能为空")
    private String name;

    @Schema(description = "下次联系时间")
    private LocalDateTime nextTime;

    // TODO @zyna：缺少 validator 的校验
    @Schema(description = "手机号")
    private String mobile;

    // TODO @zyna：缺少 validator 的校验
    @Schema(description = "电话")
    private String telephone;

    // TODO @zyna：缺少 validator 的校验
    @Schema(description = "电子邮箱")
    private String email;

    @Schema(description = "职务")
    private String post;

    // TODO @zyna：非空校验
    @Schema(description = "客户编号", example = "10795")
    private Long customerId;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

    // TODO @zyna：这个新建的时候，应该不会传递；而是后端默认设置自己为负责人；
    @Schema(description = "负责人用户编号", example = "7648")
    private Long ownerUserId;

    @Schema(description = "最后跟进时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime lastTime;

}
