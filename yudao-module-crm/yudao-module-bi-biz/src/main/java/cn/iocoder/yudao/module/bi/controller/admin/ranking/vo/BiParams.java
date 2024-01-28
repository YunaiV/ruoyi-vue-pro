package cn.iocoder.yudao.module.bi.controller.admin.ranking.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author anhaohao
 * bi参数
 */
@EqualsAndHashCode(callSuper = true)
@Schema(description = "bi查询相关参数")
@Data
public class BiParams extends PageParam {

    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户IDs")
    private List<Long> userIds;

    @Schema(description = "类型")
    private String type;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "0 部门 1员工")
    private Integer isUser = 1;


}
