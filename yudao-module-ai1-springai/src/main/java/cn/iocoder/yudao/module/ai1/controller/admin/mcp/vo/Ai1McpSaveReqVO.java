package cn.iocoder.yudao.module.ai1.controller.admin.mcp.vo;

import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.ai1.enums.mcp.Ai1McpTransportEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

@Schema(description = "管理后台 - AI1 MCP 新增/修改 Request VO")
@Data
public class Ai1McpSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "天气查询")
    @NotEmpty(message = "名称不能为空")
    @Size(max = 100, message = "名称长度不能超过 100 个字符")
    private String name;

    @Schema(description = "传输方式", requiredMode = Schema.RequiredMode.REQUIRED, example = "http")
    @NotEmpty(message = "传输方式不能为空")
    @InEnum(Ai1McpTransportEnum.class)
    private String transport;

    @Schema(description = "服务地址，远程必填；为空时取 config 中的 url", example = "http://127.0.0.1:8081/mcp")
    @Size(max = 200, message = "服务地址长度不能超过 200 个字符")
    private String url;

    @Schema(description = "请求头，远程配置以此字段为准；为空时清空请求头", example = "{\"Authorization\":\"Bearer xxx\"}")
    private Map<String, String> headers;

    @Schema(description = "完整 MCP 配置，JSON 对象；本地必填 command", example = "{\"transport\":\"stdio\",\"command\":\"npx\",\"args\":[\"-y\",\"xxx\"]}")
    private String config;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

    @Schema(description = "备注", example = "查询城市天气")
    @Size(max = 500, message = "备注长度不能超过 500 个字符")
    private String remark;

    @AssertTrue(message = "请求头长度不能超过 500 个字符")
    @JsonIgnore
    public boolean isHeadersValid() {
        return MapUtil.isEmpty(headers) || JsonUtils.toJsonString(headers).length() <= 500;
    }

}
