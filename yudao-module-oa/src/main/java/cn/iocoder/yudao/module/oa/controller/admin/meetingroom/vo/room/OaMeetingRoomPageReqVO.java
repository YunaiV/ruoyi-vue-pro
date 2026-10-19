package cn.iocoder.yudao.module.oa.controller.admin.meetingroom.vo.room;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 会议室分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class OaMeetingRoomPageReqVO extends PageParam {

    @Schema(description = "会议室名称", example = "101 项目讨论室")
    private String name;

    @Schema(description = "位置", example = "办公楼一层 101")
    private String location;

    @Schema(description = "类型", example = "1")
    private Integer type;

    @Schema(description = "可用状态", example = "0")
    private Integer status;

    @Schema(description = "负责人姓名", example = "芋艿")
    private String managerName;

}
