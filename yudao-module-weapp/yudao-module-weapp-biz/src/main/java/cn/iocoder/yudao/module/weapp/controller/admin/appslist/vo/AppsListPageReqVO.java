package cn.iocoder.yudao.module.weapp.controller.admin.appslist.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 小程序清单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppsListPageReqVO extends PageParam {

    @Schema(description = "小程序名称", example = "赵六")
    private String weappName;

    @Schema(description = "小程序OPENID", example = "25136")
    private String weappOpenid;

    @Schema(description = "分类ID", example = "5963")
    private String classId;

    @Schema(description = "小程序图标")
    private String logoImg;

    @Schema(description = "发布状态", example = "1")
    private Integer status;

    @Schema(description = "小程序简介", example = "随便")
    private String description;

}