package cn.iocoder.dashboard.modules.system.controller.notice.vo;

import cn.iocoder.dashboard.modules.system.controller.dept.vo.post.SysPostBaseVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@ApiModel("通知公告信息 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysNoticeRespVO extends SysPostBaseVO {

    @ApiModelProperty(value = "通知公告序号", required = true, example = "1024")
    private Long id;

    @ApiModelProperty(value = "创建时间", required = true, example = "时间戳格式")
    private Date createTime;

}
