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

    @Schema(description = "小程序名称;小程序名称", example = "王五")
    private String weappName;

    @Schema(description = "小程序OPENID;小程序OPENID", example = "27787")
    private String weappOpenid;

    @Schema(description = "分类ID;所属分类ID", example = "26012")
    private String classId;

    @Schema(description = "小程序简介;小程序说明", example = "你说的对")
    private String description;

    @Schema(description = "小程序图标;小程序图标")
    private String logoImg;

    @Schema(description = "状态;状态", example = "2")
    private String status;

    @Schema(description = "更新人;更新人")
    private String updatedBy;

    @Schema(description = "更新时间;数据更新时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] updatedTime;

}