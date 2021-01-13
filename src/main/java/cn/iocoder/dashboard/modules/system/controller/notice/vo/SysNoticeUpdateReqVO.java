package cn.iocoder.dashboard.modules.system.controller.notice.vo;

import cn.iocoder.dashboard.modules.system.controller.dept.vo.post.SysPostBaseVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@ApiModel("岗位公告更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysNoticeUpdateReqVO extends SysPostBaseVO {

    @ApiModelProperty(value = "岗位公告编号", required = true, example = "1024")
    @NotNull(message = "岗位公告编号不能为空")
    private Long id;

}
