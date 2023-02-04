package cn.iocoder.yudao.module.system.controller.admin.dict.vo.type;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 字典类型精简信息 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DictTypeSimpleRespVO {

    @Schema(description = "字典类型编号", required = true, example = "1024")
    private Long id;

    @Schema(description = "字典类型名称", required = true, example = "芋道")
    private String name;

    @Schema(description = "字典类型", required = true, example = "sys_common_sex")
    private String type;

}
