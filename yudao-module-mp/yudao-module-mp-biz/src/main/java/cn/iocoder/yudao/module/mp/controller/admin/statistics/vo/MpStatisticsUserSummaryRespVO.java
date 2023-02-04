package cn.iocoder.yudao.module.mp.controller.admin.statistics.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@ApiModel("管理后台 - 某一天的粉丝增减数据 Response VO")
@Data
public class MpStatisticsUserSummaryRespVO {

    @ApiModelProperty(value = "日期", required = true)
    private Date refDate;

    @ApiModelProperty(value = "粉丝来源", required = true, example = "0")
    private Integer userSource;

    @ApiModelProperty(value = "新关注的粉丝数量", required = true, example = "10")
    private Integer newUser;

    @ApiModelProperty(value = "取消关注的粉丝数量", required = true, example = "20")
    private Integer cancelUser;

}
