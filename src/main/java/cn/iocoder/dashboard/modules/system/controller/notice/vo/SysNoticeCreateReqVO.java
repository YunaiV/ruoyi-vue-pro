package cn.iocoder.dashboard.modules.system.controller.notice.vo;

import cn.iocoder.dashboard.modules.system.controller.dept.vo.post.SysPostBaseVO;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

@ApiModel("通知公告创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysNoticeCreateReqVO extends SysPostBaseVO {
}
