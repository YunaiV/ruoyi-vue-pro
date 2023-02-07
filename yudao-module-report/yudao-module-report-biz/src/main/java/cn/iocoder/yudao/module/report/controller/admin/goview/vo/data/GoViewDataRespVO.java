package cn.iocoder.yudao.module.report.controller.admin.goview.vo.data;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - GoView 数据 Response VO")
@Data
public class GoViewDataRespVO {

    @Schema(description = "数据维度", required = true, example = "['product', 'data1', 'data2']")
    private List<String> dimensions;

    @Schema(description = "数据明细列表", required = true)
    private List<Map<String, Object>> source;

}
