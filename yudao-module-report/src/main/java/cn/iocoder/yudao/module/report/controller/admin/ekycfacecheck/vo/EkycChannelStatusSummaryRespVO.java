package cn.iocoder.yudao.module.report.controller.admin.ekycfacecheck.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Schema(description = "柱状图数据响应")
@AllArgsConstructor
public class EkycChannelStatusSummaryRespVO {
    private List<String> date;
    private Map<String, List<Integer>> statusSeries;
}
