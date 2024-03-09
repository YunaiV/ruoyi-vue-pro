package cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.customer;

import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - CRM 数据统计的员工客户分析 Request VO")
@Data
public class CrmStatisticsCustomerReqVO {

    @Schema(description = "部门 id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "部门 id 不能为空")
    private Long deptId;

    /**
     * 负责人用户 id, 当用户为空, 则计算部门下用户
     */
    @Schema(description = "负责人用户 id", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "1")
    private Long userId;

    /**
     * userIds 目前不用前端传递，目前是方便后端通过 deptId 读取编号后，设置回来
     * <p>
     * 后续，可能会支持选择部分用户进行查询
     */
    @Schema(description = "负责人用户 id 集合", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "2")
    private List<Long> userIds;

    @Schema(description = "时间范围", requiredMode = Schema.RequiredMode.REQUIRED)
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @NotEmpty(message = "时间范围不能为空")
    private LocalDateTime[] times;

    // TODO @dhb52：这个时间间隔，建议前端传递；例如说：字段叫 interval，枚举有天、周、月、季度、年。因为一般分析类的系统，都是交给用户选择筛选时间间隔，而我们这里是默认根据日期选项，默认对应的 interval 而已
    // 然后实现上，可以在 common 包的 enums 加个 DateIntervalEnum，里面一个是 interval 字段，枚举过去，然后有个 pattern 字段，用于格式化时间格式；
    // 这样的话，可以通过 interval 获取到 pattern，然后前端就可以根据 pattern 格式化时间，计算还是交给数据库
    /**
     * group by DATE_FORMAT(field, #{dateFormat})
     */
    @Schema(description = "Group By 日期格式", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "%Y%m")
    private String sqlDateFormat;

    // TODO @dhb52：这个字段，目前是不是没啥用呀？
    /**
     * 数据类型 {@link CrmBizTypeEnum}
     */
    @Schema(description = "数据类型", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "2")
    private Integer bizType;

}
