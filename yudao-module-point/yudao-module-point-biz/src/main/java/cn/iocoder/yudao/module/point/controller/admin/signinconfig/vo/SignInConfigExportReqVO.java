package cn.iocoder.yudao.module.point.controller.admin.signinconfig.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

@Schema(description = "管理后台 - 积分签到规则 Excel 导出 Request VO，参数和 SignInConfigPageReqVO 是一致的")
@Data
public class SignInConfigExportReqVO {

    @Schema(description = "签到第x天", example = "7")
    private Integer day;

}
