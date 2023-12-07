package cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import java.time.LocalTime;
import java.util.List;

/**
 * 秒杀时段 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 *
 * @author HUIHUI
 */
@Data
public class SeckillConfigBaseVO {

    @Schema(description = "秒杀时段名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "早上场")
    @NotNull(message = "秒杀时段名称不能为空")
    private String name;

    @Schema(description = "开始时间点", requiredMode = Schema.RequiredMode.REQUIRED, example = "09:00:00")
    @NotNull(message = "开始时间点不能为空")
    private String startTime;

    @Schema(description = "结束时间点", requiredMode = Schema.RequiredMode.REQUIRED, example = "16:00:00")
    @NotNull(message = "结束时间点不能为空")
    private String endTime;

    @Schema(description = "秒杀轮播图", requiredMode = Schema.RequiredMode.REQUIRED, example = "[https://www.iocoder.cn/xx.png]")
    @NotNull(message = "秒杀轮播图不能为空")
    private List<String> sliderPicUrls;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @AssertTrue(message = "秒杀时段开始时间和结束时间不能相等")
    @JsonIgnore
    public boolean isValidStartTimeValid() {
        return !LocalTime.parse(startTime).equals(LocalTime.parse(endTime));
    }

    @AssertTrue(message = "秒杀时段开始时间不能在结束时间之后")
    @JsonIgnore
    public boolean isValidEndTimeValid() {
        return !LocalTime.parse(startTime).isAfter(LocalTime.parse(endTime));
    }

}
