package cn.iocoder.yudao.module.demo.controller.admin.infra.net.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@ApiModel(value = "管理后台 - 区块链网络 Excel 导出 Request VO", description = "参数和 NetPageReqVO 是一致的")
@Data
public class NetExportReqVO {

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "区块浏览器")
    private String explorer;

    @ApiModelProperty(value = "原生代币")
    private String symbol;

    @ApiModelProperty(value = "节点")
    private String endpoint;

    @ApiModelProperty(value = "网络类型")
    private String type;

    @ApiModelProperty(value = "信息")
    private String info;

    @ApiModelProperty(value = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Date[] createTime;

}
