package cn.iocoder.yudao.module.crm.controller.admin.clue.vo;

import cn.iocoder.yudao.framework.common.validation.Mobile;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 线索 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class CrmClueBaseVO {

    // TODO @wanwan：转化状态，新增和修改的时候，应该不传递的哈；而是在未来的时候，才会更新到
    @Schema(description = "转化状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "转化状态不能为空")
    private Boolean transformStatus;

    // TODO @wanwan：同 transformStatus
    @Schema(description = "跟进状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "跟进状态不能为空")
    private Boolean followUpStatus;

    @Schema(description = "线索名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "线索xxx")
    @NotNull(message = "线索名称不能为空") // TODO @wanwan：应该是 NotEmpty 噢。空串都无法接受
    private String name;

    // TODO @wanwan：中英文之间，要有个空格；例如说，客户 id 不能为空
    @Schema(description = "客户id", requiredMode = Schema.RequiredMode.REQUIRED, example = "520")
    @NotNull(message = "客户id不能为空")
    private Long customerId;

    @Schema(description = "下次联系时间", example = "2023-10-18 01:00:00")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime contactNextTime;

    // TODO @wanwan：@Schema 在 @Mobile 之前，要保持统一的顺序；2）可以加个 @Telephone 的校验格式；应该不是手机的格式哈
    @Mobile(message = "电话格式不正确")
    @Schema(description = "电话", example = "18000000000")
    private String telephone;

    // TODO @wanwan：@Schema 在 @Mobile 之前，要保持统一的顺序；2）类似 @Mobile 这个提示如果是默认的，就可以不写 message
    @Mobile(message = "手机号格式不正确")
    @Schema(description = "手机号", example = "18000000000")
    private String mobile;

    @Schema(description = "地址", example = "北京市海淀区")
    private String address;

    @Schema(description = "负责人的用户编号", example = "27199")
    // TODO @wanwan：这个是必填字段哈；
    private Long ownerUserId;

    @Schema(description = "最后跟进时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime contactLastTime;

    @Schema(description = "备注", example = "随便")
    private String remark;

}
