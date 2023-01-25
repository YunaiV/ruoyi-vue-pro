package cn.iocoder.yudao.module.mp.controller.admin.statistics.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@ApiModel("管理后台 - 某一天的粉丝增减数据 Response VO")
@Data
public class MpStatisticsUpstreamMessageRespVO {

    @ApiModelProperty(value = "日期", required = true)
    private Date refDate;

    @ApiModelProperty(value = "上行发送了（向公众号发送了）消息的粉丝数", required = true, example = "10")
    private Integer messageUser;

    @ApiModelProperty(value = "上行发送了消息的消息总数", required = true, example = "20")
    private Integer messageCount;

}
