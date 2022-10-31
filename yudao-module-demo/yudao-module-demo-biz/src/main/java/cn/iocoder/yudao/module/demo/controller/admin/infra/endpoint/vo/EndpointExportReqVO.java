package cn.iocoder.yudao.module.demo.controller.admin.infra.endpoint.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@ApiModel(value = "管理后台 - 区块链节点 Excel 导出 Request VO", description = "参数和 EndpointPageReqVO 是一致的")
@Data
public class EndpointExportReqVO {

    @ApiModelProperty(value = "网络编号")
    private Long netId;

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "地址")
    private String url;

    @ApiModelProperty(value = "是否被墙")
    private Boolean blocked;

    @ApiModelProperty(value = "信息")
    private String info;

    @ApiModelProperty(value = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Date[] createTime;

}
