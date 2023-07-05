package cn.iocoder.yudao.module.member.controller.admin.point.vo.recrod;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import javax.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 用户积分记录 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class MemberPointRecordBaseVO {

    @Schema(description = "业务编码", example = "22706")
    @NotNull(message = "业务编码不能为空")
    private String bizId;

    @Schema(description = "业务类型", example = "1")
    @NotNull(message = "业务类型不能为空")
    private String bizType;

    @Schema(description = "1增加 0扣减", example = "1")
    @NotNull(message = "操作类型不能为空")
    private String type;

    @Schema(description = "积分标题")
    @NotNull(message = "积分标题不能为空")
    private String title;

    @Schema(description = "积分描述", example = "你猜")
    private String description;

    @Schema(description = "积分")
    @NotNull(message = "操作积分不能为空")
    private Integer point;

    @Schema(description = "变动后的积分", requiredMode = Schema.RequiredMode.REQUIRED)
//    @NotNull(message = "变动后的积分不能为空")
    private Integer totalPoint;

    @Schema(description = "状态：1-订单创建，2-冻结期，3-完成，4-失效（订单退款） ", example = "1")
    @NotNull(message = "积分状态不能为空")
    private Integer status;

    @Schema(description = "用户id", example = "31169")
    @NotNull(message = "用户ID不能为空")
    private Integer userId;

    @Schema(description = "冻结时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @NotNull(message = "冻结时间不能为空")
    private LocalDateTime freezingTime;

    @Schema(description = "解冻时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @NotNull(message = "解冻时间不能为空")
    private LocalDateTime thawingTime;

}
