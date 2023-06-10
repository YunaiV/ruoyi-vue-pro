package cn.iocoder.yudao.module.point.controller.admin.pointconfig.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

@Schema(description = "管理后台 - 积分设置 Excel 导出 Request VO，参数和 PointConfigPageReqVO 是一致的")
@Data
public class PointConfigExportReqVO {

    @Schema(description = "1 开启积分抵扣 0 关闭积分抵扣", example = "1")
    private Integer tradeDeductEnable;

}
