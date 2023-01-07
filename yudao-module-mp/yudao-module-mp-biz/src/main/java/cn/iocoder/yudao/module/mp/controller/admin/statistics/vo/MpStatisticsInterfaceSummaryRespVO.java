package cn.iocoder.yudao.module.mp.controller.admin.statistics.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@ApiModel("管理后台 - 某一天的接口分析数据 Response VO")
@Data
public class MpStatisticsInterfaceSummaryRespVO {

    @ApiModelProperty(value = "日期", required = true)
    private Date refDate;

    @ApiModelProperty(value = "通过服务器配置地址获得消息后，被动回复用户消息的次数", required = true, example = "10")
    private Integer callbackCount;

    @ApiModelProperty(value = "上述动作的失败次数", required = true, example = "20")
    private Integer failCount;

    @ApiModelProperty(value = "总耗时，除以 callback_count 即为平均耗时", required = true, example = "30")
    private Integer totalTimeCost;

    @ApiModelProperty(value = "最大耗时", required = true, example = "40")
    private Integer maxTimeCost;

}
