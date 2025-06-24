package cn.iocoder.yudao.module.huawei.smarthome.dto.scenario;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Map;

@ApiModel("华为智能家居 - 导入单个场景请求 DTO")
@Data
public class ScenarioImportReqDTO {

    @ApiModelProperty(value = "空间 ID", required = true, example = "space-uuid-123")
    @NotEmpty(message = "空间 ID 不能为空")
    @Size(min = 1, max = 24, message = "空间 ID 长度必须在 1-24 之间")
    private String spaceId; // 路径参数

    @ApiModelProperty(value = "文件流对应的datas", required = true, notes = "body 对 ISV 是黑盒不建议修改，来自装维 App 导出的场景实例文件")
    @NotNull(message = "场景数据不能为空")
    private Map<String, Object> datas; // 对应文档中的 datas，是一个复杂的JSON对象
}
