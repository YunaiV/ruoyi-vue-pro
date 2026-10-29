package cn.iocoder.yudao.module.iot.controller.admin.product.vo.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

/**
 * 管理后台 - IoT 产品数据解析脚本更新 Request VO
 * <p>
 * 部分更新：scriptContent、scriptEnabled 为 null 时不修改
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - IoT 产品数据解析脚本更新 Request VO")
@Data
public class IotProductScriptUpdateReqVO {

    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "产品编号不能为空")
    private Long id;

    @Schema(description = "数据解析脚本（JS ES5）；空字符串表示清空，仅在停用时允许")
    private String scriptContent;

    @Schema(description = "是否启用数据解析脚本", example = "true")
    private Boolean scriptEnabled;

    @AssertTrue(message = "脚本内容和启用状态至少提交一个")
    @JsonIgnore
    public boolean isAnyFieldPresent() {
        return scriptContent != null || scriptEnabled != null;
    }

}
